package org.ivy.settlement.infrastructure.http;

import org.ivy.settlement.infrastructure.async.ThanosThreadFactory;
import org.ivy.settlement.infrastructure.exception.HttpResourceNotFundException;
import org.ivy.settlement.infrastructure.string.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * description:
 * @author carrot
 */
public class HttpClientUtil {


    final static HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(20))
            .executor(new ThreadPoolExecutor(8, 8,
                    0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(1024),
                    new ThanosThreadFactory("http_client_util")))
            .build();

    public static String doGet(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(120))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                throw new HttpResourceNotFundException(StringUtils.format("request for {}, response for resource not found!", url));
            }

            return response.body();
        } catch (HttpResourceNotFundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String doPost(String url, String param) throws Exception {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json;charset=utf8")
                    .POST(HttpRequest.BodyPublishers.ofString(param))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // todo:: process timeout or 404
            if (response.statusCode() == 404) {
                throw new HttpResourceNotFundException(StringUtils.format("request for {}, response for resource not found!", url));
            }

            return response.body();
        } catch (HttpResourceNotFundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
