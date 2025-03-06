package friendy.community.domain.auth.jwt;

import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenExtractor jwtTokenExtractor;
    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        // 1️⃣ Access Token 추출
        String token = jwtTokenExtractor.extractAccessToken(request);

        if (token != null) {
            // 2️⃣ 토큰 검증
            try {
                jwtTokenProvider.validateAccessToken(token);  // 토큰이 유효한지 검사

                // 3️⃣ 토큰이 유효하면 이메일과 회원 ID를 추출
                String email = jwtTokenProvider.extractEmailFromAccessToken(token);
                String memberId = redisTemplate.opsForValue().get(email);

                if (memberId == null) {
                    // 4️⃣ Redis에 memberId가 없으면 DB 조회
                    memberId = memberRepository.findIdByEmail(email)
                        .map(String::valueOf)  // 숫자 id를 String으로 변환
                        .orElseThrow(() -> new FriendyException(ErrorCode.UNAUTHORIZED_USER, "User not found in DB"));

                    // 5️⃣ Redis에 `memberId` 저장 (30분 캐싱)
                    redisTemplate.opsForValue().set(email, memberId, 30, TimeUnit.MINUTES);
                }

                // 6️⃣ Authentication 객체 생성하여 SecurityContext에 설정
                Authentication authentication = new UsernamePasswordAuthenticationToken(memberId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException | FriendyException e) {
                // 7️⃣ 만약 액세스 토큰이 만료되었으면 리프레시 토큰을 사용해 새로운 액세스 토큰 발급
                String email = jwtTokenProvider.extractEmailFromAccessToken(token);
                String refreshToken = redisTemplate.opsForValue().get(email);
            }
        }
        // 필터 체인 진행
        filterChain.doFilter(request, response);
    }
}