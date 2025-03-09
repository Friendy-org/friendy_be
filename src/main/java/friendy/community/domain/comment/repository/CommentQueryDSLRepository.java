package friendy.community.domain.comment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.comment.model.QComment;
import friendy.community.domain.post.model.QPost;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Comment> findAllComments(Pageable pageable, Long postId) {
        List<Comment> comments = queryFactory.selectFrom(QComment.comment)
                .leftJoin(QComment.comment.post, QPost.post).fetchJoin()
                .where(QComment.comment.post.id.eq(postId))
                .orderBy(QComment.comment.likeCount.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
        queryFactory.select(QComment.comment.count())
                .from(QComment.comment)
                .where(QComment.comment.post.id.eq(postId))
                .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(comments, pageable, total);
    }

}
