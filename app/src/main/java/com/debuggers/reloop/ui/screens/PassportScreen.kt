package com.debuggers.reloop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debuggers.reloop.data.model.MaterialItem
import com.debuggers.reloop.ui.components.QrCodeView
import com.debuggers.reloop.ui.theme.*

@Composable
fun PassportScreen(
    item: MaterialItem,
    onBack: () -> Unit
) {
    val passportPayload = "reloop://passport/${item.id}?mat=${item.materialName}&seller=${item.sellerName}&qty=${item.quantityTonnes}&loc=${item.city}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReLoopPageBg)
    ) {
        // Top Header
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
                        text = "Digital Material Passport",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Text(
                        text = "Circular Economy Traceability ID: DPP-${item.id.take(8).uppercase()}",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Scrollable passport content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High-Resolution Scannable QR Code Hero Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, BrandForest.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        color = BrandForest,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "OFFICIAL DIGITAL PASSPORT",
                            color = BrandLime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Scannable QR Code
                    QrCodeView(
                        content = passportPayload,
                        size = 230.dp,
                        qrColor = BrandForest
                    )

                    Text(
                        text = item.materialName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )

                    Text(
                        text = "${item.displayQuantity} · ${item.sellerName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandForest
                    )

                    Text(
                        text = "Certified for Industrial Remanufacturing & Mineral Substitution",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Chain of Custody & Provenance
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SUPPLY CHAIN TRACEABILITY NODES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 0.5.sp
                    )

                    TimelineNode("Origin Generation", item.sellerName, item.displayLocation, isDone = true)
                    TimelineNode("Chemical Profiling", "Laboratory Verification Batch #9921", "Standards: IS 3812 / 12894", isDone = true)
                    TimelineNode("Marketplace Listing", "ReLoop Exchange Verified", "Status: Active & Available", isDone = true)
                    TimelineNode("Intake Matching", "Geographic Haversine Radius Scan", "Target: Regional Cement & Brick Works", isDone = false)
                }
            }

            // Regulatory Standards & Compliance
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "REGULATORY & ENVIRONMENTAL COMPLIANCE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 0.5.sp
                    )

                    ComplianceItem("Central Pollution Control Board (CPCB) Guideline", "Compliant")
                    ComplianceItem("Fly Ash Utilization Notification 2021/2026", "Registered")
                    ComplianceItem("Hazardous Waste Classification (Rule 9)", "Non-Hazardous Secondary Feedstock")
                    ComplianceItem("Digital Product Passport ISO 14021 Scheme", "Active Hash Verification")
                }
            }
        }
    }
}

@Composable
private fun TimelineNode(title: String, subtitle: String, extra: String, isDone: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isDone) LiveGreen else ReLoopBorder),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = TextDark)
            Text(subtitle, fontSize = 12.sp, color = TextMuted)
            Text(extra, fontSize = 11.sp, color = TextSubtle)
        }
    }
}

@Composable
private fun ComplianceItem(title: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.5.sp, color = TextMuted, modifier = Modifier.weight(1f))
        Surface(color = LiveGreenBg, shape = RoundedCornerShape(4.dp)) {
            Text(status, color = LiveGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}
