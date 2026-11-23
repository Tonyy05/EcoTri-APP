plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("org.sonarqube") version "7.1.0.6387"
}


sonar {
  properties {
    property("sonar.projectKey", "Billal923_EcoTri-Api")
    property("sonar.organization", "billal923")
  }
}
