plugins {
    kotlin("jvm") version "2.2.21"
    id("me.champeau.jmh") version "0.7.2"
}

group = "ru.qixi"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":library"))

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

//    testImplementation("org.openjdk.jmh:jmh-core:1.37")
//    jmh("org.openjdk.jmh:jmh-core:1.37")
//    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

tasks.test {
    useJUnitPlatform()
}

jmh {
    warmupIterations = 3
    iterations = 5
    fork = 1
}