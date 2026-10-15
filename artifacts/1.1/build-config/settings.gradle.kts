pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal {
            content {
                includeGroup("com.alexcawl.contract.playground")
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "contract-plugin-playground"

include(":component", ":styling", ":app")
