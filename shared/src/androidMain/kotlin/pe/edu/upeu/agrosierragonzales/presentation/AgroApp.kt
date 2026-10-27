package pe.edu.upeu.agrosierragonzales.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.edu.upeu.agrosierragonzales.domain.model.Labor
import pe.edu.upeu.agrosierragonzales.domain.model.Lote
import pe.edu.upeu.agrosierragonzales.domain.model.TipoLabor

private val AgroGreen = Color(0xFF245B3A)
private val AgroGreenDark = Color(0xFF173E2A)
private val AgroMint = Color(0xFFE7F1E8)
private val AgroCream = Color(0xFFF6F7F1)
private val AgroInk = Color(0xFF1E2B22)

private enum class MainDestination(val title: String) {
    HOME("Inicio"), LOTES("Mis lotes")
}

@Composable
fun AgroApp() {
    val vm: AppViewModel = remember { org.koin.core.context.GlobalContext.get().get() }
    val state by vm.state.collectAsState()
    val colors = lightColorScheme(
        primary = AgroGreen,
        onPrimary = Color.White,
        secondary = Color(0xFFB97837),
        background = AgroCream,
        surface = Color.White,
        onSurface = AgroInk,
        surfaceVariant = AgroMint
    )
    MaterialTheme(colorScheme = colors) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (state.sesion == null) LoginScreen(state, vm) else MainScreen(state, vm)
        }
    }
}

@Composable
private fun LoginScreen(state: AppUiState, vm: AppViewModel) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = RoundedCornerShape(24.dp), color = AgroGreen, modifier = Modifier.size(76.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("AS", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("AgroSierra", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = AgroGreenDark)
            Text("Gestión agrícola en un solo lugar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Bienvenido", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("Ingresa con tu cuenta de campo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(usuario, { usuario = it }, label = { Text("Usuario") }, singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(contrasena, { contrasena = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    state.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    Button(onClick = { vm.login(usuario, contrasena) }, enabled = !state.cargando, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(if (state.cargando) "Ingresando…" else "Iniciar sesión")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(state: AppUiState, vm: AppViewModel) {
    var destination by remember { mutableStateOf(MainDestination.HOME) }
    var loteAbierto by remember { mutableStateOf<Long?>(null) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var laborEditar by remember { mutableStateOf<Labor?>(null) }
    var laborEliminar by remember { mutableStateOf<Labor?>(null) }
    var confirmarCierre by remember { mutableStateOf(false) }
    var ultimaSync by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var filtroValle by remember { mutableStateOf("") }
    val permisos = state.sesion?.privilegios ?: emptySet()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    LaunchedEffect(state.sincronizando, state.mensaje) {
        if (!state.sincronizando && state.mensaje?.startsWith("Sincronización:") == true) {
            ultimaSync = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        }
    }

    fun volver() {
        loteAbierto = null
        destination = MainDestination.LOTES
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color.White) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 26.dp)) {
                    Text("AGROSIERRA", style = MaterialTheme.typography.labelLarge, color = AgroGreen, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(state.sesion?.nombre.orEmpty(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Perfil ${state.sesion?.rol.orEmpty()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
                MainDestination.entries.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(if (item == MainDestination.HOME) "⌂   ${item.title}" else "▦   ${item.title}") },
                        selected = destination == item && loteAbierto == null,
                        onClick = {
                            destination = item
                            loteAbierto = null
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                TextButton(onClick = { vm.logout(); scope.launch { drawerState.close() } }, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    ) {
        Scaffold(
            containerColor = AgroCream,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(if (loteAbierto != null) "Detalle del lote" else destination.title, fontWeight = FontWeight.SemiBold)
                            Text("AgroSierra · ${state.sesion?.rol.orEmpty()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    navigationIcon = {
                        TextButton(onClick = {
                            if (loteAbierto != null) volver() else scope.launch { drawerState.open() }
                        }) {
                            Text(if (loteAbierto != null) "‹" else "☰", style = MaterialTheme.typography.headlineSmall, color = AgroGreen)
                        }
                    },
                    actions = {
                        TextButton(onClick = vm::sync, enabled = !state.sincronizando) {
                            Text(if (state.sincronizando) "Sync…" else "Sync", color = AgroGreen)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AgroCream)
                )
            }
        ) { innerPadding ->
            Column(Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 18.dp)) {
                state.mensaje?.let { MessageBanner(it) }
                if (state.sincronizando) {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(bottom = 10.dp), color = AgroGreen)
                }
                when {
                    loteAbierto != null -> {
                        val lote = state.loteSeleccionado
                        if (lote != null) LotDetailScreen(
                            lote = lote,
                            labores = state.labores,
                            permisos = permisos,
                            onCreateLabor = { laborEditar = null; mostrarFormulario = true },
                            onEditLabor = { laborEditar = it; mostrarFormulario = true },
                            onDeleteLabor = { laborEliminar = it },
                            onCloseCampaign = { confirmarCierre = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    destination == MainDestination.HOME -> HomeScreen(
                        state = state,
                        ultimaSync = ultimaSync,
                        onOpenLots = { destination = MainDestination.LOTES },
                        onRefresh = vm::refresh,
                        modifier = Modifier.weight(1f)
                    )
                    else -> LotsScreen(
                        lotes = state.lotes,
                        busqueda = busqueda,
                        onBusqueda = { busqueda = it },
                        filtroValle = filtroValle,
                        onFiltroValle = { filtroValle = it },
                        onSelect = { loteAbierto = it.id; vm.select(it) },
                        onRefresh = vm::refresh,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    val lote = state.loteSeleccionado
    if (confirmarCierre && lote != null) {
        AlertDialog(
            onDismissRequest = { confirmarCierre = false },
            title = { Text("Cerrar campaña") },
            text = { Text("¿Confirmas el cierre de la campaña del lote ${lote.codigo}?") },
            confirmButton = { TextButton(onClick = { vm.closeCampaign(lote.id); confirmarCierre = false }) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = { confirmarCierre = false }) { Text("Cancelar") } }
        )
    }
    if (mostrarFormulario && lote != null) LaborDialog(lote.id, vm, laborEditar) { mostrarFormulario = false; laborEditar = null }
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

@Composable
private fun HomeScreen(state: AppUiState, ultimaSync: String?, onOpenLots: () -> Unit, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(top = 12.dp)) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AgroGreen), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp)) {
                Text("BUEN DÍA", style = MaterialTheme.typography.labelLarge, color = Color(0xFFD6E8D8), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(state.sesion?.nombre.orEmpty(), style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Revisa tus campos y mantén la información al día.", color = Color.White.copy(alpha = .88f))
                Spacer(Modifier.height(18.dp))
                Button(onClick = onOpenLots, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AgroGreenDark), shape = RoundedCornerShape(14.dp)) {
                    Text("Explorar mis lotes  →")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Tu resumen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("Lotes", state.lotes.size.toString(), "registrados", Modifier.weight(1f))
            SummaryCard("Campañas", state.lotes.count { !it.campaniaCerrada }.toString(), "activas", Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Sincronización", fontWeight = FontWeight.SemiBold)
                    Text("Última: ${ultimaSync ?: "Aún no sincronizado"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onRefresh) { Text("Actualizar") }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Acceso rápido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenLots, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Text("▦   Ver lotes y labores")
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, caption: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(5.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = AgroGreen, fontWeight = FontWeight.Bold)
            Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LotsScreen(
    lotes: List<Lote>, busqueda: String, onBusqueda: (String) -> Unit,
    filtroValle: String, onFiltroValle: (String) -> Unit,
    onSelect: (Lote) -> Unit, onRefresh: () -> Unit, modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tus unidades de cultivo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${lotes.size} lotes disponibles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onRefresh) { Text("Actualizar") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(busqueda, onBusqueda, label = { Text("Buscar lote, código o cultivo") }, singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(filtroValle, onFiltroValle, label = { Text("Filtrar por valle") }, singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp))
        val filtrados = lotes.filter {
            (busqueda.isBlank() || "${it.codigo} ${it.nombre} ${it.cultivo}".contains(busqueda, true)) &&
                (filtroValle.isBlank() || it.valle.contains(filtroValle, true))
        }
        if (filtrados.isEmpty()) {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Text("No encontramos lotes con esos filtros.", Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else LazyColumn(contentPadding = PaddingValues(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtrados, key = { it.id }) { lote ->
                Card(Modifier.fillMaxWidth().clickable { onSelect(lote) }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(14.dp), color = AgroMint, modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text("▦", color = AgroGreen, style = MaterialTheme.typography.titleLarge) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${lote.codigo} · ${lote.nombre}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("${lote.cultivo} · ${lote.valle}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${lote.hectareas} ha", style = MaterialTheme.typography.labelMedium, color = AgroGreen)
                        }
                        Text("›", style = MaterialTheme.typography.headlineMedium, color = AgroGreen)
                    }
                    Surface(color = if (lote.campaniaCerrada) Color(0xFFF4E8E2) else AgroMint, shape = RoundedCornerShape(50), modifier = Modifier.padding(start = 16.dp, bottom = 14.dp)) {
                        Text(if (lote.campaniaCerrada) "Campaña cerrada" else "Campaña activa", Modifier.padding(horizontal = 11.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = if (lote.campaniaCerrada) Color(0xFF8D4931) else AgroGreenDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun LotDetailScreen(
    lote: Lote, labores: List<Labor>, permisos: Set<String>,
    onCreateLabor: () -> Unit, onEditLabor: (Labor) -> Unit,
    onDeleteLabor: (Labor) -> Unit, onCloseCampaign: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().padding(top = 8.dp)) {
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = AgroGreen), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text(lote.codigo, color = Color(0xFFD6E8D8), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Text(lote.nombre, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${lote.cultivo} · ${lote.valle}", color = Color.White.copy(alpha = .9f))
                Spacer(Modifier.height(12.dp))
                Text("${lote.hectareas} ha  ·  ${if (lote.campaniaCerrada) "Campaña cerrada" else "Campaña activa"}", color = Color.White)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if ("LABOR_CREAR" in permisos && !lote.campaniaCerrada) Button(onClick = onCreateLabor, shape = RoundedCornerShape(12.dp)) { Text("+ Registrar labor") }
            if ("CAMPANIA_CERRAR" in permisos && !lote.campaniaCerrada) OutlinedButton(onClick = onCloseCampaign, shape = RoundedCornerShape(12.dp)) { Text("Cerrar campaña") }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Labores", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${labores.size} registros", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(6.dp))
        if (labores.isEmpty()) {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Todavía no hay labores registradas para este lote.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else LazyColumn(contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(labores, key = { it.id }) { labor ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(labor.tipo.name.replace('_', ' '), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            if (labor.pendiente) Text("Pendiente de sync", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text("${labor.fecha} · ${labor.cantidad} · ${labor.responsable}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (labor.observacion.isNotBlank()) Text(labor.observacion, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if ("LABOR_EDITAR" in permisos) TextButton(onClick = { onEditLabor(labor) }) { Text("Editar") }
                            if ("LABOR_ELIMINAR" in permisos) TextButton(onClick = { onDeleteLabor(labor) }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBanner(message: String) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AgroMint), modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
        Text(message, Modifier.padding(horizontal = 14.dp, vertical = 10.dp), style = MaterialTheme.typography.bodySmall, color = AgroGreenDark)
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
