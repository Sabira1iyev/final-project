# Postalon: Flutter -> Kotlin/Jetpack Compose Dönüşümü

Bu klasör, paylaştığın "postalon" Flutter uygulamasının (Unsplash'tan rastgele
görsel çekip Firebase Realtime Database'e post atma + like/dislike) **MVVM**
mimarisiyle Kotlin + Jetpack Compose karşılığını içerir. Diğer projeden (chat_app)
tamamen bağımsız, ayrı bir Gradle modülü olarak düşünülmüştür.

## Mimari eşleştirme (Flutter -> Kotlin)

| Flutter dosyası      | Kotlin karşılığı                                                | Katman          |
|------------------------|--------------------------------------------------------------------|-----------------|
| `credentials.dart`     | `data/Credentials.kt` (+ `google-services.json`)                   | config          |
| `firebase.dart` (getImageLink) | `data/ImageRepository.kt`                                  | Model           |
| `firebase.dart` (RTDB kısmı)   | `data/FirebaseService.kt`                                  | Model           |
| `post_widget.dart` (model)     | `data/model/PostModel.kt`                                  | Model           |
| `main.dart` (AuthGate)         | `ui/viewmodel/AuthViewModel.kt` + `MainActivity.kt`         | ViewModel/View  |
| `explore_page.dart`            | `ui/viewmodel/ExploreViewModel.kt` + `ui/pages/ExplorePage.kt` | ViewModel/View |
| `create_page.dart`             | `ui/viewmodel/CreateViewModel.kt` + `ui/pages/CreatePage.kt`   | ViewModel/View |
| `post_widget.dart` (UI)        | `ui/widgets/PostItem.kt`                                    | View            |
| `home_page.dart`               | `ui/pages/HomePage.kt`                                      | View            |

`firebase.dart`'taki tek sınıfı, tek sorumluluk ilkesi için ikiye böldüm:
**`ImageRepository`** (Unsplash HTTP çağrısı) ve **`FirebaseService`** (Auth + RTDB).
ViewModel'ler ikisini birlikte kullanıyor; mantığın kendisi (hangi veri nereden
geliyor, hangi sırayla işleniyor) Flutter'dakiyle birebir aynı.

## 1) Firebase bağlantısı

1. Firebase Console -> Project settings -> "Add app" -> Android, package name'i
   `com.example.postalon` ile eşleştir (ya da kendi seçtiğin package + tüm
   dosyalardaki `package com.example.postalon` satırlarını günceller).
2. İndirdiğin `google-services.json`'ı `app/google-services.json` olarak koy.
3. Realtime Database kurallarını ve `posts/{id}` şemasını Flutter projesindekiyle
   aynı tut (`uid`, `image_link`, `created_at`, `like_count`, `dislike_count`,
   `likes/{uid}` -> 1 ya da 2).

## 2) Unsplash API token'ı

`credentials.dart`'taki `imageApiAccessToken` değeri `data/Credentials.kt`'ye
taşındı. Terminalden hızlı denemek için olduğu gibi bıraktım, ama gerçek/paylaşılan
bir repo için önerilen yol:

**`local.properties`** (commit edilmez, `.gitignore`'da zaten vardır):
```
UNSPLASH_ACCESS_TOKEN=Ix5yq3rBTwtU1xiFuLULd08Hn6LwqEm4YkSHVFA1L-c
```

**`app/build.gradle.kts`** içinde `defaultConfig` bloğuna:
```kotlin
val localProperties = java.util.Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

defaultConfig {
    // ...
    buildConfigField(
        "String",
        "UNSPLASH_ACCESS_TOKEN",
        "\"${localProperties.getProperty("UNSPLASH_ACCESS_TOKEN", "")}\""
    )
}

buildFeatures {
    compose = true
    buildConfig = true
}
```

Sonra `Credentials.kt` içindeki sabiti `BuildConfig.UNSPLASH_ACCESS_TOKEN` ile
değiştirebilirsin.

## 3) Gradle bağımlılıkları

**Proje düzeyi `build.gradle.kts`:**
```kotlin
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
```

**Modül düzeyi `app/build.gradle.kts`:**
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.postalon"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.postalon"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // FirebaseAuth/Database Task<T>.await() icin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Network görsellerini yüklemek icin (Image.network karşılığı)
    implementation("io.coil-kt:coil-compose:2.6.0")
}
```

> `ImageRepository` kendi HTTP çağrısı için `HttpURLConnection` + Android'in
> built-in `org.json` paketini kullanıyor; Unsplash isteği için ek bir HTTP
> kütüphanesine (Retrofit/OkHttp) gerek yok. Görselleri ekranda göstermek için
> ise Coil (`coil-compose`) ekledim — bu, Flutter'daki `Image.network`'ün
> `loadingBuilder`/`errorBuilder` davranışını (`SubcomposeAsyncImage` ile)
> karşılayan en pratik kütüphane.

## 4) AndroidManifest

`app/src/main/AndroidManifest.xml` içinde internet izni gerekiyor (Unsplash +
Firebase + Coil için):

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 5) Terminalden derleme

```bash
./gradlew assembleDebug
# veya cihaz/emülatör bağlıyken
./gradlew installDebug
```

## 6) Davranış notları (Flutter ile birebir eşleşen kısımlar)

- **Anonim giriş**: `AuthViewModel`, `signInAnonymously()` çağırır; bu projede
  `chat_app`'teki gibi bir profil/register adımı yok, direkt `HomePage`'e geçilir.
- **Realtime post akışı**: `ExploreViewModel.posts`, `ValueEventListener`'ı
  `callbackFlow` ile Flow'a sarar; liste değiştiğinde Compose otomatik
  yeniden çizilir (`StreamBuilder` karşılığı). Liste altına yakınsa ve yeni
  post gelirse otomatik scroll yapılır.
- **Like/Dislike**: `FirebaseService.likeQuestions`, Flutter'daki transaction
  mantığının birebir aynısı — `selected` parametresi `null` (kaldır), `1`
  (like) ya da `2` (dislike) olabilir; mevcut oy, like_count, dislike_count ve
  `likes` map'i atomik olarak güncellenir.
- **Optimistic UI**: Flutter'da `PostWidget` bir `StatefulWidget` olduğu için
  tıklamada local `widget.post` alanı hemen güncelleniyor, gerçek sonuç stream'den
  gelince üzerine yazılıyordu. Kotlin tarafında `PostItem` composable'ı aynı
  mantığı `displayPost` local state + `LaunchedEffect(post)` ile uyguluyor:
  tıklayınca anında günceller, Firebase'den gerçek veri gelince senkronlanır.
- **Görsel seçme/yayınlama**: `CreateViewModel.getImage()` Unsplash'tan rastgele
  görsel çeker, `publish()` sadece bir görsel seçiliyken çalışır ve başarılı
  olursa `imageLink`'i sıfırlar (Flutter'daki `setState(() { imageLink = null; })`
  karşılığı).

## 7) Klasör yapısı

```
app/src/main/java/com/example/postalon/
├── MainActivity.kt
├── data/
│   ├── Credentials.kt
│   ├── ImageRepository.kt
│   ├── FirebaseService.kt
│   └── model/
│       └── PostModel.kt
├── di/
│   └── AppContainer.kt
└── ui/
    ├── pages/
    │   ├── HomePage.kt
    │   ├── ExplorePage.kt
    │   └── CreatePage.kt
    ├── viewmodel/
    │   ├── AuthViewModel.kt
    │   ├── ExploreViewModel.kt
    │   └── CreateViewModel.kt
    └── widgets/
        └── PostItem.kt
```
