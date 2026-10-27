package pe.edu.upeu.agrosierragonzales.data.repository

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.agrosierragonzales.data.local.LocalStore
import pe.edu.upeu.agrosierragonzales.data.remote.*
import pe.edu.upeu.agrosierragonzales.data.remote.dto.*
import pe.edu.upeu.agrosierragonzales.domain.model.*
import pe.edu.upeu.agrosierragonzales.domain.repository.*

class AgroRepositoryImpl(private val api:AgroApi, private val db:LocalStore):AgroRepository {
    override fun observeLotes():Flow<List<Lote>> = db.observeLotes()
    override fun observeLabores(id:Long):Flow<List<Labor>> = db.observeLabores(id)

    override suspend fun refreshLotes(valle:String?) =
        runCatching { db.saveLotes(api.lotes(valle).map { it.toDomain() }) }.mapFailure()

    override suspend fun createLabor(labor:Labor):Result<Labor> {
        val local=try { db.addPending(labor.copy(pendiente=true,syncOperation=SyncOperation.CREATE)) }
        catch(e:Throwable) { return Result.failure(e) }
        return try {
            val remote=api.createLabor(local.request()).toDomain()
            db.markSynced(local.id,remote)
            Result.success(remote)
        } catch(e:Throwable) {
            if(isNetworkFailure(e)) Result.success(local)
            else { db.deleteLabor(local.id);Result.failure(e) }
        }
    }

    override suspend fun editLabor(labor:Labor):Result<Labor> = try {
        val result=if(labor.id<0 || (labor.pendiente && labor.syncOperation==SyncOperation.CREATE)) {
            labor.copy(pendiente=true,syncOperation=SyncOperation.CREATE)
        } else {
            try {
                api.editLabor(labor.id,labor.request()).toDomain().also { db.saveLabores(listOf(it)) }
            } catch(e:Throwable) {
                if(!isNetworkFailure(e)) throw e
                labor.copy(pendiente=true,syncOperation=SyncOperation.UPDATE).also { db.saveLabores(listOf(it)) }
            }
        }
        if(result.id<0) db.saveLabores(listOf(result))
        Result.success(result)
    } catch(e:Throwable) { Result.failure(e) }

    override suspend fun deleteLabor(labor:Labor):Result<Unit> = try {
        if(labor.id<0 || (labor.pendiente && labor.syncOperation==SyncOperation.CREATE)) {
            db.deleteLabor(labor.id)
        } else {
            try {
                api.deleteLabor(labor.id)
                db.deleteLabor(labor.id)
            } catch(e:Throwable) {
                if(!isNetworkFailure(e)) throw e
                db.saveLabores(listOf(labor.copy(pendiente=true,syncOperation=SyncOperation.DELETE)))
            }
        }
        Result.success(Unit)
    } catch(e:Throwable) { Result.failure(e) }

    override suspend fun closeCampaign(loteId:Long):Result<Unit> = try {
        val updated=api.closeCampaign(loteId).toDomain()
        db.saveLotes(listOf(updated))
        Result.success(Unit)
    } catch(e:Throwable) { Result.failure(e) }

    override suspend fun sync(canReadLabor:Boolean)=runCatching {
        var sent=0
        db.pending().forEach { item ->
            when(item.syncOperation) {
                SyncOperation.CREATE -> {
                    val remote=api.createLabor(item.request()).toDomain()
                    db.markSynced(item.id,remote)
                }
                SyncOperation.UPDATE -> {
                    val remote=api.editLabor(item.id,item.request()).toDomain()
                    db.markSynced(item.id,remote)
                }
                SyncOperation.DELETE -> { api.deleteLabor(item.id);db.deleteLabor(item.id) }
            }
            sent++
        }
        val lots=api.lotes()
        db.saveLotes(lots.map { it.toDomain() })
        if(canReadLabor) lots.forEach { lot -> db.saveLabores(api.labores(lot.id).map { it.toDomain() }) }
        SyncReport(sent,lots.size)
    }.mapFailure()
}

private fun Labor.request()=LaborRequestDto(loteId,tipo.name,fecha,cantidad,observacion)
private fun isNetworkFailure(error:Throwable)=error is IOException || error is HttpRequestTimeoutException
private fun <T> Result<T>.mapFailure():Result<T> = fold(
    { Result.success(it) },
    { error ->
        Result.failure(when(error) {
            is ApiFailure -> error
            is HttpRequestTimeoutException -> IllegalStateException("Tiempo de espera agotado",error)
            is IOException -> IllegalStateException("Sin conexión",error)
            else -> error
        })
    }
)

