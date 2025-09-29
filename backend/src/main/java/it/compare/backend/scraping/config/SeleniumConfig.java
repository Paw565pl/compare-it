package it.compare.backend.scraping.config;

import java.io.File;
import java.util.List;
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
    @SuppressWarnings("java:S1075")
    public WebDriver webDriver() {
        var options = new ChromeOptions();

        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-images");
        options.addArguments("--blink-settings=imagesEnabled=false");
        options.addArguments("--disable-audio-output");
        options.addArguments("--disable-webrtc");
        options.addArguments("--disable-sync");
        options.addArguments("--disable-default-apps");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--log-level=3");
        options.addArguments(
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");

        var ublockOriginLiteCrx = new File("/app/ublockOriginLite.crx");
        if (ublockOriginLiteCrx.exists() && ublockOriginLiteCrx.isFile()) options.addExtensions(ublockOriginLiteCrx);

        var prefs = Map.of("profile.managed_default_content_settings.stylesheets", 2);
        options.setExperimentalOption("prefs", prefs);
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        options.addArguments("--lang=pl-PL");
        options.addArguments("--window-size=1440,900");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--allow-running-insecure-content");

        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        return new ChromeDriver(options);
    }
}
