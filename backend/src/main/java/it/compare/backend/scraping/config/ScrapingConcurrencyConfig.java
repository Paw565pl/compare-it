package it.compare.backend.scraping.config;

import it.compare.backend.scraping.properties.ScrapingProperties;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ScrapingConcurrencyConfig {

    private final ScrapingProperties scrapingProperties;

    @Bean("rtvEuroAgdSemaphore")
    public Semaphore rtvEuroAgdSemaphore() {
        return new Semaphore(scrapingProperties.getMaxRtvEuroAgdThreads(), true);
    }

    @Bean("moreleNetSemaphore")
    public Semaphore moreleNetSemaphore() {
        return new Semaphore(scrapingProperties.getMaxMoreleNetThreads(), true);
    }

    @Bean("mediaExpertSemaphore")
    public Semaphore mediaExpertSemaphore() {
        return new Semaphore(scrapingProperties.getMaxMediaExpertThreads(), true);
    }
}
