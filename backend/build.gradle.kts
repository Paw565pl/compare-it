import com.diffplug.spotless.LineEnding

plugins {
    java
    idea
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.diffplug.spotless") version "6.25.0"
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
        indentWithSpaces()
        formatAnnotations()
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

val springdocVersion = "2.7.0"
val modelMapperVersion = "3.2.1"
val modelMapperRecordVersion = "1.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springdocVersion}")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.modelmapper:modelmapper:${modelMapperVersion}")
    implementation("org.modelmapper:modelmapper-module-record:${modelMapperRecordVersion}")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
