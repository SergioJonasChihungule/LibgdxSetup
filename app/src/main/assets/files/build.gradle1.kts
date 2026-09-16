buildscript {
    val kotlinVersion: String by project.extra
    
    repositories {
        mavenCentral()
        maven(url = "https://s01.oss.sonatype.org")
        gradlePluginPortal()
        mavenLocal()
        google()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:9.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

allprojects {
    apply(plugin = "eclipse")
    apply(plugin = "idea")

    // Configuração para "Build and run using IntelliJ IDEA"
    configure<org.gradle.plugins.ide.idea.model.IdeaModel> {
        module {
            outputDir = file("build/classes/java/main")
            testOutputDir = file("build/classes/java/test")
        }
    }
}

// Configuração para subprojetos (exceto o módulo :android)
configure(subprojects.filter { it.name != "android" }) {
    apply(plugin = "java-library")
    apply(plugin = "kotlin")

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Task para gerar lista de assets (comum em projetos libGDX)
    tasks.register("generateAssetList") {
        val assetsFolder = file("${project.rootDir}/assets/")
        val assetsFile = file("$assetsFolder/assets.txt")

        inputs.dir(assetsFolder)
        outputs.file(assetsFile)

        doLast {
            if (assetsFile.exists()) {
                assetsFile.delete()
            }

            val files = fileTree(assetsFolder).files
                .filter { it.name != "assets.txt" }
                .map { it.relativeTo(assetsFolder).path.replace("\\", "/") }
                .sorted()
            
            assetsFile.writeText(files.joinToString("\n") + "\n")
        }
    }

    tasks.named("processResources") {
        dependsOn("generateAssetList")
    }

    tasks.withType<JavaCompile> {
        options.isIncremental = true
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
}

subprojects {
    val projectVersion: String by project.extra
    version = projectVersion
    extra["appName"] = "$projectname$"

    repositories {
        mavenCentral()
        maven(url = "https://s01.oss.sonatype.org")
        mavenLocal()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://jitpack.io")
    }
}

// Configuração do nome do projeto no Eclipse
configure<org.gradle.plugins.ide.eclipse.model.EclipseModel> {
    project {
        name = "$projectname$-parent"
    }
}
