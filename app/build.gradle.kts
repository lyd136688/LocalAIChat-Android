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
        
        // 优先使用项目属性，其次使用环境变量
        val githubToken = (project.findProperty("ghToken") as? String)
            ?.takeIf { it.isNotEmpty() }
            ?: (System.getenv("GH_TOKEN") ?: "")
        val repoOwner = "lyd136688"
        val repoName = "LocalAIChat-Android"
        val tagName = "v1.0.0native"
        val assetName = "native-libs-arm64.zip"
        
        val zipUrl = "https://github.com/$repoOwner/$repoName/releases/download/$tagName/$assetName"
        val zipFile = File(project.buildDir, "tmp/native-libs-arm64.zip")
        
        jniLibsDir.mkdirs()
        zipFile.parentFile.mkdirs()
        
        println("Downloading native libraries from: $zipUrl")
        println("Token provided: ${githubToken.isNotEmpty()}")
        
        try {
            downloadFile(zipUrl, zipFile, githubToken)
            extractZip(zipFile, jniLibsDir)
            markerFile.writeText("Native libraries extracted on ${System.currentTimeMillis()}")
            println("Native libraries downloaded and extracted successfully.")
        } catch (e: Exception) {
            println("ERROR: Failed to download native libraries: ${e.message}")
            println("Please ensure:")
            println("1. The Release '$tagName' exists in your repository")
            println("2. The asset '$assetName' is attached to the Release")
            println("3. For private repos, GH_TOKEN secret is set with 'repo' permission")
            throw e
        }
    }
    
    private fun downloadFile(url: String, outputFile: File, token: String) {
        var currentUrl = url
        var redirectCount = 0
        val maxRedirects = 5
        
        while (redirectCount < maxRedirects) {
            val connection = URL(currentUrl).openConnection() as java.net.HttpURLConnection
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
                        println("Redirecting to: $location")
                        currentUrl = location
                        redirectCount++
                        connection.disconnect()
                    } else {
                        throw RuntimeException("Redirect ($responseCode) without Location header")
                    }
                }
                404 -> {
                    throw RuntimeException("File not found (404). Check tag name and asset name.")
                }
                401 -> {
                    throw RuntimeException("Unauthorized (401). Check GH_TOKEN has 'repo' permission.")
                }
                403 -> {
                    throw RuntimeException("Forbidden (403). Token may lack required permissions.")
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

