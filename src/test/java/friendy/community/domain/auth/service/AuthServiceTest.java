package friendy.community.domain.auth.service;

import friendy.community.domain.auth.dto.request.LoginRequest;
import friendy.community.domain.auth.dto.response.TokenResponse;
import friendy.community.domain.auth.jwt.JwtTokenProvider;
import friendy.community.domain.member.encryption.PasswordEncryptor;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.member.service.MemberCommandService;
import friendy.community.domain.member.service.MemberDomainService;
import friendy.community.global.exception.domain.UnAuthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncryptor passwordEncryptor;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MemberDomainService memberDomainService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공 시 토큰 반환")
    void loginSuccess() {
        // given
        String email = "test@example.com";
        String rawPassword = "password123";
        String salt = "salt123";
        String encryptedPassword = "encryptedPw";
        Member mockMember = mock(Member.class);
        LoginRequest request = new LoginRequest(email, rawPassword);

        when(memberDomainService.getMemberByEmail(email)).thenReturn(mockMember);
        when(mockMember.getSalt()).thenReturn(salt);
        when(passwordEncryptor.encrypt(rawPassword, salt)).thenReturn(encryptedPassword);
        when(mockMember.matchPassword(encryptedPassword)).thenReturn(true);
        when(mockMember.getId()).thenReturn(1L);
        when(jwtTokenProvider.generateAccessToken(email)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(email)).thenReturn("refresh-token");

        // when
        TokenResponse result = authService.login(request);

        // then
        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("비밀번호 틀릴 경우 예외 발생")
    void loginWrongPasswordThrowsException() {
        // given
        String email = "test@example.com";
        String rawPassword = "wrongPw";
        String salt = "salt123";
        String encryptedPassword = "wrongEncryptedPw";
        Member mockMember = mock(Member.class);

        when(memberDomainService.getMemberByEmail(email)).thenReturn(mockMember);
        when(mockMember.getSalt()).thenReturn(salt);
        when(passwordEncryptor.encrypt(rawPassword, salt)).thenReturn(encryptedPassword);
        when(mockMember.matchPassword(encryptedPassword)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(new LoginRequest(email, rawPassword)))
            .isInstanceOf(UnAuthorizedException.class);
    }

    @Test
    @DisplayName("로그아웃 시 refresh token 삭제됨")
    void logoutSuccess() {
        // given
        String token = "access-token";
        when(jwtTokenProvider.extractEmailFromAccessToken(token)).thenReturn("user@example.com");

        // when
        authService.logout(token);

        // then
        verify(jwtTokenProvider).deleteRefreshToken("user@example.com");
    }

    @Test
    @DisplayName("refresh token으로 토큰 재발급")
    void reissueTokenSuccess() {
        // given
        String refreshToken = "refresh-token";
        String email = "user@example.com";
        Member member = mock(Member.class);

        when(jwtTokenProvider.extractEmailFromRefreshToken(refreshToken)).thenReturn(email);
        when(memberDomainService.getMemberByEmail(email)).thenReturn(member);
        when(member.getEmail()).thenReturn(email);
        when(member.getId()).thenReturn(10L);
        when(jwtTokenProvider.generateAccessToken(email)).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken(email)).thenReturn("new-refresh");

        // when
        TokenResponse response = authService.reissueToken(refreshToken);

        // then
        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        assertThat(response.memberId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("회원 탈퇴 시 refresh token 삭제 및 유저 삭제")
    void withdrawalSuccess() {
        // given
        String token = "access-token";
        String email = "user@example.com";
        Member member = mock(Member.class);

        when(jwtTokenProvider.extractEmailFromAccessToken(token)).thenReturn(email);
        when(memberDomainService.getMemberByEmail(email)).thenReturn(member);

        // when
        authService.withdrawal(token);

        // then
        verify(jwtTokenProvider).deleteRefreshToken(email);
        verify(memberRepository).delete(member);
    }
}
