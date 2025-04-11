package friendy.community.domain.member.service;

import friendy.community.domain.member.controller.code.MemberExceptionCode;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.global.exception.domain.BadRequestException;
import friendy.community.global.exception.domain.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberDomainServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberDomainService memberDomainService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.memberFixture();
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "email", "test@example.com");
    }

    @Test
    @DisplayName("ID로 회원 조회에 성공한다")
    void shouldReturnMemberWhenFoundById() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when
        Member found = memberDomainService.getMemberById(1L);

        // then
        assertThat(found).isEqualTo(member);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 NotFoundException을 던진다")
    void shouldThrowNotFoundExceptionWhenMemberIdDoesNotExist() {
        // given
        Long id = 999L;
        when(memberRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberDomainService.getMemberById(id))
            .isInstanceOf(NotFoundException.class)
            .satisfies(e -> assertThat(((NotFoundException) e).getExceptionType())
                .isEqualTo(MemberExceptionCode.USER_NOT_FOUND_EXCEPTION));
    }

    @Test
    @DisplayName("이메일로 회원 조회에 성공한다")
    void shouldReturnMemberWhenFoundByEmail() {
        // given
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(member));

        // when
        Member found = memberDomainService.getMemberByEmail("test@example.com");

        // then
        assertThat(found).isEqualTo(member);
    }

    @Test
    @DisplayName("중복된 이메일이면 BadRequestException을 던진다")
    void shouldThrowBadRequestExceptionWhenEmailIsDuplicated() {
        // given
        String email = "test@example.com";
        when(memberRepository.existsByEmail(email)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberDomainService.assertUniqueEmail(email))
            .isInstanceOf(BadRequestException.class)
            .satisfies(e -> assertThat(((BadRequestException) e).getExceptionType())
                .isEqualTo(MemberExceptionCode.DUPLICATE_EMAIL_EXCEPTION));
    }

    @Test
    @DisplayName("중복된 닉네임이면 BadRequestException을 던진다")
    void shouldThrowBadRequestExceptionWhenNicknameIsDuplicated() {
        // given
        String name = "nickname";
        when(memberRepository.existsByNickname(name)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberDomainService.assertUniqueName(name))
            .isInstanceOf(BadRequestException.class)
            .satisfies(e -> assertThat(((BadRequestException) e).getExceptionType())
                .isEqualTo(MemberExceptionCode.DUPLICATE_NICKNAME_EXCEPTION));
    }
}
