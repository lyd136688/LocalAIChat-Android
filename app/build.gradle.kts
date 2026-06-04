plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
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
    
    // NDK 配置 - 使用预编译库
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.glide)
    kapt(libs.glide.compiler)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.material)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// 下载和解压 native libraries 的任务
tasks.register<DownloadAndExtractNativeLibs>("downloadNativeLibs") {
    group = "build"
    description = "Download and extract native libraries from GitHub Release"
}

// 让 preBuild 依赖下载任务
tasks.named("preBuild").configure {
    dependsOn("downloadNativeLibs")
}

// 自定义任务：下载并解压
abstract class DownloadAndExtractNativeLibs : DefaultTask() {
    
    @TaskAction
    fun execute() {
        val jniLibsDir = project.file("src/main/jniLibs/arm64-v8a")
        val markerFile = File(jniLibsDir, ".libs_ready")
        
        // 如果已经解压过，跳过
        if (markerFile.exists()) {
            println("Native libraries already extracted. Skipping download.")
            return
        }
        
        val zipUrl = "https://github.com/lyd136688/LocalAIChat-Android/releases/download/v1.0.0-native-libs/native-libs-arm64.zip"
        val zipFile = File(project.buildDir, "tmp/native-libs-arm64.zip")
        
        // 创建目录
        jniLibsDir.mkdirs()
        zipFile.parentFile.mkdirs()
        
        // 下载 zip 文件
        println("Downloading native libraries from GitHub Release...")
        downloadFile(zipUrl, zipFile)
        
        // 解压 zip 文件
        println("Extracting native libraries...")
        extractZip(zipFile, jniLibsDir)
        
        // 创建标记文件
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

