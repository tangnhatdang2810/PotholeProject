plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.projectmobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.projectmobile"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.facebook.android:facebook-android-sdk:17.0.2")
    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.mapbox.search:mapbox-search-android-ui:1.0.0-rc.6")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.mapbox.navigation:android:2.15.2")
}