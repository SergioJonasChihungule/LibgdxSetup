import java.util.Properties
import java.io.InputStream

plugins {
    id("com.android.application")
}

// Recuperando variáveis do projeto raiz
val gdxVersion: String by rootProject.extra
val appName: String by project.extra

android {
    namespace = "$packagename$"
    compileSdk = 37
    

    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.setSrcDirs(listOf("src/main/java", "src/main/kotlin"))
            aidl.setSrcDirs(listOf("src/main/java", "src/main/kotlin"))
            renderscript.setSrcDirs(listOf("src/main/java", "src/main/kotlin"))
            res.setSrcDirs(listOf("res"))
            assets.setSrcDirs(listOf("../assets"))
            jniLibs.setSrcDirs(listOf("libs"))
        }
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/robovm/ios/robovm.xml",
                "META-INF/DEPENDENCIES.txt",
                "META-INF/DEPENDENCIES",
                "META-INF/dependencies.txt",
                "**/*.gwt.xml"
            )
            pickFirsts += listOf(
                "META-INF/LICENSE.txt",
                "META-INF/LICENSE",
                "META-INF/license.txt",
                "META-INF/LGPL2.1",
                "META-INF/NOTICE.txt",
                "META-INF/NOTICE",
                "META-INF/notice.txt"
            )
        }
    }

    defaultConfig {
        applicationId = "$packagename$"
        minSdk = $minsdk$
        targetSdk = 34
        versionCode = 1
        versionName = "1.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }

    buildFeatures {
        viewBinding = false
    }
}

repositories {
    google()
}

// Configuração para dependências nativas (libGDX)
val natives by configurations.creating

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-freetype:$gdxVersion")
    implementation(project(":core"))

    val platforms = listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
    platforms.forEach { platform ->
        natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-$platform")
        natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-$platform")
    }
}

// Tarefa para copiar as bibliotecas nativas para as pastas corretas
tasks.register("copyAndroidNatives") {
    doFirst {
        val platforms = mapOf(
            "natives-armeabi-v7a.jar" to "libs/armeabi-v7a",
            "natives-arm64-v8a.jar" to "libs/arm64-v8a",
            "natives-x86_64.jar" to "libs/x86_64",
            "natives-x86.jar" to "libs/x86"
        )

        platforms.values.forEach { file(it).mkdirs() }

        configurations.getByName("natives").files.forEach { jar ->
            val targetDir = platforms.entries.firstOrNull { jar.name.endsWith(it.key) }?.value
            if (targetDir != null) {
                copy {
                    from(zipTree(jar))
                    into(file(targetDir))
                    include("*.so")
                }
            }
        }
    }
}

// Garante que a cópia ocorra antes do merge das libs JNI
tasks.matching { it.name.contains("merge") && it.name.contains("JniLibFolders") }.configureEach {
    dependsOn("copyAndroidNatives")
}

// Tarefa para rodar o app via ADB
tasks.register<Exec>("run") {
    val localProperties = project.file("../local.properties")
    var sdkDir: String? = System.getenv("ANDROID_SDK_ROOT")

    if (localProperties.exists()) {
        val properties = Properties()
        localProperties.inputStream().use { stream: InputStream -> 
            properties.load(stream)
        }
        val pathFromProps = properties.getProperty("sdk.dir")
        if (pathFromProps != null) {
            sdkDir = pathFromProps
        }
    }

    val adb = if (sdkDir != null) "$sdkDir/platform-tools/adb" else "adb"
    
    commandLine(adb, "shell", "am", "start", "-n", "seran.pulapula.game/seran.pulapula.game.android.AndroidLauncher")
}

configure<org.gradle.plugins.ide.eclipse.model.EclipseModel> {
    project {
        name = "$appName-android"
    }
}
tasks.matching { it.name.contains("merge") && it.name.contains("Assets") }.configureEach {
    dependsOn(":core:generateAssetList")
}
