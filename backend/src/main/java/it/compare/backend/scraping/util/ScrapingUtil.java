package it.compare.backend.scraping.util;

import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ScrapingUtil {

    private ScrapingUtil() {
        throw new IllegalStateException("Attempted to instantiate utility class");
    }

    public static void sleep() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(300, 2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        }
    }
}
