package friendy.community.domain.member.service;

import friendy.community.domain.member.dto.request.MemberUpdateRequest;
import friendy.community.domain.member.dto.request.PasswordRequest;
import friendy.community.domain.member.encryption.PasswordEncryptor;
import friendy.community.domain.member.encryption.SaltGenerator;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.model.MemberImage;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.upload.service.S3service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private S3service s3Service;

    @Mock
    private MemberDomainService memberDomainService;

    @Mock
    private SaltGenerator saltGenerator;

    @Mock
    private PasswordEncryptor passwordEncryptor;

    @InjectMocks
    private MemberCommandService memberCommandService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.memberFixture();
        ReflectionTestUtils.setField(member, "id", 1L);
    }

    @Test
    @DisplayName("비밀번호를 성공적으로 변경한다")
    void shouldChangePassword() {
        // given
        PasswordRequest request = new PasswordRequest("example@friendy.com", "password123!");
        when(memberDomainService.getMemberByEmail("example@friendy.com")).thenReturn(member);
        when(saltGenerator.generate()).thenReturn("salt");
        when(passwordEncryptor.encrypt(request.newPassword(), "salt")).thenReturn("encryptedPassword");

        // when
        memberCommandService.changePassword(request);

        // then
        assertThat(member.getPassword()).isEqualTo("encryptedPassword");
        assertThat(member.getSalt()).isEqualTo("salt");
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("프로필 이미지를 저장한다")
    void shouldSaveProfileImage() {
        // given
        String originalUrl = "https://temp-bucket.s3.amazonaws.com/tmp/image.jpg";
        String movedUrl = "https://real-bucket.s3.amazonaws.com/profile/image.jpg";
        String s3Key = "profile/image.jpg";
        String fileType = "image/jpeg";

        when(s3Service.moveS3Object(originalUrl, "profile")).thenReturn(movedUrl);
        when(s3Service.extractFilePath(movedUrl)).thenReturn(s3Key);
        when(s3Service.getContentTypeFromS3(s3Key)).thenReturn(fileType);

        // when
        MemberImage result = memberCommandService.saveProfileImage(originalUrl);

        // then
        assertThat(result.getImageUrl()).isEqualTo(movedUrl);
        assertThat(result.getS3Key()).isEqualTo(s3Key);
        assertThat(result.getFileType()).isEqualTo(fileType);
    }

    @Test
    @DisplayName("프로필을 성공적으로 수정한다")
    void shouldUpdateProfile() {
        // given
        when(memberDomainService.getMemberById(1L)).thenReturn(member);
        member.updateMemberImage(new MemberImage("origin", "origin-key", "jpg"));
        doNothing().when(s3Service).deleteFromS3(anyString());

        when(s3Service.moveS3Object(anyString(), eq("profile"))).thenReturn("https://moved.url/image.jpg");
        when(s3Service.extractFilePath(anyString())).thenReturn("profile/image.jpg");
        when(s3Service.getContentTypeFromS3(anyString())).thenReturn("image/jpeg");

        MemberUpdateRequest request = new MemberUpdateRequest("newnickname", LocalDate.of(2000, 1, 1), "new-image.png");

        // when
        memberCommandService.updateMember(request, 1L);

        // then
        assertThat(member.getNickname()).isEqualTo("newnickname");
        assertThat(member.getMemberImage().getImageUrl()).isEqualTo("https://moved.url/image.jpg");
    }


}
