import java.net.URL
import java.net.HttpURLConnection
import java.util.zip.ZipFile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.localai.chat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.localai.chat"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        externalNativeBuild {
            cmake {
                cppFlags += "-O3 -ffast-math -funroll-loops"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
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

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// 下载并解压原生库的任务
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
            println("Native libraries already extracted, skipping download.")
            return
        }
        
        val githubToken = (project.findProperty("ghToken") as? String)
            ?.takeIf { it.isNotEmpty() }
            ?: (System.getenv("GH_TOKEN") ?: "")
        val repoOwner = "lyd136688"
        val repoName = "LocalAIChat-Android"
        val tagName = "v1.0.0native"
        val assetName = "native-libs-arm64.zip"
        
        jniLibsDir.mkdirs()
        
        println("Token provided: ${githubToken.isNotEmpty()}")
        
        try {
            val zipFile = File(project.buildDir, "tmp/native-libs-arm64.zip")
            zipFile.parentFile.mkdirs()
            
            if (githubToken.isNotEmpty()) {
                // 私有仓库：通过 API 获取 asset ID，再用 API 下载
                val assetId = getAssetId(repoOwner, repoName, tagName, assetName, githubToken)
                downloadFromAssetApi(repoOwner, repoName, assetId, zipFile, githubToken)
            } else {
                // 公开仓库：直接下载
                val downloadUrl = "https://github.com/$repoOwner/$repoName/releases/download/$tagName/$assetName"
                downloadFile(downloadUrl, zipFile, githubToken)
            }
            
            extractZip(zipFile, jniLibsDir)
            markerFile.writeText("Native libraries extracted on ${System.currentTimeMillis()}")
            println("Native libraries downloaded and extracted successfully.")
        } catch (e: Exception) {
            println("ERROR: Failed to download native libraries: ${e.message}")
            throw e
        }
    }
    
    private fun getAssetId(owner: String, repo: String, tag: String, assetName: String, token: String): Long {
        val apiUrl = "https://api.github.com/repos/$owner/$repo/releases/tags/$tag"
        println("Fetching release info from API: $apiUrl")
        
        val connection = URL(apiUrl).openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", "Gradle-LocalAIChat-Android")
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
        connection.setRequestProperty("Authorization", "token $token")
        connection.connectTimeout = 30000
        connection.readTimeout = 120000
        
        val responseCode = connection.responseCode
        if (responseCode != 200) {
            val error = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details"
            throw RuntimeException("GitHub API returned $responseCode: $error")
        }
        
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        
        // 找到 assets 部分
        val assetsSection = response.split("\"assets\":").getOrNull(1) ?: ""
        val assetEntries = assetsSection.split("\"id\":").drop(1)
        
        for (entry in assetEntries) {
            val nameMatch = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"").find(entry)
            val idMatch = Regex("\"id\"\\s*:\\s*(\\d+)").find(entry)
            
            if (nameMatch != null && idMatch != null) {
                val name = nameMatch.groupValues[1]
                val id = idMatch.groupValues[1].toLong()
                println("Found asset: $name (id=$id)")
                if (name == assetName) {
                    return id
                }
            }
        }
        
        throw RuntimeException("Asset '$assetName' not found in release")
    }
    
    private fun downloadFromAssetApi(owner: String, repo: String, assetId: Long, outputFile: File, token: String) {
        // 私有仓库需要通过 assets API 下载
        // 设置 Accept: application/octet-stream 让 API 返回文件内容而不是 JSON
        val assetUrl = "https://api.github.com/repos/$owner/$repo/releases/assets/$assetId"
        println("Downloading from asset API: $assetUrl")
        
        var currentUrl = assetUrl
        var redirectCount = 0
        val maxRedirects = 5
        
        while (redirectCount < maxRedirects) {
            val connection = URL(currentUrl).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Gradle-LocalAIChat-Android")
            connection.setRequestProperty("Accept", "application/octet-stream")
            connection.setRequestProperty("Authorization", "token $token")
            connection.instanceFollowRedirects = false
            
            connection.connectTimeout = 30000
            connection.readTimeout = 120000
            
            val responseCode = connection.responseCode
            println("Response code: $responseCode")
            
            when (responseCode) {
                200 -> {
                    connection.inputStream.buffered().use { input ->
                        outputFile.outputStream().buffered().use { output ->
                            input.copyTo(output)
                        }
                    }
                    println("Download completed: ${outputFile.length()} bytes")
                    return
                }
                301, 302, 303, 307, 308 -> {
                    val location = connection.getHeaderField("Location")
                    if (location != null) {
                        println("Redirecting to: $location")
                        currentUrl = location
                        redirectCount++
                        connection.disconnect()
                    } else {
                        throw RuntimeException("Redirect ($responseCode) without Location header")
                    }
                }
                401 -> {
                    throw RuntimeException("Unauthorized (401). Token may be invalid or expired.")
                }
                403 -> {
                    throw RuntimeException("Forbidden (403). Token may lack required permissions.")
                }
                404 -> {
                    throw RuntimeException("Asset not found (404). Asset ID: $assetId")
                }
                else -> {
                    throw RuntimeException("Unexpected HTTP response: $responseCode")
                }
            }
        }
        throw RuntimeException("Too many redirects (>$maxRedirects)")
    }
    
    private fun downloadFile(url: String, outputFile: File, token: String) {
        var currentUrl = url
        var redirectCount = 0
        val maxRedirects = 5
        
        while (redirectCount < maxRedirects) {
            val connection = URL(currentUrl).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Gradle-LocalAIChat-Android")
            connection.setRequestProperty("Accept", "application/octet-stream")
            connection.instanceFollowRedirects = false
            
            if (token.isNotEmpty()) {
                connection.setRequestProperty("Authorization", "token $token")
            }
            
            connection.connectTimeout = 30000
            connection.readTimeout = 120000
            
            val responseCode = connection.responseCode
            
            when (responseCode) {
                200 -> {
                    connection.inputStream.buffered().use { input ->
                        outputFile.outputStream().buffered().use { output ->
                            input.copyTo(output)
                        }
                    }
                    println("Download completed: ${outputFile.length()} bytes")
                    return
                }
                301, 302, 303, 307, 308 -> {
                    val location = connection.getHeaderField("Location")
                    if (location != null) {
                        currentUrl = location
                        redirectCount++
                        connection.disconnect()
                    } else {
                        throw RuntimeException("Redirect ($responseCode) without Location header")
                    }
                }
                404 -> {
                    throw RuntimeException("File not found (404)")
                }
                else -> {
                    throw RuntimeException("Unexpected HTTP response: $responseCode")
                }
            }
        }
        throw RuntimeException("Too many redirects (>$maxRedirects)")
    }
    
    private fun extractZip(zipFile: File, outputDir: File) {
        ZipFile(zipFile).use { zip ->
            zip.entries().toList().forEach { entry ->
                if (!entry.isDirectory && entry.name.endsWith(".so")) {
                    val fileName = entry.name.substringAfterLast("/")
                    val outputFile = File(outputDir, fileName)
                    zip.getInputStream(entry).buffered().use { input ->
                        outputFile.outputStream().buffered().use { output ->
                            input.copyTo(output)
                        }
                    }
                    println("Extracted: $fileName")
                }
            }
        }
    }
}

