package friendy.community.domain.hashtag.service;

import friendy.community.domain.hashtag.model.Hashtag;
import friendy.community.domain.hashtag.repository.HashtagRepository;
import friendy.community.domain.hashtag.repository.PostHashtagRepository;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.model.PostHashtag;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext
class HashtagServiceTest {

    @Autowired
    private HashtagService hashtagService;
    @Autowired
    private HashtagRepository hashtagRepository;
    @Autowired
    private PostHashtagRepository postHashtagRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Post post;

    @BeforeEach
    void setup() {
        Member member = MemberFixture.memberFixture();
        memberRepository.save(member);

        PostCreateRequest postCreateRequest = new PostCreateRequest("This is a test post", List.of(),null);
        post = Post.of(postCreateRequest, member);
        postRepository.save(post);
    }

    @Test
    @DisplayName("게시글에 해시태그를 추가하면 PostHashtag도 함께 저장된다")
    void saveHashtagsSuccessfullySavesPostHashtags() {
        // Given
        List<String> hashtagNames = List.of("프렌디", "개발", "스터디");

        // When
        hashtagService.saveHashtags(post, hashtagNames);

        // Then
        List<Hashtag> savedHashtags = hashtagRepository.findAll();
        assertThat(savedHashtags).hasSize(3);
        assertThat(savedHashtags).extracting(Hashtag::getName).containsExactlyInAnyOrder("프렌디", "개발", "스터디");

        List<PostHashtag> postHashtags = postHashtagRepository.findAll();
        assertThat(postHashtags).hasSize(3);
        assertThat(postHashtags).extracting(postHashtag -> postHashtag.getHashtag().getName())
                .containsExactlyInAnyOrder("프렌디", "개발", "스터디");
    }

    @Test
    @DisplayName("게시글 해시태그를 수정하면 기존 PostHashtag가 삭제되고 새 해시태그가 저장된다")
    void updateHashtagsSuccessfullyUpdatesPostHashtags() {
        // Given
        List<String> initialHashtags = List.of("프렌디", "개발");
        hashtagService.saveHashtags(post, initialHashtags);

        List<String> updatedHashtags = List.of("스터디", "코딩");

        // When
        hashtagService.updateHashtags(post, updatedHashtags);

        // Then
        List<PostHashtag> postHashtags = postHashtagRepository.findAll();
        assertThat(postHashtags).hasSize(2);
        assertThat(postHashtags).extracting(postHashtag -> postHashtag.getHashtag().getName())
                .containsExactlyInAnyOrder("스터디", "코딩");
    }

    @Test
    @DisplayName("게시글이 삭제되면 해당 게시글의 PostHashtag도 함께 삭제된다")
    void deleteHashtagsSuccessfullyRemovesPostHashtags() {
        // Given
        List<String> hashtags = List.of("프렌디", "개발", "스터디");
        hashtagService.saveHashtags(post, hashtags);

        // When
        hashtagService.deleteHashtags(post.getId());

        // Then
        List<PostHashtag> postHashtags = postHashtagRepository.findAll();
        assertThat(postHashtags).isEmpty();
    }
}
