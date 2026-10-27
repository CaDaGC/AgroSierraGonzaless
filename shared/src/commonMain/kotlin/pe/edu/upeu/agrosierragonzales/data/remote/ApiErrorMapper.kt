package pe.edu.upeu.agrosierragonzales.data.remote
import io.ktor.client.call.*
import io.ktor.client.statement.*
import pe.edu.upeu.agrosierragonzales.domain.error.ApiError
fun mapHttpError(s:Int,m:String="Error del servidor"):ApiError=when(s){400,422->ApiError.Validation(m);401->ApiError.Unauthorized;403->ApiError.Forbidden;404->ApiError.NotFound;409->ApiError.Conflict(m);in 500..599->ApiError.Server(m);else->ApiError.Unknown(m)}
class ApiFailure(val apiError:ApiError):IllegalStateException(apiError.message)
suspend inline fun <reified T> HttpResponse.bodyOrFailure():T{
 if(status.value !in 200..299) throw ApiFailure(mapHttpError(status.value))
 return body()
}
