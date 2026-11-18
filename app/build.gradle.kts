plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.paparazzi)
    jacoco
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

android {
    namespace = "pl.marcin.malocha.flickrrecruitmenttask"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "pl.marcin.malocha.flickrrecruitmenttask"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

val jacocoExcludes = listOf(
    // Android
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",
    // App entry points and DI wiring
    "**/MainActivity.*",
    "**/FlickrApp.*",
    "**/di/**",
    // Hilt / Dagger
    "**/*_Factory*.*",
    "**/*_MembersInjector*.*",
    "**/*_Provide*Factory*.*",
    "**/Hilt_*.*",
    "**/*_HiltModules*.*",
    "**/*_ComponentTreeDeps*.*",
    "**/*_GeneratedInjector*.*",
    "**/*_HiltComponents*.*",
    "**/Dagger*Component*.*",
    "**/dagger/**",
    "**/hilt_aggregated_deps/**",
    // Compose
    "**/*ComposableSingletons*.*",
    "**/*\$lambda$*.*",
    "**/*Preview*.*",
    // Kotlin
    "**/*\$WhenMappings.*",
    "**/*\$serializer.*",
)

val unitTestExecutionData = "outputs/unit_test_code_coverage/debugUnitTest/*.exec"
val androidTestExecutionData = "outputs/code_coverage/debugAndroidTest/connected/**/*.ec"

fun JacocoReport.configureDebugCoverageReport(vararg executionDataPatterns: String) {
    group = "verification"

    reports {
        html.required = true
        xml.required = true
        csv.required = false
    }

    val buildDir = layout.buildDirectory
    classDirectories.setFrom(
        buildDir.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes").map {
            fileTree(it) { exclude(jacocoExcludes) }
        },
        buildDir.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes").map {
            fileTree(it) { exclude(jacocoExcludes) }
        },
    )
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        buildDir.map { fileTree(it) { include(*executionDataPatterns) } },
    )
}

tasks.register<JacocoReport>("jacocoTestReport") {
    description = "Generates a JaCoCo coverage report for debug unit tests (including screenshot tests)."
    dependsOn("testDebugUnitTest")
    configureDebugCoverageReport(unitTestExecutionData)
}

tasks.register<JacocoReport>("jacocoFullTestReport") {
    description = "Generates a merged JaCoCo coverage report for debug unit and UI tests (needs a device or emulator)."
    dependsOn("testDebugUnitTest", "connectedDebugAndroidTest")
    configureDebugCoverageReport(unitTestExecutionData, androidTestExecutionData)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.truth)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}