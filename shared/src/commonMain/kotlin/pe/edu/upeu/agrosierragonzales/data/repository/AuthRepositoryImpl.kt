package pe.edu.upeu.agrosierragonzales.data.repository
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.utils.io.errors.IOException
import pe.edu.upeu.agrosierragonzales.data.remote.AgroApi
import pe.edu.upeu.agrosierragonzales.data.remote.ApiFailure
import pe.edu.upeu.agrosierragonzales.domain.model.Sesion
import pe.edu.upeu.agrosierragonzales.domain.repository.AuthRepository
interface SecureSessionStore{fun read():Sesion?;fun write(x:Sesion);fun clear()}
class AuthRepositoryImpl(private val api:AgroApi,private val store:SecureSessionStore):AuthRepository{
 override suspend fun login(usuario:String,contrasena:String):Result<Sesion> = try{
  val response=api.login(usuario,contrasena)
  Result.success(Sesion(response.token,response.usuario,response.nombre,response.rol,response.privilegios).also(store::write))
 }catch(error:Throwable){
  val message=when(error){is ApiFailure->error.message;is HttpRequestTimeoutException->"El servidor no respondió a tiempo";is IOException->"Sin conexión. Verifica tu internet e inténtalo nuevamente";else->error.message?:"No se pudo iniciar sesión"}
  Result.failure(IllegalStateException(message,error))
 }
 override suspend fun currentSession()=store.read()
 override suspend fun logout()=store.clear()
}
