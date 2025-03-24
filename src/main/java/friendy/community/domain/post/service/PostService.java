package friendy.community.domain.post.service;

import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.comment.repository.CommentRepository;
import friendy.community.domain.comment.service.CommentService;
import friendy.community.domain.hashtag.service.HashtagService;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.service.MemberService;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.dto.request.PostUpdateRequest;
import friendy.community.domain.post.dto.response.FindAllPostResponse;
import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostQueryDSLRepository;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public long updatePost(final PostUpdateRequest request, final Long memberId, final Long postId) {
        final Member member = memberService.findMemberById(memberId);

        final Post post = validatePostExistence(postId);
        validatePostAuthor(member, post);

        if (request.imageUrls() != null) {
            postImageService.updateImagesForPost(post, request.imageUrls());
        }
        post.updatePost(request);
        hashtagService.updateHashtags(post, request.hashtags());
        postRepository.save(post);

        return post.getId();
    }

    public void deletePost(final Long memberId, final Long postId) {
        final Member member = memberService.findMemberById(memberId);

        final Post post = validatePostExistence(postId);
        validatePostAuthor(member, post);

        postImageService.deleteImagesForPost(post);
        hashtagService.deleteHashtags(postId);
        postRepository.delete(post);
    }

    public FindPostResponse getPost(final Long postId) {
        Post post = postQueryDSLRepository.findPostById(postId)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 게시글입니다."));

        return FindPostResponse.from(post);
    }

    public FindAllPostResponse getAllPosts(Pageable pageable) {
        Pageable defaultPageable = PageRequest.of(pageable.getPageNumber(), 10);
        Page<Post> postPage = postQueryDSLRepository.findAllPosts(defaultPageable);

        validatePageNumber(defaultPageable.getPageNumber(), postPage);
        List<FindPostResponse> findPostResponses = postPage.getContent().stream()
            .map(FindPostResponse::from)
            .toList();

        return new FindAllPostResponse(findPostResponses, postPage.getTotalPages());
    }

    private Post validatePostExistence(Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 게시글입니다."));
    }

    private void validatePostAuthor(Member member, Post post) {
        if (!post.getMember().getId().equals(member.getId())) {
            throw new FriendyException(ErrorCode.FORBIDDEN_ACCESS, "게시글은 작성자 본인만 관리할 수 있습니다.");
        }
    }

    private void validatePageNumber(int requestedPage, Page<?> page) {
        if (requestedPage >= page.getTotalPages()) {
            throw new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "요청한 페이지가 존재하지 않습니다.");
        }
    }
}