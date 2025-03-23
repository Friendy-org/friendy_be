package friendy.community.domain.post.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import friendy.community.domain.member.model.QMember;
import friendy.community.domain.post.dto.response.FindAllPostResponse;
import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.model.QPost;
import friendy.community.domain.post.model.QPostImage;
import friendy.community.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PostQueryDSLRepository {

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

    public FindAllPostResponse getPostsByLastId(Long lastPostId, int size) {
        List<Post> posts = queryFactory.selectFrom(QPost.post)
            .leftJoin(QPost.post.member, QMember.member).fetchJoin()
            .leftJoin(QPost.post.images, QPostImage.postImage).fetchJoin()
            .where(
                lastPostId != null ? QPost.post.id.lt(lastPostId) : null
            )
            .orderBy(QPost.post.id.desc())
            .limit(size + 1)
            .fetch();

        if (posts.isEmpty()) {
            throw new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "게시글이 없습니다.");
        }

        boolean hasNext = posts.size() > size;
        if (hasNext) {
            posts.removeLast();
        }
        Long newLastPostId = posts.getLast().getId();

        List<FindPostResponse> postResponses = posts.stream()
            .map(FindPostResponse::from)
            .collect(Collectors.toList());

        return new FindAllPostResponse(postResponses, hasNext, newLastPostId);
    }
}
