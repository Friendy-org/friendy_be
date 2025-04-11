package friendy.community.domain.post.service;

import friendy.community.domain.hashtag.service.HashtagService;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.service.MemberCommandService;
import friendy.community.domain.member.service.MemberDomainService;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.dto.request.PostUpdateRequest;
import friendy.community.domain.post.dto.response.PostIdResponse;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandService {

    private final PostRepository postRepository;
    private final PostDomainService postDomainService;
    private final PostImageService postImageService;
    private final HashtagService hashtagService;
    private final MemberDomainService memberDomainService;

    public long savePost(final PostCreateRequest request, final Long memberId) {
        final Member member = memberDomainService.getMemberById(memberId);
        final Post post = Post.of(request, member);

        if (request.imageUrls() != null) {
            postImageService.saveImagesForPost(post, request.imageUrls());
        }
        postRepository.save(post);
        hashtagService.saveHashtags(post, request.hashtags());


        return post.getId();
    }

    public PostIdResponse updatePost(final PostUpdateRequest request, final Long memberId, final Long postId) {
        final Member member = memberDomainService.getMemberById(memberId);
        final Post post = postDomainService.validatePostExistence(postId);
        postDomainService.validatePostAuthor(member, post);

        if (request.imageUrls() != null) {
            postImageService.updateImagesForPost(post, request.imageUrls());
        }
        post.updatePost(request);
        hashtagService.updateHashtags(post, request.hashtags());
        postRepository.save(post);

        return PostIdResponse.from(post);
    }

    public void deletePost(final Long memberId, final Long postId) {
        final Member member = memberDomainService.getMemberById(memberId);
        final Post post = postDomainService.validatePostExistence(postId);
        postDomainService.validatePostAuthor(member, post);
        postImageService.deleteImagesForPost(post);
        hashtagService.deleteHashtags(postId);
        postRepository.delete(post);
    }
}