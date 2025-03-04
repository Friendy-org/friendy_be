package friendy.community.domain.comment.service;

import friendy.community.domain.auth.jwt.JwtTokenExtractor;
import friendy.community.domain.auth.jwt.JwtTokenProvider;
import friendy.community.domain.auth.service.AuthService;
import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.dto.CommentUpdateRequest;
import friendy.community.domain.comment.dto.ReplyCreateRequest;
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

    public void saveComment(final CommentCreateRequest commentCreateRequest, final HttpServletRequest httpServletRequest) {
        final Member member = getMemberFromRequest(httpServletRequest);
        final Post post = getPostByPostId(commentCreateRequest.postId());
        final Comment comment = Comment.of(commentCreateRequest, member, post);

        commentRepository.save(comment);
    }

    public void saveReply(final ReplyCreateRequest replyCreateRequest, final HttpServletRequest httpServletRequest) {
        final Member member = getMemberFromRequest(httpServletRequest);
        final Post post = getPostByPostId(replyCreateRequest.postId());
        final Comment parentComment = getCommentByCommentId(replyCreateRequest.commentId());
        final Comment reply = Comment.of(replyCreateRequest, member, post, parentComment);

        parentComment.updateReplyCount(parentComment.getReplyCount() + 1);
        commentRepository.save(reply);
    }

    public void updateComment(final CommentUpdateRequest commentUpdateRequest, Long id, final HttpServletRequest httpServletRequest) {
        final Comment comment = getCommentByCommentId(id);
        final Member member = getMemberFromRequest(httpServletRequest);
        validateCommentAuthor(comment, member);

        comment.updateContent(commentUpdateRequest.content());
        commentRepository.save(comment);
    }

    private void validateCommentAuthor(final Comment comment, final Member member) {
        if (!member.equals(comment.getMember()))
            throw new FriendyException(ErrorCode.UNAUTHORIZED_USER, "작성자만 댓글(답글)을 수정할 수 있습니다.");
    }

    private Comment getCommentByCommentId(final Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 댓글(답글)입니다."));
    }

    private Member getMemberFromRequest(final HttpServletRequest httpServletRequest) {
        final String accessToken = jwtTokenExtractor.extractAccessToken(httpServletRequest);
        final String email = jwtTokenProvider.extractEmailFromAccessToken(accessToken);
        return authService.getMemberByEmail(email);
    }

    private Post getPostByPostId(final Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "댓글 작성 대상 게시글이 존재하지 않습니다."));
    }

}
