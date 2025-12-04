import org.gradle.api.JavaVersion

val javaVersion = JavaVersion.current()
if (javaVersion > JavaVersion.VERSION_17) {
    throw GradleException(
        "Versão do Java detectada no Gradle daemon: $javaVersion\n" +
                "O build requer JDK 17 para o daemon. Ações possíveis:\n" +
                " - Em Android Studio: File > Settings > Build, Execution, Deployment > Build Tools > Gradle > escolha Gradle JDK 17\n" +
                " - Para executar no terminal sem mudar variáveis globais, rode o wrapper definindo JAVA_HOME temporariamente (exemplos abaixo)\n" +
                " - Ou adicione `org.gradle.java.home` localmente se for desejado para toda a equipe\n"
    )
}

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Unasp Marketplace"
include(":app")
