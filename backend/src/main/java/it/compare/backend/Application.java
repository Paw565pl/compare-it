package it.compare.backend;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import it.compare.backend.scraping.rtveuroagd.scraper.RtvEuroAgdScraper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableMongoAuditing
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class Application {
//        @Bean
//        public CommandLineRunner commandLineRunner(
//                RtvEuroAgdScraper rtvEuroAgdScraper) {
//            return args -> {
//                rtvEuroAgdScraper.scrape();
//            };
//        }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
