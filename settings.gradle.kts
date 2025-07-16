pluginManagement {
	repositories {
		gradlePluginPortal()
		maven { url = uri("https://maven.fabricmc.net/") }
		maven { url = uri("https://jitpack.io") }
		maven { url = uri("https://maven.glass-launcher.net/babric") }
		maven { url = uri("https://maven.thesignalumproject.net/infrastructure") }
	}
	plugins {
		id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
		id("fabric-loom") version "1.10.0-bta"
		id("com.modrinth.minotaur") version "2.+"
		id("org.jetbrains.kotlin.jvm") version "2.1.0"
	}
}
rootProject.name = "bta-poverh"

include("momentum")
include("parcool")
