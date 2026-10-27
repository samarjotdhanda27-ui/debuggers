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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debuggers.reloop.data.model.*
import com.debuggers.reloop.data.repository.MaterialRepository
import com.debuggers.reloop.ui.components.QrCodeView
import com.debuggers.reloop.ui.components.RequestQuoteBottomSheet
import com.debuggers.reloop.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDetailScreen(
    item: MaterialItem,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onViewPassportFull: (MaterialItem) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(initialTab) }
    var showQuoteDialog by remember { mutableStateOf(false) }
    var quoteSubmittedNotice by remember { mutableStateOf<String?>(null) }

    // Live AI Matches State
    var searchRadiusKm by remember { mutableStateOf(300.0) }
    var matchesResult by remember { mutableStateOf<MaterialMatchesResult?>(null) }
    var isLoadingMatches by remember { mutableStateOf(false) }

    // Live Reuse Pathways State
    var pathwaysList by remember { mutableStateOf<List<ReusePathway>>(emptyList()) }
    var isLoadingPathways by remember { mutableStateOf(false) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ReLoopPageBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Surface(
                color = Color.White,
                border = BorderStroke(0.5.dp, ReLoopBorder),
                shadowElevation = 0.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(ReLoopPageBg, RoundedCornerShape(8.dp))
                            .border(1.dp, ReLoopBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = item.materialName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1
                        )
                        Text(
                            text = "${item.category} · ${item.displayLocation}",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    }

                    if (item.isVerifiedListing) {
                        Surface(color = StatusVerifiedBg, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "VERIFIED",
                                color = StatusVerified,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Tab Navigation Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BrandForest,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = ReLoopBorder, thickness = 0.5.dp) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    selectedContentColor = BrandPrimary,
                    unselectedContentColor = TextMuted,
                    text = { Text("Product Details", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    selectedContentColor = BrandPrimary,
                    unselectedContentColor = TextMuted,
                    text = { Text("Matching Buyers", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    selectedContentColor = BrandPrimary,
                    unselectedContentColor = TextMuted,
                    text = { Text("Reuse Pathways", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    selectedContentColor = BrandPrimary,
                    unselectedContentColor = TextMuted,
                    text = { Text("Material Passport", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp) }
                )
            }

            // Notice banner if quote submitted
            quoteSubmittedNotice?.let { notice ->
                Surface(
                    color = StatusVerifiedBg,
                    border = BorderStroke(1.dp, StatusVerified.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(notice, fontSize = 12.sp, color = StatusVerified, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { quoteSubmittedNotice = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, null, tint = StatusVerified, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Tab View Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> ProductDetailsTab(item = item, onExploreMatches = { selectedTab = 1 })
                    1 -> MatchingBuyersTab(
                        item = item,
                        radiusKm = searchRadiusKm,
                        onRadiusChange = { searchRadiusKm = it },
                        result = matchesResult,
                        isLoading = isLoadingMatches,
                        onRefresh = {
                            coroutineScope.launch {
                                isLoadingMatches = true
                                matchesResult = MaterialRepository.getMatches(item.id, searchRadiusKm)
                                isLoadingMatches = false
                            }
                        }
                    )
                    2 -> ReusePathwaysTab(pathways = pathwaysList, isLoading = isLoadingPathways, materialName = item.materialName)
                    3 -> MaterialPassportTab(item = item, onExpandPassport = { onViewPassportFull(item) })
                }
            }

            // Fixed Sticky Bottom Action Bar (Amazon/Flipkart Style)
            Surface(
                color = Color.White,
                border = BorderStroke(0.5.dp, ReLoopBorder),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("PRICE & LOT", fontSize = 9.sp, color = TextSubtle, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                        Text(
                            text = item.priceDisplay,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandForest
                        )
                        Text(
                            text = "${item.displayQuantity} available",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    }

                    Button(
                        onClick = { showQuoteDialog = true },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(15.dp), tint = BrandLime)
                        Spacer(Modifier.width(6.dp))
                        Text("Request Quote", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Enquiry / RFQ Bottom Sheet
        if (showQuoteDialog) {
            RequestQuoteBottomSheet(
                material = item,
                onDismiss = { showQuoteDialog = false },
                onSubmit = { qty, city, contact, message ->
                    MaterialRepository.submitQuoteRequest(
                        material = item,
                        buyerName = contact.split("·").firstOrNull()?.trim() ?: "My Business",
                        buyerContact = contact,
                        quantityTonnes = qty,
                        deliveryCity = city,
                        message = message
                    )
                    quoteSubmittedNotice = "Official RFQ submitted to ${item.sellerName} for $qty ${item.unit}."
                    showQuoteDialog = false
                }
            )
        }
    }
}

@Composable
private fun ProductDetailsTab(
    item: MaterialItem,
    onExploreMatches: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Material Industrial Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Surface(color = ReLoopSurfaceSubtle, shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = item.category.uppercase(),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.materialName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDark
                            )
                            Text(
                                text = "Origin Industry: ${item.sourceIndustry}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Surface(
                            color = BrandForest,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.symbol,
                                    color = BrandLime,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = ReLoopBorder, thickness = 0.5.dp)

                    // Price & Availability Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("PRICING BASIS", fontSize = 9.sp, color = TextSubtle, fontWeight = FontWeight.SemiBold)
                            Text(item.priceDisplay, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = BrandForest)
                            Text(item.pricingBasis, fontSize = 11.sp, color = TextMuted)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL LOT AVAILABLE", fontSize = 9.sp, color = TextSubtle, fontWeight = FontWeight.SemiBold)
                            Text(item.displayQuantity, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(item.availabilityStatus, fontSize = 11.sp, color = StatusVerified)
                        }
                    }
                }
            }
        }

        // Supplier & Logistics Details Card
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "SUPPLIER & ORIGIN FACILITY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 0.5.sp
                    )

                    SpecRow("Generating Facility", item.sellerName)
                    SpecRow("Collection Hub", item.displayLocation)
                    SpecRow("Geo Coordinates", "%.3f° N, %.3f° E".format(item.latitude, item.longitude))
                    SpecRow("Listing Identifier", item.id.take(16) + if (item.id.length > 16) "…" else "")
                }
            }
        }

        // Material Description Card
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "MATERIAL DESCRIPTION & HANDLING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = item.description,
                        fontSize = 13.sp,
                        color = TextDark,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // Technical Specifications Table Card
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "TECHNICAL & CHEMICAL PARAMETERS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 0.5.sp
                    )

                    item.grade?.let { SpecRow("Material Grade", it) }
                    item.condition?.let { SpecRow("Physical Condition", it) }

                    if (item.properties.isNotEmpty()) {
                        item.properties.forEach { (key, value) ->
                            if (key !in listOf("grade", "condition", "description", "price", "price_per_tonne", "pricing_basis", "availability", "unit")) {
                                SpecRow(
                                    label = key.replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
                                    value = value.toString()
                                )
                            }
                        }
                    } else {
                        Text(
                            "Standard batch parameters subject to laboratory inspection certificate.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Quality Documents & Verification
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "QUALITY DOCUMENTATION & CERTIFICATES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 0.5.sp
                    )

                    if (item.availableDocuments.isNotEmpty()) {
                        item.availableDocuments.forEach { doc ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = StatusVerified, modifier = Modifier.size(15.dp))
                                Text(doc, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = TextDark)
                                Surface(color = ReLoopSurfaceSubtle, shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        text = if (item.isVerifiedListing) "Verified" else "Seller Declared",
                                        fontSize = 9.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                            Text("No documents uploaded yet by supplier.", fontSize = 12.5.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchingBuyersTab(
    item: MaterialItem,
    radiusKm: Double,
    onRadiusChange: (Double) -> Unit,
    result: MaterialMatchesResult?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Radius Slider Control
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TRANSIT SEARCH RADIUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                        Text("${radiusKm.toInt()} km", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandForest)
                    }

                    Slider(
                        value = radiusKm.toFloat(),
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
                        Text("50 km", fontSize = 10.sp, color = TextMuted)
                        Text("300 km (Optimal B2B transit)", fontSize = 10.sp, color = TextMuted)
                        Text("500 km", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = BrandForest)
                        Text("Evaluating spatial & specification matches…", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        } else if (result == null || result.matches.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ReLoopBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Radar, null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Text("No Buyers Found Within ${radiusKm.toInt()} km", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = TextDark)
                        Text("Increase the search radius slider above to scan broader manufacturing hubs.", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
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
                    Text("${result.matches.size} Potential Matching Facilities", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                    IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Refresh, null, tint = BrandForest, modifier = Modifier.size(16.dp))
                    }
                }
            }

            items(result.matches) { match ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ReLoopBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(match.buyerName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                                Text(match.buyerIndustry, fontSize = 12.sp, color = TextMuted)
                            }
                            Surface(
                                color = StatusVerifiedBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${match.compatibilityScore.toInt()}% FIT",
                                    color = StatusVerified,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ReLoopPageBg, RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transit: ${match.distanceKm} km", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandForest)
                            Text("Demand: ${match.requiredQuantityTonnes.toInt()} MT", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }

                        Text("Application: ${match.recommendedApplication}", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)

                        if (match.explanations.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                match.explanations.take(2).forEach { exp ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("•", color = BrandForest, fontWeight = FontWeight.Bold)
                                        Text(exp, fontSize = 11.5.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReusePathwaysTab(
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
            Text("Circular Substitution Pathways", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(
                "Standard industrial reuse applications defined by BIS / IRC specifications for $materialName.",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandForest)
                }
            }
        } else if (pathways.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ReLoopBorder)
                ) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No verified pathways registered for this material.", color = TextMuted, fontSize = 12.5.sp)
                    }
                }
            }
        } else {
            items(pathways) { pathway ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ReLoopBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pathway.application, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = BrandForest)
                            Surface(color = ReLoopSurfaceSubtle, shape = RoundedCornerShape(4.dp)) {
                                Text(pathway.id, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text("Target Industry: ${pathway.buyerIndustry}", fontSize = 12.sp, color = TextMuted)
                        pathway.referenceStandard?.let {
                            Surface(color = ReLoopPageBg, shape = RoundedCornerShape(4.dp)) {
                                Text("Standard: $it", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialPassportTab(
    item: MaterialItem,
    onExpandPassport: () -> Unit
) {
    val passportPayload = "reloop://passport/${item.id}?mat=${item.materialName}&seller=${item.sellerName}&qty=${item.quantityTonnes}"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "DIGITAL PRODUCT PASSPORT (DPP)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandForest,
                        letterSpacing = 0.5.sp
                    )

                    QrCodeView(
                        content = passportPayload,
                        size = 180.dp,
                        qrColor = BrandForest
                    )

                    Text(
                        item.materialName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Text(
                        "Scannable traceability record certifying batch provenance and specification profile.",
                        fontSize = 11.5.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = onExpandPassport,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandForest),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View Full Traceability Record", fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("PASSPORT ATTRIBUTES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                    SpecRow("Passport Identifier", "DPP-${item.id.take(8).uppercase()}")
                    SpecRow("Generating Facility", item.sellerName)
                    SpecRow("Dispatch Readiness", item.availabilityStatus)
                    SpecRow("Verification Standard", if (item.isVerifiedListing) "Certified Live Listing" else "Seller Declared")
                }
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.5.sp, color = TextMuted)
        Text(value, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}
