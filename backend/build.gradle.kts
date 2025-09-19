import com.diffplug.spotless.LineEnding

plugins {
    java
    idea
    id("org.springframework.boot") version "3.5.6"
    id("org.springframework.boot.aot") version "3.5.6" apply false
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.2.1"
    id("se.solrike.sonarlint") version "2.2.0"
}

if (gradle.startParameter.taskNames.any { it.contains("bootJar") || it.contains("build") }) {
    apply(plugin = "org.springframework.boot.aot")
}

group = "it.compare"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        cleanthat()
        lineEndings = LineEnding.UNIX
        palantirJavaFormat()
        trimTrailingWhitespace()
        endWithNewline()
        leadingTabsToSpaces()
        formatAnnotations()
    }
}

sonarlint {
    reports {
        create("sarif") {
            enabled.set(true)
        }
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

val springdocVersion = "2.8.13"
val mapstructVersion = "1.6.3"
val apacheHttpClientVersion = "5.5"
val apacheHttpCoreVersion = "5.3.5"
val randomUserAgentGeneratorVersion = "1.3"
val jsoupVersion = "1.21.2"
val seleniumVersion = "4.35.0"
val dataFakerVersion = "2.5.0"
val junitJupiterParamsVersion = "5.13.4"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springdocVersion}")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    implementation("org.apache.httpcomponents.client5:httpclient5:${apacheHttpClientVersion}")
    implementation("org.apache.httpcomponents.core5:httpcore5:${apacheHttpCoreVersion}")
    implementation("org.apache.httpcomponents.core5:httpcore5-h2:${apacheHttpCoreVersion}")
    implementation("com.sezinkarli:random-user-agent-generator:${randomUserAgentGeneratorVersion}")
    implementation("org.jsoup:jsoup:${jsoupVersion}")
    implementation("org.seleniumhq.selenium:selenium-java:${seleniumVersion}")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("net.datafaker:datafaker:${dataFakerVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${junitJupiterParamsVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    sonarlintPlugins("org.sonarsource.java:sonar-java-plugin:7.30.1.34514")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
