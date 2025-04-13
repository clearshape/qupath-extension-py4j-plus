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
// 2. located   - in folder 'io/github/qupath/qupath-extension-py4j' of 'snapshotsRepoUrl'
// 3. *.jar     - in folder '0.1.0-SNAPSHOT' of 'io/github/qupath/qupath-extension-py4j' 
qupathExtension {
	name = "qupath-extension-py4j-plus"
	version = "0.1.0-SNAPSHOT"
	group = "io.github.qupath"
	description = "Connect QuPath to Python using Py4J"
	automaticModule = "qupath.extension.py4j"
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
	// 1. the extension needs Py4J Java module to work
	// 2. it is at https://mvnrepository.com/artifact/net.sf.py4j/py4j/0.10.9.7
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
