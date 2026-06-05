plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.localaichat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.localaichat"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // NDK 配置
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    // jniLibs 配置
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
}

dependencies {
    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Room 数据库
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Hilt 依赖注入
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")

    // 网络
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 图片加载
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // 序列化
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // 测试
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// ==================== Native Libraries 下载任务 ====================

tasks.register<DownloadAndExtractNativeLibs>("downloadNativeLibs") {
    group = "build"
    description = "Download and extract native libraries from GitHub Release"
}

tasks.named("preBuild").configure {
    dependsOn("downloadNativeLibs")
}

abstract class DownloadAndExtractNativeLibs : DefaultTask() {

    @TaskAction
    fun execute() {
        val jniLibsDir = project.file("src/main/jniLibs/arm64-v8a")
        val markerFile = File(jniLibsDir, ".libs_ready")

        if (markerFile.exists()) {
            println("Native libraries already extracted. Skipping download.")
            return
        }

        val zipUrl = "https://github.com/lyd136688/LocalAIChat-Android/releases/download/v1.0.0-native-libs/native-libs-arm64.zip"
        val zipFile = File(project.buildDir, "tmp/native-libs-arm64.zip")

        jniLibsDir.mkdirs()
        zipFile.parentFile.mkdirs()

        println("Downloading native libraries from GitHub Release...")
        downloadFile(zipUrl, zipFile)

        println("Extracting native libraries...")
        extractZip(zipFile, jniLibsDir)

        markerFile.writeText("Native libraries extracted on ${java.util.Date()}")

        println("Native libraries ready at: ${jniLibsDir.absolutePath}")
    }

    private fun downloadFile(url: String, outputFile: File) {
        val connection = java.net.URL(url).openConnection()
        connection.setRequestProperty("User-Agent", "Gradle")
        connection.connectTimeout = 30000
        connection.readTimeout = 120000

        connection.getInputStream().use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun extractZip(zipFile: File, outputDir: File) {
        java.util.zip.ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                if (!entry.isDirectory && entry.name.endsWith(".so")) {
                    val outputFile = File(outputDir, entry.name.substringAfterLast("/"))
                    zip.getInputStream(entry).use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    println("Extracted: ${outputFile.name}")
                }
            }
        }
    }
}
