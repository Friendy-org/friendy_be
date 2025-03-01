package friendy.community.domain.auth.fixtures;

public class TokenFixtures {

    /**
     * CORRECT_REFRESH_TOKEN : 만료 기한 - 2123년 10월 30일
     */
    public static final String CORRECT_ACCESS_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImV4YW1wbGVAZnJpZW5keS5jb20iLCJpYXQiOjE2MDAwMDAwMDAsImV4cCI6NDg1NDI3ODQwMH0.I5Y8zaMf6ys1X3ESNzVK3HzH7mJFquPwiyAnmiH4RQg";
    public static final String CORRECT_REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImV4YW1wbGVAZnJpZW5keS5jb20iLCJpYXQiOjE2MDAwMDAwMDAsImV4cCI6NDg1NDI3ODQwMH0.I5Y8zaMf6ys1X3ESNzVK3HzH7mJFquPwiyAnmiH4RQg";

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

    /**
     * MISSING_CLAIM_TOKEN : 만료 기한 - 2123년 10월 30일
     */
    public static final String MISSING_CLAIM_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2MDAwMDAwMDAsImV4cCI6NDg1NDI3ODQwMH0.65lNI_07FgETBnasvHCzxc1RDfLSoDBJr0Vebocu2gI";

}

