package friendy.community.domain.auth.jwt;

import friendy.community.domain.auth.controller.code.AuthExceptionCode;
import friendy.community.global.exception.domain.UnAuthorizedException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Date;
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
    @DisplayName("엑세스토큰 유효성 검사 통과")
    void validateAccessTokenSuccessfully() {
        jwtTokenProvider.validateAccessToken(CORRECT_ACCESS_TOKEN_WITHOUT_BEARER);
    }

    @Test
    @DisplayName("엑세스토큰 예외")
    void validateAccessToken_ShouldThrowCorrectException() {
        // given
        String unsupportedToken =
            Jwts.builder()
                .setSubject("test@example.com")
                .setExpiration(new Date(System.currentTimeMillis() + 10000)) // 만료 시간 설정
                .setHeaderParam("alg", "none")  // 서명하지 않음
                .compact();

        String invalidJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.invalid_signature"; // 서명이 잘못된 토큰

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateAccessToken(MALFORMED_JWT_TOKEN))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining(AuthExceptionCode.MALFORMED_ACCESS_TOKEN.getMessage());

        assertThatThrownBy(() -> jwtTokenProvider.validateAccessToken(EXPIRED_TOKEN))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining(AuthExceptionCode.EXPIRED_ACCESS_TOKEN.getMessage());

        assertThatThrownBy(() -> jwtTokenProvider.validateAccessToken(""))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining(AuthExceptionCode.EMPTY_ACCESS_TOKEN.getMessage());

        assertThatThrownBy(() -> jwtTokenProvider.validateAccessToken(INVALID_JWT_TOKEN))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining(AuthExceptionCode.INVALID_ACCESS_TOKEN.getMessage());

        assertThatThrownBy(() -> jwtTokenProvider.validateAccessToken(UNSUPPORTED_JWT_TOKEN))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining(AuthExceptionCode.UNSUPPORTED_ACCESS_TOKEN.getMessage());
    }


    @Test
    @DisplayName("엑세스 토큰에 이메일 클레임이 누락된 경우 예외를 발생시킨다")
    void throwExceptionForMissingEmailClaimInAccessToken() {
        // given
        String tokenWithoutEmailClaim = MISSING_CLAIM_TOKEN;

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.extractEmailFromAccessToken(tokenWithoutEmailClaim))
            .isInstanceOf(UnAuthorizedException.class);
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
            .isInstanceOf(UnAuthorizedException.class);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰에서 이메일 추출 시 예외를 발생시킨다")
    void throwExceptionForExpiredRefreshTokenEmailExtraction() {
        // given
        String expiredRefreshToken = EXPIRED_TOKEN;

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.extractEmailFromRefreshToken(expiredRefreshToken))
            .isInstanceOf(UnAuthorizedException.class);
    }

    @Test
    @DisplayName("리프레시 토큰에 이메일 클레임이 누락된 경우 예외를 발생시킨다")
    void throwExceptionForMissingEmailClaimInRefreshToken() {
        // given
        String tokenWithoutEmailClaim = MISSING_CLAIM_TOKEN;

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.extractEmailFromRefreshToken(tokenWithoutEmailClaim))
            .isInstanceOf(UnAuthorizedException.class);
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
            .isInstanceOf(UnAuthorizedException.class);
    }
}
