package pe.edu.upeu.agrosierragonzales.domain.error
sealed class ApiError(open val message:String){
 data class Validation(override val message:String):ApiError(message);data object Unauthorized:ApiError("Usuario o contraseña incorrectos o sesión vencida")
 data object Forbidden:ApiError("Tu perfil no tiene permiso");data object NotFound:ApiError("Recurso no encontrado")
 data class Conflict(override val message:String):ApiError(message);data class Server(override val message:String):ApiError(message)
 data object Offline:ApiError("Sin conexión; cambio pendiente");data object Timeout:ApiError("Tiempo de espera agotado")
 data class Unknown(override val message:String):ApiError(message)
}
