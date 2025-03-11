package friendy.community.domain.auth.jwt;

import friendy.community.domain.member.service.MemberService;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenExtractor jwtTokenExtractor;
    private final StringRedisTemplate redisTemplate;
    private final MemberService memberService;

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 1. 액세스 토큰 추출
            String token = jwtTokenExtractor.extractAccessToken(request);

            // 2. 토큰이 없는 경우에는 필터 체인 진행 (다음 필터로 이동)
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 토큰이 있는 경우, 이메일을 추출하고 인증 정보를 설정
            setAuthentication(token);
            // 4. 필터 체인 계속 진행
            filterChain.doFilter(request, response);

        } catch (final IllegalStateException e) {
            log.error("Token extraction or validation failed", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Invalid token. Please log in again.\"}");
        } catch (final ExpiredJwtException e) {
            log.error("Token expired", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Expired token. Please log in again.\"}");
        } catch (IOException e) {
            log.error("IOException while writing response", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Internal server error. Please try again later.\"}");
        }
    }

    public void setAuthentication(String token) {
        // 1. JWT에서 이메일 추출
        String email = jwtTokenProvider.extractEmailFromAccessToken(token);

        // 2. Redis에서 이메일로 memberId 조회
        String memberId = redisTemplate.opsForValue().get(email); // Redis에서 email을 key로 memberId를 조회

        if (memberId == null) { // Redis에 없으면 DB에서 조회

            memberId = memberService.findMemberIdByEmail(email);
            if (memberId != null) {
                redisTemplate.opsForValue().set(email, memberId, 30, TimeUnit.MINUTES); // Redis에 memberId 캐싱 (30분 동안 유효)
            }
        }

        if (memberId != null) {
            // 3. 이메일과 memberId로 인증 정보 설정
            Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            // DB에서 memberId를 찾을 수 없는 경우 처리 (예: 인증 실패)
            throw new FriendyException(ErrorCode.UNAUTHORIZED_USER, "유효한 회원 정보가 없습니다.");
        }
    }
}
