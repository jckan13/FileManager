plugins {
    id("java")
    //application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.53.1.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.+")
    testImplementation("org.mockito:mockito-junit-jupiter:5.+")
}

tasks.test {
    useJUnitPlatform()
}
//points Gradle to the TopSecret class that runs the entire project
/*application {
    mainClass.set("TopSecret")
}*/
tasks.jar {
    archiveBaseName = "TopSecret"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes("Main-Class" to "controller.TopSecret")
    }
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory()) it else zipTree(it) }
    })
}
tasks.register<JavaExec>("run") {
    group = "Application"
    description = "Runs the main class."
    mainClass.set("controller.TopSecret")
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
}