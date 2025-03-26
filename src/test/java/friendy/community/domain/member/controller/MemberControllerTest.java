package friendy.community.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import friendy.community.domain.auth.jwt.JwtTokenFilter;
import friendy.community.domain.auth.service.AuthService;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.dto.request.PasswordRequest;
import friendy.community.domain.member.dto.response.FindMemberResponse;
import friendy.community.domain.member.service.MemberService;
import friendy.community.global.config.MockSecurityConfig;
import friendy.community.global.config.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class)
    })
@Import(MockSecurityConfig.class)
class MemberControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 요청이 성공적으로 처리되면 201 Created와 함께 응답을 반환한다")
    void signupSuccessfullyReturns201Created() throws Exception {
        // given
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("example@friendy.com", "bokSungKim", "password123!", LocalDate.parse("2002-08-13"), null);

        // Mock Service
        when(memberService.signup(any(MemberSignUpRequest.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberSignUpRequest)))
            .andDo(print())
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("이메일이 없으면 400 Bad Request를 반환한다")
    void signupWithoutEmailReturns400BadRequest() throws Exception {
        // Given
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(null, "bokSungKim", "password123!", LocalDate.parse("2002-08-13"), null);

        // When & Then
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberSignUpRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400 Bad Request를 반환한다")
    void signupWithInvalidEmailReturns400BadRequest() throws Exception {
        // Given
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("invalid-email", "bokSungKim", "password123!", LocalDate.parse("2002-08-13"), null);

        // When & Then
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberSignUpRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("닉네임이 없으면 400 Bad Request를 반환한다")
    void signupWithoutNicknameReturns400BadRequest() throws Exception {
        // Given
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("example@friendy.com", null, "password123!", LocalDate.parse("2002-08-13"), null);

        // When & Then
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberSignUpRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @DisplayName("닉네임이 길이 제한을 벗어나면 400 Bad Request를 반환한다")
    @CsvSource({
        "example@friendy.com, a, password123!, 닉네임은 2~20자 사이로 입력해주세요.",
        "example@friendy.com, thisisaveryverylongnickname, password123!, 닉네임은 2~20자 사이로 입력해주세요."
    })
    void signupWithInvalidNicknameLengthReturns400BadRequest(
        String email, String nickname, String password, String expectedMessage) throws Exception {
        // Given
        MemberSignUpRequest request = new MemberSignUpRequest(email, nickname, password, LocalDate.parse("2002-08-13"), null);

        // When & Then
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(result ->
                assertThat(result.getResolvedException().getMessage())
                    .contains(expectedMessage));
    }

    @Test
    @DisplayName("비밀번호가 없으면 400 Bad Request를 반환한다")
    void signupWithoutChangePasswordReturns400BadRequest() throws Exception {
        // Given
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("example@friendy.com", "bokSungKim", null, LocalDate.parse("2002-08-13"), null);

        // When & Then
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberSignUpRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @DisplayName("비밀번호가 숫자, 영문자, 특수문자를 포함하지 않으면 400 Bad Request를 반환한다")
    @CsvSource({
        "example@friendy.com, validNickname, simplepassword, 숫자, 영문자, 특수문자(~!@#$%^&*?)를 포함해야 합니다.",
        "example@friendy.com, validNickname, password123, 숫자, 영문자, 특수문자(~!@#$%^&*?)를 포함해야 합니다.",
        "example@friendy.com, validNickname, 12345678, 숫자, 영문자, 특수문자(~!@#$%^&*?)를 포함해야 합니다."
    })
    void signupWithInvalidChangePasswordPatternReturns400BadRequest(
        String email, String nickname, String password, String expectedMessage) throws Exception {
        // Given
        MemberSignUpRequest request = new MemberSignUpRequest(email, nickname, password, LocalDate.parse("2002-08-13"), null);

        // When & Then
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(result ->
                assertThat(result.getResolvedException().getMessage()).contains(expectedMessage));
    }

    @ParameterizedTest
    @DisplayName("비밀번호가 길이 제한을 벗어나면 400 Bad Request를 반환한다")
    @CsvSource({
        "example@friendy.com, bokSungKim, short, 비밀번호는 8~16자 사이로 입력해주세요.",
        "example@friendy.com, bokSungKim, thispasswordiswaytoolong123!, 비밀번호는 8~16자 사이로 입력해주세요."
    })
    void signupWithInvalidChangePasswordLengthReturns400BadRequest(
        String email, String nickname, String password, String expectedMessage) throws Exception {
        // Given
        MemberSignUpRequest request = new MemberSignUpRequest(email, nickname, password, LocalDate.parse("2002-08-13"), null);

        // When & Then
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(result ->
                assertThat(result.getResolvedException().getMessage())
                    .contains(expectedMessage));
    }

    @Test
    @DisplayName("생년월일이 없으면 400 Bad Request를 반환한다")
    void signupWithoutBirthDateReturns400BadRequest() throws Exception {
        // Given
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("example@friendy.com", "bokSungKim", "password123!", null, null);

        // When & Then
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberSignUpRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 변경이 완료되면 200 OK가 반환된다")
    void resetChangePasswordSuccessfullyReturns200() throws Exception {
        // Given
        PasswordRequest passwordRequest = new PasswordRequest("example@friendy.com", "newPassword123!");

        // When & Then
        mockMvc.perform(post("/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(passwordRequest)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원가입 요청이 성공적으로 처리되면 201 Created와 함께 응답을 반환한다 (이미지 포함)")
    void signupSuccessfullyReturns201CreatedWithImage() throws Exception {
        // Given
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(
            "example@friendy.com", "bokSungKim", "password123!", LocalDate.parse("2002-08-13"),
            "https://example.com/image.jpg" // 이미지 URL 추가
        );

        // Mock Service
        when(memberService.signup(any(MemberSignUpRequest.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberSignUpRequest)))
            .andDo(print())
            .andExpect(status().isCreated());
    }


    @Test
    @DisplayName("회원 정보 조회 성공 시 200 OK와 회원 정보를 반환한다")
    void getMemberInfoSuccessfullyReturns200Ok() throws Exception {
        // Given
        Long memberId = 1L;
        HttpServletRequest request = new MockHttpServletRequest();

        FindMemberResponse response = new FindMemberResponse(
            true, 1L, "example@friendy.com", "bokSungKim", LocalDate.parse("2002-08-13")
        );

        when(memberService.getMemberInfo(any(HttpServletRequest.class), eq(memberId)))
            .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/member/{memberId}", memberId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());
    }
}