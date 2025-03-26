package friendy.community.domain.auth.fixtures;

import io.jsonwebtoken.Jwts;

import java.util.Date;

public class TokenFixtures {

    /**
     * CORRECT_REFRESH_TOKEN : 만료 기한 - 2123년 10월 30일
     */
    public static final String CORRECT_ACCESS_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImV4YW1wbGVAZnJpZW5keS5jb20iLCJpYXQiOjE2MDAwMDAwMDAsImV4cCI6NDg1NDI3ODQwMH0.I5Y8zaMf6ys1X3ESNzVK3HzH7mJFquPwiyAnmiH4RQg";
    public static final String CORRECT_REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImV4YW1wbGVAZnJpZW5keS5jb20iLCJpYXQiOjE2MDAwMDAwMDAsImV4cCI6NDg1NDI3ODQwMH0.I5Y8zaMf6ys1X3ESNzVK3HzH7mJFquPwiyAnmiH4RQg";
    public static final String CORRECT_ACCESS_TOKEN_WITHOUT_BEARER = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImV4YW1wbGVAZnJpZW5keS5jb20iLCJpYXQiOjE2MDAwMDAwMDAsImV4cCI6NDg1NDI3ODQwMH0.I5Y8zaMf6ys1X3ESNzVK3HzH7mJFquPwiyAnmiH4RQg";

    /**
     * OTHER_USER_TOKEN
     * email: user@example.com
     * nickname: 홍길동
     * password: password123!
     * birthday: 2002-08-13
     */
    public static final String OTHER_USER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDcwNTI4MDAsImV4cCI6NDg1NDg4MzIwMH0.KBSErlIecTDvYFvmO0D0QQX1cnNeTf9KDgTGgOFY1f0";

    public static final String MALFORMED_JWT_TOKEN = "aabbcc";

    public static final String EXPIRED_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImV4YW1wbGVAZnJpZW5keS5jb20iLCJpYXQiOjE2MDAwMDAwMDAsImV4cCI6MTYwMDAwMDEwMH0.mqOL2LPVIqTlrjqAWElM5XsJMgTjxWsEpOkr0atIdKs";

    public static final String UNSUPPORTED_JWT_TOKEN =
        Jwts.builder()
        .setSubject("test@example.com")
        .setExpiration(new Date(System.currentTimeMillis() + 10000)) // 만료 시간 설정
        .setHeaderParam("alg", "none")  // 서명하지 않음
        .compact();

    public static final String INVALID_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.invalid_signature"; // 서명이 잘못된 토큰

    public static final String ACCESS_TOKEN_WITHOUT_EMAIL = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJleGFtcGxlU3ViamVjdCIsInNvbWVJbnB1dCI6InNvbWVWaWx1ZSJ9.yE4xFsm_BxgU8-TPSbCqD8wPTI9nkvSjb7dfA0RgkHg";

    /**
     ** MISSING_CLAIM_TOKEN : 만료 기한 - 2123년 10월 30일
     */
    public static final String MISSING_CLAIM_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2MDAwMDAwMDAsImV4cCI6NDg1NDI3ODQwMH0.65lNI_07FgETBnasvHCzxc1RDfLSoDBJr0Vebocu2gI";


}

