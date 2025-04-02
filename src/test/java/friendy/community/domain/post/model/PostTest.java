package friendy.community.domain.post.model;

import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PostTest {

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.memberFixture();
    }

    @Test
    @DisplayName("Post 객체가 요청을 기반으로 생성되는지 테스트")
    void ofMethodCreatesPostFromRequest() {
        // Given
        String content = "This is a new post content.";
        List<String> hashtags = List.of("프렌디", "개발", "스터디");
        PostCreateRequest postCreateRequest = new PostCreateRequest(content, hashtags, null,"창원시");

        // When
        Post post = Post.of(postCreateRequest, member);

        // Then
        assertNotNull(post);
        assertEquals(content, post.getContent());
        assertEquals(member, post.getMember());
    }
}

