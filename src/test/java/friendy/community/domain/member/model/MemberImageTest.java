package friendy.community.domain.member.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberImageTest {

    private MemberImage memberImage;

    @BeforeEach
    void setUp() {
        memberImage = MemberImage.of("old-url.com/image.jpg", "old-file-name", "image/jpeg");
    }

    @Test
    @DisplayName("updateImage 메서드는 imageUrl, s3Key, fileType을 정상적으로 변경해야 한다.")
    void updateImageUpdatesFields() {
        // given
        String newImageUrl = "new-url.com/image.jpg";
        String newS3Key = "new-file-name";
        String newFileType = "image/png";
        // when
        memberImage.updateImage(newImageUrl, newS3Key, newFileType);
        // then
        assertThat(memberImage.getImageUrl()).isEqualTo(newImageUrl);
        assertThat(memberImage.getS3Key()).isEqualTo(newS3Key);
        assertThat(memberImage.getFileType()).isEqualTo(newFileType);
    }
}