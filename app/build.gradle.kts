import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvmToolchain(21)

  compilerOptions {
    freeCompilerArgs.addAll(
      listOf(
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        "-Xannotation-default-target=param-property"
      )
    )
  }

  androidTarget {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  jvm("desktop") {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain {
      kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
      dependencies {
        implementation(libs.jb.compose.runtime)
        implementation(libs.jb.compose.foundation)
        implementation(libs.jb.compose.material3)
        implementation(libs.jb.compose.ui)
        implementation(libs.jb.compose.components.resources)
        implementation(libs.jb.compose.components.ui.tooling.preview)
        implementation(libs.jb.compose.material.icons.extended)
        implementation(libs.compose.hooks)
        implementation(libs.palette)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.schema.annotations)
      }
    }

    androidMain.dependencies {
      implementation(project.dependencies.platform(libs.androidx.compose.bom))
      implementation(libs.androidx.compose.ui)
      implementation(libs.androidx.compose.ui.graphics)
      implementation(libs.androidx.compose.ui.tooling.preview)
      implementation(libs.androidx.compose.material3)
      implementation(libs.androidx.compose.material.icons.extended)

      implementation(libs.compose.ai)
      implementation(libs.androidx.room.runtime)
      implementation(libs.androidx.room.ktx)

      implementation(libs.androidx.navigation.compose)
      implementation(libs.androidx.lifecycle.runtime.ktx)
      implementation(libs.androidx.activity.compose)
      implementation(libs.androidx.core.ktx)
      implementation(libs.androidx.exifinterface)
      implementation(libs.coil.compose)
      implementation(libs.dav4jvm)
      implementation(libs.okhttp)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.androidx.documentfile)
      implementation(libs.androidx.security.crypto)
      implementation(libs.androidx.work.runtime)
      implementation(libs.androidx.datastore.preferences)
    }

    val desktopMain by getting {
      dependencies {
        implementation(compose.desktop.currentOs)
      }
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
    }

    androidUnitTest.dependencies {
      implementation(libs.junit)
      implementation(libs.mockk)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.androidx.room.testing)
    }
  }
}

ksp {
  arg("kotlinx.schema.withSchemaObject", "true")
  arg("kotlinx.schema.rootPackage", "com.eatwhat")
  arg("room.schemaLocation", "$projectDir/schemas")
}

android {
  namespace = "com.eatwhat"
  compileSdk = 36

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  sourceSets["main"].res.srcDirs("src/androidMain/res")
  sourceSets["main"].resources.srcDirs("src/commonMain/resources")

  defaultConfig {
    applicationId = "com.eatwhat"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    vectorDrawables {
      useSupportLibrary = true
    }
  }

  signingConfigs {
    create("release") {
      storeFile = file("../eatwhat.keystore")
      storePassword = "eatwhat123"
      keyAlias = "eatwhat"
      keyPassword = "eatwhat123"
      enableV1Signing = true
      enableV2Signing = true
    }
  }

  buildTypes {
    debug {
      signingConfig = signingConfigs.getByName("release")
    }
    release {
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("release")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  dependencies {
    debugImplementation(libs.jb.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  }
}

compose.desktop {
  application {
    mainClass = "com.eatwhat.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "EatWhat"
      packageVersion = "1.0.0"
    }
  }
}

dependencies {
  add("kspCommonMainMetadata", "org.jetbrains.kotlinx:kotlinx-schema-ksp:0.0.2")
  add("kspAndroid", libs.androidx.room.compiler)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
  if (name != "kspCommonMainKotlinMetadata") {
    dependsOn("kspCommonMainKotlinMetadata")
  }
}

tasks.matching {
  it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata"
}.configureEach {
  dependsOn("kspCommonMainKotlinMetadata")
}
