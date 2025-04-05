package it.compare.backend.scraping.restclient.config;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

public class HttpRequestRetryStrategy extends DefaultHttpRequestRetryStrategy {

    private final int maxRetries;
    private final Set<Integer> retriableCodes;

    public HttpRequestRetryStrategy(
            final int maxRetries, final TimeValue defaultRetryInterval, final Collection<Integer> retriableCodes) {
        super(maxRetries, defaultRetryInterval);

        this.maxRetries = maxRetries;
        this.retriableCodes = new HashSet<>(retriableCodes);
    }

    @Override
    public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
        // Do not retry if over max retries
        if (execCount > this.maxRetries) return false;

        return super.retryRequest(response, execCount, context) || retriableCodes.contains(response.getCode());
    }

    @Override
    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
        // Do not retry if over max retries
        if (execCount > this.maxRetries) return false;

        if (exception instanceof SocketTimeoutException || exception instanceof ConnectException) return true;

        return super.retryRequest(request, exception, execCount, context);
    }
}
