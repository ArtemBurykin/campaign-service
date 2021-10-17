import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin ("jvm") version "1.3.71"
    application
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.google.cloud.tools.jib") version "2.2.0"
}

group = "ru.avesystems.maise"
version = "0.1"

repositories {
    mavenCentral()
    jcenter()
}

val kotlinVersion = "1.3.72"
val vertxVersion = "3.9.1"
val junitJupiterVersion = "5.6.0"

val mainVerticleName = "$group.campaign.MainVerticle"
val watchForChange = "src/**/*"
val doOnChange = "./gradlew classes"
val launcherClassName = "io.vertx.core.Launcher"

application {
    mainClassName = launcherClassName
}

dependencies {
    implementation("io.vertx:vertx-rx-java2:$vertxVersion")
    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-web-client:$vertxVersion")
    implementation("io.vertx:vertx-pg-client:$vertxVersion")
    implementation("io.vertx:vertx-config:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("com.rabbitmq:amqp-client:5.9.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("junit:junit:4.12")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("io.vertx:vertx-unit:$vertxVersion")
    testImplementation("io.rest-assured:rest-assured:4.3.0")
    testImplementation("io.rest-assured:rest-assured-all:4.3.0")
    testImplementation("io.rest-assured:kotlin-extensions:4.3.0")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("org.testcontainers:testcontainers:1.15.2")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "11"

tasks.withType<ShadowJar> {
    archiveClassifier.set("fat")
    manifest {
        attributes(mapOf("Main-Verticle" to mainVerticleName))
    }
    mergeServiceFiles {
        include("META-INF/services/io.vertx.core.spi.VerticleFactory")
    }
}

tasks.withType<Test> {
    testLogging {
        events = setOf(PASSED, SKIPPED, FAILED)
    }
}

tasks.withType<JavaExec> {
    args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
