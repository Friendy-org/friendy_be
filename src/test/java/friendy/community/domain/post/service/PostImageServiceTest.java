package friendy.community.domain.post.service;

import friendy.community.domain.post.fixture.PostFixture;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.model.PostImage;
import friendy.community.domain.post.repository.PostImageRepository;
import friendy.community.domain.upload.service.S3service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostImageServiceTest {

    @InjectMocks
    private PostImageService postImageService;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private S3service s3service;

    private Post post;

    @BeforeEach
    void setUp() {
        post = PostFixture.postFixture();
        ReflectionTestUtils.setField(post, "id", 1L);
    }

    @Test
    @DisplayName("게시글 이미지 저장 성공")
    void saveImagesForPost_success() {
        // given
        Post post = mock(Post.class);
        List<String> imageUrls = List.of("url1", "url2");

        when(s3service.moveS3Object(anyString(), eq("post")))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        postImageService.saveImagesForPost(post, imageUrls);

        // then
        verify(post, times(2)).addImage(any(PostImage.class));
    }

    @Test
    @DisplayName("게시글 이미지 수정 성공")
    void updateImagesForPost_success() {
        // given
        List<String> newImageUrls = List.of("url3", "url4");

        PostImage oldImage1 = mock(PostImage.class);
        when(oldImage1.getImageUrl()).thenReturn("old1");

        PostImage oldImage2 = mock(PostImage.class);
        when(oldImage2.getImageUrl()).thenReturn("old2");

        List<PostImage> existingImages = List.of(oldImage1, oldImage2);

        when(postImageRepository.findByPostIdOrderByImageOrderAsc(1L))
            .thenReturn(existingImages)
            .thenReturn(Collections.emptyList());

        when(s3service.extractS3Key(anyString())).thenReturn("key");

        when(s3service.moveS3Object(anyString(), eq("post")))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        postImageService.updateImagesForPost(post, newImageUrls);

        // then
        verify(postImageRepository, times(2)).findByPostIdOrderByImageOrderAsc(1L);
        verify(postImageRepository, times(2)).delete(any(PostImage.class));
    }

    @Test
    @DisplayName("게시글 이미지 삭제 성공")
    void deleteImagesForPost_success() {
        // given
        Post post = mock(Post.class);
        when(post.getId()).thenReturn(1L);

        PostImage image1 = mock(PostImage.class);
        when(image1.getImageUrl()).thenReturn("url1");

        PostImage image2 = mock(PostImage.class);
        when(image2.getImageUrl()).thenReturn("url2");

        List<PostImage> images = List.of(image1, image2);

        when(postImageRepository.findByPostIdOrderByImageOrderAsc(1L)).thenReturn(images);
        when(s3service.extractS3Key(anyString())).thenReturn("s3-key");

        // when
        postImageService.deleteImagesForPost(post);

        // then
        verify(postImageRepository).findByPostIdOrderByImageOrderAsc(1L);
        verify(s3service, times(2)).extractS3Key(anyString());
        verify(s3service, times(2)).deleteFromS3(anyString());
        verify(postImageRepository, times(2)).delete(any(PostImage.class));
    }

    @Test
    @DisplayName("게시글 이미지 수정 성공 - 기존 이미지와 새로운 이미지 모두 존재")
    void updateImagesForPost_withExistingAndNewImages_success() {
        // given
        PostImage existingImage = new PostImage("existing-url", 1);
        existingImage.assignPost(post);
        post.addImage(existingImage);

        List<String> newImageUrls = List.of("existing-url", "new-url");

        when(postImageRepository.findByPostIdOrderByImageOrderAsc(1L))
            .thenReturn(List.of(existingImage));

        when(s3service.moveS3Object("new-url", "post")).thenReturn("new-url");

        // when
        postImageService.updateImagesForPost(post, newImageUrls);

        // then
        assertThat(post.getImages()).hasSize(2);
        assertThat(post.getImages())
            .extracting(PostImage::getImageUrl)
            .containsExactlyInAnyOrder("existing-url", "new-url");
    }
}
