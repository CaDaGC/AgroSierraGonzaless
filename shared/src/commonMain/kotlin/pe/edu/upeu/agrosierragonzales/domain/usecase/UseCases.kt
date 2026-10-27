package pe.edu.upeu.agrosierragonzales.domain.usecase
import pe.edu.upeu.agrosierragonzales.domain.model.Labor
import pe.edu.upeu.agrosierragonzales.domain.repository.*
class LoginUseCase(private val r:AuthRepository){suspend operator fun invoke(u:String,p:String)=r.login(u,p)}
class ObserveLotesUseCase(private val r:AgroRepository){operator fun invoke()=r.observeLotes()}
class RefreshLotesUseCase(private val r:AgroRepository){suspend operator fun invoke(v:String?=null)=r.refreshLotes(v)}
class CreateLaborUseCase(private val r:AgroRepository){suspend operator fun invoke(l:Labor)=r.createLabor(l)}
class SyncUseCase(private val r:AgroRepository){suspend operator fun invoke()=r.sync()}
