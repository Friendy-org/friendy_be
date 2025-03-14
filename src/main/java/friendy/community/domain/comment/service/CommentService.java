package friendy.community.domain.comment.service;

import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.dto.CommentUpdateRequest;
import friendy.community.domain.comment.dto.ReplyCreateRequest;
import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.comment.model.Reply;
import friendy.community.domain.comment.repository.CommentRepository;
import friendy.community.domain.comment.repository.ReplyRepository;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.service.MemberService;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final MemberService memberService;
    private final PostRepository postRepository;

    public void saveComment(final CommentCreateRequest commentCreateRequest, final Long memberId) {
        final Member member = memberService.findMemberById(memberId);
        final Post post = getPostByPostId(commentCreateRequest.postId());
        final Comment comment = Comment.of(commentCreateRequest, member, post);

        commentRepository.save(comment);
    }

    public void saveReply(final ReplyCreateRequest replyCreateRequest, final Long memberId) {
        final Member member = memberService.findMemberById(memberId);
        final Post post = getPostByPostId(replyCreateRequest.postId());
        final Comment parentComment = getCommentByCommentId(replyCreateRequest.commentId());
        final Reply reply = Reply.of(replyCreateRequest, member, post, parentComment);

        parentComment.updateReplyCount(parentComment.getReplyCount() + 1);
        replyRepository.save(reply);
    }

    public void updateComment(final CommentUpdateRequest commentUpdateRequest, Long id, final Long memberId) {
        final Member member = memberService.findMemberById(memberId);
        final Comment comment = getCommentByCommentId(id);
        validateAuthor(comment, member);

        comment.updateContent(commentUpdateRequest.content());
        commentRepository.save(comment);
    }

    public void updateReply(final CommentUpdateRequest commentUpdateRequest, Long id, final Long memberId) {
        final Member member = memberService.findMemberById(memberId);
        final Reply reply = getReplyByReplyId(id);
        validateAuthor(reply, member);

        reply.updateContent(commentUpdateRequest.content());
        replyRepository.save(reply);
    }

    private void validateAuthor(final Comment comment, final Member member) {
        if (!member.equals(comment.getMember()))
            throw new FriendyException(ErrorCode.UNAUTHORIZED_USER, "작성자만 댓글을 수정할 수 있습니다.");
    }

    private void validateAuthor(final Reply reply, final Member member) {
        if (!member.equals(reply.getMember()))
            throw new FriendyException(ErrorCode.UNAUTHORIZED_USER, "작성자만 답글을 수정할 수 있습니다.");
    }

    private Comment getCommentByCommentId(final Long commentId) {
        return commentRepository.findById(commentId)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 댓글입니다."));
    }

    private Reply getReplyByReplyId(final Long replyId) {
        return replyRepository.findById(replyId)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 답글입니다."));
    }

    private Post getPostByPostId(final Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "댓글 작성 대상 게시글이 존재하지 않습니다."));
    }
}
