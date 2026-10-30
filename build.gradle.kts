plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.24"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.magmaguy.com/releases")
    flatDir {
        dir("libs")
    }
}

dependencies {
    paperweight.paperDevBundle("26.3.build.+")
    compileOnly("com.MT:Beasts:0.1")
    compileOnly("com.magmaguy:FreeMinecraftModels:2.14.0")
    compileOnly("io.github.darksoulq:AbyssalLib:2.5.0-mc.26.3-alpha.8")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        minecraftVersion("26.3")
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")

        downloadPlugins {
            modrinth("free-minecraft-models", "2.14.0")
            modrinth("resourcepackmanager", "2.4.9")
            modrinth("abyssallib", "2.5.0-mc.26.3-alpha.8")
        }
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
