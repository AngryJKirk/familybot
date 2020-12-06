buildscript {
    ext {
        kotlinVersion = "1.4.20"
        springBootVersion = "2.2.6.RELEASE"
        telegramBotVersion = "5.0.1"
        ktlintVersion = "9.4.1"
        coroutinesVersion = "1.4.1"
        testcontainersVersion = "1.13.0"
        guavaVersion = "28.2-jre"
        postgresVersion = "42.2.11"
    }
    repositories {
        mavenCentral()
        maven(
"https://plugins.gradle.org/m2/"
)
    }
    dependencies {
        classpath(group = "org.springframework.boot", name = "spring-boot-gradle-plugin", version = "${springBootVersion}")
        classpath(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version = "${kotlinVersion}")
        classpath(group = "org.jetbrains.kotlin", name = "kotlin-allopen", version = "${kotlinVersion}")
        classpath(group = "org.jlleitschuh.gradle", name = "ktlint-gradle", version = "${ktlintVersion}")
    }
}
repositories {
    mavenCentral()
}
plugins {
    id("kotlin")
    id("kotlin-spring")
    id("org.springframework.boot")
    id("org.jlleitschuh.gradle.ktlint")
}

group = "space.yaroslav"

compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(group = "com.google.guava", name = "guava", version = "${guavaVersion}")
    implementation(group = "org.postgresql", name = "postgresql", version = "${postgresVersion}")
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-jdbc", version = "${springBootVersion}")
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-web", version = "${springBootVersion}")
    implementation(group = "org.telegram", name = "telegrambots", version = "${telegramBotVersion}")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = "${kotlinVersion}")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "${kotlinVersion}")
    testImplementation(group = "org.testcontainers", name = "testcontainers", version = "${testcontainersVersion}")
    testImplementation(group = "org.springframework.boot", name = "spring-boot-starter-test", version = "${springBootVersion}")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "${coroutinesVersion}")
}

bootRun {
    jvmArgs = listOf("-Xmx1024m")
}

