import org.gradle.kotlin.dsl.all
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("jacoco")
}

android {
    namespace = "com.unasp.unaspmarketplace"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.unasp.unaspmarketplace"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.systemProperty("robolectric.enabledSdks", "34")
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

jacoco {
    toolVersion = "0.8.11"
}

configure<JacocoPluginExtension> {
    toolVersion = "0.8.11"
}

tasks.withType<Test>().configureEach {
    maxHeapSize = "1024m"
    jvmArgs(
        "-XX:MaxMetaspaceSize=256m",
        "-XX:+UseG1GC",
        "-XX:MaxGCPauseMillis=100"
    )
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }

    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val excludes = listOf(
        // Android / build generated
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/generated/**",
        "**/intermediates/**",


        // DataBinding / ViewBinding
        "**/databinding/**",

        // Application class (inicialização do app)
        "**/UnaspMarketplaceApplication.*",

        // DI / annotation processors
        "**/*_Factory.class",
        "**/*_MembersInjector*.*",
        "**/Dagger*.*",
        "**/hilt_*/**",

        // Test classes / test helpers
        "**/*Test*.*",
        "**/test/**",

        // Activities simples, sem lógica relevante
        "**/OrderSuccessActivity.*",

        // Auth helpers e utils com Firebase
        "**/auth/**",
        "**/utils/UserUtils.*",
        "**/utils/EmailService.*",

        // Third party libs / stdlib
        "**/androidx/**",
        "**/com/google/**",
        "**/com/facebook/**",
        "**/com/github/**",
        "**/kotlin/**",

        // Metadata / resources
        "**/META-INF/**",
        "**/resources/**"
    )

    val buildDirFile = layout.buildDirectory.get().asFile

    val classDirs = listOf(
        file("${buildDirFile.path}/tmp/kotlin-classes/debug"),
        file("${buildDirFile.path}/tmp/kotlin-classes/debugUnitTest"),
        file("${buildDirFile.path}/intermediates/javac/debug/classes"),
        file("${buildDirFile.path}/intermediates/javac/debugUnitTest/classes")
    ).filter { it.exists() }

    classDirectories.setFrom(
        files(classDirs.map { dir ->
            fileTree(dir).apply {
                setExcludes(excludes)
            }
        })
    )

    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))

    executionData.setFrom(
        files(
            fileTree(buildDirFile).apply {
                setIncludes(listOf(
                    "jacoco/testDebugUnitTest.exec",
                    "jacoco/test.exec",
                    "**/*.exec",
                    "**/*.ec"
                ))
            }
        )
    )
}

tasks.register("coverage") {
    group = "verification"
    description = "Executa unit tests de `src/test` e gera relatório Jacoco (HTML + XML)."

    dependsOn("testDebugUnitTest", "jacocoTestReport")

    doLast {
        val report = file("${buildDir}/reports/jacoco/jacocoTestReport/html/index.html")
        println("Relatório Jacoco gerado em: ${report.absolutePath}")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Testes unitários (Robolectric + JUnit)
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.12.2")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:2.0.21")

    // Android instrumented tests (mantidos para compatibilidade)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase Test Lab e mocks
    testImplementation(platform("com.google.firebase:firebase-bom:32.2.3"))
    testImplementation("com.google.firebase:firebase-auth-ktx")
    testImplementation("com.google.firebase:firebase-firestore-ktx")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")


    // Firebase e demais libs do app
    implementation(platform("com.google.firebase:firebase-bom:32.2.3"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

    implementation("com.facebook.android:facebook-login:17.0.1")
    implementation("androidx.browser:browser:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
}