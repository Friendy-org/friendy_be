package friendy.community.domain.post.service;

import friendy.community.domain.member.model.Member;
import friendy.community.domain.post.controller.code.PostExceptionCode;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.global.exception.domain.NotFoundException;
import friendy.community.global.exception.domain.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostDomainService {

    private final PostRepository postRepository;

    public Post validatePostExistence(Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(PostExceptionCode.POST_NOT_FOUND));
    }

    public void validatePostAuthor(Member member, Post post) {
        if (!post.getMember().getId().equals(member.getId())) {
            throw new UnAuthorizedException(PostExceptionCode.POST_FORBIDDEN_ACCESS);
        }
    }
}
