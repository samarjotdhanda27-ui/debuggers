package com.debuggers.reloop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.debuggers.reloop.data.model.MaterialItem
import com.debuggers.reloop.data.model.RequirementItem
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
                ReLoopMainApp()
            }
        }
    }
}

enum class AppScreen {
    HOME,
    DISCOVER,
    LIST_MATERIAL,
    POST_REQUIREMENT,
    MATERIAL_DETAIL,
    PASSPORT_FULL,
    PROFILE
}

@Composable
fun ReLoopMainApp() {
    val coroutineScope = rememberCoroutineScope()

    val materials by MaterialRepository.materials.collectAsState()
    val requirements by MaterialRepository.requirements.collectAsState()
    val serverHealth by MaterialRepository.serverHealth.collectAsState()
    val isLoading by MaterialRepository.isLoading.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var previousScreen by remember { mutableStateOf(AppScreen.HOME) }
    var selectedMaterial by remember { mutableStateOf<MaterialItem?>(null) }
    var detailInitialTab by remember { mutableStateOf(0) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }

    // Initial background sync with live backend
    LaunchedEffect(Unit) {
        MaterialRepository.refreshAll()
    }

    fun navigateTo(screen: AppScreen) {
        previousScreen = currentScreen
        currentScreen = screen
    }

    fun openMaterialDetails(item: MaterialItem, initialTab: Int = 0) {
        selectedMaterial = item
        detailInitialTab = initialTab
        navigateTo(AppScreen.MATERIAL_DETAIL)
    }

    val topLevelScreens = listOf(AppScreen.HOME, AppScreen.DISCOVER, AppScreen.PROFILE)

    Scaffold(
        containerColor = ReLoopPageBg,
        topBar = {
            if (currentScreen in topLevelScreens) {
                ReLoopTopBar(
                    serverHealth = serverHealth,
                    onHealthClick = { showDiagnosticsDialog = true },
                    onProfileClick = { navigateTo(AppScreen.PROFILE) }
                )
            }
        },
        bottomBar = {
            if (currentScreen in topLevelScreens) {
                ReLoopBottomBar(
                    currentScreen = currentScreen,
                    materialsCount = materials.size,
                    requirementsCount = requirements.size,
                    onSelectScreen = { currentScreen = it }
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
                AppScreen.HOME -> HomeScreen(
                    materials = materials,
                    requirements = requirements,
                    serverHealth = serverHealth,
                    isLoading = isLoading,
                    onRefresh = {
                        coroutineScope.launch {
                            MaterialRepository.refreshAll()
                        }
                    },
                    onListMaterial = { navigateTo(AppScreen.LIST_MATERIAL) },
                    onPostRequirement = { navigateTo(AppScreen.POST_REQUIREMENT) },
                    onOpenMaterial = { item -> openMaterialDetails(item, initialTab = 0) },
                    onOpenRequirement = { req ->
                        // If matching material exists, open details in Matches tab
                        val matched = materials.find { m -> m.materialName.contains(req.requiredMaterial, true) }
                        if (matched != null) {
                            openMaterialDetails(matched, initialTab = 1)
                        } else if (materials.isNotEmpty()) {
                            openMaterialDetails(materials.first(), initialTab = 1)
                        }
                    },
                    onFindMatchesForMaterial = { item -> openMaterialDetails(item, initialTab = 1) },
                    onViewPassport = { item -> openMaterialDetails(item, initialTab = 3) },
                    onViewAllDiscover = { currentScreen = AppScreen.DISCOVER }
                )

                AppScreen.DISCOVER -> DiscoverScreen(
                    materials = materials,
                    onOpenMaterial = { item -> openMaterialDetails(item, initialTab = 0) },
                    onFindMatches = { item -> openMaterialDetails(item, initialTab = 1) },
                    onViewPassport = { item -> openMaterialDetails(item, initialTab = 3) }
                )

                AppScreen.LIST_MATERIAL -> ListMaterialScreen(
                    onBack = { currentScreen = previousScreen },
                    onSuccessCreated = {
                        val newest = MaterialRepository.materials.value.firstOrNull()
                        if (newest != null) {
                            openMaterialDetails(newest, initialTab = 0)
                        } else {
                            currentScreen = AppScreen.HOME
                        }
                    }
                )

                AppScreen.POST_REQUIREMENT -> PostRequirementScreen(
                    onBack = { currentScreen = previousScreen },
                    onSuccessSubmitted = {
                        currentScreen = AppScreen.HOME
                    }
                )

                AppScreen.MATERIAL_DETAIL -> {
                    selectedMaterial?.let { item ->
                        MaterialDetailScreen(
                            item = item,
                            initialTab = detailInitialTab,
                            onBack = { currentScreen = previousScreen },
                            onViewPassportFull = {
                                navigateTo(AppScreen.PASSPORT_FULL)
                            }
                        )
                    } ?: run {
                        currentScreen = AppScreen.HOME
                    }
                }

                AppScreen.PASSPORT_FULL -> {
                    selectedMaterial?.let { item ->
                        PassportScreen(
                            item = item,
                            onBack = { currentScreen = AppScreen.MATERIAL_DETAIL }
                        )
                    } ?: run {
                        currentScreen = AppScreen.HOME
                    }
                }

                AppScreen.PROFILE -> ProfileScreen(
                    serverHealth = serverHealth,
                    myListingsCount = materials.size,
                    myRequirementsCount = requirements.size,
                    onListMaterial = { navigateTo(AppScreen.LIST_MATERIAL) },
                    onPostRequirement = { navigateTo(AppScreen.POST_REQUIREMENT) }
                )
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
    currentScreen: AppScreen,
    materialsCount: Int,
    requirementsCount: Int,
    onSelectScreen: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 2.dp
    ) {
        val items = listOf(
            Triple(AppScreen.HOME, Icons.Default.Home, "Exchange"),
            Triple(AppScreen.DISCOVER, Icons.Default.Search, "Market"),
            Triple(AppScreen.PROFILE, Icons.Default.Person, "Account")
        )

        items.forEach { (screen, icon, label) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandForest,
                    selectedTextColor = BrandForest,
                    indicatorColor = BrandLime.copy(alpha = 0.5f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
