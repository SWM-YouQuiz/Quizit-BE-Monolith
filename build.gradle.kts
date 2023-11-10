import com.epages.restdocs.apispec.gradle.OpenApi3Task

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    id("com.epages.restdocs-api-spec") version "0.17.1"
    id("jacoco")
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
}

group = "com.quizit"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("io.github.microutils:kotlin-logging:3.0.5")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("commons-io:commons-io:2.14.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("com.epages:restdocs-api-spec-webtestclient:0.17.1")
    testImplementation("com.epages:restdocs-api-spec:0.17.1")
    testImplementation("it.ozimov:embedded-redis:0.7.2")
}

tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "17"
        }
    }

    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport, withType<OpenApi3Task>())
    }

    bootJar {
        archiveFileName.set("quizit.jar")
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule {
                element = "CLASS"

                limit {
                    counter = "LINE"
                    value = "COVEREDRATIO"
                    minimum = BigDecimal("0.8")
                }

                limit {
                    counter = "METHOD"
                    value = "COVEREDRATIO"
                    minimum = BigDecimal("0.8")
                }

                excludes = listOf(
                    "**.*Application*",
                    "**.*Configuration*",
                    "**.*Exception*",
                    "**.*Util*",
                    "**.*Aspect*",
                    "**.*Request*",
                    "**.*Response*",
                    "**.*Cache*",
                    "**.*Client*",
                    "**.*OAuth*",
                    "**.*Jwt*",
                    "**.*Mdc*"
                )
            }
        }
    }

    jacocoTestReport {
        reports {
            html.required.set(true)
            xml.required.set(true)
            finalizedBy(jacocoTestCoverageVerification)
        }
    }
}

jacoco {
    toolVersion = "0.8.8"
}

openapi3 {
    setServer("Quizit")
    title = "Quizit API"
    version = "v1"
    format = "yml"
    outputFileNamePrefix = "api"
    outputDirectory = "src/main/resources/static/docs"
}