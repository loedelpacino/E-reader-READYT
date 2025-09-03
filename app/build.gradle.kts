// app/build.gradle.kts
// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    // KAPT yerine Kotlin Symbol Processing (KSP) plug‑in'ini kullanıyoruz.
    // KSP, derleme zamanında annotation processing yapmak için modern bir alternatiftir
    // ve JDK'nin iç API'lerine erişmediği için JDK 21 ile uyumludur.
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.iremnisabedirbeyoglu.ereaderreadyt"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.iremnisabedirbeyoglu.ereaderreadyt"
        minSdk = 26
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
    // Build and Kotlin toolchain ayarları
    // Java/Gradle toolchain'i JDK 17 olarak yapılandırıyoruz. Bu ayar, sistemde
    // JDK 21 yüklü olsa bile derleme aşamasında JDK 17'nin kullanılmasını sağlar
    // ve kapt ile ilgili IllegalAccessError hatalarının önüne geçer.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        // Kotlin derleyicisinin hedef JVM sürümünü 17 olarak ayarlıyoruz. 1.8 yerine 17
        // kullanmak, JDK 17 toolchain'iyle uyumlu hale getirir.
        jvmTarget = "17"
    }
    // Android Gradle Plugin 8.4+ ile JDK toolchain'i kullanılabiliyor
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // Kotlin sürümünüzle uyumlu olmalı
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Proje düzeyinde "dependencyResolutionManagement" bölümünde zaten bir "repositories"
// tanımlandığından, burada tekrar tanımlamıyoruz. "settings.gradle.kts" dosyasındaki
// `repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)` ayarı, modül
// içerisinde repository tanımlarına izin vermez. Bu nedenle lokal `repositories`
// bloğunu kaldırıyoruz ve tüm bağımlılık çözümlemesi kök düzeyde tanımlanan
// kaynaklar üzerinden yapılacak. JitPack gibi ek repositoriler de orada
// tanımlandığı için tekrar eklemeye gerek yoktur.

dependencies {
    // Android KTX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")

    // Jetpack Compose BOM (Bill of Materials) - Compose kütüphanelerinin uyumlu sürümlerini yönetir
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.material:material-icons-extended")

    // Jetpack Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.0")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0-beta02")

    // Hilt - Dependency Injection
    implementation("com.google.dagger:hilt-android:2.51.1")
    // Annotation processor olarak KSP kullanıyoruz. Hilt'in derleyici eklentisi için
    // 'hilt-compiler' artefaktını kullanmak KAPT'e göre önerilir. Ayrı olarak
    // androidx.hilt derleyicisi de KSP ile çağrılır.
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Accompanist - System UI Controller (Tema geçişleri ve sistem çubuğu kontrolü için)
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    // PDF Viewer - PdfiumAndroid (android-pdf-viewer yerine daha stabil bir alternatif)
    // JitPack'te "PdfiumAndroid" paketi farklı kullanıcı adlarıyla barındırılabiliyor.
    // Kullandığınız "shockar" kullanıcısı altındaki paket bazı sürümler için mevcut değil
    // ve bu yüzden derleme sırasında çözümleme hatası veriyor. PdfiumAndroid projesinin
    // resmi artefaktı `com.github.barteksc:pdfium-android` olarak Maven Central
    // deposunda yer alıyor ve sürüm 1.9.0 stabil olarak yayınlanmış durumda【821090003402523†L9-L43】. Bu
    // artefaktı kullanarak PDF görüntüleme işlemlerini gerçekleştirebilirsiniz.
    implementation("com.github.barteksc:pdfium-android:1.9.0") // PdfiumAndroid kütüphanesinin stabil sürümü

    // ExoPlayer - Arka plan müziği için
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")

    // Datastore - Kullanıcı tercihleri ve ayarları için
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Kotlin Coroutines - Asenkron işlemler için
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // ViewModel - Compose ile durum yönetimi için
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")

    // Test Bağımlılıkları
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

}

// KAPT kullanımını kaldırdığımız için kapt bloğuna gerek yok. KSP kullandığımızda
// benzer ayarlar otomatik olarak uygulanır.

configurations.all {
    exclude(group = "com.android.support", module = "support-compat")
    exclude(group = "com.android.support", module = "support-media-compat")
}