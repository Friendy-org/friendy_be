package friendy.community.domain.auth.service;

import friendy.community.domain.auth.controller.code.AuthExceptionCode;
import friendy.community.domain.auth.dto.request.LoginRequest;
import friendy.community.domain.auth.dto.response.TokenResponse;
import friendy.community.domain.auth.jwt.JwtTokenProvider;
import friendy.community.domain.member.encryption.PasswordEncryptor;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.member.service.MemberCommandService;
import friendy.community.domain.member.service.MemberDomainService;
import friendy.community.global.exception.domain.NotFoundException;
import friendy.community.global.exception.domain.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncryptor passwordEncryptor;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDomainService memberDomainService;

    public TokenResponse login(final LoginRequest request) {
        final Member member = getVerifiedMember(request.email(), request.password());

        final String accessToken = jwtTokenProvider.generateAccessToken(request.email());
        final String refreshToken = jwtTokenProvider.generateRefreshToken(request.email());

        return TokenResponse.of(member.getId(), accessToken, refreshToken);
    }

    public void logout(final String accessToken) {
        final String email = jwtTokenProvider.extractEmailFromAccessToken(accessToken);
        jwtTokenProvider.deleteRefreshToken(email);
    }

    public TokenResponse reissueToken(final String refreshToken) {
        final String extractedEmail = jwtTokenProvider.extractEmailFromRefreshToken(refreshToken);
        final Member member = memberDomainService.getMemberByEmail(extractedEmail);
        final String newAccessToken = jwtTokenProvider.generateAccessToken(member.getEmail());
        final String newRefreshToken = jwtTokenProvider.generateRefreshToken(member.getEmail());

        return TokenResponse.of(member.getId(), newAccessToken, newRefreshToken);
    }

    public void withdrawal(final String accessToken) {
        logout(accessToken);

        final String email = jwtTokenProvider.extractEmailFromAccessToken(accessToken);
        final Member member = memberDomainService.getMemberByEmail(email);

        memberRepository.delete(member);
    }

    private Member getVerifiedMember(String email, String password) {
        try {
            Member member = memberDomainService.getMemberByEmail(email);
            validateCorrectPassword(member, password);
            return member;
        } catch (NotFoundException e) {
            throw new UnAuthorizedException(AuthExceptionCode.EMAIL_NOT_REGISTERED);
        }
    }

    private void validateCorrectPassword(Member member, String password) {
        String salt = member.getSalt();
        String encryptedPassword = passwordEncryptor.encrypt(password, salt);
        if (!member.matchPassword(encryptedPassword)) {
            throw new UnAuthorizedException(AuthExceptionCode.INVALID_PASSWORD);
        }
    }
}
