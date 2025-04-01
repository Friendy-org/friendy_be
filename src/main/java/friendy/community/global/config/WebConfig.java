package friendy.community.global.config;

import friendy.community.global.security.FriendyUserDetailsService;
import friendy.community.global.security.resolver.LoggedInUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    private final FriendyUserDetailsService userDetailsService;

    public WebConfig(FriendyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoggedInUserArgumentResolver(userDetailsService));
    }
}