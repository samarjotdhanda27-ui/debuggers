package com.debuggers.reloop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
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
import com.debuggers.reloop.data.repository.MaterialRepository
import com.debuggers.reloop.ui.components.MarketplaceSearchBar
import com.debuggers.reloop.ui.components.MaterialListingCard
import com.debuggers.reloop.ui.components.RequestQuoteBottomSheet
import com.debuggers.reloop.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    materials: List<MaterialItem>,
    onOpenMaterial: (MaterialItem) -> Unit,
    onViewPassport: (MaterialItem) -> Unit,
    onOpenMap: (() -> Unit)? = null
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedRegion by remember { mutableStateOf("All Regions") }
    var selectedMinQuantity by remember { mutableStateOf(0.0) }
    var onlyVerified by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("Newest First") }

    var sortDropdownExpanded by remember { mutableStateOf(false) }
    var regionDropdownExpanded by remember { mutableStateOf(false) }
    var materialForQuote by remember { mutableStateOf<MaterialItem?>(null) }
    var quoteSubmittedNotice by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Minerals & By-Products", "Metals", "Construction", "Plastics", "Chemicals", "Paper")
    val regions = listOf("All Regions", "Punjab", "Haryana", "Himachal Pradesh", "NCR / Delhi")
    val sortOptions = listOf("Newest First", "Quantity: High to Low", "Quantity: Low to High", "Verified First")

    val filtered = materials.filter { item ->
        val textMatch = query.isBlank() ||
                item.materialName.contains(query, true) ||
                item.sellerName.contains(query, true) ||
                item.city.contains(query, true) ||
                item.category.contains(query, true) ||
                (item.grade?.contains(query, true) == true)

        val catMatch = selectedCategory == "All" ||
                item.category.equals(selectedCategory, ignoreCase = true) ||
                (selectedCategory == "Minerals & By-Products" && item.category.contains("Mineral", true))

        val regMatch = when (selectedRegion) {
            "All Regions" -> true
            "Punjab" -> item.state.contains("Punjab", true) || item.city.contains("Punjab", true)
            "Haryana" -> item.state.contains("Haryana", true)
            "Himachal Pradesh" -> item.state.contains("Himachal", true)
            "NCR / Delhi" -> item.city.contains("Gurugram", true) || item.city.contains("Faridabad", true) || item.state.contains("NCR", true)
            else -> true
        }

        val qtyMatch = item.quantityTonnes >= selectedMinQuantity
        val verifiedMatch = !onlyVerified || item.isVerifiedListing

        textMatch && catMatch && regMatch && qtyMatch && verifiedMatch
    }.sortedWith { a, b ->
        when (sortBy) {
            "Quantity: High to Low" -> b.quantityTonnes.compareTo(a.quantityTonnes)
            "Quantity: Low to High" -> a.quantityTonnes.compareTo(b.quantityTonnes)
            "Verified First" -> b.isVerifiedListing.compareTo(a.isVerifiedListing)
            else -> 0
        }
    }

    val activeFiltersCount = (if (selectedCategory != "All") 1 else 0) +
            (if (selectedRegion != "All Regions") 1 else 0) +
            (if (selectedMinQuantity > 0) 1 else 0) +
            (if (onlyVerified) 1 else 0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ReLoopPageBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Search & Filter Bar
            Surface(
                color = Color.White,
                border = BorderStroke(0.5.dp, ReLoopBorder),
                shadowElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MarketplaceSearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = "Search materials, suppliers, specs…"
                    )

                    // Horizontal Category Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                color = if (isSelected) BrandForest else ReLoopSurfaceSubtle,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) BrandForest else ReLoopBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Region, Sort & Verified Filter Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Region Dropdown
                        Box {
                            OutlinedButton(
                                onClick = { regionDropdownExpanded = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, ReLoopBorder)
                            ) {
                                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(13.dp), tint = BrandForest)
                                Spacer(Modifier.width(4.dp))
                                Text(selectedRegion, fontSize = 11.5.sp, color = TextDark)
                                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp), tint = TextMuted)
                            }
                            DropdownMenu(
                                expanded = regionDropdownExpanded,
                                onDismissRequest = { regionDropdownExpanded = false }
                            ) {
                                regions.forEach { reg ->
                                    DropdownMenuItem(
                                        text = { Text(reg, fontSize = 12.5.sp) },
                                        onClick = {
                                            selectedRegion = reg
                                            regionDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Hub Map Button
                        onOpenMap?.let { openMap ->
                            OutlinedButton(
                                onClick = openMap,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = BrandMintBg)
                            ) {
                                Icon(Icons.Default.Map, null, modifier = Modifier.size(13.dp), tint = BrandPrimary)
                                Spacer(Modifier.width(4.dp))
                                Text("Map", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                            }
                        }

                        // Sort Dropdown
                        Box {
                            OutlinedButton(
                                onClick = { sortDropdownExpanded = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, ReLoopBorder)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Sort, null, modifier = Modifier.size(13.dp), tint = BrandForest)
                                Spacer(Modifier.width(4.dp))
                                Text(sortBy, fontSize = 11.5.sp, color = TextDark)
                                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp), tint = TextMuted)
                            }
                            DropdownMenu(
                                expanded = sortDropdownExpanded,
                                onDismissRequest = { sortDropdownExpanded = false }
                            ) {
                                sortOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt, fontSize = 12.5.sp) },
                                        onClick = {
                                            sortBy = opt
                                            sortDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Applied Filters Pill Row
                    if (activeFiltersCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Showing ${filtered.size} of ${materials.size} listings",
                                fontSize = 11.5.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )

                            TextButton(
                                onClick = {
                                    query = ""
                                    selectedCategory = "All"
                                    selectedRegion = "All Regions"
                                    selectedMinQuantity = 0.0
                                    onlyVerified = false
                                    sortBy = "Newest First"
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Clear All Filters", fontSize = 11.5.sp, color = BrandForest, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Results count header
            if (activeFiltersCount == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Material Listings (${filtered.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }

            // Notice banner if quote submitted
            quoteSubmittedNotice?.let { msg ->
                Surface(
                    color = StatusVerifiedBg,
                    border = BorderStroke(1.dp, StatusVerified.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(msg, fontSize = 12.sp, color = StatusVerified, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { quoteSubmittedNotice = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, null, tint = StatusVerified, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Content Feed
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.FilterListOff, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Text("No Matching Materials", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                        Text(
                            "No industrial listings match the chosen filters. Try resetting region or keyword filters.",
                            color = TextMuted,
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = {
                                query = ""
                                selectedCategory = "All"
                                selectedRegion = "All Regions"
                                selectedMinQuantity = 0.0
                                onlyVerified = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
                        ) {
                            Text("Reset Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { item ->
                        MaterialListingCard(
                            item = item,
                            onClick = { onOpenMaterial(item) },
                            onRequestQuote = { materialForQuote = item }
                        )
                    }
                }
            }
        }

        // Enquiry / Request Quote Modal Bottom Sheet
        materialForQuote?.let { mat ->
            RequestQuoteBottomSheet(
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
                    quoteSubmittedNotice = "RFQ sent to ${mat.sellerName} for $qty ${mat.unit} of ${mat.materialName}."
                    materialForQuote = null
                }
            )
        }
    }
}
