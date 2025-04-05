package it.compare.backend.scraping.config;

import java.util.Map;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SeleniumConfig {

    @Bean
    @Scope("prototype")
    public WebDriver webDriver() {
        var options = new ChromeOptions();

        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-images");
        options.addArguments("--blink-settings=imagesEnabled=false");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--log-level=3");

        var userAgent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36";
        options.addArguments("--user-agent=" + userAgent);

        var prefs = Map.of("profile.managed_default_content_settings.stylesheets", 2);
        options.setExperimentalOption("prefs", prefs);
        options.setExperimentalOption("excludeSwitches", new String[] {"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        options.addArguments("--lang=pl-PL");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--allow-running-insecure-content");

        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        return new ChromeDriver(options);
    }
}
