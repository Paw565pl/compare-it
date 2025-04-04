package it.compare.backend.scraping.util;

import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ScrapingUtil {

    private ScrapingUtil() {}

    public static void sleep() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(200, 2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        }
    }
}
