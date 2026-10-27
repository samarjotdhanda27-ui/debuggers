package com.debuggers.reloop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debuggers.reloop.data.model.*
import com.debuggers.reloop.data.repository.MaterialRepository
import com.debuggers.reloop.ui.components.*
import com.debuggers.reloop.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MaterialDetailScreen(
    item: MaterialItem,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onViewPassportFull: (MaterialItem) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(initialTab) }

    // State for Live Matches tab
    var searchRadiusKm by remember { mutableStateOf(300.0) }
    var matchesResult by remember { mutableStateOf<MaterialMatchesResult?>(null) }
    var isLoadingMatches by remember { mutableStateOf(false) }

    // State for Circular Pathways tab
    var pathwaysList by remember { mutableStateOf<List<ReusePathway>>(emptyList()) }
    var isLoadingPathways by remember { mutableStateOf(false) }

    // Fetch Matches when tab is active or radius changes
    LaunchedEffect(selectedTab, searchRadiusKm) {
        if (selectedTab == 1) {
            isLoadingMatches = true
            matchesResult = MaterialRepository.getMatches(item.id, searchRadiusKm)
            isLoadingMatches = false
        } else if (selectedTab == 2) {
            isLoadingPathways = true
            pathwaysList = MaterialRepository.getPathways(item.materialName)
            isLoadingPathways = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReLoopPageBg)
    ) {
        // Top Bar
        Surface(
            color = Color.White,
            border = BorderStroke(0.5.dp, ReLoopBorder),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .background(ReLoopPageBg, RoundedCornerShape(10.dp))
                        .border(1.dp, ReLoopBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = TextDark)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.materialName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Text(
                        text = item.sellerName,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Surface(
                    color = if (item.isSynthetic) DemoAmberBg else LiveGreenBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (item.isSynthetic) "DEMO" else "VERIFIED",
                        color = if (item.isSynthetic) DemoAmber else LiveGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // 4 Tabs: Specs, Matches, Pathways, Passport
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = BrandForest,
            edgePadding = 16.dp,
            divider = { HorizontalDivider(color = ReLoopBorder) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Specifications", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("AI Matches", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        Surface(color = BrandLime, shape = CircleShape) {
                            Box(modifier = Modifier.size(6.dp))
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Reuse Pathways", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Material Passport", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> SpecificationsTab(item, onFindMatches = { selectedTab = 1 }, onViewPassport = { selectedTab = 3 })
                1 -> MatchesTab(
                    item = item,
                    searchRadiusKm = searchRadiusKm,
                    onRadiusChange = { searchRadiusKm = it },
                    matchesResult = matchesResult,
                    isLoading = isLoadingMatches,
                    onRefreshMatches = {
                        coroutineScope.launch {
                            isLoadingMatches = true
                            matchesResult = MaterialRepository.getMatches(item.id, searchRadiusKm)
                            isLoadingMatches = false
                        }
                    }
                )
                2 -> PathwaysTab(pathwaysList, isLoading = isLoadingPathways, materialName = item.materialName)
                3 -> PassportTab(item, onViewFull = { onViewPassportFull(item) })
            }
        }
    }
}

@Composable
private fun SpecificationsTab(
    item: MaterialItem,
    onFindMatches: () -> Unit,
    onViewPassport: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Overview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text("CATEGORY & SECTOR", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)
                            Spacer(Modifier.height(3.dp))
                            Text(item.sourceIndustry, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandForest)
                        }
                        Surface(
                            color = BrandForest,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(item.symbol, color = BrandLime, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = ReLoopBorder)

                    DetailRow("Available Lot Size", item.displayQuantity, isEmphasized = true)
                    DetailRow("Origin Hub", item.displayLocation)
                    DetailRow("Geo Coordinates", "%.3f° N, %.3f° E".format(item.latitude, item.longitude))
                    DetailRow("Backend ID", item.id.take(18) + if (item.id.length > 18) "…" else "")
                }
            }
        }

        // Technical Properties Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("LABORATORY TECHNICAL PARAMETERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                    if (item.properties.isNotEmpty()) {
                        item.properties.forEach { (key, value) ->
                            DetailRow(
                                label = key.replace("_", " ").capitalizeWords(),
                                value = value.toString()
                            )
                        }
                    } else {
                        Text(
                            "Baseline technical properties under laboratory analysis. Complete specification report available in digital passport.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Available Quality Documents
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("AVAILABLE REGULATORY DOCUMENTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                    if (item.availableDocuments.isNotEmpty()) {
                        item.availableDocuments.forEach { doc ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = LiveGreen, modifier = Modifier.size(16.dp))
                                Text(doc, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, null, tint = DemoAmber, modifier = Modifier.size(16.dp))
                            Text("Pending laboratory upload.", fontSize = 12.5.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onFindMatches,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
                ) {
                    Icon(Icons.Default.CompareArrows, null, tint = BrandLime)
                    Spacer(Modifier.width(8.dp))
                    Text("Scan Matches", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onViewPassport,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BrandForest)
                ) {
                    Icon(Icons.Default.QrCode2, null, tint = BrandForest)
                    Spacer(Modifier.width(8.dp))
                    Text("Passport", fontWeight = FontWeight.Bold, color = BrandForest)
                }
            }
        }
    }
}

@Composable
private fun MatchesTab(
    item: MaterialItem,
    searchRadiusKm: Double,
    onRadiusChange: (Double) -> Unit,
    matchesResult: MaterialMatchesResult?,
    isLoading: Boolean,
    onRefreshMatches: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Spatial Radius Control Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SEARCH RADIUS (HAIVERSINE)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)
                        Text(
                            "${searchRadiusKm.toInt()} km",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandForest
                        )
                    }

                    Slider(
                        value = searchRadiusKm.toFloat(),
                        onValueChange = { onRadiusChange(it.toDouble()) },
                        valueRange = 50f..500f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = BrandForest,
                            activeTrackColor = BrandForest,
                            inactiveTrackColor = ReLoopBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("50 km", fontSize = 11.sp, color = TextMuted)
                        Text("300 km (Standard)", fontSize = 11.sp, color = TextMuted)
                        Text("500 km", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = BrandForest)
                        Text("Computing spatial & property compatibility…", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else if (matchesResult == null || matchesResult.matches.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ReLoopBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Radar, null, tint = TextSubtle, modifier = Modifier.size(48.dp))
                        Text("No Buyers Found Within ${searchRadiusKm.toInt()} km", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        Text("Try extending the search radius slider above to scan broader industrial clusters.", fontSize = 12.5.sp, color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${matchesResult.matches.size} Qualified Circular Matches",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextDark
                    )
                    IconButton(onClick = onRefreshMatches) {
                        Icon(Icons.Default.Refresh, null, tint = BrandForest, modifier = Modifier.size(18.dp))
                    }
                }
            }

            items(matchesResult.matches) { match ->
                IntelligentMatchCard(
                    match = match,
                    onContactBuyer = {}
                )
            }

            if (matchesResult.disclaimer.isNotEmpty()) {
                item {
                    Text(
                        matchesResult.disclaimer,
                        fontSize = 11.sp,
                        color = TextSubtle,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PathwaysTab(
    pathways: List<ReusePathway>,
    isLoading: Boolean,
    materialName: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Research-Backed Circular Pathways",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Approved industrial substitution pathways with BIS / IRC standard specifications for $materialName.",
                fontSize = 12.5.sp,
                color = TextMuted
            )
        }

        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandForest)
                }
            }
        } else if (pathways.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ReLoopBorder)
                ) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No verified pathways registered for this specific material category.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(pathways) { pathway ->
                PathwayCard(pathway)
            }
        }
    }
}

@Composable
private fun PassportTab(
    item: MaterialItem,
    onViewFull: () -> Unit
) {
    val passportContent = "reloop://passport/${item.id}?mat=${item.materialName}&seller=${item.sellerName}&qty=${item.quantityTonnes}"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // QR Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "DIGITAL PRODUCT PASSPORT (DPP)",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandForest,
                        letterSpacing = 0.8.sp
                    )

                    // Scannable QR Code
                    QrCodeView(
                        content = passportContent,
                        size = 200.dp,
                        qrColor = BrandForest
                    )

                    Text(
                        item.materialName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Text(
                        "Scan with any mobile camera to verify provenance hash & chemical characterization.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Button(
                        onClick = onViewFull,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandForest),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Fullscreen, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Expand Fullscreen Passport")
                    }
                }
            }
        }

        // Metadata Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("CIRCULAR PASSPORT ATTRIBUTES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)
                    DetailRow("Passport ID", "DPP-${item.id.take(8).uppercase()}")
                    DetailRow("Verification Standard", "ISO 14021 / EU DPP Spec")
                    DetailRow("Supply Chain Node", item.sellerName)
                    DetailRow("Batch Integrity", if (item.isSynthetic) "Synthetic Demo Hash" else "Cryptographically Sealed")
                    DetailRow("Custody Status", "Ready for Dispatch")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isEmphasized: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        Text(
            value,
            fontSize = if (isEmphasized) 15.sp else 13.sp,
            fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isEmphasized) BrandForest else TextDark
        )
    }
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
