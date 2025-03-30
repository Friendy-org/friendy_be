package friendy.community.domain.comment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import friendy.community.domain.comment.controller.code.CommentExceptionCode;
import friendy.community.domain.comment.dto.FindAllReplyResponse;
import friendy.community.domain.comment.dto.FindReplyResponse;
import friendy.community.domain.comment.model.QReply;
import friendy.community.domain.comment.model.Reply;
import friendy.community.domain.member.model.QMember;
import friendy.community.global.exception.domain.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReplyQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    public FindAllReplyResponse getRepliesByLastId(Long lastReplyId, int size) {
        List<Reply> replies = queryFactory.selectFrom(QReply.reply)
            .leftJoin(QReply.reply.member, QMember.member).fetchJoin()
            .where(
                lastReplyId != null ?  QReply.reply.id.lt(lastReplyId) : null
            )
            .orderBy(QReply.reply.id.desc())
            .limit(size + 1)
            .fetch();

        if (replies.isEmpty())
            throw new NotFoundException(CommentExceptionCode.REPLY_NOT_FOUND);

        boolean hasNext = replies.size() > size;
        if (hasNext)
            replies.removeLast();
        Long newLastReplyId = replies.getLast().getId();

        List<FindReplyResponse> replyResponses = replies.stream()
            .map(FindReplyResponse::from)
            .collect(Collectors.toList());

        return new FindAllReplyResponse(replyResponses, hasNext, newLastReplyId);
    }
}
