import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
}

val localTestFrameworkPath = providers.gradleProperty("localTestFrameworkPath")

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    testImplementation(libs.junit)
    testRuntimeOnly("org.opentest4j:opentest4j:1.3.0")
    if (localTestFrameworkPath.isPresent) {
        testImplementation(files(localTestFrameworkPath.get()))
    }

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        val localIdePath = providers.gradleProperty("localIdePath")
        if (localIdePath.isPresent) {
            local(localIdePath.get())
        } else {
            intellijIdea("2026.1.3")
        }
        if (!localTestFrameworkPath.isPresent) {
            testFramework(TestFrameworkType.Platform)
        }

        bundledModule("com.intellij.modules.xml")
        bundledPlugin("com.intellij.java")
        if (providers.gradleProperty("testWebLanguages").orNull == "true") {
            testBundledPlugins("JavaScript", "com.intellij.css")
        }

        // Add plugin dependencies for compilation here, for example:
        // bundledPlugin("com.intellij.java")
    }
}

intellijPlatform {
    instrumentCode = providers.gradleProperty("instrumentCode").map { it.toBoolean() }.orElse(true)
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "261"
        }
    }
}

tasks.test {
    systemProperty("jsp.test.web.languages", providers.gradleProperty("testWebLanguages").orElse("false").get())
}
