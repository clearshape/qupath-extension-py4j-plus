pluginManagement {
	// to specify where to locate the plugins
	// note: core plugins are built into Gradle already
	repositories {
		// community plugins (com.gradleup.shadow is located here)
		gradlePluginPortal()
		// custom plugins (qupath-extension-settings & qupath-conventions are here)
		maven {
			url = uri("https://maven.scijava.org/content/repositories/releases")
		}
	}
}


// specify which version of QuPath the extension is targeting here
// 1. the new extension defined by plugin 'qupath-extension-settings'
qupath {
	version = "0.6.0-SNAPSHOT"
	catalogName = "libs"  // optional ("libs" is the default)
}


// Apply QuPath Gradle settings plugin to handle configuration
// 1. the settings plugin QuPath created to simplify the project settings
plugins {
	id("io.github.qupath.qupath-extension-settings") version "0.2.1"
}


// Include qupath-extension-py4j as a subproject.
include(":qupath-extension-py4j")
project(":qupath-extension-py4j").projectDir = file("/Users/yaoting/Downloads/qupath-extension-py4j")


