package friendy.community.domain.post.model;

import friendy.community.domain.hashtag.model.Hashtag;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PostHashtagTest {

    private Post post;
    private Hashtag hashtag;

    @BeforeEach
    void setUp() {
        PostCreateRequest postCreateRequest = new PostCreateRequest("This is a new post content.", List.of("프렌디", "개발", "스터디"), null);
        post = new Post(postCreateRequest, MemberFixture.memberFixture());
        hashtag = new Hashtag("프렌디");
    }

    @Test
    @DisplayName("PostHashtag 객체가 요청을 기반으로 생성되는지 테스트")
    void postHashtagCreatesSuccessfully() {
        // Given & When
        PostHashtag postHashtag = new PostHashtag(post, hashtag);

        // Then
        assertNotNull(postHashtag);
        assertEquals(post, postHashtag.getPost());
        assertEquals(hashtag, postHashtag.getHashtag());
    }
}

