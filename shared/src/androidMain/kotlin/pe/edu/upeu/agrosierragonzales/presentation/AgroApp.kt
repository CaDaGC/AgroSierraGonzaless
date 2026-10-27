package pe.edu.upeu.agrosierragonzales.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import pe.edu.upeu.agrosierragonzales.domain.model.Labor
import pe.edu.upeu.agrosierragonzales.domain.model.TipoLabor

@Composable
fun AgroApp() {
    val vm: AppViewModel = remember { org.koin.core.context.GlobalContext.get().get() }
    val state by vm.state.collectAsState()
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            if (state.sesion == null) LoginScreen(state, vm) else MainScreen(state, vm)
        }
    }
}

@Composable
private fun LoginScreen(state: AppUiState, vm: AppViewModel) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("AgroSierra", style = MaterialTheme.typography.headlineLarge)
        Text("Ingresa con tu cuenta de campo")
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(usuario, { usuario = it }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(contrasena, { contrasena = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        state.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { vm.login(usuario, contrasena) }, enabled = !state.cargando, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.cargando) "Ingresando…" else "Iniciar sesión")
        }
    }
}

@Composable
private fun MainScreen(state: AppUiState, vm: AppViewModel) {
    var loteAbierto by remember { mutableStateOf<Long?>(null) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var laborEditar by remember { mutableStateOf<Labor?>(null) }
    var laborEliminar by remember { mutableStateOf<Labor?>(null) }
    var confirmarCierre by remember { mutableStateOf(false) }
    var ultimaSync by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state.sincronizando, state.mensaje) { if (!state.sincronizando && state.mensaje?.startsWith("Sincronización:") == true) ultimaSync = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) }
    var busqueda by remember { mutableStateOf("") }
    var filtroValle by remember { mutableStateOf("") }
    val permisos = state.sesion?.privilegios ?: emptySet()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("AgroSierra", style = MaterialTheme.typography.titleLarge)
                Text(state.sesion?.nombre.orEmpty())
                Text("Perfil: ${state.sesion?.rol.orEmpty()}")
            }
            TextButton(onClick = vm::logout) { Text("Cerrar sesión") }
        }
        Row {
            Button(onClick = vm::refresh) { Text("Actualizar lotes") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = vm::sync, enabled = !state.sincronizando) {
                Text(if (state.sincronizando) "Sincronizando…" else "Sincronizar")
            }
        }
        if (state.sincronizando) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.mensaje?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        Text("Última sincronización: ${ultimaSync ?: "Aún no sincronizado"}", style = MaterialTheme.typography.bodySmall)
        if (loteAbierto == null) {
            OutlinedTextField(busqueda, { busqueda = it }, label = { Text("Buscar código, lote o cultivo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(filtroValle, { filtroValle = it }, label = { Text("Filtrar por valle") }, modifier = Modifier.fillMaxWidth())
            Text("Lotes disponibles", style = MaterialTheme.typography.titleLarge)
            val filtrados = state.lotes.filter {
                (busqueda.isBlank() || "${it.codigo} ${it.nombre} ${it.cultivo}".contains(busqueda, true)) &&
                    (filtroValle.isBlank() || it.valle.contains(filtroValle, true))
            }
            LazyColumn {
                items(filtrados, key = { it.id }) { lote ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { loteAbierto = lote.id; vm.select(lote) }) {
                        Column(Modifier.padding(14.dp)) {
                            Text("${lote.codigo} · ${lote.nombre}", style = MaterialTheme.typography.titleMedium)
                            Text("${lote.cultivo} · ${lote.valle} · ${lote.hectareas} ha")
                            Text(if (lote.campaniaCerrada) "Campaña cerrada" else "Campaña activa")
                        }
                    }
                }
            }
        } else {
            TextButton(onClick = { loteAbierto = null }) { Text("← Volver a lotes") }
            val lote = state.loteSeleccionado
            Text(lote?.nombre.orEmpty(), style = MaterialTheme.typography.headlineSmall)
            Text("${lote?.cultivo.orEmpty()} · ${lote?.valle.orEmpty()} · ${lote?.hectareas ?: 0.0} ha")
            Text(if (lote?.campaniaCerrada == true) "Campaña cerrada" else "Campaña activa")
            if ("LABOR_CREAR" in permisos && lote?.campaniaCerrada == false) {
                Button(onClick = { laborEditar = null; mostrarFormulario = true }) { Text("Registrar labor") }
            }
            if ("CAMPANIA_CERRAR" in permisos && lote?.campaniaCerrada == false) {
                TextButton(onClick = { confirmarCierre = true }) { Text("Cerrar campaña") }
            }
            Text("Labores", style = MaterialTheme.typography.titleLarge)
            LazyColumn {
                items(state.labores, key = { it.id }) { labor ->
                    ListItem(
                        headlineContent = { Text(labor.tipo.name) },
                        supportingContent = { Text("${labor.fecha} · ${labor.cantidad} · ${labor.responsable}\n${labor.observacion}") },
                        trailingContent = {
                            Row {
                                if (labor.pendiente) Text("Pendiente")
                                if ("LABOR_EDITAR" in permisos) TextButton(onClick = { laborEditar = labor; mostrarFormulario = true }) { Text("Editar") }
                                if ("LABOR_ELIMINAR" in permisos) TextButton(onClick = { laborEliminar = labor }) { Text("Eliminar") }
                            }
                        }
                    )
                }
            }
            if (confirmarCierre && lote != null) AlertDialog(onDismissRequest = { confirmarCierre = false }, title = { Text("Cerrar campaña") }, text = { Text("¿Confirmas el cierre de la campaña del lote ${lote.codigo}?") }, confirmButton = { TextButton(onClick = { vm.closeCampaign(lote.id); confirmarCierre = false }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { confirmarCierre = false }) { Text("Cancelar") } })
            if (mostrarFormulario) LaborDialog(lote?.id ?: 0L, vm, laborEditar) { mostrarFormulario = false; laborEditar = null }
            laborEliminar?.let { labor ->
                AlertDialog(
                    onDismissRequest = { laborEliminar = null },
                    title = { Text("Eliminar labor") },
                    text = { Text("¿Eliminar el registro ${labor.tipo} del ${labor.fecha}?") },
                    confirmButton = { TextButton(onClick = { vm.deleteLabor(labor); laborEliminar = null }) { Text("Eliminar") } },
                    dismissButton = { TextButton(onClick = { laborEliminar = null }) { Text("Cancelar") } }
                )
            }
        }
    }
}

@Composable
private fun LaborDialog(loteId: Long, vm: AppViewModel, existente: Labor?, cerrar: () -> Unit) {
    var tipo by remember(existente) { mutableStateOf(existente?.tipo ?: TipoLabor.RIEGO) }
    var fecha by remember(existente) { mutableStateOf(existente?.fecha ?: "2026-10-27") }
    var cantidad by remember(existente) { mutableStateOf(existente?.cantidad?.toString() ?: "") }
    var observacion by remember(existente) { mutableStateOf(existente?.observacion ?: "") }
    AlertDialog(
        onDismissRequest = cerrar,
        title = { Text(if (existente == null) "Registrar labor" else "Editar labor") },
        text = {
            Column {
                Row { TipoLabor.entries.forEach { item -> TextButton(onClick = { tipo = item }) { Text(item.name) } } }
                OutlinedTextField(fecha, { fecha = it }, label = { Text("Fecha AAAA-MM-DD") })
                OutlinedTextField(cantidad, { cantidad = it }, label = { Text("Cantidad") })
                OutlinedTextField(observacion, { observacion = it }, label = { Text("Observación") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                cantidad.toDoubleOrNull()?.let { amount ->
                    if (existente == null) vm.createLabor(loteId, tipo, fecha, amount, observacion)
                    else vm.editLabor(existente.copy(tipo = tipo, fecha = fecha, cantidad = amount, observacion = observacion))
                    cerrar()
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = cerrar) { Text("Cancelar") } }
    )
}




