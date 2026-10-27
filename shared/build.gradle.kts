plugins { kotlin("multiplatform"); id("com.android.library"); kotlin("plugin.serialization"); id("org.jetbrains.compose"); id("org.jetbrains.kotlin.plugin.compose"); id("app.cash.sqldelight") }
kotlin {
 androidTarget()
 sourceSets {
  commonMain.dependencies {
   implementation(compose.runtime); implementation(compose.foundation); implementation(compose.material3); implementation(compose.ui)
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
   implementation("io.ktor:ktor-client-core:3.2.3"); implementation("io.ktor:ktor-client-content-negotiation:3.2.3"); implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.3")
   implementation("io.insert-koin:koin-core:4.1.1"); implementation("app.cash.sqldelight:runtime:2.1.0")
  }
  androidMain.dependencies { implementation("io.ktor:ktor-client-android:3.2.3"); implementation("app.cash.sqldelight:android-driver:2.1.0"); implementation("app.cash.sqldelight:coroutines-extensions:2.1.0"); implementation("io.insert-koin:koin-android:4.1.1"); implementation("io.insert-koin:koin-androidx-compose:4.1.1") }
  commonTest.dependencies { implementation(kotlin("test")); implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2"); implementation("io.ktor:ktor-client-mock:3.2.3"); implementation("app.cash.sqldelight:sqlite-driver:2.1.0") }
 }
}
android { namespace="pe.edu.upeu.agrosierragonzales.shared"; compileSdk=36; defaultConfig { minSdk=26 } }
sqldelight { databases { create("AgroDatabase") { packageName.set("pe.edu.upeu.agrosierragonzales.data.local.db") } } }


