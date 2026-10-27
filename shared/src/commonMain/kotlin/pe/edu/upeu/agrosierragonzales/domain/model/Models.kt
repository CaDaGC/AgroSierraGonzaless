package pe.edu.upeu.agrosierragonzales.domain.model
enum class TipoLabor { RIEGO, FUMIGACION, COSECHA }
enum class SyncOperation { CREATE, UPDATE, DELETE }
data class Lote(val id:Long,val codigo:String,val nombre:String,val hectareas:Double,val cultivo:String,val valle:String,val campaniaCerrada:Boolean,val dosisMaximaPorHectarea:Double,val ultimaActualizacion:String="")
data class Labor(val id:Long,val loteId:Long,val tipo:TipoLabor,val fecha:String,val responsable:String,val cantidad:Double,val observacion:String,val pendiente:Boolean=false,val syncOperation:SyncOperation=SyncOperation.CREATE)
data class Sesion(val token:String,val usuario:String,val nombre:String,val rol:String,val privilegios:Set<String>)
