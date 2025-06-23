pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.acrobits.net/repository/maven-releases/")
            credentials {
                username = "net.acrobits.interview.test"
                password = "r1sl6vl1gms0v014t922vuc0r2"
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.acrobits.net/repository/maven-releases/")
            credentials {
                username = "net.acrobits.interview.test"
                password = "r1sl6vl1gms0v014t922vuc0r2"
            }
        }
    }
}

rootProject.name = "AcrobitsCaller"
include(":app")
 