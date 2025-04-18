package friendy.community.domain.post.repository.query.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import friendy.community.domain.member.model.QMember;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.model.QPost;
import friendy.community.domain.post.model.QPostImage;
import friendy.community.domain.post.repository.query.PostQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<Post> findPostById(final Long postId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(QPost.post)
                .leftJoin(QPost.post.member, QMember.member).fetchJoin()
                .leftJoin(QPost.post.images, QPostImage.postImage).fetchJoin()
                .where(QPost.post.id.eq(postId))
                .fetchOne()
        );
    }

    public List<Post> findPostsByLastId(Long lastPostId, int size) {
        return queryFactory.selectFrom(QPost.post)
            .leftJoin(QPost.post.member, QMember.member).fetchJoin()
            .leftJoin(QPost.post.images, QPostImage.postImage).fetchJoin()
            .where(lastPostId != null ? QPost.post.id.lt(lastPostId) : null)
            .orderBy(QPost.post.id.desc())
            .limit(size + 1)
            .fetch();
    }

    public List<Post> findPostsByMemberId(Long memberId, Long lastPostId) {
        return queryFactory.selectFrom(QPost.post)
            .leftJoin(QPost.post.member, QMember.member).fetchJoin()
            .leftJoin(QPost.post.images, QPostImage.postImage).fetchJoin()
            .where(
                QMember.member.id.eq(memberId)
                    .and(lastPostId != null ? QPost.post.id.lt(lastPostId) : null)
            )
            .orderBy(QPost.post.id.desc())
            .limit(13)
            .fetch();
    }
}
