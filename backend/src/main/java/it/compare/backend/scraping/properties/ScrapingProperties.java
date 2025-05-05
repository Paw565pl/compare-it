package it.compare.backend.scraping.properties;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ToString
@Validated
@ConfigurationProperties("scraping")
public class ScrapingProperties {

    private @Positive(message = "maxRtvEuroAgdThreads must be positive") Integer maxRtvEuroAgdThreads = 8;
    private @Positive(message = "maxMoreleNetThreads must be positive") Integer maxMoreleNetThreads = 8;
    private @Positive(message = "maxMediaExpertThreads must be positive") Integer maxMediaExpertThreads = 4;
}
