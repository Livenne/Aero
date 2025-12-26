import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-platform`
    signing
    id("com.vanniktech.maven.publish") version "0.30.0"
}


javaPlatform {
    allowDependencies()
}

configure<SigningExtension> {
    useInMemoryPgpKeys(
        project.property("signingInMemoryKeyId").toString(),
        project.property("signingInMemoryKey").toString(),
        project.property("signingInMemoryKeyPassword").toString()
    )
}

mavenPublishing {
    coordinates("io.github.livenne", "aero-bom", "1.0.4")
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

repositories {
    mavenCentral()
}

dependencies {
    constraints {
        api("jakarta.servlet:jakarta.servlet-api:6.1.0")
        api("org.apache.tomcat.embed:tomcat-embed-core:11.0.12")
        api("com.fasterxml.jackson.core:jackson-databind:2.15.2")
        api("ch.qos.logback:logback-classic:1.5.19")
        api("com.mysql:mysql-connector-j:9.3.0")
        api("com.auth0:java-jwt:3.8.1")
        api("org.reflections:reflections:0.10.2")
        api("org.slf4j:jul-to-slf4j:2.0.9")
    }
}
