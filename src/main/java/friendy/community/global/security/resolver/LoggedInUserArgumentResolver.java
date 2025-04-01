package friendy.community.global.security.resolver;

import friendy.community.domain.auth.controller.code.AuthExceptionCode;
import friendy.community.global.exception.domain.UnAuthorizedException;
import friendy.community.global.security.FriendyUserDetails;
import friendy.community.global.security.FriendyUserDetailsService;
import friendy.community.global.security.annotation.LoggedInUser;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoggedInUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final FriendyUserDetailsService userDetailsService;

    public LoggedInUserArgumentResolver(FriendyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoggedInUser.class) &&
            FriendyUserDetails.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        FriendyUserDetails userDetails = (FriendyUserDetails) authentication.getPrincipal();

        if (userDetails.getMemberId() == -1L) {
            throw new UnAuthorizedException(AuthExceptionCode.UNAUTHORIZED_ACCESS);
        }
        return authentication.getPrincipal();
    }
}
