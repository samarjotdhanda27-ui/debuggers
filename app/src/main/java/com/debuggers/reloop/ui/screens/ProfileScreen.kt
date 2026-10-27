package com.debuggers.reloop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.debuggers.reloop.data.api.ReLoopApiClient
import com.debuggers.reloop.data.model.EnquiryQuoteRequest
import com.debuggers.reloop.data.model.ServerHealth
import com.debuggers.reloop.data.repository.MaterialRepository
import com.debuggers.reloop.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    serverHealth: ServerHealth,
    myListingsCount: Int,
    myRequirementsCount: Int,
    quoteRequests: List<EnquiryQuoteRequest>,
    onListMaterial: () -> Unit,
    onPostRequirement: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentHealth by remember { mutableStateOf(serverHealth) }
    var isCheckingHealth by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReLoopPageBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Page Title
        Text(
            text = "Enterprise Account & Profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )

        // Facility Identity Card
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ReLoopBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = BrandForest,
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Factory,
                                contentDescription = null,
                                tint = BrandLime,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rajpura Industrial Facility",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.Verified, null, tint = StatusVerified, modifier = Modifier.size(15.dp))
                        }
                        Text(
                            text = "Thermal & Mineral Circular Hub",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "Node ID: RELOOP-IN-PB-7489",
                            fontSize = 10.5.sp,
                            color = TextSubtle
                        )
                    }
                }

                HorizontalDivider(color = ReLoopBorder, thickness = 0.5.dp)

                // Metric Counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$myListingsCount", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BrandForest)
                        Text("Active Listings", fontSize = 11.sp, color = TextMuted)
                    }
                    Box(modifier = Modifier.height(28.dp).width(1.dp).background(ReLoopBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${quoteRequests.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BrandDarkGreen)
                        Text("Quote RFQs", fontSize = 11.sp, color = TextMuted)
                    }
                    Box(modifier = Modifier.height(28.dp).width(1.dp).background(ReLoopBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$myRequirementsCount", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = InfoBlue)
                        Text("Active Demands", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        // My Quote Requests (RFQs) Section
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
                    "RECENT ENQUIRIES & QUOTE REQUESTS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 0.5.sp
                )

                if (quoteRequests.isEmpty()) {
                    Text(
                        "No quote requests submitted yet. Browse materials in the marketplace and tap 'Request Quote' to initiate commercial inquiries.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                } else {
                    quoteRequests.forEach { rfq ->
                        Surface(
                            color = ReLoopPageBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, ReLoopBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(rfq.materialName, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = BrandForest)
                                    Surface(color = StatusVerifiedBg, shape = RoundedCornerShape(4.dp)) {
                                        Text(rfq.status, color = StatusVerified, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                }
                                Text("Supplier: ${rfq.sellerName} · ${rfq.requestedQuantityTonnes.toInt()} MT", fontSize = 11.5.sp, color = TextDark)
                                Text("Destination: ${rfq.deliveryCity} · ${rfq.timestamp}", fontSize = 10.5.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // Live Backend Infrastructure Diagnostics Card
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ReLoopBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "FASTAPI CLOUD CONNECTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        color = if (currentHealth.isOnline) LiveGreenBg else DemoAmberBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (currentHealth.isOnline) "LIVE API" else "OFFLINE",
                            color = if (currentHealth.isOnline) BrandForest else DemoAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                ProfileInfoRow("Cloud Host", ReLoopApiClient.baseUrl.removePrefix("https://"))
                ProfileInfoRow("Database Cluster", currentHealth.database)
                ProfileInfoRow("API Status", if (currentHealth.isOnline) "HTTP 200 OK" else "Unreachable / Sleeping")
                ProfileInfoRow("Latency", "${currentHealth.latencyMs} ms")

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            isCheckingHealth = true
                            currentHealth = MaterialRepository.checkServerStatus()
                            isCheckingHealth = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BrandForest)
                ) {
                    if (isCheckingHealth) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = BrandForest, strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                    } else {
                        Icon(Icons.Default.Speed, null, tint = BrandForest, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text("Ping Backend Server", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandForest)
                }
            }
        }

        // Quick Actions
        Text("Account Actions", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextDark)

        Button(
            onClick = onListMaterial,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
        ) {
            Icon(Icons.Default.Add, null, tint = BrandLime, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("List a New Material By-Product", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onPostRequirement,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, BrandForest),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Icon(Icons.Default.PostAdd, null, tint = BrandForest, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Post Feedstock Demand", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandForest)
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}
