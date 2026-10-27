package pe.edu.upeu.agrosierragonzales.domain.repository
import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.agrosierragonzales.domain.model.*
interface AuthRepository{suspend fun login(usuario:String,contrasena:String):Result<Sesion>;suspend fun currentSession():Sesion?;suspend fun logout()}
interface AgroRepository{
 fun observeLotes():Flow<List<Lote>>
 fun observeLabores(id:Long):Flow<List<Labor>>
 suspend fun refreshLotes(valle:String?=null):Result<Unit>
 suspend fun createLabor(labor:Labor):Result<Labor>
 suspend fun editLabor(labor:Labor):Result<Labor>
 suspend fun deleteLabor(labor:Labor):Result<Unit>
 suspend fun closeCampaign(loteId:Long):Result<Unit>
 suspend fun sync(canReadLabor:Boolean=true):Result<SyncReport>
}
data class SyncReport(val enviados:Int,val descargados:Int)

