package friendy.community.domain.member.service;

import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.dto.request.PasswordRequest;
import friendy.community.domain.member.encryption.PasswordEncryptor;
import friendy.community.domain.member.encryption.SaltGenerator;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.model.MemberImage;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.upload.service.S3service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final SaltGenerator saltGenerator;
    private final PasswordEncryptor passwordEncryptor;
    private final S3service s3service;
    private final MemberDomainService memberDomainService;

    public Long signup(MemberSignUpRequest request) {
        final String salt = saltGenerator.generate();
        final String encryptedPassword = passwordEncryptor.encrypt(request.password(), salt);
        memberDomainService.validateUniqueMemberAttributes(request.email(), request.nickname());
        Member member = new Member(request, encryptedPassword, salt);

        if (request.imageUrl() != null) {
            MemberImage memberImage = saveProfileImage(request.imageUrl());
            member.updateMemberImage(memberImage);
        }
        memberRepository.save(member);
        return member.getId();
    }

    public void changePassword(PasswordRequest request) {
        Member member =  memberDomainService.getMemberByEmail(request.email());
        final String salt = saltGenerator.generate();
        final String encryptedPassword = passwordEncryptor.encrypt(request.newPassword(), salt);

        member.changePassword(encryptedPassword, salt);
        memberRepository.save(member);
    }

    public MemberImage saveProfileImage(String imageUrl) {
        String movedUrl = s3service.moveS3Object(imageUrl, "profile");
        String s3Key = s3service.extractFilePath(movedUrl);
        String fileType = s3service.getContentTypeFromS3(s3Key);
        return MemberImage.of(movedUrl, s3Key, fileType);
    }
}
