package friendy.community.domain.member.service;

import friendy.community.domain.auth.service.AuthService;
import friendy.community.domain.member.controller.code.MemberExceptionCode;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.dto.request.PasswordRequest;
import friendy.community.domain.member.dto.response.FindMemberResponse;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.upload.service.S3service;
import friendy.community.global.exception.domain.BadRequestException;
import friendy.community.global.exception.domain.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static friendy.community.domain.auth.fixtures.TokenFixtures.CORRECT_ACCESS_TOKEN;
import static friendy.community.domain.auth.fixtures.TokenFixtures.OTHER_USER_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@DirtiesContext
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private S3service s3Service;

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resetMemberIdSequence();
    }

    private void resetMemberIdSequence() {
        entityManager.createNativeQuery("ALTER TABLE member AUTO_INCREMENT = 1").executeUpdate();
    }

    @Test
    @DisplayName("회원가입이 성공적으로 처리되면 회원 ID를 반환한다")
    void signupSuccessfullyReturnsMemberId() {
        // Given
        MemberSignUpRequest request = new MemberSignUpRequest(
            "test@email.com", "testNickname", "password123!", LocalDate.parse("2002-08-13"), null
        );
        // When
        Long memberId = memberService.signup(request);
        // Then
        assertThat(memberId).isEqualTo(1L);
    }

    @Test
    @DisplayName("이메일이 중복되면 FriendyException을 던진다")
    void throwsExceptionWhenDuplicateEmail() {
        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());

        // When & Then
        assertThatThrownBy(() -> memberService.assertUniqueEmail(savedMember.getEmail()))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", MemberExceptionCode.DUPLICATE_EMAIL_EXCEPTION);
    }

    @Test
    @DisplayName("닉네임이 중복되면 FriendyException을 던진다")
    void throwsExceptionWhenDuplicateNickname() {
        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());

        // When & Then
        assertThatThrownBy(() -> memberService.assertUniqueName(savedMember.getNickname()))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", MemberExceptionCode.DUPLICATE_NICKNAME_EXCEPTION);
    }

    @Test
    @DisplayName("비밀번호 변경 성공 시 해당 객체의 비밀번호가 변경된다")
    void changePasswordSuccessfullyPasswordIsChanged() {
        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());
        PasswordRequest request = new PasswordRequest(savedMember.getEmail(), "newPassword123!");
        String originPassword = savedMember.getPassword();

        // When
        memberService.changePassword(request);
        Member changedMember = memberService.findMemberByEmail(savedMember.getEmail());

        //Then
        assertThat(originPassword).isNotEqualTo(changedMember.getPassword());
    }

    @Test
    @DisplayName("요청받은 이메일이 존재하지 않으면 예외를 던진다")
    void throwsExceptionWhenEmailDosentExists() {
        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());
        PasswordRequest request = new PasswordRequest("wrongEmail@friendy.com", "newPassword123!");

        // When & Then
        assertThatThrownBy(() -> memberService.changePassword(request))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", MemberExceptionCode.EMAIL_NOT_FOUND_EXCEPTION);
    }

    @Test
    @DisplayName("회원가입시 프로필이있는경우 성공하면 회원ID를 반한한다")
    void signUpwithimageSuccessfullyReturnsMemberId() {
        // Given
        MemberSignUpRequest request = new MemberSignUpRequest(
            "test@email.com", "testNickname", "password123!", LocalDate.parse("2002-08-13"),
            "https://test.s3.us-east-2.amazonaws.com/temp/5f48c9c9-76eb-4309-8fe5-a2f31d9e0d53.jpg"
        );
        String expectedImageUrl = "https://test.s3.us-east-2.amazonaws.com/profile/5f48c9c9-76eb-4309-8fe5-a2f31d9e0d53.jpg";
        String expectedFilePath = "profile/5f48c9c9-76eb-4309-8fe5-a2f31d9e0d53.jpg";

        when(s3Service.moveS3Object(request.imageUrl(), "profile")).thenReturn(expectedImageUrl);
        when(s3Service.extractFilePath(anyString())).thenReturn(expectedFilePath);
        when(s3Service.getContentTypeFromS3(anyString())).thenReturn("jpeg");
        // When
        Long memberId = memberService.signup(request);
        // Then
        assertThat(memberId).isEqualTo(1L);
        verify(s3Service).moveS3Object(request.imageUrl(), "profile");
    }

    @Test
    @DisplayName("회원 조회 요청이 성공하면 FindMemberResponse를 반환한다")
    void getMemberSuccessfullyReturnsFindMemberinfoResponse() {
        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());
        Long memberId = savedMember.getId();

        // When
        FindMemberResponse response = memberService.getMemberInfo(1L, memberId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(memberId);
        assertThat(response.email()).isEqualTo(savedMember.getEmail());
        assertThat(response.nickname()).isEqualTo(savedMember.getNickname());
        assertThat(response.birthDate()).isEqualTo(savedMember.getBirthDate());
    }

    @Test
    @DisplayName("존재하지 않는 회원을 조회하면 예외를 던진다")
    void throwsExceptionWhenMemberNotFound() {
        // Given
        Long nonExistentMemberId = 999L;

        // When & Then
        assertThatThrownBy(() -> memberService.getMemberInfo(1L, nonExistentMemberId))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", MemberExceptionCode.USER_NOT_FOUND_EXCEPTION);
    }

    @Test
    @DisplayName("현재 로그인된 사용자와 조회하는 사용자가 같으면 isMe가 true를 반환한다")
    void getMemberInfoIdentifiesCurrentUserCorrectly() {
        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());

        // When
        FindMemberResponse response = memberService.getMemberInfo(1L, savedMember.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedMember.getId());
        assertThat(response.me()).isTrue();
    }

    @Test
    @DisplayName("현재 로그인된 사용자와 조회하는 사용자가 다르면 isMe가 false를 반환한다")
    void getMemberInfoIdentifiesDifferentUserCorrectly() {
        // Given
        Member savedMember = memberRepository.save(MemberFixture.memberFixture());

        // When
        FindMemberResponse response = memberService.getMemberInfo(999L, savedMember.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedMember.getId());
        assertThat(response.me()).isFalse();
    }

    @Test
    @DisplayName("이메일로 멤버를 조회하여 존재하는 경우 반환한다")
    void findMemberByEmail_MemberExists() {
        // Given
        Member member = memberRepository.save(MemberFixture.memberFixture());

        // When
        Member foundMember = memberService.findMemberByEmail(member.getEmail());

        // Then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getEmail()).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("이메일로 멤버를 조회하여 존재하지 않는 경우 예외를 발생시킨다")
    void findMemberByEmail_MemberNotFound() {
        // given
        String email = "nonexistent@example.com";

        // when & then
        assertThatThrownBy(() -> memberService.findMemberByEmail(email))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", MemberExceptionCode.EMAIL_NOT_FOUND_EXCEPTION);
    }
}