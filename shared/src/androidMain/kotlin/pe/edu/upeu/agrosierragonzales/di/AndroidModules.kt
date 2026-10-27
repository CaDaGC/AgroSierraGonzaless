package pe.edu.upeu.agrosierragonzales.di
import io.ktor.client.engine.android.Android
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import pe.edu.upeu.agrosierragonzales.data.local.*
import pe.edu.upeu.agrosierragonzales.data.remote.*
import pe.edu.upeu.agrosierragonzales.data.repository.*
import pe.edu.upeu.agrosierragonzales.domain.repository.*
import pe.edu.upeu.agrosierragonzales.presentation.AppViewModel
val androidModules=module{
 single<SecureSessionStore>{EncryptedSessionStore(androidContext())}
 single{AgroApi(createHttpClient(Android.create()){get<SecureSessionStore>().read()?.token})}
 single<LocalStore>{AndroidLocalStore(androidContext())}
 single<AuthRepository>{AuthRepositoryImpl(get(),get())}
 single<AgroRepository>{AgroRepositoryImpl(get(),get())}
 single{AppViewModel(get(),get())}
}

