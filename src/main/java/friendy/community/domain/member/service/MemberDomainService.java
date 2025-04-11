package friendy.community.domain.member.service;

import friendy.community.domain.member.controller.code.MemberExceptionCode;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.global.exception.domain.BadRequestException;
import friendy.community.global.exception.domain.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberDomainService {

    private final MemberRepository memberRepository;

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(MemberExceptionCode.USER_NOT_FOUND_EXCEPTION));
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(MemberExceptionCode.USER_NOT_FOUND_EXCEPTION));
    }

    public void assertUniqueEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BadRequestException(MemberExceptionCode.DUPLICATE_EMAIL_EXCEPTION);
        }
    }

    public void assertUniqueName(String name) {
        if (memberRepository.existsByNickname(name)) {
            throw new BadRequestException(MemberExceptionCode.DUPLICATE_NICKNAME_EXCEPTION);
        }
    }

    public void validateUniqueMemberAttributes(String email, String nickname) {
        assertUniqueEmail(email);
        assertUniqueName(nickname);
    }
}
