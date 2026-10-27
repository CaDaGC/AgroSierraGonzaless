package pe.edu.upeu.agrosierragonzales
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.*
import kotlin.test.*
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.agrosierragonzales.data.local.LocalStore
import pe.edu.upeu.agrosierragonzales.data.local.db.AgroDatabase
import pe.edu.upeu.agrosierragonzales.data.remote.*
import pe.edu.upeu.agrosierragonzales.data.repository.AgroRepositoryImpl
import pe.edu.upeu.agrosierragonzales.domain.model.*
import pe.edu.upeu.agrosierragonzales.domain.repository.*
import pe.edu.upeu.agrosierragonzales.domain.error.ApiError

class MobileRequirementsTest {
 private val labor=Labor(0,1,TipoLabor.RIEGO,"2026-10-27","Ana",2.0,"turno mañana")
 @Test fun localDatabaseStoresPendingLabor(){
  val driver=JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
  AgroDatabase.Schema.create(driver)
  val db=AgroDatabase(driver)
  db.laborQueries.insertLabor(-1,1,"RIEGO","2026-10-27","Ana",2.0,"turno mañana",1,"CREATE")
  assertEquals(-1L,db.laborQueries.selectPending().executeAsOne().id)
  driver.close()
 }
 @Test fun offlineCreateKeepsTemporaryPendingRecord()=runTest {
  val store=MemoryStore()
  val engine=MockEngine{throw IOException("offline")}
  val repo=AgroRepositoryImpl(AgroApi(createHttpClient(engine){null}),store)
  val saved=repo.createLabor(labor).getOrThrow()
  assertTrue(saved.id<0);assertTrue(saved.pendiente);assertEquals(1,store.rows.size)
 }
 @Test fun offlineEditsAndDeletesRemainQueued()=runTest {
  val store=MemoryStore();val engine=MockEngine{throw IOException("offline")};val repo=AgroRepositoryImpl(AgroApi(createHttpClient(engine){null}),store)
  val existing=labor.copy(id=12,pendiente=false);val edited=repo.editLabor(existing.copy(observacion="corregida")).getOrThrow()
  assertTrue(edited.pendiente);assertEquals(SyncOperation.UPDATE,edited.syncOperation)
  repo.deleteLabor(edited).getOrThrow();assertEquals(SyncOperation.DELETE,store.rows.single().syncOperation)
 }
 @Test fun syncPushesPendingBeforeRefreshingFromServer()=runTest {
  val store=MemoryStore().apply{rows+=labor.copy(id=-1,pendiente=true)}
  val requests=mutableListOf<String>()
  val engine=MockEngine { request ->
   requests+=request.method.value+" "+request.url.encodedPath
   val json=when(request.url.encodedPath){
    "/api/v1/labores"->"""{"id":20,"loteId":1,"tipo":"RIEGO","fecha":"2026-10-27","responsable":"Ana","cantidad":2.0,"observacion":"turno mañana"}"""
    "/api/v1/lotes"->"""{"contenido":[{"id":1,"codigo":"L1","nombre":"Sur","hectareas":2.0,"cultivo":"Café","valle":"Majes","campaniaCerrada":false,"dosisMaximaPorHectarea":5.0}],"pagina":0,"tamanio":100,"totalElementos":1,"totalPaginas":1,"ultima":true}"""
    else->"""{"contenido":[],"pagina":0,"tamanio":100,"totalElementos":0,"totalPaginas":0,"ultima":true}"""
   }
   respond(json,HttpStatusCode.OK,headersOf(HttpHeaders.ContentType,"application/json"))
  }
  val repo=AgroRepositoryImpl(AgroApi(createHttpClient(engine){null}),store)
  repo.sync().getOrThrow()
  assertTrue(requests.first().startsWith("POST /api/v1/labores"))
  assertTrue(requests.indexOfFirst{it=="GET /api/v1/lotes"}>0)
 }
 @Test fun httpFailuresAreMappedToTypedErrors(){
  assertEquals(ApiError.Unauthorized,mapHttpError(401))
  assertEquals(ApiError.Forbidden,mapHttpError(403))
  assertIs<ApiError.Validation>(mapHttpError(400,"validación"))
 }
 private class MemoryStore:LocalStore{
  val rows=mutableListOf<Labor>()
  override fun observeLotes():Flow<List<Lote>> = flowOf(emptyList())
  override fun observeLabores(id:Long):Flow<List<Labor>> = flowOf(rows.filter{it.loteId==id})
  override suspend fun saveLotes(x:List<Lote>){}
  override suspend fun saveLabores(x:List<Labor>){rows.removeAll{it.id>0};rows.addAll(x)}
  override suspend fun addPending(x:Labor):Labor{val next=(rows.filter{it.id<0}.minOfOrNull{it.id}?:0)-1;return x.copy(id=next,pendiente=true).also{rows+=it}}
  override suspend fun pending()=rows.filter{it.pendiente}
  override suspend fun markSynced(localId:Long,x:Labor){rows.removeAll{it.id==localId};rows+=x.copy(pendiente=false)}
  override suspend fun deleteLabor(id:Long){rows.removeAll{it.id==id}}
 }
}



