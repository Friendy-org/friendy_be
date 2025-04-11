package friendy.community.domain.member.service;

import friendy.community.domain.member.dto.request.PasswordRequest;
import friendy.community.domain.member.encryption.PasswordEncryptor;
import friendy.community.domain.member.encryption.SaltGenerator;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.model.MemberImage;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.upload.service.S3service;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@DirtiesContext
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
        MockitoAnnotations.openMocks(this);
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
}
