import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-library`
    signing
    id("com.vanniktech.maven.publish") version "0.30.0"
}

repositories {
    mavenCentral()
}

configure<SigningExtension> {
    useInMemoryPgpKeys(
        project.property("signingInMemoryKeyId").toString(),
        project.property("signingInMemoryKey").toString(),
        project.property("signingInMemoryKeyPassword").toString()
    )
}

mavenPublishing {
    coordinates("io.github.livenne", "aero", "1.0.3")
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name = "Aero"
        description = "A lightweight web framework"
        url = "https://github.com/Livenne/Aero"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "Livenne"
                name = "livenne"
                email = "livennea@gmail.com"
            }
        }
        scm {
            connection = "scm:git:git://github.com/Livenne/Aero.git"
            developerConnection = "scm:git:ssh://github.com/Livenne/Aero.git"
            url = "https://github.com/Livenne/Aero"
        }
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    api(platform(project(":aero-bom")))
    api("jakarta.servlet:jakarta.servlet-api")
    api("org.apache.tomcat.embed:tomcat-embed-core")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("ch.qos.logback:logback-classic")
    api("com.mysql:mysql-connector-j")
    api("com.auth0:java-jwt")
    api("org.reflections:reflections")
    api("org.slf4j:jul-to-slf4j")
}

tasks.test {
    useJUnitPlatform()
}