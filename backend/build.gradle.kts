import com.diffplug.spotless.LineEnding

plugins {
    java
    idea
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.0.3"
    id("se.solrike.sonarlint") version "2.2.0"
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

val springdocVersion = "2.8.8"
val modelMapperVersion = "3.2.3"
val modelMapperRecordVersion = "1.0.0"
val apacheHttpClientVersion = "5.5"
val apacheHttpCoreVersion = "5.3.4"
val randomUserAgentGeneratorVersion = "1.3"
val jsoupVersion = "1.20.1"
val seleniumVersion = "4.33.0"
val dataFakerVersion = "2.4.3"
val junitJupiterParamsVersion = "5.12.2"

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
    implementation("org.modelmapper:modelmapper:${modelMapperVersion}")
    implementation("org.modelmapper:modelmapper-module-record:${modelMapperRecordVersion}")
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
