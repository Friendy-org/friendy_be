package friendy.community.domain.follow.model;

import friendy.community.domain.common.BaseEntity;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.model.Member;
import jakarta.persistence.*;
import lombok.*;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "follow",
    uniqueConstraints = { @UniqueConstraint(name = "unique_follow", columnNames = {"follower_id", "following_id"}) },
    indexes = {
        @Index(name = "idx_follower", columnList = "follower_id"),
        @Index(name = "idx_following", columnList = "following_id")
    }
)
public class Follow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private Member follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private Member following;

    private Follow(Member follower, Member following) {
        this.follower = follower;
        this.following = following;
    }

    public static Follow of(Member follower, Member following) {
        return new Follow(follower, following);
    }
}
