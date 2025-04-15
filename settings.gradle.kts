pluginManagement {
	repositories {				// specify the plugin repositories
		gradlePluginPortal()	// community plugins (eg. com.gradleup.shadow)
		maven {					// custom plugins (eg. qupath-extension-settings & qupath-conventions)
			url = uri("https://maven.scijava.org/content/repositories/releases")
		}
	}
}

// the version of QuPath this extension is targeting
qupath {
	version = "0.6.0-SNAPSHOT"
}

// Apply QuPath Gradle settings plugin to handle configuration
plugins {
	id("io.github.qupath.qupath-extension-settings") version "0.2.1"
}

// Include qupath-extension-py4j as a subproject.
include(":qupath-extension-py4j")
project(":qupath-extension-py4j").projectDir = file("/Users/yaoting/Downloads/qupath-extension-py4j")
