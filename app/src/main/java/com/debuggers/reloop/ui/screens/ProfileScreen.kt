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
import com.debuggers.reloop.data.model.ServerHealth
import com.debuggers.reloop.data.repository.MaterialRepository
import com.debuggers.reloop.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    serverHealth: ServerHealth,
    myListingsCount: Int,
    myRequirementsCount: Int,
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
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title
        Text(
            text = "Enterprise Account & Node",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )

        // Enterprise Profile Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ReLoopBorder),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        color = BrandForest,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Factory,
                                contentDescription = null,
                                tint = BrandLime,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rajpura Industrial Node",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.Verified, null, tint = LiveGreen, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "Thermal & Mineral Processing Facility",
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "Node ID: RELOOP-IN-PB-7489",
                            fontSize = 11.sp,
                            color = TextSubtle
                        )
                    }
                }

                HorizontalDivider(color = ReLoopBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$myListingsCount", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BrandForest)
                        Text("Active Listings", fontSize = 11.5.sp, color = TextMuted)
                    }
                    Box(modifier = Modifier.height(28.dp).width(1.dp).background(ReLoopBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$myRequirementsCount", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = InfoBlue)
                        Text("Active Demands", fontSize = 11.5.sp, color = TextMuted)
                    }
                    Box(modifier = Modifier.height(28.dp).width(1.dp).background(ReLoopBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("100%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = LiveGreen)
                        Text("DPP Compliance", fontSize = 11.5.sp, color = TextMuted)
                    }
                }
            }
        }

        // Live Backend Infrastructure Diagnostics Card
        Card(
            shape = RoundedCornerShape(16.dp),
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BACKEND INFRASTRUCTURE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        color = if (currentHealth.isOnline) LiveGreenBg else DemoAmberBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (currentHealth.isOnline) "LIVE CONNECTION" else "OFFLINE",
                            color = if (currentHealth.isOnline) LiveGreen else DemoAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                InfoRow("API Host URL", ReLoopApiClient.baseUrl)
                InfoRow("Database Cluster", currentHealth.database)
                InfoRow("FastAPI Engine Status", if (currentHealth.isOnline) "Active (HTTP 200)" else "Connecting / Offline")
                InfoRow("Response Latency", "${currentHealth.latencyMs} ms")

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            isCheckingHealth = true
                            currentHealth = MaterialRepository.checkServerStatus()
                            isCheckingHealth = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrandForest)
                ) {
                    if (isCheckingHealth) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandForest, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Speed, null, tint = BrandForest, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Ping Live Server Diagnostics", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = BrandForest)
                }
            }
        }

        // Quick Actions
        Text("Quick Operations", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)

        Button(
            onClick = onListMaterial,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
        ) {
            Icon(Icons.Default.Add, null, tint = BrandLime)
            Spacer(Modifier.width(8.dp))
            Text("List Available By-Product Feedstock", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onPostRequirement,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BrandForest)
        ) {
            Icon(Icons.Default.PostAdd, null, tint = BrandForest)
            Spacer(Modifier.width(8.dp))
            Text("Submit Buyer Feedstock Requirement", fontWeight = FontWeight.Bold, color = BrandForest)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.5.sp, color = TextMuted)
        Text(value, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}
