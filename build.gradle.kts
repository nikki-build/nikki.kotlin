import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.6.10"
    kotlin("kapt") version "1.6.10"
    `maven-publish`
}

group = "com.nikkibuild.websocket"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.mockito:mockito-inline:4.6.1")
    testImplementation("org.mockito:mockito-junit-jupiter:4.6.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("org.assertj:assertj-core:3.23.1")

    val okV = "4.10.0"
    implementation("com.squareup.okhttp3:logging-interceptor:$okV")
    implementation("com.squareup.okhttp3:okhttp:$okV")
    implementation("com.squareup.okhttp:okhttp-ws:2.7.5")

    implementation("io.reactivex.rxjava3:rxkotlin:3.0.1")

    implementation("com.google.code.gson:gson:2.9.0")
    implementation("commons-codec:commons-codec:1.15")

    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")


}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

application {
    mainClass.set("com.nikkibuild.websocket.app.Main")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    test {
        useJUnitPlatform()
        reports {
            junitXml.required.set(false)
            html.required.set(true)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.nikkibuild.websocket"
            artifactId = "kotlin"
            version = "1.0"
            from(components["java"])
            pom {
                name.set("Nikki Kotlin")
                description.set("A websocket service from nikkibuild")
                url.set("https://github.com/nikki-build/nikki.kotlin")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("nikkibuild")
                        name.set("NikkiBuild")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/nikki-build/nikki.kotlin.git")
                    developerConnection.set("scm:git:ssh://git@github.com:nikki-build/nikki.kotlin.git")
                    url.set("https://github.com/nikki-build/nikki.kotlin")
                }
            }
        }
    }
}

tasks {
    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(
            listOf(
                "compileJava",
                "compileKotlin",
                "processResources"
            )
        )
        // We need this for Gradle optimization to work
        archiveClassifier.set("standalone") // Naming the jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
}