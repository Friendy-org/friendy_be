package friendy.community.domain.member.controller.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberExceptionCode {

    DUPLICATE_USER_EXCEPTION(4000,"이" +
            "")

    private final int code;
    private final String message;
}
