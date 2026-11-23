plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //id("org.jetbrains.kotlin.kapt")       // pour Room (kapt)
    // NOTE : on n'ajoute PAS encore le plugin Firebase ici pour éviter les erreurs,
    // on se concentre juste sur les dépendances qui compilent.
}

android {
    namespace = "iut.dam.ecotri"
    compileSdk = 35

    defaultConfig {
        applicationId = "iut.dam.ecotri"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }

    // Pas obligé de mettre kotlinOptions pour l’instant, on garde simple.
}

dependencies {
    // Dépendances de base générées par Android Studio
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation("androidx.gridlayout:gridlayout:1.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // --- Retrofit (API REST) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // --- Room (base de données locale) ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    //kapt("androidx.room:room-compiler:2.6.1")   // --> marche grâce au plugin kapt plus haut

    // --- Lifecycle / ViewModel ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // --- WorkManager ---
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // --- Firebase (lib de base pour FCM) ---
    implementation(platform("com.google.firebase:firebase-bom:32.1.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    // --- Google Maps & Location ---
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // --- RecyclerView (classements, listes, etc.) ---
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")


    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")

}
