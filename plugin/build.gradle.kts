plugins {
    `java-library`
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        //options.release.set(17)
        options.compilerArgs.add("-parameters")
    }

    javadoc { options.encoding = Charsets.UTF_8.name() }

    processResources {
        filteringCharset = Charsets.UTF_8.name()

        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
