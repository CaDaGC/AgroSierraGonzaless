# AgroSierraGonzales

Aplicación móvil Android en Kotlin y Jetpack Compose con Clean Architecture. El proyecto tiene módulos `shared` (dominio, datos y presentación Android) y `androidApp`; no configura destinos ni código iOS.

## Requisitos y conexión

- Android Studio, Android SDK 36 y JDK 17 o superior.
- Inicia el backend Spring Boot y MySQL en el puerto 8080.
- En el emulador Android, `10.0.2.2` apunta al localhost del equipo. La URL y los timeout HTTP están centralizados en `shared/src/commonMain/.../HttpClientFactory.kt`.
- Para iniciar sesión con los datos semilla del backend: `tecnico1`, `jefe1` o `admin`; contraseña inicial `Agro2026*`.

## Arquitectura

- `domain`: modelos, errores, interfaces de repositorios y casos de uso.
- `data`: DTO y mapeadores, cliente Ktor, repositorios offline-first y persistencia SQLDelight.
- `presentation`: pantallas Compose y estado observable con StateFlow.
- `di`: módulos de inyección Koin.
- Token y sesión cifrados con AES-GCM y Android Keystore.

## Ejecutar

Abre esta carpeta en Android Studio, sincroniza Gradle y ejecuta la configuración `androidApp`. También puedes compilar con `gradlew.bat :androidApp:assembleDebug`; las pruebas comunes se ejecutan con `gradlew.bat :shared:testDebugUnitTest`.

La app incluye login, lista y detalle de lotes, búsqueda y filtro por valle, registro local offline de labores, marca de pendientes, sincronización manual y automática al iniciar/restaurar sesión y al recuperar la conexión, edición/eliminación según privilegios, cierre de campaña para Jefe de Campo y cierre de sesión. La sincronización envía los pendientes antes de descargar lotes y labores permitidas por el perfil.


