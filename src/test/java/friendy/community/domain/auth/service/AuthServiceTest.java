package friendy.community.domain.auth.service;

import friendy.community.domain.auth.dto.request.LoginRequest;
import friendy.community.domain.auth.dto.response.TokenResponse;
import friendy.community.domain.auth.jwt.JwtTokenProvider;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static friendy.community.domain.auth.fixtures.TokenFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@DirtiesContext
class AuthServiceTest {

    @Autowired
    AuthService authService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private StringRedisTemplate redisTemplate;


    @Test
    @DisplayName("로그인 성공 시 액세스 토큰과 리프레시 토큰이 생성된다.")
    void loginSuccessfullyGeneratesTokens() {
        // Redis Mock 셋업
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());
        LoginRequest loginRequest = new LoginRequest(savedMember.getEmail(), MemberFixture.getFixturePlainPassword());

        // When
        TokenResponse response = authService.login(loginRequest);

        // Then
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.refreshToken()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외를 던진다")
    void throwsExceptionWhenEmailNotFound() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent@example.com", MemberFixture.getFixturePlainPassword());

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("해당 이메일의 회원이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외를 던진다")
    void throwsExceptionWhenPasswordDoesNotMatch() {
        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());
        LoginRequest loginRequest = new LoginRequest(savedMember.getEmail(), "wrongPassword");

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("로그인에 실패하였습니다. 비밀번호를 확인해주세요.");
    }

    @Test
    @DisplayName("로그아웃에 성공하면 Redis에서 리프레시 토큰이 삭제된다.")
    void logoutSuccessfullyDeleteRefreshTokenInRedis() {
        // Redis Mock 셋업
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());
        final String memberEmail = savedMember.getEmail();
        final String accessToken = jwtTokenProvider.generateAccessToken(memberEmail);

        when(redisTemplate.hasKey(memberEmail)).thenReturn(true);

        // When
        authService.logout(accessToken);

        // Then
        verify(redisTemplate, times(1)).delete(memberEmail);
    }

    @Test
    @DisplayName("로그인 상태가 아닌 사용자가 로그아웃 요청을 하면 예외가 발생한다.")
    void inValidLogoutRequestThrowsException() {
        // Redis Mock 셋업
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());
        final String memberEmail = savedMember.getEmail();
        String accessToken = jwtTokenProvider.generateAccessToken(memberEmail);

        when(redisTemplate.hasKey(memberEmail)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.logout(accessToken))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("로그인 되어있지 않은 사용자입니다.");
    }


    @Test
    @DisplayName("토큰 재발급 성공 시 새로운 액세스 토큰과 리프레시 토큰이 반환된다.")
    void reissueTokenSuccessfullyReturnsNewTokens() {
        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());
        String refreshToken = CORRECT_REFRESH_TOKEN;

        // Redis Mock 셋업
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.hasKey(savedMember.getEmail())).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForValue().get(savedMember.getEmail())).thenReturn(refreshToken);

        // When
        TokenResponse response = authService.reissueToken(refreshToken);

        // Then
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.refreshToken()).isNotNull();
    }

    @Test
    @DisplayName("유효한 액세스 토큰으로 요청 시 성공적으로 회원 정보가 데이터베이스에서 삭제된다.")
    void requestWithValidTokensWithdrawalSuccessfully() {
        // Redis Mock 셋업
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Given
        final Member savedMember = memberRepository.save(MemberFixture.memberFixture());
        final String memberEmail = savedMember.getEmail();
        final String validAccessToken = jwtTokenProvider.generateAccessToken(memberEmail);

        when(redisTemplate.hasKey(memberEmail)).thenReturn(true);

        // When
        authService.withdrawal(validAccessToken);

        // Then
        assertThat(memberRepository.findByEmail(memberEmail)).isEmpty();
    }

    @Test
    @DisplayName("로그인하지 않은 회원의 토큰으로 탈퇴 요청 시 예외를 발생한다.")
    void requestWithUnauthorizedUsersTokenThrowsException() {
        // Redis Mock 셋업
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Given
        final Member savedMember = memberRepository.save(MemberFixture.memberFixture());
        final String memberEmail = savedMember.getEmail();
        final String accessToken = jwtTokenProvider.generateAccessToken(memberEmail);

        when(redisTemplate.hasKey(memberEmail)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.withdrawal(accessToken))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("로그인 되어있지 않은 사용자입니다.");
    }

}
