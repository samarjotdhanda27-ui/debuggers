package com.debuggers.reloop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debuggers.reloop.data.model.MaterialItem
import com.debuggers.reloop.data.repository.MaterialRepository
import com.debuggers.reloop.ui.components.ReLoopTopBar
import com.debuggers.reloop.ui.components.ServerDiagnosticsDialog
import com.debuggers.reloop.ui.screens.*
import com.debuggers.reloop.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReLoopTheme {
                var showSplash by remember { mutableStateOf(true) }

                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(durationMillis = 450),
                    label = "AppEntranceCrossfade"
                ) { isSplash ->
                    if (isSplash) {
                        ReLoopSplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    } else {
                        ReLoopAppRoot()
                    }
                }
            }
        }
    }
}

enum class AppDestination {
    HOME,
    EXPLORE,
    MAP,
    LIST_MATERIAL,
    PASSPORTS,
    PROFILE,
    MATERIAL_DETAIL,
    POST_REQUIREMENT
}

@Composable
fun ReLoopAppRoot() {
    val coroutineScope = rememberCoroutineScope()

    val materials by MaterialRepository.materials.collectAsState()
    val requirements by MaterialRepository.requirements.collectAsState()
    val quoteRequests by MaterialRepository.quoteRequests.collectAsState()
    val serverHealth by MaterialRepository.serverHealth.collectAsState()
    val isLoading by MaterialRepository.isLoading.collectAsState()

    var currentScreen by remember { mutableStateOf(AppDestination.HOME) }
    var previousScreen by remember { mutableStateOf(AppDestination.HOME) }
    var selectedMaterial by remember { mutableStateOf<MaterialItem?>(null) }
    var detailTab by remember { mutableStateOf(0) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var mapFocusMaterialId by remember { mutableStateOf<String?>(null) }

    // Initial background sync with live backend
    LaunchedEffect(Unit) {
        MaterialRepository.refreshAll()
    }

    // Hardware & Gesture Back Navigation Handling
    BackHandler(enabled = currentScreen != AppDestination.HOME) {
        if (currentScreen == AppDestination.MATERIAL_DETAIL || 
            currentScreen == AppDestination.POST_REQUIREMENT ||
            currentScreen == AppDestination.MAP) {
            currentScreen = previousScreen
        } else {
            currentScreen = AppDestination.HOME
        }
    }

    fun navigateTo(dest: AppDestination) {
        previousScreen = currentScreen
        currentScreen = dest
    }

    fun openMaterialDetails(item: MaterialItem, tab: Int = 0) {
        selectedMaterial = item
        detailTab = tab
        navigateTo(AppDestination.MATERIAL_DETAIL)
    }

    val bottomNavDestinations = listOf(
        AppDestination.HOME,
        AppDestination.EXPLORE,
        AppDestination.MAP,
        AppDestination.PASSPORTS,
        AppDestination.PROFILE
    )

    Scaffold(
        containerColor = ReLoopPageBg,
        topBar = {
            if (currentScreen in bottomNavDestinations && currentScreen != AppDestination.PASSPORTS) {
                ReLoopTopBar(
                    serverHealth = serverHealth,
                    currentLocation = "Punjab Hub",
                    onLocationClick = { 
                        mapFocusMaterialId = null
                        navigateTo(AppDestination.MAP) 
                    },
                    onHealthClick = { showDiagnosticsDialog = true },
                    onProfileClick = { navigateTo(AppDestination.PROFILE) }
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == AppDestination.HOME || currentScreen == AppDestination.EXPLORE) {
                ExtendedFloatingActionButton(
                    onClick = { navigateTo(AppDestination.LIST_MATERIAL) },
                    containerColor = BrandCobalt,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                    text = { Text("List Material", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        },
        bottomBar = {
            if (currentScreen in bottomNavDestinations) {
                ReLoopBottomBar(
                    currentScreen = currentScreen,
                    onSelectScreen = { dest ->
                        if (dest == AppDestination.MAP) {
                            mapFocusMaterialId = null
                        }
                        currentScreen = dest
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppDestination.HOME -> HomeScreen(
                    materials = materials,
                    requirements = requirements,
                    serverHealth = serverHealth,
                    isLoading = isLoading,
                    onRefresh = {
                        coroutineScope.launch {
                            MaterialRepository.refreshAll()
                        }
                    },
                    onListMaterial = { navigateTo(AppDestination.LIST_MATERIAL) },
                    onPostRequirement = { navigateTo(AppDestination.POST_REQUIREMENT) },
                    onOpenMaterial = { item -> openMaterialDetails(item, tab = 0) },
                    onViewPassport = { item -> openMaterialDetails(item, tab = 3) },
                    onNavigateToExplore = { currentScreen = AppDestination.EXPLORE },
                    onOpenMap = { matId ->
                        mapFocusMaterialId = matId
                        navigateTo(AppDestination.MAP)
                    }
                )

                AppDestination.EXPLORE -> DiscoverScreen(
                    materials = materials,
                    onOpenMaterial = { item -> openMaterialDetails(item, tab = 0) },
                    onViewPassport = { item -> openMaterialDetails(item, tab = 3) },
                    onOpenMap = { matId ->
                        mapFocusMaterialId = matId
                        navigateTo(AppDestination.MAP)
                    }
                )

                AppDestination.MAP -> InteractiveMapScreen(
                    materials = materials,
                    requirements = requirements,
                    initialMaterialId = mapFocusMaterialId,
                    onBack = { currentScreen = previousScreen },
                    onOpenMaterial = { item -> openMaterialDetails(item, tab = 0) },
                    onRequestQuote = { item -> openMaterialDetails(item, tab = 0) }
                )

                AppDestination.LIST_MATERIAL -> ListMaterialScreen(
                    onBack = { currentScreen = previousScreen.takeIf { it != AppDestination.LIST_MATERIAL } ?: AppDestination.HOME },
                    onSuccessCreated = {
                        val newest = MaterialRepository.materials.value.firstOrNull()
                        if (newest != null) {
                            openMaterialDetails(newest, tab = 0)
                        } else {
                            currentScreen = AppDestination.HOME
                        }
                    }
                )

                AppDestination.POST_REQUIREMENT -> PostRequirementScreen(
                    onBack = { currentScreen = previousScreen },
                    onSuccessSubmitted = {
                        currentScreen = AppDestination.HOME
                    }
                )

                AppDestination.PASSPORTS -> PassportScreen(
                    materials = materials,
                    initialMaterial = selectedMaterial,
                    onBack = { currentScreen = AppDestination.HOME }
                )

                AppDestination.PROFILE -> ProfileScreen(
                    serverHealth = serverHealth,
                    myListingsCount = materials.size,
                    myRequirementsCount = requirements.size,
                    quoteRequests = quoteRequests,
                    onListMaterial = { navigateTo(AppDestination.LIST_MATERIAL) },
                    onPostRequirement = { navigateTo(AppDestination.POST_REQUIREMENT) }
                )

                AppDestination.MATERIAL_DETAIL -> {
                    selectedMaterial?.let { item ->
                        MaterialDetailScreen(
                            item = item,
                            initialTab = detailTab,
                            onBack = { currentScreen = previousScreen },
                            onViewPassportFull = {
                                navigateTo(AppDestination.PASSPORTS)
                            },
                            onOpenMap = { matId ->
                                mapFocusMaterialId = matId
                                navigateTo(AppDestination.MAP)
                            }
                        )
                    } ?: run {
                        currentScreen = AppDestination.HOME
                    }
                }
            }
        }

        if (showDiagnosticsDialog) {
            ServerDiagnosticsDialog(
                health = serverHealth,
                onDismiss = { showDiagnosticsDialog = false },
                onRefresh = {
                    coroutineScope.launch {
                        MaterialRepository.checkServerStatus()
                    }
                }
            )
        }
    }
}

@Composable
fun ReLoopBottomBar(
    currentScreen: AppDestination,
    onSelectScreen: (AppDestination) -> Unit
) {
    Surface(
        color = Color.White,
        border = BorderStroke(0.5.dp, ReLoopBorder),
        shadowElevation = 3.dp
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            val navItems = listOf(
                Triple(AppDestination.HOME, Icons.Default.Home, "Home"),
                Triple(AppDestination.EXPLORE, Icons.Default.Search, "Explore"),
                Triple(AppDestination.MAP, Icons.Default.Map, "Map"),
                Triple(AppDestination.PASSPORTS, Icons.Default.QrCode2, "Passports"),
                Triple(AppDestination.PROFILE, Icons.Default.PersonOutline, "Account")
            )

            navItems.forEach { (destination, icon, label) ->
                val isSelected = currentScreen == destination
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectScreen(destination) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandCobalt,
                        selectedTextColor = BrandCobalt,
                        indicatorColor = BrandCobaltBg,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    }
}
