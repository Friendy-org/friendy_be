package friendy.community.domain.auth.jwt;

import friendy.community.global.exception.FriendyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JwtTokenExtractorTest {

    @Autowired
    private JwtTokenExtractor jwtTokenExtractor;

    @Test
    @DisplayName("액세스 토큰 추출에 성공한다")
    void extractAccessTokenSuccessfully() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = "Bearer validAccessToken123";
        request.addHeader("Authorization", token);

        // when
        String accessToken = jwtTokenExtractor.extractAccessToken(request);

        // then
        assertThat(accessToken).isEqualTo("validAccessToken123");
    }

    @Test
    @DisplayName("리프레시 토큰 추출에 성공한다")
    void extractRefreshTokenSuccessfully() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = "Bearer validRefreshToken123";
        request.addHeader("Authorization-Refresh", token);

        // when
        String refreshToken = jwtTokenExtractor.extractRefreshToken(request);

        // then
        assertThat(refreshToken).isEqualTo("validRefreshToken123");
    }

    @Test
    @DisplayName("리프레시 토큰이 잘못된 형식이면 예외를 발생시킨다")
    void throwExceptionForInvalidRefreshTokenFormat() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = "InvalidToken123";
        request.addHeader("Authorization-Refresh", token);

        // when & then
        assertThatThrownBy(() -> jwtTokenExtractor.extractRefreshToken(request))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("인증 실패(리프레시 토큰 추출 실패) - 토큰 : " + token);
    }

    @Test
    @DisplayName("액세스 토큰이 null이면 null을 반환한다")
    void throwExceptionWhenAccessTokenIsNull() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when
        String token = jwtTokenExtractor.extractAccessToken(request);

        // then
        assertThat(token).isNull();
    }

    @Test
    @DisplayName("액세스 토큰이 'Bearer '로 시작하지 않으면 null을 반환한다")
    void throwExceptionWhenAccessTokenFormatIsInvalid() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = "InvalidToken";
        request.addHeader("Authorization", token);

        // when
        String accessToken = jwtTokenExtractor.extractAccessToken(request);

        // then
        assertThat(accessToken).isNull();
    }

    @Test
    @DisplayName("리프레시 토큰이 null이거나 빈 값이면 예외를 발생시킨다")
    void throwExceptionWhenRefreshTokenIsNullOrEmpty() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when & then
        assertThatThrownBy(() -> jwtTokenExtractor.extractRefreshToken(request))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("인증 실패(리프레시 토큰 추출 실패) - 토큰 : null");
    }
}
