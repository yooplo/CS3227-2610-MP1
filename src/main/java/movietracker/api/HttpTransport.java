package movietracker.api;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.Objects;

/**
 * Injectable HTTP boundary used by the TMDB client.
 */
@FunctionalInterface
public interface HttpTransport {

    HttpResult send(HttpRequest request) throws IOException, InterruptedException;

    /**
     * HTTP response data needed by the TMDB client.
     */
    record HttpResult(int statusCode, String body) {

        public HttpResult {
            Objects.requireNonNull(body, "body");
        }
    }
}
