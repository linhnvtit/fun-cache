plugins {
    id(libs.plugins.kotlin.kapt.get().pluginId)
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.maven.publish.get().pluginId)
}

fun Any?.getInt() = this?.toString()?.toIntOrNull()
fun Any?.getString() = this?.toString()

dependencies {
    compileOnly(libs.kotlin.compiler)
    compileOnly(libs.google.autoservice)
    kapt(libs.google.autoservice.annotation)
    implementation(libs.kotlin.reflect)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = properties["groupId"].getString()
            artifactId = properties["artifactId"].getString()
            version = properties["version"].getString()
        }
    }

    repositories {
        maven {
            url = uri("https://jitpack.io")
        }
    }
}