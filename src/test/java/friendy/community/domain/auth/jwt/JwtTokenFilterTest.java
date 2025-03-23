package friendy.community.domain.auth.jwt;

import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.service.MemberService;
import friendy.community.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.io.PrintWriter;

import static friendy.community.domain.auth.fixtures.TokenFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@DirtiesContext
class JwtTokenFilterTest {
    @Autowired
    JwtTokenFilter jwtTokenFilter;
    @Autowired
    MemberService memberService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private JwtTokenExtractor jwtTokenExtractor;
    @MockitoBean
    private HttpServletRequest request;
    @MockitoBean
    private HttpServletResponse response;
    @MockitoBean
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        filterChain = Mockito.mock(FilterChain.class);

        Member member = MemberFixture.memberFixture();

        memberService.signup(new MemberSignUpRequest(
            member.getEmail(), member.getNickname(), member.getPassword(), member.getBirthDate(), null));
    }

    @Test
    @DisplayName("토큰이 없으면 필터 체인을 계속 진행한다.")
    void shouldProceedWithFilterChainWhenNoToken() throws ServletException, IOException {
        // given
        when(jwtTokenExtractor.extractAccessToken(request)).thenReturn(null);

        // when
        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효한 토큰이 있을 경우 필터 체인을 진행한다.")
    void shouldProceedWithFilterChainWhenTokenIsPresent() throws Exception {
        // Given
        when(jwtTokenExtractor.extractAccessToken(request)).thenReturn(CORRECT_ACCESS_TOKEN_WITHOUT_BEARER);
        when(jwtTokenProvider.extractEmailFromAccessToken(CORRECT_ACCESS_TOKEN_WITHOUT_BEARER)).thenReturn("example@friendy.com");

        // When
        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtTokenProvider).validateAccessToken(CORRECT_ACCESS_TOKEN_WITHOUT_BEARER);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("사용자가 이메일을 찾을 수 없으면 예외가 발생한다.")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(jwtTokenExtractor.extractAccessToken(request)).thenReturn(CORRECT_ACCESS_TOKEN);
        when(jwtTokenProvider.extractEmailFromAccessToken(CORRECT_ACCESS_TOKEN)).thenReturn(null);

        // When
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            jwtTokenFilter.setAuthentication(CORRECT_ACCESS_TOKEN);
        });

        //Then
        assertEquals("이메일 사용자가 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("이메일 정보가 없는 JWT 토큰일 경우 예외가 발생한다.")
    void shouldThrowExceptionWhenEmailNotFound() {
        // Given
        when(jwtTokenExtractor.extractAccessToken(request)).thenReturn(ACCESS_TOKEN_WITHOUT_EMAIL);
        when(jwtTokenProvider.extractEmailFromAccessToken(ACCESS_TOKEN_WITHOUT_EMAIL)).thenReturn("invalid@domain.com");
        doThrow(new FriendyException(ErrorCode.UNAUTHORIZED_USER, "이메일 정보가 포함되지 않은 JWT 토큰입니다."))
            .when(jwtTokenProvider).extractEmailFromAccessToken(ACCESS_TOKEN_WITHOUT_EMAIL);

        // When
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            jwtTokenFilter.setAuthentication(ACCESS_TOKEN_WITHOUT_EMAIL);
        });

        // Then
        assertEquals("이메일 정보가 포함되지 않은 JWT 토큰입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰일 경우 예외를 던진다.")
    void shouldThrowExceptionWhenTokenIsInvalid() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid-token";
        when(jwtTokenExtractor.extractAccessToken(request)).thenReturn(invalidToken);
        doThrow(new FriendyException(ErrorCode.INVALID_TOKEN, "유효하지 않은 JWT 토큰 형식입니다."))
            .when(jwtTokenProvider).validateAccessToken(invalidToken);

        PrintWriter mockWriter = Mockito.mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(mockWriter);
        doNothing().when(mockWriter).write(anyString());

        // When
        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
    }
}
