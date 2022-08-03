plugins {
    `java-library`
}

allprojects  {
    group = "${project.group}"
    version = "${project.version}"

    repositories {
        mavenCentral()
        maven {
            name = "papermc-repo"
            url = uri("https://papermc.io/repo/repository/maven-public/")
        }
        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/content/groups/public/")
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}