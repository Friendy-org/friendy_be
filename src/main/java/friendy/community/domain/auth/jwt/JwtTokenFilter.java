package friendy.community.domain.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.security.FriendyUserDetails;
import friendy.community.global.security.FriendyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenExtractor jwtTokenExtractor;
    private final FriendyUserDetailsService friendyUserDetailsService;

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = jwtTokenExtractor.extractAccessToken(request);

            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            jwtTokenProvider.validateAccessToken(token);

            setAuthentication(token);

            filterChain.doFilter(request, response);
        } catch (FriendyException e) {
            log.error("JWT 검증 실패", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(
                new ExceptionResponse(e.getErrorCode().getCode(), e.getMessage())
            );
            response.getWriter().write(jsonResponse);
        }
    }

    public void setAuthentication(String token) {
        String email;
        try {
            email = jwtTokenProvider.extractEmailFromAccessToken(token);
        } catch (FriendyException e) {
            throw new FriendyException(ErrorCode.UNAUTHORIZED_USER, "이메일 정보가 포함되지 않은 JWT 토큰입니다.");
        }

        FriendyUserDetails friendyUserDetails;
        try {
            friendyUserDetails = friendyUserDetailsService.loadUserByUsername(email);
        } catch (Exception e) {
            throw new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "이메일 사용자가 없습니다.");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            friendyUserDetails,
            null,
            friendyUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
