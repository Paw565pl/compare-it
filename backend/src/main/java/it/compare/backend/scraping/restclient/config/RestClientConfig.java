package it.compare.backend.scraping.restclient.config;

import static org.springframework.web.client.RestClient.Builder;

import java.time.Duration;
import java.util.List;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.LaxRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(
            Builder restClientBuilder,
            BasicCookieStore cookieStore,
            LaxRedirectStrategy redirectStrategy,
            HttpRequestRetryStrategy httpRequestRetryStrategy) {
        var httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setRedirectStrategy(redirectStrategy)
                .setRetryStrategy(httpRequestRetryStrategy)
                .build();

        var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(Duration.ofSeconds(20));
        requestFactory.setConnectionRequestTimeout(Duration.ofSeconds(20));

        return restClientBuilder
                .requestFactory(requestFactory)
                .defaultHeaders((httpHeaders -> {
                    httpHeaders.set("Accept", "*/*");
                    httpHeaders.set("Accept-Encoding", "gzip, deflate, br, zstd");
                    httpHeaders.set("Accept-Language", "pl,en-US;q=0.7,en;q=0.3");
                    httpHeaders.set("Cache-Control", "no-cache");
                    httpHeaders.set("Connection", "keep-alive");
                    httpHeaders.set(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
                }))
                .build();
    }

    @Bean
    public BasicCookieStore cookieStore() {
        return new BasicCookieStore();
    }

    @Bean
    public LaxRedirectStrategy redirectStrategy() {
        return new LaxRedirectStrategy();
    }

    @Bean
    public HttpRequestRetryStrategy httpRequestRetryStrategy() {
        return new HttpRequestRetryStrategy(3, TimeValue.ofSeconds(2), List.of(HttpStatus.FORBIDDEN.value()));
    }
}
