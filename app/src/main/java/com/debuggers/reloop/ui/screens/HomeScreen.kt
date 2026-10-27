package com.debuggers.reloop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.debuggers.reloop.data.repository.MaterialRepository
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
    onViewPassport: (MaterialItem) -> Unit,
    onNavigateToExplore: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedQuickFilter by remember { mutableStateOf("All") }
    var materialForQuote by remember { mutableStateOf<MaterialItem?>(null) }
    var quoteSubmittedNotice by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        Pair("All", Icons.Default.AllInclusive),
        Pair("Minerals", Icons.Default.Factory),
        Pair("Metals", Icons.Default.PrecisionManufacturing),
        Pair("Construction", Icons.Default.Foundation),
        Pair("Plastics", Icons.Default.Recycling),
        Pair("Chemicals", Icons.Default.Science),
        Pair("Paper", Icons.Default.Description)
    )

    val quickFilters = listOf("All", "In Stock", "Verified Lots", "Bulk (> 50 MT)")

    val filteredMaterials = materials.filter { item ->
        val textMatch = searchQuery.isBlank() ||
                item.materialName.contains(searchQuery, true) ||
                item.sellerName.contains(searchQuery, true) ||
                item.city.contains(searchQuery, true) ||
                item.category.contains(searchQuery, true)

        val categoryMatch = selectedCategory == "All" ||
                item.category.contains(selectedCategory, ignoreCase = true)

        val filterMatch = when (selectedQuickFilter) {
            "In Stock" -> item.availabilityStatus.contains("In Stock", true) || item.availabilityStatus.contains("Ready", true)
            "Verified Lots" -> item.isVerifiedListing
            "Bulk (> 50 MT)" -> item.quantityTonnes >= 50.0
            else -> true
        }

        textMatch && categoryMatch && filterMatch
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ReLoopPageBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search Bar & Horizontal Category Shortcuts in a unified clean header container
            item {
                Surface(
                    color = Color.White,
                    border = BorderStroke(0.5.dp, ReLoopBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MarketplaceSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search materials, grades, suppliers…",
                            onFilterClick = onNavigateToExplore
                        )

                        // Horizontal Category Shortcuts
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { (cat, icon) ->
                                CategoryShortcutItem(
                                    title = cat,
                                    icon = icon,
                                    isSelected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat }
                                )
                            }
                        }
                    }
                }
            }

            // Notice if quote submitted
            quoteSubmittedNotice?.let { notice ->
                item {
                    Surface(
                        color = StatusVerifiedBg,
                        border = BorderStroke(1.dp, StatusVerified.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = StatusVerified, modifier = Modifier.size(16.dp))
                                Text(notice, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StatusVerified)
                            }
                            IconButton(onClick = { quoteSubmittedNotice = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, null, tint = StatusVerified, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // Sleek B2B Action Bar (List Material / Post Demand)
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ReLoopBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Storefront, null, tint = BrandForest, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Industrial Supplier Portal", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("List by-products or post procurement demand", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = onListMaterial,
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandForest),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(Modifier.width(4.dp))
                                Text("List", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onPostRequirement,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, ReLoopBorder),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Post Demand", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            }
                        }
                    }
                }
            }

            // Section Header & Quick Filter Pills
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedCategory == "All") "Available Industrial Feedstocks" else "$selectedCategory Materials",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        TextButton(
                            onClick = onNavigateToExplore,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("View All", color = BrandForest, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            Spacer(Modifier.width(2.dp))
                            Icon(Icons.Default.ChevronRight, null, tint = BrandForest, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Quick Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickFilters) { filter ->
                            val isSelected = selectedQuickFilter == filter
                            Surface(
                                color = if (isSelected) BrandForest else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) BrandForest else ReLoopBorder),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedQuickFilter = filter }
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextMuted,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Material Listing Feed
            if (filteredMaterials.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, ReLoopBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, null, tint = TextMuted, modifier = Modifier.size(40.dp))
                            Text("No Materials Found", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            Text(
                                "No materials match your current criteria. Try resetting filters or post a requirement.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = "All"
                                    selectedQuickFilter = "All"
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
                            ) {
                                Text("Reset Filters", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(filteredMaterials, key = { it.id }) { item ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        MaterialListingCard(
                            item = item,
                            onClick = { onOpenMaterial(item) },
                            onRequestQuote = { materialForQuote = item },
                            onViewPassport = { onViewPassport(item) }
                        )
                    }
                }
            }
        }

        // Enquiry / Request Quote Modal Dialog
        materialForQuote?.let { mat ->
            RequestQuoteDialog(
                material = mat,
                onDismiss = { materialForQuote = null },
                onSubmit = { qty, city, contact, message ->
                    MaterialRepository.submitQuoteRequest(
                        material = mat,
                        buyerName = contact.split("·").firstOrNull()?.trim() ?: "My Business",
                        buyerContact = contact,
                        quantityTonnes = qty,
                        deliveryCity = city,
                        message = message
                    )
                    quoteSubmittedNotice = "RFQ sent to ${mat.sellerName} for $qty ${mat.unit}."
                    materialForQuote = null
                }
            )
        }
    }
}
