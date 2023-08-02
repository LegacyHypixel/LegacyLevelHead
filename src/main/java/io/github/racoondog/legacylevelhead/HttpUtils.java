package io.github.racoondog.legacylevelhead;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Optional;

import static io.github.racoondog.legacylevelhead.LegacyLevelHead.GSON;

public class HttpUtils {
    public static int postStatus(String url, JsonObject bodyContent) throws IOException {
        return post(url, bodyContent).getStatusLine().getStatusCode();
    }

    public static Optional<JsonObject> postJson(String url, JsonObject bodyContent) throws IOException {
        return readJson(post(url, bodyContent).getEntity().getContent());
    }

    private static HttpResponse post(String url, JsonObject bodyContent) throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(url);

            request.setEntity(new JsonHttpEntity(bodyContent));
            request.addHeader("User-Agent", "Mozilla/4.76 (SK1ER LEVEL HEAD V8.2.2)");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return response;
            }
        }
    }

    public static Optional<JsonObject> getJson(String url) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(url);

            request.addHeader("User-Agent", "Mozilla/4.76 (Essential)");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return readJson(response.getEntity().getContent());
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static Optional<JsonObject> readJson(InputStream stream) {
        try {
            String string = consumeAll(stream);
            return Optional.of(GSON.fromJson(string, JsonObject.class));
        } catch (JsonSyntaxException | IOException e) {
            return Optional.empty();
        }
    }

    private static String consumeAll(InputStream stream) throws IOException {
        try (BufferedInputStream buffered = new BufferedInputStream(stream)) {
            StringBuilder builder = new StringBuilder();
            int data;
            while ((data = buffered.read()) != -1) {
                builder.append((char) data);
            }
            return builder.toString();
        }
    }

    private static class JsonHttpEntity extends StringEntity {
        private static final ContentType CONTENT_TYPE = ContentType.create("application/json", StandardCharsets.UTF_8);

        public JsonHttpEntity(JsonElement element) throws UnsupportedCharsetException {
            super(GSON.toJson(element), CONTENT_TYPE);
        }
    }
}
