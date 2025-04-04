package friendy.community.domain.member.service;

import friendy.community.domain.auth.jwt.JwtTokenExtractor;
import friendy.community.domain.auth.jwt.JwtTokenProvider;
import friendy.community.domain.auth.service.AuthService;
import friendy.community.domain.member.controller.code.MemberExceptionCode;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.dto.request.PasswordRequest;
import friendy.community.domain.member.dto.response.FindMemberResponse;
import friendy.community.domain.member.encryption.PasswordEncryptor;
import friendy.community.domain.member.encryption.SaltGenerator;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.model.MemberImage;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.global.exception.domain.BadRequestException;
import friendy.community.global.exception.domain.ConflictException;
import friendy.community.global.exception.domain.NotFoundException;
import friendy.community.domain.upload.service.S3service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final SaltGenerator saltGenerator;
    private final PasswordEncryptor passwordEncryptor;
    private final JwtTokenExtractor jwtTokenExtractor;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3service s3service;

    public Long signup(MemberSignUpRequest request) {
        validateUniqueMemberAttributes(request);
        final String salt = saltGenerator.generate();
        final String encryptedPassword = passwordEncryptor.encrypt(request.password(), salt);
        Member member = new Member(request, encryptedPassword, salt);

        if (request.imageUrl() != null) {
            MemberImage memberImage = saveProfileImage(request);
            member.updateMemberImage(memberImage);
        }
        memberRepository.save(member);
        return member.getId();
    }

    public void changePassword(PasswordRequest request) {
        Member member = findMemberByEmail(request.email());

        final String salt = saltGenerator.generate();
        final String encryptedPassword = passwordEncryptor.encrypt(request.newPassword(), salt);

        member.changePassword(encryptedPassword, salt);
        memberRepository.save(member);
    }

    public FindMemberResponse getMemberInfo(HttpServletRequest httpServletRequest, Long memberId) {
        final String accessToken = jwtTokenExtractor.extractAccessToken(httpServletRequest);
        final String email = jwtTokenProvider.extractEmailFromAccessToken(accessToken);

        final Member member = findMemberById(memberId);
        boolean isMe = isCurrentUser(member, email);

        return FindMemberResponse.from(member, isMe);
    }

    public void validateUniqueMemberAttributes(MemberSignUpRequest request) {
        assertUniqueEmail(request.email());
        assertUniqueName(request.nickname());
    }

    public void assertUniqueEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BadRequestException(MemberExceptionCode.DUPLICATE_EMAIL_EXCEPTION);
        }
    }

    public void assertUniqueName(String name) {
        if (memberRepository.existsByNickname(name)) {
            throw new BadRequestException(MemberExceptionCode.DUPLICATE_NICKNAME_EXCEPTION);
        }
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException(MemberExceptionCode.USER_NOT_FOUND_EXCEPTION));
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(MemberExceptionCode.EMAIL_NOT_FOUND_EXCEPTION));
    }

    private boolean isCurrentUser(Member member, String email) {
        return member.getEmail().equals(email);
    }

    public MemberImage saveProfileImage(MemberSignUpRequest request) {
        String imageUrl = s3service.moveS3Object(request.imageUrl(), "profile");
        String s3Key = s3service.extractFilePath(imageUrl);
        String fileType = s3service.getContentTypeFromS3(s3Key);
        return MemberImage.of(imageUrl, s3Key, fileType);
    }
}
