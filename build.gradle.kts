import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.api.tasks.SourceSetContainer


plugins {
	id("com.gradleup.shadow") version "8.3.5"
	id("qupath-conventions")
	`maven-publish`
}


qupathExtension {
	name = "qupath-extension-py4j-plus"
	version = "0.1.0-rc1"
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
	shadow(libs.qupath.ext.openslide)
	shadow(libs.qupath.ext.bioformats)

	implementation("net.sf.py4j:py4j:0.10.9.7")
	implementation(project(":qupath-extension-py4j"))

	// For testing
	testImplementation(libs.bundles.qupath)
	testImplementation(libs.junit)
}


publishing {
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


// cross-reference the external Javadoc
tasks.javadoc {
	(options as StandardJavadocDocletOptions).apply {
		encoding = "UTF-8"
		charSet = "UTF-8"

		// Cross-reference QuPath, Py4J & JavaFX API docs
		links("https://qupath.github.io/javadoc/docs/")
		links("https://javadoc.io/doc/net.sf.py4j/py4j/0.10.9.7")
		links("https://openjfx.io/javadoc/21")
	}
}


tasks.register<Javadoc>("mergeJavadoc") {
	group = "documentation"
	description = "Generates unified Javadoc for the main project and the qupath-extension-py4j subproject."

	// Retrieve the main project's source set
	val mainSourceSet = (project.extensions.getByName("sourceSets") as SourceSetContainer)["main"]
	// Retrieve the subproject's source set
	val subSourceSet = (project(":qupath-extension-py4j").extensions.getByName("sourceSets") as SourceSetContainer)["main"]

	// Combine all Java sources from the main project and the subproject
	source = files(mainSourceSet.allJava.srcDirs, subSourceSet.allJava.srcDirs).asFileTree

	// Combine the classpaths for compilation from both projects
	classpath = files(mainSourceSet.compileClasspath, subSourceSet.compileClasspath)

	// cross-reference the external Javadoc
	(options as StandardJavadocDocletOptions).apply {
		encoding = "UTF-8"
		charSet = "UTF-8"

		// Cross-reference QuPath & Py4J API docs
		links("https://qupath.github.io/javadoc/docs/")
		links("https://javadoc.io/doc/net.sf.py4j/py4j/0.10.9.7")
		links("https://openjfx.io/javadoc/21")
	}

	// Ensure that the individual javadoc tasks are executed first
	dependsOn(tasks.named("javadoc"))
	dependsOn(project(":qupath-extension-py4j").tasks.named("javadoc"))
}
