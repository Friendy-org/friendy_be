package friendy.community.domain.comment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import friendy.community.domain.comment.controller.code.CommentExceptionCode;
import friendy.community.domain.comment.dto.response.FindAllCommentsResponse;
import friendy.community.domain.comment.dto.response.FindCommentResponse;
import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.comment.model.QComment;
import friendy.community.domain.member.model.QMember;
import friendy.community.global.exception.domain.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CommentQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    public FindAllCommentsResponse getCommentsByLastId(Long lastCommentId, int size) {
        List<Comment> comments = queryFactory.selectFrom(QComment.comment)
                .leftJoin(QComment.comment.member, QMember.member).fetchJoin()
                .where(
                        lastCommentId != null ? QComment.comment.id.lt(lastCommentId) : null
                )
                .orderBy(QComment.comment.id.desc())
                .limit(size + 1)
                .fetch();

        if (comments.isEmpty()) {
            throw new NotFoundException(CommentExceptionCode.COMMENT_NOT_FOUND);
        }

        boolean hasNext = comments.size() > size;
        if (hasNext) {
            comments.removeLast();
        }
        Long newLastCommentId = comments.getLast().getId();

        List<FindCommentResponse> commentResponses = comments.stream()
                .map(FindCommentResponse::from)
                .collect(Collectors.toList());

        return new FindAllCommentsResponse(commentResponses, hasNext, newLastCommentId);

    }

}