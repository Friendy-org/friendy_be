package friendy.community.domain.member.model;

import friendy.community.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private String fileType;

    public MemberImage(String imageUrl, String s3Key, String fileType) {
        this.imageUrl = imageUrl;
        this.s3Key = s3Key;
        this.fileType = fileType;
    }

    public static MemberImage of(String imageUrl, String storedFileName, String fileType) {
        return new MemberImage(imageUrl, storedFileName, fileType);
    }

    public void updateImage(String imageUrl, String storedFileName, String fileType) {
        this.imageUrl = imageUrl;
        this.s3Key = storedFileName;
        this.fileType = fileType;
    }



}