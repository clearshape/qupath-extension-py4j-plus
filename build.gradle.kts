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


// configure task 'javadoc'
tasks.javadoc {
	// generate Javadoc based on the source codes of both projects
	source = sourceSets["main"].allJava +
			project(":qupath-extension-py4j").sourceSets["main"].allJava
	// needed to access the dependencies of both projects to resolve all referenced types
	classpath = sourceSets["main"].compileClasspath +
			project(":qupath-extension-py4j").sourceSets["main"].compileClasspath
	// cross-reference the external document APIs
	(options as StandardJavadocDocletOptions).apply {
		links("https://qupath.github.io/javadoc/docs/")
		links("https://javadoc.io/doc/net.sf.py4j/py4j/0.10.9.7")
		links("https://openjfx.io/javadoc/21")
	}
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


