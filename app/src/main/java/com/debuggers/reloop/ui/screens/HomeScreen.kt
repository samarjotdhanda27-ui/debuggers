package com.debuggers.reloop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debuggers.reloop.data.model.*
import com.debuggers.reloop.ui.components.*
import com.debuggers.reloop.ui.theme.*

@Composable
fun HomeScreen(
    materials: List<MaterialItem>,
    requirements: List<RequirementItem>,
    serverHealth: ServerHealth,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onListMaterial: () -> Unit,
    onPostRequirement: () -> Unit,
    onOpenMaterial: (MaterialItem) -> Unit,
    onOpenRequirement: (RequirementItem) -> Unit,
    onFindMatchesForMaterial: (MaterialItem) -> Unit,
    onViewPassport: (MaterialItem) -> Unit,
    onViewAllDiscover: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Materials, 1 = Requirements
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("All", "Mineral", "Foundry", "Metal", "Process", "Construction")

    val filteredMaterials = materials.filter { item ->
        val textMatch = searchQuery.isBlank() ||
                item.materialName.contains(searchQuery, true) ||
                item.sellerName.contains(searchQuery, true) ||
                item.city.contains(searchQuery, true)
        val catMatch = selectedCategory == "All" ||
                item.materialName.contains(selectedCategory, true) ||
                item.sourceIndustry.contains(selectedCategory, true) ||
                item.shortCategory.contains(selectedCategory, true)
        textMatch && catMatch
    }

    val filteredRequirements = requirements.filter { req ->
        val textMatch = searchQuery.isBlank() ||
                req.requiredMaterial.contains(searchQuery, true) ||
                req.buyerName.contains(searchQuery, true) ||
                req.city.contains(searchQuery, true)
        val catMatch = selectedCategory == "All" ||
                req.requiredMaterial.contains(selectedCategory, true) ||
                req.buyerIndustry.contains(selectedCategory, true)
        textMatch && catMatch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ReLoopPageBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner & Location
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Industrial Exchange",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        letterSpacing = (-0.5).sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = BrandEmerald, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Regional Hub: Punjab · Haryana · NCR",
                            fontSize = 12.5.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Refresh button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, ReLoopBorder, CircleShape)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BrandForest, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BrandForest, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Live Exchange Metrics Dashboard
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Feedstock Listings",
                    value = "${materials.size}",
                    subtitle = "Available By-Products",
                    icon = Icons.Default.Inventory2,
                    accentColor = BrandForest,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Buyer Demands",
                    value = "${requirements.size}",
                    subtitle = "Active Requirements",
                    icon = Icons.Default.ShoppingBag,
                    accentColor = InfoBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action CTA Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onListMaterial,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = BrandLime)
                    Spacer(Modifier.width(6.dp))
                    Text("List By-Product", fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onPostRequirement,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, BrandForest)
                ) {
                    Icon(Icons.Default.PostAdd, null, modifier = Modifier.size(18.dp), tint = BrandForest)
                    Spacer(Modifier.width(6.dp))
                    Text("Post Demand", fontWeight = FontWeight.Bold, color = BrandForest)
                }
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search materials, locations, or buyers…", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = ReLoopBorder,
                    focusedBorderColor = BrandForest
                )
            )
        }

        // Category Filter Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandLime,
                            selectedLabelColor = BrandLimeDark,
                            containerColor = Color.White,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == cat,
                            borderColor = ReLoopBorder,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }

        // Segmented Tabs: Materials vs Demands
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BrandForest,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, ReLoopBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Available Feedstocks", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                            Surface(
                                color = if (selectedTab == 0) BrandForest else ReLoopSurfaceSubtle,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${filteredMaterials.size}",
                                    fontSize = 11.sp,
                                    color = if (selectedTab == 0) Color.White else TextMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Buyer Demands", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                            Surface(
                                color = if (selectedTab == 1) InfoBlue else ReLoopSurfaceSubtle,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${filteredRequirements.size}",
                                    fontSize = 11.sp,
                                    color = if (selectedTab == 1) Color.White else TextMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                )
            }
        }

        // Content Feed
        if (selectedTab == 0) {
            if (filteredMaterials.isEmpty()) {
                item {
                    EmptyFeedState(
                        title = "No materials match your filter",
                        subtitle = "Try adjusting your search terms or submit a new listing.",
                        actionText = "List a Material",
                        onAction = onListMaterial
                    )
                }
            } else {
                items(filteredMaterials, key = { it.id }) { item ->
                    MaterialCard(
                        item = item,
                        onClick = { onOpenMaterial(item) },
                        onFindMatches = { onFindMatchesForMaterial(item) },
                        onViewPassport = { onViewPassport(item) }
                    )
                }
            }
        } else {
            if (filteredRequirements.isEmpty()) {
                item {
                    EmptyFeedState(
                        title = "No buyer demands match your filter",
                        subtitle = "Post what raw material your facility requires.",
                        actionText = "Post a Requirement",
                        onAction = onPostRequirement
                    )
                }
            } else {
                items(filteredRequirements, key = { it.id }) { req ->
                    RequirementCard(
                        item = req,
                        onClick = { onOpenRequirement(req) },
                        onFindSellers = {
                            // Find matching material from list
                            val match = materials.find { m -> m.materialName.contains(req.requiredMaterial, true) }
                            if (match != null) {
                                onFindMatchesForMaterial(match)
                            } else {
                                onOpenRequirement(req)
                            }
                        }
                    )
                }
            }
        }

        // Bottom notice
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ReLoopBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Verified, null, tint = LiveGreen, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Connected live to ReLoop matching backend. All transactions are logged for regulatory circular economy compliance.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFeedState(
    title: String,
    subtitle: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ReLoopBorder)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.SearchOff, null, tint = TextSubtle, modifier = Modifier.size(44.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark, textAlign = TextAlign.Center)
            Text(subtitle, color = TextMuted, fontSize = 12.5.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = BrandForest),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(actionText)
            }
        }
    }
}
