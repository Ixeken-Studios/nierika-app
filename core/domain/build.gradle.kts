plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.ixeken.nierika.core.domain"
    compileSdk = 37

    defaultConfig {
        minSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.javax.inject)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
}
