package friendy.community.global.security;

import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class FriendyUserDetailsService implements UserDetailsService {

    private final MemberService memberService;

    @Override
    public FriendyUserDetails loadUserByUsername(String email) {
        Member member = memberService.findMemberByEmail(email);

        return new FriendyUserDetails(
            member.getId(),  // memberId
            member.getEmail(),  // 이메일
            member.getPassword(),  // 비밀번호
            Collections.emptyList()
        );
    }

    public FriendyUserDetails createAnonymousUser() {
        return new FriendyUserDetails(
            -1L,  
            "",
            "",
            Collections.emptyList()
        );
    }
}
