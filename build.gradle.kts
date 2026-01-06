plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.24")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.yaml:snakeyaml:2.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("bot.Main")
}
