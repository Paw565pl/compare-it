package it.compare.backend.scraping.restclient.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

public class HttpRequestRetryStrategy extends DefaultHttpRequestRetryStrategy {
    private final Set<Integer> retriableCodes;

    public HttpRequestRetryStrategy(
            final int maxRetries, final TimeValue defaultRetryInterval, final Collection<Integer> retriableCodes) {
        super(maxRetries, defaultRetryInterval);
        this.retriableCodes = new HashSet<>(retriableCodes);
    }

    @Override
    public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
        return super.retryRequest(response, execCount, context) || retriableCodes.contains(response.getCode());
    }
}
