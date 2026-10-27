package pe.edu.upeu.agrosierragonzales.data.remote.dto
import kotlinx.serialization.Serializable
import pe.edu.upeu.agrosierragonzales.domain.model.*
@Serializable data class LoginRequestDto(val usuario:String,val contrasena:String)
@Serializable data class LoginResponseDto(val token:String,val usuario:String,val nombre:String,val rol:String,val privilegios:Set<String> = emptySet())
@Serializable data class LoteDto(val id:Long,val codigo:String,val nombre:String,val hectareas:Double,val cultivo:String,val valle:String,val campaniaCerrada:Boolean,val dosisMaximaPorHectarea:Double)
@Serializable data class LaborDto(val id:Long,val loteId:Long,val tipo:String,val fecha:String,val responsable:String,val cantidad:Double,val observacion:String="")
@Serializable data class LaborRequestDto(val loteId:Long,val tipo:String,val fecha:String,val cantidad:Double,val observacion:String)
@Serializable data class PageDto<T>(val contenido:List<T> = emptyList(),val pagina:Int=0,val tamanio:Int=20,val totalElementos:Long=0,val totalPaginas:Int=0,val ultima:Boolean=true)
fun LoteDto.toDomain()=Lote(id,codigo,nombre,hectareas,cultivo,valle,campaniaCerrada,dosisMaximaPorHectarea)
fun LaborDto.toDomain()=Labor(id,loteId,TipoLabor.valueOf(tipo),fecha,responsable,cantidad,observacion)
