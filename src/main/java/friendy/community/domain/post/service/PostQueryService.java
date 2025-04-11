package friendy.community.domain.post.service;

import friendy.community.domain.member.dto.response.FindMemberPostsResponse;
import friendy.community.domain.member.dto.response.PostPreview;
import friendy.community.domain.post.controller.code.PostExceptionCode;
import friendy.community.domain.post.dto.response.FindAllPostResponse;
import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.global.exception.domain.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostRepository postRepository;

    public FindPostResponse getPost(final Long postId, final Long memberId) {
        Post post = postRepository.findPostById(postId)
            .orElseThrow(() -> new NotFoundException(PostExceptionCode.POST_NOT_FOUND));
        return FindPostResponse.from(post, isPostOwner(post, memberId));
    }

    public FindAllPostResponse getPostsByLastId(final Long lastPostId, final Long memberId) {
        List<Post> posts = postRepository.findPostsByLastId(lastPostId, 10);

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

    public FindMemberPostsResponse getMemberPosts(final Long memberId, final Long lastPostId) {
        List<Post> posts = postRepository.findPostsByMemberId(memberId, lastPostId);

        if (posts.isEmpty()) {
            throw new NotFoundException(PostExceptionCode.POST_NOT_FOUND);
        }

        boolean hasNext = posts.size() > 12;
        if (hasNext) {
            posts.remove(posts.size() - 1);
        }
        Long newLastPostId = posts.get(posts.size() - 1).getId();

        List<PostPreview> previews = posts.stream()
            .map(PostPreview::from)
            .collect(Collectors.toList());

        return new FindMemberPostsResponse(previews, hasNext, newLastPostId);
    }

    private boolean isPostOwner(final Post post, final Long memberId) {
        return post.getMember().getId().equals(memberId);
    }
}
