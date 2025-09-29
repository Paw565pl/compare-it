package it.compare.backend;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import generator.RandomUserAgentGenerator;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.scraping.service.ScrapingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableMongoAuditing
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner commandLineRunner(ProductRepository productRepository, ScrapingService scrapingService) {
        return args -> {
            // initialize random user agent generator
            RandomUserAgentGenerator.getNext();

            if (productRepository.count() == 0) scrapingService.scrapeAll();
        };
    }
}
