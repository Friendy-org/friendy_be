package friendy.community.domain.post.fixture;

import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.model.Post;

import java.util.List;

public class PostFixture {

    public static Post postFixture() {
        return createPost("This is a sample post content.", List.of("프렌디", "개발", "스터디"));
    }

    public static Post createPost(String content, List<String> hashtags) {
        Member member = MemberFixture.memberFixture();
        return Post.of(new PostCreateRequest(content, hashtags, null), member);
    }
}
