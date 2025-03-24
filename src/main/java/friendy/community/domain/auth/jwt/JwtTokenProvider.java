package friendy.community.domain.auth.jwt;

import friendy.community.domain.auth.controller.code.AuthExceptionCode;
import friendy.community.global.exception.domain.UnAuthorizedException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final String EMAIL_KEY = "email";
    private final StringRedisTemplate redisTemplate;
    @Value("${jwt.access.secret}")
    private String jwtAccessTokenSecret;
    @Value("${jwt.access.expiration}")
    private long jwtAccessTokenExpirationInMs;
    @Value("${jwt.refresh.secret}")
    private String jwtRefreshTokenSecret;
    @Value("${jwt.refresh.expiration}")
    private long jwtRefreshTokenExpirationInMs;

    public String generateAccessToken(final String email) {
        final SecretKey secretKey = new SecretKeySpec(jwtAccessTokenSecret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        return buildJwtToken(email, jwtAccessTokenExpirationInMs, secretKey);
    }

    public String generateRefreshToken(final String email) {
        final SecretKey secretKey = new SecretKeySpec(jwtRefreshTokenSecret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        final String generatedToken = buildJwtToken(email, jwtRefreshTokenExpirationInMs, secretKey);

        saveRefreshToken(email, generatedToken);

        return generatedToken;
    }

    public String extractEmailFromAccessToken(final String token) {
        final Jws<Claims> claimsJws = getAccessTokenParser().parseClaimsJws(token);
        final String extractedEmail = claimsJws.getBody().get(EMAIL_KEY, String.class);
        if (extractedEmail == null) {
            throw new UnAuthorizedException(AuthExceptionCode.ACCESS_TOKEN_EMAIL_MISSING);
        }
        return extractedEmail;
    }

    public String extractEmailFromRefreshToken(final String token) {
        validateRefreshToken(token);
        final Jws<Claims> claimsJws = getRefreshTokenParser().parseClaimsJws(token);
        final String extractedEmail = claimsJws.getBody().get(EMAIL_KEY, String.class);
        if (extractedEmail == null) {
            throw new UnAuthorizedException(AuthExceptionCode.REFRESH_TOKEN_EMAIL_MISSING);
        }
        validateUserAuthorization(extractedEmail);
        return extractedEmail;
    }

    public void validateAccessToken(final String token) {
        try {
            final Claims claims = getAccessTokenParser().parseClaimsJws(token).getBody();
        } catch (MalformedJwtException e) {
            throw new UnAuthorizedException(AuthExceptionCode.MALFORMED_ACCESS_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new UnAuthorizedException(AuthExceptionCode.UNSUPPORTED_ACCESS_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new UnAuthorizedException(AuthExceptionCode.EXPIRED_ACCESS_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new UnAuthorizedException(AuthExceptionCode.EMPTY_ACCESS_TOKEN);
        } catch (JwtException e) {
            throw new UnAuthorizedException(AuthExceptionCode.INVALID_ACCESS_TOKEN);
        }
    }


    public void deleteRefreshToken(final String email) {
        validateUserAuthorization(email);
        redisTemplate.delete(email);
    }

    public void validateRefreshToken(final String token) {
        try {
            final Claims claims = getRefreshTokenParser().parseClaimsJws(token).getBody();
        } catch (MalformedJwtException | UnsupportedJwtException e) {
            throw new UnAuthorizedException(AuthExceptionCode.INVALID_REFRESH_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new UnAuthorizedException(AuthExceptionCode.EXPIRED_REFRESH_TOKEN);
        }
    }

    private void validateUserAuthorization(final String email) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(email))) {
            final String logMessage = "로그인 되어있지 않은 사용자입니다.";
            throw new UnAuthorizedException(AuthExceptionCode.USER_NOT_LOGGED_IN);
        }
    }

    private String buildJwtToken(final String email, final long tokenExpirationInMs, final SecretKey secretKey) {
        final Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenExpirationInMs);

        return Jwts.builder()
            .claim(EMAIL_KEY, email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey)
            .compact();
    }

    private JwtParser getAccessTokenParser() {
        return Jwts.parserBuilder()
            .setSigningKey(jwtAccessTokenSecret.getBytes(StandardCharsets.UTF_8))
            .build();
    }

    private JwtParser getRefreshTokenParser() {
        return Jwts.parserBuilder()
            .setSigningKey(jwtRefreshTokenSecret.getBytes(StandardCharsets.UTF_8))
            .build();
    }

    public void saveRefreshToken(final String email, final String refreshToken) {
        redisTemplate.opsForValue().set(
            email,
            refreshToken,
            jwtRefreshTokenExpirationInMs,
            TimeUnit.MILLISECONDS
        );
    }
}
