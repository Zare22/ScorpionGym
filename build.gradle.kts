import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "hr.kotwave"
version = "1.1.3"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
    implementation("io.insert-koin:koin-core:3.2.0")
    implementation("cafe.adriel.voyager:voyager-navigator:1.1.0-beta01")
    implementation("cafe.adriel.voyager:voyager-transitions:1.1.0-beta01")
}

compose.desktop {
    application {
        mainClass = "hr.kotwave.scorpiongym.MainKt"

        nativeDistributions {
            modules("java.sql")
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ScorpionGym"
            packageVersion = "1.1.3"

            windows {
                upgradeUuid = "123e4567-e89b-12d3-a456-426614174000"
                menuGroup = "Scorpion Gym"
                iconFile.set(project.file("src/main/resources/windows.ico"))
            }
        }

        buildTypes.release {
            proguard {
                version.set("7.4.0")
                obfuscate.set(false)
                optimize.set(false)
                configurationFiles.from("compose-desktop.pro")
            }
        }
    }
}
