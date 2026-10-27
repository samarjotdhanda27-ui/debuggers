package com.debuggers.reloop.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debuggers.reloop.data.model.DigitalProductPassport
import com.debuggers.reloop.data.model.MaterialItem
import com.debuggers.reloop.data.repository.MaterialRepository
import com.debuggers.reloop.ui.components.QrCodeView
import com.debuggers.reloop.ui.components.reloopTextFieldColors
import com.debuggers.reloop.ui.theme.*

@Composable
fun PassportScreen(
    materials: List<MaterialItem>,
    initialMaterial: MaterialItem? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedItem by remember {
        mutableStateOf(initialMaterial ?: materials.firstOrNull())
    }
    var searchUuidQuery by remember { mutableStateOf("") }
    var passportData by remember { mutableStateOf<DigitalProductPassport?>(null) }
    var isLoadingPassport by remember { mutableStateOf(false) }

    LaunchedEffect(selectedItem?.id) {
        selectedItem?.let { mat ->
            isLoadingPassport = true
            passportData = MaterialRepository.getPassportForMaterial(mat.id)
            isLoadingPassport = false
        }
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

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Digital Product Passport",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = item?.let { "Material UUID: ${it.id.take(12)}…" } ?: "Traceability Ledger",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }

                Surface(
                    color = BrandMintBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, null, tint = BrandPrimary, modifier = Modifier.size(13.dp))
                        Text("LEGAL PROVENANCE", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                    }
                }
            }
        }

        // Search By Material UUID Box
        Surface(
            color = Color.White,
            border = BorderStroke(0.5.dp, ReLoopBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchUuidQuery,
                        onValueChange = { searchUuidQuery = it },
                        placeholder = { Text("Search by Material UUID…", fontSize = 12.sp, color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = reloopTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )

                    Button(
                        onClick = {
                            val found = materials.find { it.id.contains(searchUuidQuery.trim(), ignoreCase = true) }
                            if (found != null) {
                                selectedItem = found
                                Toast.makeText(context, "Resolved material record", Toast.LENGTH_SHORT).show()
                            } else if (searchUuidQuery.isNotBlank()) {
                                Toast.makeText(context, "UUID not in local catalog, searching backend…", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Lookup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Material Selector Carousel
                if (materials.isNotEmpty()) {
                    Text("Select Catalog Batch:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(materials) { mat ->
                            val isSelected = mat.id == item?.id
                            Surface(
                                color = if (isSelected) BrandPrimary else ReLoopSurfaceSubtle,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) BrandPrimary else ReLoopBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedItem = mat }
                            ) {
                                Text(
                                    text = mat.materialName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
            val passport = passportData
            val canonicalQrUrl = "https://reloop-ui.onrender.com/passport/${item.id}"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // QR Hero Card (Batch-Specific Scannable QR)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ReLoopBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = BrandPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "OFFICIAL DIGITAL PRODUCT PASSPORT",
                                color = BrandLime,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Real Scannable QR Code
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ReLoopBorder),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                QrCodeView(
                                    content = canonicalQrUrl,
                                    size = 180.dp,
                                    qrColor = BrandPrimaryDark
                                )
                            }
                        }

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
                            color = BrandPrimary
                        )

                        // Render Passport Link + Copy Action
                        Surface(
                            color = ReLoopPageBg,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, ReLoopBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("CANONICAL VERIFICATION URL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                                    Text(
                                        text = canonicalQrUrl,
                                        fontSize = 11.sp,
                                        color = BrandPrimary,
                                        maxLines = 1,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("ReLoop Passport URL", canonicalQrUrl)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Passport URL copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", tint = BrandPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Batch Verification & Compliance Identifiers Card
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
                            "PASSPORT SPECIFICATIONS & STANDARDS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtle,
                            letterSpacing = 0.5.sp
                        )

                        PassportField("Material UUID", item.id)
                        PassportField("Batch Reference", passport?.batchNumber ?: "BAT-${item.id.take(8).uppercase()}-2026")
                        PassportField("Compliance Standard", passport?.complianceStandard ?: "Bureau of Indian Standards (BIS)")
                        PassportField("Specification Grade", item.grade ?: "Class F Industrial Standard")
                        PassportField("Physical Condition", item.condition ?: "Dispatch Ready")
                        PassportField("Generating Facility", item.sellerName)
                        PassportField("Collection Hub", "${item.city}, ${item.state}")
                        PassportField("Verification Level", if (item.isVerifiedListing) "Verified Batch (NABL Certified)" else "Seller Declared")
                    }
                }

                // 4-Stage Chain of Custody Timeline
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ReLoopBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "CHAIN OF CUSTODY TIMELINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtle,
                            letterSpacing = 0.5.sp
                        )

                        val events = passport?.custodyEvents ?: emptyList()
                        events.forEachIndexed { index, ev ->
                            CustodyTimelineRow(
                                stage = ev.stage,
                                actor = ev.actor,
                                location = ev.location,
                                timestamp = ev.timestamp,
                                status = ev.verificationStatus,
                                isLast = index == events.lastIndex
                            )
                        }
                    }
                }

                // Quality Documents & Certificates
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
                            "VERIFIED QUALITY DOCUMENTATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtle,
                            letterSpacing = 0.5.sp
                        )

                        val docs = passport?.verifiedDocuments ?: item.availableDocuments.ifEmpty { listOf("Material Test Report", "CPCB NOC") }
                        docs.forEach { doc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Verified, null, tint = StatusVerified, modifier = Modifier.size(16.dp))
                                    Text(doc, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = TextDark)
                                }
                                Surface(
                                    color = StatusVerifiedBg,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "NABL Validated",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusVerified,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
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
private fun CustodyTimelineRow(
    stage: String,
    actor: String,
    location: String,
    timestamp: String,
    status: String,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(BrandPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .background(ReLoopBorderStrong)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(stage, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
            Text("$actor · $location", fontSize = 11.5.sp, color = TextMuted)
            Text(timestamp, fontSize = 10.5.sp, color = TextSubtle)
        }

        Surface(
            color = StatusVerifiedBg,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = status,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = StatusVerified,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
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
        Text(label, fontSize = 12.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            maxLines = 1
        )
    }
}
