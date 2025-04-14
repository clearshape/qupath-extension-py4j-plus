plugins {
	// QuPath Gradle extension convention plugin
	// 1. configured by qupathExtension { ... }
	id("qupath-conventions")

	// a core Gradle plugin to publish this extension
	// 1. configured by publishing { ... }
	`maven-publish`

	// optional - create a shadow/fat jar that bundle up any non-core dependencies
	// 1. shadow(...)         - for the dependencies we do not want to bundle
	// 2. implementation(...) - for the dependencies we want to bundle
	id("com.gradleup.shadow") version "8.3.5"

	// optional - writing the extension in Groovy
	// 1. for example, we might want to write QuPathEntryPoint2 in Groovy
	groovy
}

// specify the details of the extension here
// 1. published - to repository 'snapshotsRepoUrl' defined in repositories { ... }
// 2. group - io.github.qupath
// 3. name - qupath-extension-py4j-plus
// 4. version - 0.1.0-SNAPSHOT
qupathExtension {
	name = "qupath-extension-py4j"
	version = "0.1.0-rc1"
	group = "io.github.qupath"
	description = "Connect QuPath to Python using Py4J"
	automaticModule = "qupath.extension.py4j"
}

// cross-reference the external QuPath Javadoc
tasks.javadoc {
	(options as StandardJavadocDocletOptions).apply {
		encoding = "UTF-8"
		charSet = "UTF-8"

		// Cross-reference the external Javadoc for QuPath
		links("https://qupath.github.io/javadoc/docs/")
	}
}

dependencies {
	// the dependencies we do not want to bundle
	// 1. they are already part of QuPath
	// 2. catalog 'libs' is defined by plugin 'qupath-extension-settings'
	// 3. BioFormats & Openslide are not part of core QuPath (libs.bundles.qupath)
	shadow(libs.bundles.qupath)
	shadow(libs.bundles.logging)
	shadow(libs.qupath.fxtras)
	shadow(libs.ikonli.javafx)
	shadow(libs.guava)
	shadow(libs.qupath.ext.openslide)
	shadow(libs.qupath.ext.bioformats)

	// the dependencies we want to bundle
	// 1. "py4j" is optional and kept here for convenience
	//    a. will have all needed dependencies
	//    b. when "./gradlew copyDependencies" is executed
	// 2. "qupath-extension-py4j" is mandatory
	implementation("net.sf.py4j:py4j:0.10.9.7")

	// the dependencies required for testing
	testImplementation(libs.bundles.qupath)
	testImplementation(libs.junit)
}

// to configure plugin 'maven-publish'
publishing {
	// specify where the repositories are located
	// 1. use 'releases' if the module can be found there
	// 2. switch to 'snapshots' if not
	repositories {
		maven {
			name = "SciJava"
			val releasesRepoUrl = uri("https://maven.scijava.org/content/repositories/releases")
			val snapshotsRepoUrl = uri("https://maven.scijava.org/content/repositories/snapshots")
			// Use gradle -Prelease publish
			url = if (project.hasProperty("release")) releasesRepoUrl else snapshotsRepoUrl
			credentials {
				username = System.getenv("MAVEN_USER")
				password = System.getenv("MAVEN_PASS")
			}
		}
	}

	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			pom {
				licenses {
					license {
						name = "Apache License v2.0"
						url = "https://www.apache.org/licenses/LICENSE-2.0"
					}
				}
			}
		}
	}
}
