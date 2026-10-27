package pe.edu.upeu.agrosierragonzales.data.local
import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.agrosierragonzales.domain.model.*
interface LocalStore{
 fun observeLotes():Flow<List<Lote>>
 fun observeLabores(id:Long):Flow<List<Labor>>
 suspend fun saveLotes(x:List<Lote>)
 suspend fun saveLabores(x:List<Labor>)
 suspend fun addPending(x:Labor):Labor
 suspend fun pending():List<Labor>
 suspend fun markSynced(localId:Long,x:Labor)
 suspend fun deleteLabor(id:Long)
}
