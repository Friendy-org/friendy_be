package friendy.community.global.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityPathConfig {

    public static final List<String> NO_AUTH_API_URIS = List.of(
        "/swagger-ui",
        "/v3/api-docs",
        "/auth",
        "/signup",
        "/h2-console",
        "/email",
        "/file"
    );

    public static boolean isNoAuthUri(final String path) {
        return NO_AUTH_API_URIS.stream().anyMatch(path::startsWith);
    }
}
