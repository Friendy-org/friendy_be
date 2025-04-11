package friendy.community.domain.post.repository.query.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.fixture.PostFixture;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.model.PostImage;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.global.config.TestQuerydslConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestQuerydslConfig.class)
class PostRepositoryImplTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepositoryImpl postRepositoryImpl;

    @Autowired
    private PostRepository postRepository;

    private Member member;
    private Post post;

    @BeforeEach
    void setUp() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        postRepositoryImpl = new PostRepositoryImpl(queryFactory);

        member = MemberFixture.memberFixture();
        memberRepository.save(member);

        PostCreateRequest request = new PostCreateRequest(
            "테스트 게시글입니다.",
            List.of("프렌디", "개발", "스터디"),
            List.of("https://example.com/image.jpg"),
            "부산 광역시"
        );
        post = Post.of(request, member);
        post.addImage(new PostImage("https://example.com/image.jpg", 1));
        postRepository.save(post);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("postId로 게시글 단건 조회")
    void findPostById() {
        // given
        Post savedPost = postRepositoryImpl.findPostsByLastId(null, 1).get(0);

        // when
        Optional<Post> result = postRepositoryImpl.findPostById(savedPost.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("테스트 게시글입니다.");
        assertThat(result.get().getImages()).hasSize(1);
    }

    @Test
    @DisplayName("lastPostId 없이 게시글 리스트 조회")
    void findPostsByLastId() {
        // when
        List<Post> result = postRepositoryImpl.findPostsByLastId(null, 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("테스트 게시글입니다.");
    }

    @Test
    @DisplayName("memberId로 게시글 조회")
    void findPostsByMemberId() {
        // when
        List<Post> result = postRepositoryImpl.findPostsByMemberId(member.getId(), null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("테스트 게시글입니다.");
        assertThat(result.get(0).getMember().getId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("lastPostId가 존재할 때 해당 ID보다 작은 게시글만 조회")
    void findPostsByLastIdWithValue() {
        // given
        PostCreateRequest request2 = new PostCreateRequest(
            "두 번째 게시글",
            List.of("테스트", "샘플"),
            List.of("https://example.com/image2.jpg"),
            "서울특별시"
        );
        Post post2 = Post.of(request2, member);
        post2.addImage(new PostImage("https://example.com/image2.jpg", 1));
        postRepository.save(post2);
        em.flush();
        em.clear();

        // when
        List<Post> result = postRepositoryImpl.findPostsByLastId(post2.getId(), 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("테스트 게시글입니다.");
    }

    @Test
    @DisplayName("memberId로 게시글 조회 시 lastPostId가 있을 때 해당 ID보다 작은 게시글만 조회")
    void findPostsByMemberIdWithLastPostId() {
        // given
        PostCreateRequest request2 = new PostCreateRequest(
            "두 번째 게시글입니다.",
            List.of("추가", "샘플"),
            List.of("https://example.com/image2.jpg"),
            "서울시"
        );
        Post post2 = Post.of(request2, member);
        post2.addImage(new PostImage("https://example.com/image2.jpg", 1));
        postRepository.save(post2);
        em.flush();
        em.clear();

        // when
        List<Post> result = postRepositoryImpl.findPostsByMemberId(member.getId(), post2.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("테스트 게시글입니다.");
    }
}
