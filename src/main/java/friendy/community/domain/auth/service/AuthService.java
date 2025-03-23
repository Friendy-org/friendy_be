package friendy.community.domain.auth.service;

import friendy.community.domain.auth.dto.request.LoginRequest;
import friendy.community.domain.auth.dto.response.TokenResponse;
import friendy.community.domain.auth.jwt.JwtTokenProvider;
import friendy.community.domain.member.encryption.PasswordEncryptor;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.global.exception.ErrorCode;
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

    public TokenResponse login(final LoginRequest request) {
        final Member member = getVerifiedMember(request.email(), request.password());

        final String accessToken = jwtTokenProvider.generateAccessToken(request.email());
        final String refreshToken = jwtTokenProvider.generateRefreshToken(request.email());

        return TokenResponse.of(accessToken, refreshToken);
    }

    public void logout(final String accessToken) {
        final String email = jwtTokenProvider.extractEmailFromAccessToken(accessToken);
        jwtTokenProvider.deleteRefreshToken(email);
    }

    public TokenResponse reissueToken(final String refreshToken) {
        final String extractedEmail = jwtTokenProvider.extractEmailFromRefreshToken(refreshToken);
        final Member member = getMemberByEmail(extractedEmail);
        final String newAccessToken = jwtTokenProvider.generateAccessToken(member.getEmail());
        final String newRefreshToken = jwtTokenProvider.generateRefreshToken(member.getEmail());

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    public void withdrawal(final String accessToken) {
        logout(accessToken);

        final String email = jwtTokenProvider.extractEmailFromAccessToken(accessToken);
        final Member member = getMemberByEmail(email);

        memberRepository.delete(member);
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email) //4100
                .orElseThrow(() -> new FriendyException(ErrorCode.UNAUTHORIZED_EMAIL, "해당 이메일의 회원이 존재하지 않습니다."));
    }

    private Member getVerifiedMember(String email, String password) {
        Member member = getMemberByEmail(email);
        validateCorrectPassword(member, password);
        return member;
    }

    private void validateCorrectPassword(Member member, String password) {
        String salt = member.getSalt();
        String encryptedPassword = passwordEncryptor.encrypt(password, salt);
        if (!member.matchPassword(encryptedPassword)) {   //4101
            throw new FriendyException(ErrorCode.UNAUTHORIZED_PASSWORD, "로그인에 실패하였습니다.");
        }
    }
}
