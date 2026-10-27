package com.debuggers.reloop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.debuggers.reloop.data.model.MaterialItem
import com.debuggers.reloop.ui.components.QrCodeView
import com.debuggers.reloop.ui.theme.*

@Composable
fun PassportScreen(
    materials: List<MaterialItem>,
    initialMaterial: MaterialItem? = null,
    onBack: () -> Unit
) {
    var selectedItem by remember {
        mutableStateOf(initialMaterial ?: materials.firstOrNull())
    }

    val item = selectedItem

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReLoopPageBg)
    ) {
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

                Column {
                    Text(
                        text = "Digital Product Passport",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = item?.let { "Record ID: DPP-${it.id.take(8).uppercase()}" } ?: "Industrial Traceability",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Material Selector Carousel if multiple materials exist
        if (materials.size > 1) {
            Surface(
                color = Color.White,
                border = BorderStroke(0.5.dp, ReLoopBorder)
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Select Material Lot to Inspect:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(materials) { mat ->
                            val isSelected = mat.id == item?.id
                            Surface(
                                color = if (isSelected) BrandForest else ReLoopSurfaceSubtle,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) BrandForest else ReLoopBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedItem = mat }
                            ) {
                                Text(
                                    text = mat.materialName,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (item == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No material selected for passport inspection.", color = TextMuted)
            }
        } else {
            val passportPayload = "reloop://passport/${item.id}?mat=${item.materialName}&seller=${item.sellerName}&qty=${item.quantityTonnes}&loc=${item.city}"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // QR Hero Card
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
                        Surface(
                            color = BrandForest,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "OFFICIAL DIGITAL PASSPORT",
                                color = BrandLime,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Real Scannable QR Code
                        QrCodeView(
                            content = passportPayload,
                            size = 190.dp,
                            qrColor = BrandForest
                        )

                        Text(
                            text = item.materialName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Text(
                            text = "${item.displayQuantity} · ${item.sellerName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandForest
                        )

                        Text(
                            text = "Scannable with standard camera apps. Verifies supply chain provenance, certified specifications, and custody events.",
                            fontSize = 11.5.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Traceability & Custody Nodes
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
                            "SUPPLY CHAIN TRACEABILITY LOG",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtle,
                            letterSpacing = 0.5.sp
                        )

                        TraceabilityNode("Origin Facility", item.sellerName, item.displayLocation, isVerified = true)
                        TraceabilityNode(
                            "Batch Documentation",
                            if (item.availableDocuments.isNotEmpty()) item.availableDocuments.joinToString(", ") else "Not provided",
                            if (item.isVerifiedListing) "Verified Laboratory Upload" else "Seller Declared Only",
                            isVerified = item.isVerifiedListing
                        )
                        TraceabilityNode("Marketplace Registration", "ReLoop Industrial Network Node", item.availabilityStatus, isVerified = true)
                        TraceabilityNode(
                            "Commercial Dispatch",
                            "Tender Open for Procurement",
                            if (item.pricePerTon != null) "Commercial pricing declared" else "Quote required before physical dispatch",
                            isVerified = false
                        )
                    }
                }

                // Technical Composition Profile
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
                            "PASSPORT CHARACTERISTICS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtle,
                            letterSpacing = 0.5.sp
                        )

                        PassportField("Material Category", item.category)
                        PassportField("Specification Grade", item.grade ?: "Not provided")
                        PassportField("Physical Condition", item.condition ?: "Not provided")
                        PassportField("Pricing Basis", item.pricingBasis)
                        PassportField("Verification Level", if (item.isVerifiedListing) "Live Verified Node" else "Seller Declared (Unverified)")
                    }
                }

                // Disclaimer
                Surface(
                    color = ReLoopSurfaceSubtle,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ReLoopBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Text(
                            text = "This digital passport summarizes declared specifications. Suitability and regulatory compliance for reuse must be independently verified by the procurement facility prior to physical blending or remanufacturing.",
                            fontSize = 11.5.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TraceabilityNode(stage: String, entity: String, note: String, isVerified: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(if (isVerified) StatusVerified else ReLoopBorder),
            contentAlignment = Alignment.Center
        ) {
            if (isVerified) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(11.dp))
            }
        }
        Column {
            Text(stage, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
            Text(entity, fontSize = 12.sp, color = TextDark)
            Text(note, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
private fun PassportField(label: String, value: String) {
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
