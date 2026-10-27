package com.debuggers.reloop.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debuggers.reloop.data.model.*
import com.debuggers.reloop.ui.theme.*

@Composable
fun ReLoopTopBar(
    serverHealth: ServerHealth,
    onHealthClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        color = Color.White,
        border = BorderStroke(0.5.dp, ReLoopBorder),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = BrandForest,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "ReLoop Logo",
                            tint = BrandLime,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ReLoop",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandForest,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = BrandLime,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = BrandLimeDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                    Text(
                        text = "CIRCULAR INDUSTRIAL EXCHANGE",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Right side: Server Health Pill & Profile Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Server status pill
                Surface(
                    color = if (serverHealth.isOnline) LiveGreenBg else DemoAmberBg,
                    border = BorderStroke(1.dp, if (serverHealth.isOnline) LiveGreen.copy(alpha = 0.3f) else DemoAmber.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { onHealthClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (serverHealth.isOnline) LiveGreen else DemoAmber)
                        )
                        Text(
                            text = if (serverHealth.isOnline) "${serverHealth.latencyMs}ms" else "Offline",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (serverHealth.isOnline) LiveGreen else DemoAmber
                        )
                    }
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Surface(
                        color = ReLoopSurfaceSubtle,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, ReLoopBorder),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = BrandForest,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServerDiagnosticsDialog(
    health: ServerHealth,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (health.isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (health.isOnline) LiveGreen else AlertRed
                )
                Text("Backend Infrastructure", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "ReLoop connects directly to a live FastAPI engine backed by Supabase PostgreSQL.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                HorizontalDivider(color = ReLoopBorder)
                DiagRow("Server Status", if (health.isOnline) "ONLINE (Active)" else "OFFLINE / Connecting")
                DiagRow("Database", health.database)
                DiagRow("Latency", "${health.latencyMs} ms")
                DiagRow("Cloud Host", "reloop-ui.onrender.com")
                if (!health.isOnline && health.errorMessage != null) {
                    Surface(color = AlertRedBg, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            health.errorMessage,
                            color = AlertRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = BrandForest),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Test Connection")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextMuted)
            }
        }
    )
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextMuted, fontSize = 12.5.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = TextDark)
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        color = Color.White,
        border = BorderStroke(1.dp, ReLoopBorder),
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Surface(
                    color = accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = accentColor, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MaterialCard(
    item: MaterialItem,
    onClick: () -> Unit,
    onFindMatches: () -> Unit,
    onViewPassport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ReLoopBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Symbol, Name, Category, Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = BrandForest,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.symbol,
                            color = BrandLime,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.materialName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.sellerName,
                        fontSize = 12.5.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            HorizontalDivider(color = ReLoopBorder.copy(alpha = 0.7f))

            // Details: Quantity & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("AVAILABLE QUANTITY", fontSize = 9.5.sp, color = TextSubtle, letterSpacing = 0.4.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.displayQuantity,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandForest
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("ORIGIN / LOGISTICS HUB", fontSize = 9.5.sp, color = TextSubtle, letterSpacing = 0.4.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = BrandForest, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = item.displayLocation,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                    }
                }
            }

            // Documents tags
            if (item.availableDocuments.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VerifiedUser, null, tint = LiveGreen, modifier = Modifier.size(13.dp))
                    Text(
                        text = "${item.availableDocuments.size} Quality Docs: ${item.availableDocuments.joinToString(", ")}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFindMatches,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandForest),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.CompareArrows, null, modifier = Modifier.size(16.dp), tint = BrandLime)
                    Spacer(Modifier.width(6.dp))
                    Text("Find Matches", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onViewPassport,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrandForest.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(16.dp), tint = BrandForest)
                    Spacer(Modifier.width(6.dp))
                    Text("Passport", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = BrandForest)
                }
            }
        }
    }
}

@Composable
fun RequirementCard(
    item: RequirementItem,
    onClick: () -> Unit,
    onFindSellers: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ReLoopBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = InfoBlueBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = InfoBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.requiredMaterial,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextDark
                    )
                    Text(
                        text = "${item.buyerName} · ${item.buyerIndustry}",
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = InfoBlueBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "DEMAND",
                        color = InfoBlue,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            HorizontalDivider(color = ReLoopBorder.copy(alpha = 0.7f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("DEMAND QUANTITY", fontSize = 9.5.sp, color = TextSubtle, letterSpacing = 0.4.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.displayQuantity,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = InfoBlue
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("FACILITY INTAKE", fontSize = 9.5.sp, color = TextSubtle, letterSpacing = 0.4.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = InfoBlue, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = item.displayLocation,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                    }
                }
            }

            Button(
                onClick = onFindSellers,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
            ) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Match Nearby Feedstock", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun IntelligentMatchCard(
    match: IntelligentMatch,
    onContactBuyer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (match.compatibilityScore >= 90) BrandLime.copy(alpha = 0.8f) else ReLoopBorder),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Buyer header + Compatibility score ring/badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = match.buyerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp,
                        color = TextDark
                    )
                    Text(
                        text = match.buyerIndustry,
                        fontSize = 12.5.sp,
                        color = TextMuted
                    )
                }

                // Score pill
                Surface(
                    color = when {
                        match.compatibilityScore >= 90 -> LiveGreenBg
                        match.compatibilityScore >= 70 -> DemoAmberBg
                        else -> AlertRedBg
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            match.compatibilityScore >= 90 -> LiveGreen.copy(alpha = 0.4f)
                            match.compatibilityScore >= 70 -> DemoAmber.copy(alpha = 0.4f)
                            else -> AlertRed.copy(alpha = 0.4f)
                        }
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${match.compatibilityScore.toInt()}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (match.compatibilityScore >= 90) LiveGreen else DemoAmber
                        )
                        Text(
                            text = "MATCH",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                }
            }

            // Transit Distance & Quantity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ReLoopSurfaceSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Navigation, null, tint = BrandForest, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${match.distanceKm} km away",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandForest
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (match.quantitySufficient) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (match.quantitySufficient) LiveGreen else DemoAmber,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Demand: ${match.requiredQuantityTonnes.toInt()} t",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )
                }
            }

            // Recommended Circular Pathway
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "REUSE PATHWAY & SPECIFICATION",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = match.recommendedApplication,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandForest
                )
                if (match.standardReference != null) {
                    Text(
                        text = "Standard Reference: ${match.standardReference}",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }
            }

            // AI Decision Support Explanations
            if (match.explanations.isNotEmpty()) {
                Surface(
                    color = ReLoopPageBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, ReLoopBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "AI MATCH REASONING",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtle,
                            letterSpacing = 0.5.sp
                        )
                        match.explanations.forEach { exp ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("•", color = BrandEmerald, fontWeight = FontWeight.Bold)
                                Text(exp, fontSize = 11.5.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }

            // Contact / Engage Button
            Button(
                onClick = onContactBuyer,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
            ) {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Text("Engage Material Dispatch", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun PathwayCard(pathway: ReusePathway) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ReLoopBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pathway.application,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BrandForest
                )
                Surface(color = BrandLime, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = pathway.id,
                        color = BrandLimeDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Target Industry: ${pathway.buyerIndustry}",
                fontSize = 12.sp,
                color = TextMuted
            )

            if (pathway.referenceStandard != null) {
                Surface(
                    color = ReLoopSurfaceSubtle,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "BIS / Standard: ${pathway.referenceStandard}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandForest,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (pathway.relevantProperties.isNotEmpty()) {
                Text(
                    text = "Key Verified Properties: ${pathway.relevantProperties.joinToString(", ")}",
                    fontSize = 11.sp,
                    color = TextSubtle
                )
            }

            if (pathway.evidenceSources.isNotEmpty()) {
                Text(
                    text = "Scientific Evidence: ${pathway.evidenceSources.joinToString(", ")}",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}
