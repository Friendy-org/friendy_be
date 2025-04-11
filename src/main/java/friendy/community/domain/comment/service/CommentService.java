package friendy.community.domain.comment.service;

import friendy.community.domain.comment.controller.code.CommentExceptionCode;
import friendy.community.domain.comment.dto.request.CommentCreateRequest;
import friendy.community.domain.comment.dto.request.CommentUpdateRequest;
import friendy.community.domain.comment.dto.response.FindAllCommentsResponse;
import friendy.community.domain.comment.dto.request.ReplyCreateRequest;
import friendy.community.domain.comment.dto.FindAllReplyResponse;
import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.comment.model.Reply;
import friendy.community.domain.comment.repository.CommentQueryDSLRepository;
import friendy.community.domain.comment.repository.CommentRepository;
import friendy.community.domain.comment.repository.ReplyQueryDSLRepository;
import friendy.community.domain.comment.repository.ReplyRepository;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.service.MemberCommandService;
import friendy.community.domain.member.service.MemberDomainService;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.global.exception.domain.NotFoundException;
import friendy.community.global.exception.domain.UnAuthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final ReplyQueryDSLRepository replyQueryDSLRepository;
    private final MemberCommandService memberCommandService;
    private final CommentQueryDSLRepository commentQueryDSLRepository;
    private final PostRepository postRepository;
    private final MemberDomainService memberDomainService;

    public void saveComment(final CommentCreateRequest commentCreateRequest, final Long memberId) {
        final Member member = memberDomainService.getMemberById(memberId);
        final Post post = getPostByPostId(commentCreateRequest.postId());
        final Comment comment = Comment.of(commentCreateRequest, member, post);

        post.updateCommentCount(post.getCommentCount() + 1);

        commentRepository.save(comment);
    }

    public void saveReply(final ReplyCreateRequest replyCreateRequest, final Long memberId) {
        final Member member = memberDomainService.getMemberById(memberId);
        final Post post = getPostByPostId(replyCreateRequest.postId());
        final Comment parentComment = getCommentByCommentId(replyCreateRequest.commentId());
        final Reply reply = Reply.of(replyCreateRequest, member, post, parentComment);

        parentComment.updateReplyCount(parentComment.getReplyCount() + 1);
        replyRepository.save(reply);
    }

    public void updateComment(final CommentUpdateRequest commentUpdateRequest, Long id, final Long memberId) {
        final Member member = memberDomainService.getMemberById(memberId);
        final Comment comment = getCommentByCommentId(id);
        validateAuthor(comment, member);

        comment.updateContent(commentUpdateRequest.content());
        commentRepository.save(comment);
    }

    public void updateReply(final CommentUpdateRequest commentUpdateRequest, Long id, final Long memberId) {
        final Member member = memberDomainService.getMemberById(memberId);
        final Reply reply = getReplyByReplyId(id);
        validateAuthor(reply, member);

        reply.updateContent(commentUpdateRequest.content());
        replyRepository.save(reply);
    }

    public void deleteComment(final Long commentId, final Long memberId) {
        final Member member = memberDomainService.getMemberById(memberId);
        final Comment comment = getCommentByCommentId(commentId);
        validateAuthor(comment, member);

        List<Reply> replies = replyRepository.findAllByComment(comment);
        replyRepository.deleteAll(replies);

        Post post = comment.getPost();
        post.updateCommentCount(post.getCommentCount() - 1);

        commentRepository.delete(comment);
    }

    public void deleteReply(final Long replyId, final Long memberId) {
        final Member member = memberDomainService.getMemberById(memberId);
        final Reply reply = getReplyByReplyId(replyId);
        validateAuthor(reply, member);

        Comment comment = reply.getComment();
        comment.updateReplyCount(comment.getReplyCount() - 1);

        replyRepository.delete(reply);
    }

    public FindAllCommentsResponse getCommentsByLastId(final Long lastCommentId) {
        return commentQueryDSLRepository.getCommentsByLastId(lastCommentId, 10);
    }

    public FindAllReplyResponse getRepliesByLastId(Long lastReplyId) {
        return replyQueryDSLRepository.getRepliesByLastId(lastReplyId, 10);
    }

    private void validateAuthor(final Comment comment, final Member member) {
        if (!member.equals(comment.getMember()))
            throw new UnAuthorizedException(CommentExceptionCode.UNAUTHORIZED_COMMENT_USER);
    }

    private void validateAuthor(final Reply reply, final Member member) {
        if (!member.equals(reply.getMember()))
            throw new UnAuthorizedException(CommentExceptionCode.UNAUTHORIZED_REPLY_USER);
    }

    private Comment getCommentByCommentId(final Long commentId) {
        return commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException(CommentExceptionCode.COMMENT_NOT_FOUND));
    }

    private Reply getReplyByReplyId(final Long replyId) {
        return replyRepository.findById(replyId)
            .orElseThrow(() -> new NotFoundException(CommentExceptionCode.REPLY_NOT_FOUND));
    }

    private Post getPostByPostId(final Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(CommentExceptionCode.POST_NOT_FOUND));
    }
}
