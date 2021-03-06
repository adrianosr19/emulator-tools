import com.google.protobuf.gradle.*
import org.gradle.kotlin.dsl.provider.gradleKotlinDslOf

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    application
    id("com.github.ben-manes.versions") version "0.28.0"
    id("net.nemerosa.versioning") version "2.8.2"
    id("com.google.protobuf") version "0.8.12"
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.maven.apache.org/maven2")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
}

group = "com.github.yschimke"
version = versioning.info.display

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.3"
    kotlinOptions.languageVersion = "1.3"
}

application {
    mainClassName = "ee.schimke.emulatortools.MainKt"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.grpc:grpc-kotlin-stub:0.1.1")

    implementation("info.picocli:picocli:4.2.0")
    implementation("com.github.yschimke:oksocial-output:5.1")
    implementation("com.squareup.okio:okio:2.5.0")
    implementation("javax.annotation:javax.annotation-api:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.8.11")
    implementation("com.google.protobuf:protobuf-java:3.11.1")
    implementation("com.google.protobuf:protobuf-java-util:3.11.1")
    implementation("io.grpc:grpc-netty-shaded:1.26.0")
    implementation("io.grpc:grpc-protobuf:1.26.0")
    implementation("io.grpc:grpc-stub:1.26.0")
    implementation("org.slf4j:slf4j-jdk14:2.0.0-alpha0")
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.11.4" }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.28.1"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:0.1.1"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.generateDescriptorSet = true
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/java")
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/grpckt")
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven(url = "build/repository")
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}

// https://github.com/square/okio/issues/647
configurations.all {
    if (name.contains("kapt") || name.contains("proto", ignoreCase = true)) {
        attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
    }
}
