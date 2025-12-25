plugins {
    id("java-library")
    id("maven-publish")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "io.github.livenne"
            artifactId = "aero"
            version = "1.0.0"
            pom {
                name = "Aero"
                description = "A lightweight Java web framework"
                url = "https://github.com/livenne/aero"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "livenne"
                        name = "Livenne"
                        email = "livennea@gmail.com"
                    }
                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

repositories {
    mavenCentral()
}

group = "io.github.livenne"
version = "1.0.0"

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    api("jakarta.servlet:jakarta.servlet-api:6.1.0")
    api("org.apache.tomcat.embed:tomcat-embed-core:11.0.12")
    api("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    api("ch.qos.logback:logback-classic:1.5.19")
    api("com.mysql:mysql-connector-j:9.3.0")
    api("com.auth0:java-jwt:3.8.1")
    api("org.reflections:reflections:0.10.2")
    implementation("org.slf4j:jul-to-slf4j:2.0.9")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks.test {
    useJUnitPlatform()
}
