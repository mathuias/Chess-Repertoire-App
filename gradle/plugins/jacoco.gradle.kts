import org.gradle.testing.jacoco.tasks.JacocoReport

// jacoco plugin configuration
configure<JacocoPluginExtension> {
    toolVersion = "0.8.8"
}

tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isEnabled = true
    }
}


