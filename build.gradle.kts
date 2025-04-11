plugins {
	id("qupath-conventions")
	id("com.gradleup.shadow") version "8.3.5"
	`maven-publish`
}

// specify the details of the extension here
qupathExtension {
	name = "qupath-extension-py4j"
	version = "0.1.0-SNAPSHOT"
	group = "io.github.qupath"
	description = "Connect QuPath to Python using Py4J"
	automaticModule = "qupath.extension.py4j"
}

dependencies {

	shadow(libs.bundles.qupath)
	shadow(libs.bundles.logging)
	shadow(libs.qupath.fxtras)
	shadow(libs.ikonli.javafx)
	shadow(libs.guava)
	shadow("io.github.qupath:qupath-extension-bioformats:0.6.0-SNAPSHOT")
	shadow("io.github.qupath:qupath-extension-openslide:0.6.0-SNAPSHOT")

	implementation("net.sf.py4j:py4j:0.10.9.7")
//	implementation("io.github.qupath:qupath-extension-formats:0.6.0-SNAPSHOT")

	// For testing
	testImplementation(libs.bundles.qupath)
	testImplementation(libs.junit)

}

publishing {
	// specify where the repositories are located
	// 1. ues 'releases' if the module can be found there
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
