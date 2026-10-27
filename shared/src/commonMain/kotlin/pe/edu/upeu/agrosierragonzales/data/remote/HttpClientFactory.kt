package pe.edu.upeu.agrosierragonzales.data.remote
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
fun createHttpClient(engine:HttpClientEngine,token:()->String?)=HttpClient(engine){
 expectSuccess=false
 install(ContentNegotiation){json(Json{ignoreUnknownKeys=true;isLenient=true;explicitNulls=false})}
 install(HttpTimeout){requestTimeoutMillis=15_000;connectTimeoutMillis=10_000;socketTimeoutMillis=15_000}
 defaultRequest { url("http://10.0.2.2:8080/");token()?.takeIf{it.isNotBlank()}?.let{headers.append("Authorization","Bearer $it")} }
}
