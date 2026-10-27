package pe.edu.upeu.agrosierragonzales.presentation

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.edu.upeu.agrosierragonzales.data.remote.ApiFailure
import pe.edu.upeu.agrosierragonzales.domain.error.ApiError
import pe.edu.upeu.agrosierragonzales.domain.model.*
import pe.edu.upeu.agrosierragonzales.domain.repository.*

data class AppUiState(
    val cargando:Boolean=false, val sesion:Sesion?=null, val lotes:List<Lote> = emptyList(),
    val loteSeleccionado:Lote?=null, val labores:List<Labor> = emptyList(),
    val mensaje:String?=null, val sincronizando:Boolean=false
)

class AppViewModel(private val auth:AuthRepository, private val agro:AgroRepository) {
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main)
    private val _state=MutableStateFlow(AppUiState())
    val state:StateFlow<AppUiState> = _state.asStateFlow()
    private var laboresJob:Job?=null

    init {
        scope.launch {
            auth.currentSession()?.let { session ->
                _state.update { it.copy(sesion=session) }
                sync()
            }
            agro.observeLotes().collect { items -> _state.update { it.copy(lotes=items) } }
        }
    }

    fun login(usuario:String,contrasena:String)=scope.launch {
        _state.update { it.copy(cargando=true,mensaje=null) }
        auth.login(usuario,contrasena)
            .onSuccess { session -> _state.update { it.copy(sesion=session) }; sync() }
            .onFailure { error -> _state.update { it.copy(mensaje=error.message ?: "No se pudo iniciar sesión") } }
        _state.update { it.copy(cargando=false) }
    }

    fun logout()=scope.launch { laboresJob?.cancel(); auth.logout(); _state.value=AppUiState() }

    fun select(lote:Lote) {
        laboresJob?.cancel()
        _state.update { it.copy(loteSeleccionado=lote,labores=emptyList()) }
        laboresJob=scope.launch {
            agro.observeLabores(lote.id).collect { items -> _state.update { it.copy(labores=items) } }
        }
    }

    fun refresh()=scope.launch {
        agro.refreshLotes().onFailure(::handleError)
    }

    fun createLabor(loteId:Long,tipo:TipoLabor,fecha:String,cantidad:Double,observacion:String)=scope.launch {
        val actor=_state.value.sesion?.nombre.orEmpty()
        agro.createLabor(Labor(0,loteId,tipo,fecha,actor,cantidad,observacion)).onFailure(::handleError)
    }

    fun editLabor(labor:Labor)=scope.launch { agro.editLabor(labor).onFailure(::handleError) }
    fun deleteLabor(labor:Labor)=scope.launch { agro.deleteLabor(labor).onFailure(::handleError) }

    fun closeCampaign(loteId:Long)=scope.launch {
        agro.closeCampaign(loteId).onSuccess {
            _state.update { it.copy(mensaje="Campaña cerrada correctamente") }
            refresh()
        }.onFailure(::handleError)
    }

    fun sync()=scope.launch {
        _state.update { it.copy(sincronizando=true,mensaje=null) }
        agro.sync("LABOR_LEER" in (_state.value.sesion?.privilegios ?: emptySet()))
            .onSuccess { report -> _state.update { it.copy(mensaje="Sincronización: ${report.enviados} pendientes enviados; ${report.descargados} lotes") } }
            .onFailure(::handleError)
        _state.update { it.copy(sincronizando=false) }
    }

    private fun handleError(error:Throwable) {
        if ((error as? ApiFailure)?.apiError == ApiError.Unauthorized) {
            scope.launch {
                auth.logout()
                _state.value=AppUiState(mensaje="La sesión venció. Inicia sesión nuevamente.")
            }
        } else {
            _state.update { it.copy(mensaje=error.message ?: "Ocurrió un error") }
        }
    }
}

