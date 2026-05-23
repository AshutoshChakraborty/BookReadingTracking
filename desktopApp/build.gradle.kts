import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.example.bookreadingtracking.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "BookTracker"
            packageVersion = "1.0.0"

            val commonIcon = project.file("../shared/src/commonMain/composeResources/drawable/app_icon.png")
            val favicon = project.file("../shared/src/commonMain/composeResources/drawable/favicon.ico")

            windows {
                iconFile.set(favicon)
                // Allows users to choose the installation path
                dirChooser = true
                // Creates a shortcut on the desktop
                menu = true
                shortcut = true
                
            }

            macOS {
                bundleID = "com.example.bookreadingtracking"
                iconFile.set(commonIcon)
            }

            linux {
                iconFile.set(commonIcon)
            }
        }
    }
}
