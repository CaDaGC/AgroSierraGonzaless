package pe.edu.upeu.agrosierragonzales
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pe.edu.upeu.agrosierragonzales.presentation.AgroApp
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{AgroApp()}}}
