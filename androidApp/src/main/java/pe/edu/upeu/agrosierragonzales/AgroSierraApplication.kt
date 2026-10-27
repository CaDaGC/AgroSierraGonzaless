package pe.edu.upeu.agrosierragonzales
import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import pe.edu.upeu.agrosierragonzales.di.androidModules
import pe.edu.upeu.agrosierragonzales.presentation.AppViewModel
class AgroSierraApplication:Application(){
 override fun onCreate(){
  super.onCreate()
  startKoin{androidContext(this@AgroSierraApplication);modules(androidModules)}
  val vm=GlobalContext.get().get<AppViewModel>()
  val connectivity=getSystemService(ConnectivityManager::class.java)
  var wasConnected=false
  connectivity.registerDefaultNetworkCallback(object:ConnectivityManager.NetworkCallback(){
   override fun onAvailable(network:Network){
    if(wasConnected.not() && vm.state.value.sesion!=null) vm.sync()
    wasConnected=true
   }
   override fun onLost(network:Network){wasConnected=false}
  })
 }
}
