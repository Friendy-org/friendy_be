package friendy.community.domain.auth.jwt;

import friendy.community.domain.auth.service.AuthService;
import friendy.community.global.exception.FriendyException;
import org.assertj.core.data.Percentage;
import org.hamcrest.number.IsCloseTo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static friendy.community.domain.auth.fixtures.TokenFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("엑세스 토큰 생성에 성공한다")
    void generateAccessTokenSuccessfully() {
        // given
        String email = "example@friendy.com";

        // when
        String accessToken = jwtTokenProvider.generateAccessToken(email);

        // then
        assertThat(accessToken).isNotNull();
    }

    @Test
    @DisplayName("엑세스 토큰에서 이메일을 추출한다")
    void extractEmailFromAccessTokenSuccessfully() {
        // given
        String email = "example@friendy.com";
        String accessToken = jwtTokenProvider.generateAccessToken(email);

        when(redisTemplate.hasKey(email)).thenReturn(true);

        // when
        String extractedEmail = jwtTokenProvider.extractEmailFromAccessToken(accessToken);

        // then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("잘못된 형식의 엑세스 토큰에서 이메일 추출 시 예외를 발생시킨다")
    void throwExceptionForMalformedAccessTokenEmailExtraction() {
        // given
        String malFormedJwtToken = MALFORMED_JWT_TOKEN;

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.extractEmailFromAccessToken(malFormedJwtToken))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("인증 실패(잘못된 액세스 토큰) - 토큰 : " + malFormedJwtToken);
    }

    @Test
    @DisplayName("만료된 엑세스 토큰에서 이메일 추출 시 예외를 발생시킨다")
    void throwExceptionForExpiredAccessTokenEmailExtraction() {
        // given
        String expiredAccessToken = EXPIRED_TOKEN;

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.extractEmailFromAccessToken(expiredAccessToken))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("인증 실패(만료된 액세스 토큰) - 토큰 : " + expiredAccessToken);
    }

    @Test
    @DisplayName("엑세스 토큰에 이메일 클레임이 누락된 경우 예외를 발생시킨다")
    void throwExceptionForMissingEmailClaimInAccessToken() {
        // given
        String tokenWithoutEmailClaim = MISSING_CLAIM_TOKEN;

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.extractEmailFromAccessToken(tokenWithoutEmailClaim))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("인증 실패(JWT 액세스 토큰 Payload 이메일 누락) - 토큰 : " + tokenWithoutEmailClaim);
    }

    @Test
    @DisplayName("리프레시 토큰 생성에 성공하면 redis에 토큰이 저장된다.")
    void generateRefreshTokenSuccessfullyStoresRefreshToken() {
        // given
        String email = "example@friendy.com";

        // Redis Mock 설정
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        // then
        assertThat(refreshToken).isNotNull();
        verify(valueOperations, times(1)).set(
                eq(email),
                eq(refreshToken),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("리프레시 토큰에서 이메일을 추출한다")
    void extractEmailFromRefreshTokenSuccessfully() {
        // Redis Mock 설정
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // given
        String email = "example@friendy.com";
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        when(redisTemplate.hasKey(email)).thenReturn(true);
        when(redisTemplate.opsForValue().get(email)).thenReturn(refreshToken);

        // when
        String extractedEmail = jwtTokenProvider.extractEmailFromRefreshToken(refreshToken);

        // then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("잘못된 형식의 리프레시 토큰에서 이메일 추출 시 예외를 발생시킨다")
    void throwExceptionForMalformedRefreshTokenEmailExtraction() {
        // given
        String malFormedJwtToken = MALFORMED_JWT_TOKEN;

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.extractEmailFromRefreshToken(malFormedJwtToken))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("인증 실패(잘못된 리프레시 토큰) - 토큰 : " + malFormedJwtToken);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰에서 이메일 추출 시 예외를 발생시킨다")
    void throwExceptionForExpiredRefreshTokenEmailExtraction() {
        // given
        String expiredRefreshToken = EXPIRED_TOKEN;

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.extractEmailFromRefreshToken(expiredRefreshToken))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("인증 실패(만료된 리프레시 토큰) - 토큰 : " + expiredRefreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰에 이메일 클레임이 누락된 경우 예외를 발생시킨다")
    void throwExceptionForMissingEmailClaimInRefreshToken() {
        // given
        String tokenWithoutEmailClaim = MISSING_CLAIM_TOKEN;

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.extractEmailFromRefreshToken(tokenWithoutEmailClaim))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("인증 실패(JWT 리프레시 토큰 Payload 이메일 누락) - 토큰 : " + tokenWithoutEmailClaim);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰이 Redis에 저장되어 있지 않으면 예외를 발생시킨다")
    void throwExceptionForValidRefreshTokenNotSavedInRedis() {
        // Redis Mock 설정
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // given
        String email = "example@friendy.com";
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        when(redisTemplate.hasKey(email)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.extractEmailFromRefreshToken(refreshToken))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("로그인 되어있지 않은 사용자입니다.");
    }

}
