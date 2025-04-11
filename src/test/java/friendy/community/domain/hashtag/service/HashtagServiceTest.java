package friendy.community.domain.hashtag.service;

import friendy.community.domain.hashtag.model.Hashtag;
import friendy.community.domain.hashtag.repository.HashtagRepository;
import friendy.community.domain.hashtag.repository.PostHashtagRepository;
import friendy.community.domain.post.model.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

    @Mock
    private HashtagRepository hashtagRepository;

    @Mock
    private PostHashtagRepository postHashtagRepository;

    @InjectMocks
    private HashtagService hashtagService;

    @Test
    @DisplayName("해시태그 저장 - 기존 해시태그와 새로운 해시태그 혼합")
    void saveHashtags_mixedExistingAndNew() {
        // given
        Post post = mock(Post.class);
        List<String> hashtagNames = List.of("java", "spring", "newTag");

        Hashtag existing1 = new Hashtag("java");
        Hashtag existing2 = new Hashtag("spring");

        when(hashtagRepository.findAllByNameIn(hashtagNames))
            .thenReturn(new ArrayList<>(List.of(existing1, existing2)));

        when(hashtagRepository.saveAll(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        hashtagService.saveHashtags(post, hashtagNames);

        // then
        verify(postHashtagRepository).saveAll(any());
    }

    @Test
    @DisplayName("해시태그 업데이트 시 기존 해시태그 삭제 후 저장")
    void updateHashtags_success() {
        // given
        Post post = mock(Post.class);
        when(post.getId()).thenReturn(1L);
        List<String> hashtags = List.of("tag1", "tag2");

        when(hashtagRepository.findAllByNameIn(any()))
            .thenReturn(new ArrayList<>());

        when(hashtagRepository.saveAll(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        hashtagService.updateHashtags(post, hashtags);

        // then
        verify(postHashtagRepository).deleteAllByPostId(1L);
        verify(postHashtagRepository).saveAll(any());
    }

    @Test
    @DisplayName("해시태그 삭제 - 게시글 ID 기반")
    void deleteHashtags_success() {
        // when
        hashtagService.deleteHashtags(10L);

        // then
        verify(postHashtagRepository).deleteAllByPostId(10L);
    }
}
