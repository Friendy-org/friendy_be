package friendy.community.domain.post.service;

import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.model.PostImage;
import friendy.community.domain.post.repository.PostImageRepository;
import friendy.community.domain.upload.service.S3service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
class PostImageService {
    private final PostImageRepository postImageRepository;
    private final S3service s3service;

    public void saveImagesForPost(Post post, List<String> imageUrls) {
        int imageOrder = 1;
        for (String imageUrl : imageUrls) {
            PostImage postImage = savePostImage(imageUrl, imageOrder++);
            postImage.assignPost(post);
            post.addImage(postImage);
        }
    }

    public void updateImagesForPost(Post post, List<String> newImageUrls) {
        List<PostImage> existingPostImages = postImageRepository.findByPostIdOrderByImageOrderAsc(post.getId());

        Set<String> newImageUrlSet = new HashSet<>(newImageUrls);
        deleteUnusedImages(existingPostImages, newImageUrlSet);

        existingPostImages = postImageRepository.findByPostIdOrderByImageOrderAsc(post.getId());
        addOrUpdateImages(newImageUrls, existingPostImages, post);
    }

    public void deleteImagesForPost(Post post) {
        List<PostImage> imagesToDelete = postImageRepository.findByPostIdOrderByImageOrderAsc(post.getId());
        for (PostImage image : imagesToDelete) {
            String s3Key = s3service.extractS3Key(image.getImageUrl());
            s3service.deleteFromS3(s3Key);
            postImageRepository.delete(image);
        }
    }

    private void addOrUpdateImages(List<String> newImageUrls, List<PostImage> existingPostImages, Post post) {
        int imageOrder = 1;

        for (String newImageUrl : newImageUrls) {
            Optional<PostImage> existingImageOpt = findExistingImage(newImageUrl, existingPostImages);

            if (existingImageOpt.isPresent()) {
                PostImage existingImage = existingImageOpt.get();
                existingImage.changeImageOrder(imageOrder++);
                postImageRepository.save(existingImage);
            } else {
                PostImage postNewImage = savePostImage(newImageUrl, imageOrder++);
                postNewImage.assignPost(post);
                post.addImage(postNewImage);
            }
        }
    }

    private void deleteUnusedImages(List<PostImage> existingPostImages, Set<String> newImageUrlSet) {
        List<PostImage> imagesToRemove = existingPostImages.stream()
            .filter(existingImage -> !newImageUrlSet.contains(existingImage.getImageUrl()))
            .toList();

        for (PostImage imageToRemove : imagesToRemove) {
            String s3Key = s3service.extractS3Key(imageToRemove.getImageUrl());
            s3service.deleteFromS3(s3Key);
            postImageRepository.delete(imageToRemove);
        }
    }

    private Optional<PostImage> findExistingImage(String newImageUrl, List<PostImage> existingPostImages) {
        return existingPostImages.stream()
            .filter(image -> image.getImageUrl().equals(newImageUrl))
            .findFirst();
    }

    private PostImage savePostImage(String requestImageUrl, int imageOrder) {
        String imageUrl = s3service.moveS3Object(requestImageUrl, "post");
        return PostImage.of(imageUrl, imageOrder);
    }
}