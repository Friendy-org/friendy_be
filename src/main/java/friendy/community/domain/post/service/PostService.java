package friendy.community.domain.post.service;

import friendy.community.domain.hashtag.service.HashtagService;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.service.MemberService;
import friendy.community.domain.post.controller.code.PostExceptionCode;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.dto.request.PostUpdateRequest;
import friendy.community.domain.post.dto.response.FindAllPostResponse;
import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.domain.post.dto.response.PostIdResponse;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostQueryDSLRepository;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.global.exception.domain.NotFoundException;
import friendy.community.global.exception.domain.UnAuthorizedException;
import friendy.community.global.security.FriendyUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final PostQueryDSLRepository postQueryDSLRepository;
    private final HashtagService hashtagService;
    private final PostImageService postImageService;
    private final MemberService memberService;

    public long savePost(final PostCreateRequest request, final Long memberId) {
        final Member member = memberService.findMemberById(memberId);
        final Post post = Post.of(request, member);

        if (request.imageUrls() != null) {
            postImageService.saveImagesForPost(post, request.imageUrls());
        }

        postRepository.save(post);
        hashtagService.saveHashtags(post, request.hashtags());

        return post.getId();
    }

    public PostIdResponse updatePost(final PostUpdateRequest request, final Long memberId, final Long postId) {
        final Member member = memberService.findMemberById(memberId);

        final Post post = validatePostExistence(postId);
        validatePostAuthor(member, post);

        if (request.imageUrls() != null) {
            postImageService.updateImagesForPost(post, request.imageUrls());
        }
        post.updatePost(request);
        hashtagService.updateHashtags(post, request.hashtags());
        postRepository.save(post);

        PostIdResponse response = PostIdResponse.from(post);

        return response;
    }

    public void deletePost(final Long memberId, final Long postId) {
        final Member member = memberService.findMemberById(memberId);

        final Post post = validatePostExistence(postId);
        validatePostAuthor(member, post);

        postImageService.deleteImagesForPost(post);
        hashtagService.deleteHashtags(postId);
        postRepository.delete(post);
    }

    public FindPostResponse getPost(final Long postId, final Long memberId) {
        Post post = postQueryDSLRepository.findPostById(postId)
            .orElseThrow(() -> new NotFoundException(PostExceptionCode.POST_NOT_FOUND));
        return FindPostResponse.from(post, isPostOwner(post, memberId));
    }

    public FindAllPostResponse getPostsByLastId(Long lastPostId, final Long memberId) {
        List<Post> posts = postQueryDSLRepository.findPostsByLastId(lastPostId, 10);

        if (posts.isEmpty()) {
            throw new NotFoundException(PostExceptionCode.POST_NOT_FOUND);
        }

        boolean hasNext = posts.size() > 10;
        if (hasNext) {
            posts.remove(posts.size() - 1);
        }
        Long newLastPostId = posts.get(posts.size() - 1).getId();

        List<FindPostResponse> postResponses = posts.stream()
            .map(post -> FindPostResponse.from(post, isPostOwner(post, memberId)))
            .collect(Collectors.toList());

        return new FindAllPostResponse(postResponses, hasNext, newLastPostId);
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(PostExceptionCode.POST_NOT_FOUND));
    }

    public boolean isPostOwner(final Post post, final Long memberId) {
        return post.getMember().getId().equals(memberId);
    }

    private Post validatePostExistence(Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(PostExceptionCode.POST_NOT_FOUND));
    }

    private void validatePostAuthor(Member member, Post post) {
        if (!post.getMember().getId().equals(member.getId())) {
            throw new UnAuthorizedException(PostExceptionCode.POST_FORBIDDEN_ACCESS);
        }
    }
}
