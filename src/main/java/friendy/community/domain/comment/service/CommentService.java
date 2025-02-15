package friendy.community.domain.comment.service;

import friendy.community.domain.auth.jwt.JwtTokenExtractor;
import friendy.community.domain.auth.jwt.JwtTokenProvider;
import friendy.community.domain.auth.service.AuthService;
import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.comment.repository.CommentRepository;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final JwtTokenExtractor jwtTokenExtractor;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    private final PostRepository postRepository;

    public long saveComment(final CommentCreateRequest commentCreateRequest, final HttpServletRequest httpServletRequest) {
        final Member member = getMemberFromRequest(httpServletRequest);
        final Post post = getPostByPostId(commentCreateRequest.postId());
        final Comment comment = Comment.of(commentCreateRequest, member, post);
        commentRepository.save(comment);

        return comment.getId();
    }

    private Member getMemberFromRequest(HttpServletRequest httpServletRequest) {
        final String accessToken = jwtTokenExtractor.extractAccessToken(httpServletRequest);
        final String email = jwtTokenProvider.extractEmailFromAccessToken(accessToken);
        return authService.getMemberByEmail(email);
    }

    private Post getPostByPostId(final Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "댓글 작성 대상 게시글이 존재하지 않습니다."));
    }

}
