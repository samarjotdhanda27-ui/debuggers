package com.debuggers.reloop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.debuggers.reloop.data.model.MaterialItem
import com.debuggers.reloop.data.model.RequirementItem
import com.debuggers.reloop.ui.theme.*
import kotlin.math.max
import kotlin.math.min

sealed class MapFacilityMarker {
    data class SellerMarker(val item: MaterialItem) : MapFacilityMarker()
    data class BuyerMarker(val item: RequirementItem) : MapFacilityMarker()
}

@Composable
fun InteractiveMapScreen(
    materials: List<MaterialItem>,
    requirements: List<RequirementItem>,
    initialMaterialId: String? = null,
    onBack: () -> Unit,
    onOpenMaterial: (MaterialItem) -> Unit,
    onRequestQuote: (MaterialItem) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All Hubs") }
    var selectedMarker by remember {
        mutableStateOf<MapFacilityMarker?>(
            initialMaterialId?.let { id -> materials.find { it.id == id } }?.let { MapFacilityMarker.SellerMarker(it) }
                ?: materials.firstOrNull()?.let { MapFacilityMarker.SellerMarker(it) }
        )
    }

    // Coordinates bounding box for Punjab, Haryana & NCR
    val minLat = 28.3
    val maxLat = 31.8
    val minLon = 74.8
    val maxLon = 77.4

    // Map gesture state
    var scale by remember { mutableStateOf(if (initialMaterialId != null) 1.6f else 1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(initialMaterialId) {
        if (initialMaterialId != null) {
            val mat = materials.find { it.id == initialMaterialId }
            if (mat != null) {
                selectedMarker = MapFacilityMarker.SellerMarker(mat)
                scale = 1.7f
            }
        }
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.85f, 3.5f)
        offset += offsetChange
    }

    val allMarkers = remember(materials, requirements, selectedFilter) {
        val list = mutableListOf<MapFacilityMarker>()
        if (selectedFilter == "All Hubs" || selectedFilter == "Sellers Only" || selectedFilter == "Active Matches") {
            list.addAll(materials.map { MapFacilityMarker.SellerMarker(it) })
        }
        if (selectedFilter == "All Hubs" || selectedFilter == "Buyers Only" || selectedFilter == "Active Matches") {
            list.addAll(requirements.map { MapFacilityMarker.BuyerMarker(it) })
        }
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // High-tech dark satellite/spatial canvas
    ) {
        // Interactive Canvas Map
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height

                        // Find closest marker within touch radius
                        var closestMarker: MapFacilityMarker? = null
                        var closestDist = Float.MAX_VALUE

                        allMarkers.forEach { m ->
                            val (lat, lon) = when (m) {
                                is MapFacilityMarker.SellerMarker -> Pair(m.item.latitude, m.item.longitude)
                                is MapFacilityMarker.BuyerMarker -> Pair(m.item.latitude, m.item.longitude)
                            }
                            val normX = ((lon - minLon) / (maxLon - minLon)).toFloat().coerceIn(0.08f, 0.92f)
                            val normY = (1f - ((lat - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0.12f, 0.88f)

                            val screenX = (normX * w * scale) + offset.x
                            val screenY = (normY * h * scale) + offset.y

                            val dx = tapOffset.x - screenX
                            val dy = tapOffset.y - screenY
                            val d = dx * dx + dy * dy
                            if (d < 1600f && d < closestDist) { // ~40px radius
                                closestDist = d
                                closestMarker = m
                            }
                        }

                        if (closestMarker != null) {
                            selectedMarker = closestMarker
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // Draw spatial grid lines
            val gridColor = Color(0xFF1E293B)
            for (i in 1..8) {
                val x = w * (i / 8f)
                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                val y = h * (i / 8f)
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }

            // Draw transit connection lines between sellers and buyers with matching materials
            materials.forEach { mat ->
                val normMatX = ((mat.longitude - minLon) / (maxLon - minLon)).toFloat().coerceIn(0.08f, 0.92f)
                val normMatY = (1f - ((mat.latitude - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0.12f, 0.88f)
                val p1 = Offset((normMatX * w * scale) + offset.x, (normMatY * h * scale) + offset.y)

                requirements.forEach { req ->
                    if (mat.materialName.contains(req.requiredMaterial, ignoreCase = true) ||
                        req.requiredMaterial.contains(mat.materialName, ignoreCase = true)
                    ) {
                        val normReqX = ((req.longitude - minLon) / (maxLon - minLon)).toFloat().coerceIn(0.08f, 0.92f)
                        val normReqY = (1f - ((req.latitude - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0.12f, 0.88f)
                        val p2 = Offset((normReqX * w * scale) + offset.x, (normReqY * h * scale) + offset.y)

                        // Draw dashed line for circular transit corridor
                        drawLine(
                            color = BrandEmerald.copy(alpha = 0.45f),
                            start = p1,
                            end = p2,
                            strokeWidth = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }
                }
            }

            // Draw all markers
            allMarkers.forEach { marker ->
                val (lat, lon, isSeller) = when (marker) {
                    is MapFacilityMarker.SellerMarker -> Triple(marker.item.latitude, marker.item.longitude, true)
                    is MapFacilityMarker.BuyerMarker -> Triple(marker.item.latitude, marker.item.longitude, false)
                }

                val normX = ((lon - minLon) / (maxLon - minLon)).toFloat().coerceIn(0.08f, 0.92f)
                val normY = (1f - ((lat - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0.12f, 0.88f)

                val center = Offset((normX * w * scale) + offset.x, (normY * h * scale) + offset.y)

                val isSelected = when (val sel = selectedMarker) {
                    is MapFacilityMarker.SellerMarker -> isSeller && (sel.item.id == (marker as? MapFacilityMarker.SellerMarker)?.item?.id)
                    is MapFacilityMarker.BuyerMarker -> !isSeller && (sel.item.id == (marker as? MapFacilityMarker.BuyerMarker)?.item?.id)
                    null -> false
                }

                // Outer pulsing / highlight ring if selected
                if (isSelected) {
                    drawCircle(
                        color = if (isSeller) BrandLime.copy(alpha = 0.35f) else BrandAccent.copy(alpha = 0.35f),
                        radius = 24f * scale,
                        center = center
                    )
                }

                // Node Circle
                val nodeColor = if (isSeller) BrandLime else BrandAccent
                drawCircle(
                    color = nodeColor,
                    radius = 9f * scale,
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.5f * scale,
                    center = center
                )
            }
        }

        // Top Navigation Header
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.92f),
            border = BorderStroke(0.5.dp, Color(0xFF334155)),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Industrial Spatial Visualizer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Punjab & Haryana Circular Transport Network",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Surface(
                        color = BrandLime.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${allMarkers.size} NODES",
                            color = BrandLime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                // Filter Pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All Hubs", "Sellers Only", "Buyers Only", "Active Matches").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            color = if (isSelected) BrandPrimary else Color(0xFF1E293B),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isSelected) BrandLime else Color(0xFF334155)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = filter,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Map Legend Floating Pill
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.88f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 135.dp, end = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(BrandLime, CircleShape))
                    Text("Suppliers / Lots", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(BrandAccent, CircleShape))
                    Text("Procurement Demands", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.width(12.dp).height(2.dp).background(BrandEmerald))
                    Text("Transit Corridors", color = Color(0xFF94A3B8), fontSize = 10.sp)
                }
            }
        }

        // Floating Map Controls (Zoom In, Zoom Out, Reset Center)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.9f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .size(42.dp)
                    .clickable { scale = (scale * 1.35f).coerceAtMost(3.5f) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.9f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .size(42.dp)
                    .clickable { scale = (scale / 1.35f).coerceAtLeast(0.85f) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.9f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        scale = 1f
                        offset = Offset.Zero
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter", tint = BrandEmeraldLight, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Selected Facility Detail Card (Anchored at Bottom)
        selectedMarker?.let { marker ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                when (marker) {
                    is MapFacilityMarker.SellerMarker -> {
                        val item = marker.item
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail Photo
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ReLoopSurfaceSubtle)
                                ) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.materialName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (item.isVerifiedListing) {
                                        Surface(
                                            color = StatusVerified,
                                            shape = RoundedCornerShape(bottomEnd = 4.dp),
                                            modifier = Modifier.align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = "VERIFIED",
                                                color = Color.White,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Column(Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item.category.uppercase(), fontSize = 9.5.sp, color = BrandCobalt, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = item.priceDisplay,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BrandNavy
                                        )
                                    }
                                    Text(
                                        text = item.materialName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.sellerName} · ${item.displayLocation}",
                                        fontSize = 11.5.sp,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.displayQuantity} available · ${item.availabilityStatus}",
                                        fontSize = 10.5.sp,
                                        color = StatusVerified,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onOpenMaterial(item) },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCobalt)
                                ) {
                                    Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { onRequestQuote(item) },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, ReLoopBorder)
                                ) {
                                    Text("Request Quote", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                }
                            }
                        }
                    }
                    is MapFacilityMarker.BuyerMarker -> {
                        val req = marker.item
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(color = BrandMintBg, shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        text = "PROCUREMENT DEMAND",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text("${req.requiredQuantityTonnes.toInt()} MT Requested", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandAccent)
                            }

                            Text(
                                text = req.requiredMaterial,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            Text(
                                text = "${req.buyerName} · ${req.city}, ${req.state} (%.3f°N, %.3f°E)".format(req.latitude, req.longitude),
                                fontSize = 12.sp,
                                color = TextMuted
                            )

                            Surface(
                                color = ReLoopSurfaceSubtle,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Sector: ${req.buyerIndustry} · Transit Match Corridor Active",
                                    fontSize = 11.5.sp,
                                    color = TextDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
