package friendy.community.global.config;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityPathConfig {

    public static final List<String> PUBLIC_START_URIS = List.of(
        "/swagger-ui",
        "/v3/api-docs",
        "/auth",
        "/signup",
        "/h2-console",
        "/email",
        "/file"
    );

    public static final List<String> PUBLIC_URIS = List.of(
        "/follow/following",
        "/follow/follower"
    );

    public static final List<String> PUBLIC_GET_API_URIS = List.of(
        "/posts/"
    );

    public static boolean isPublicApiUriWithMethod(final String path, final String method) {
        if ("GET".equalsIgnoreCase(method)) {
            return PUBLIC_GET_API_URIS.stream().anyMatch(path::startsWith);
        }
        return false;
    }

    public static boolean isPublicUri(final String path) {
        return PUBLIC_URIS.contains(path) ||
            PUBLIC_START_URIS.stream().anyMatch(path::startsWith);
    }
}
