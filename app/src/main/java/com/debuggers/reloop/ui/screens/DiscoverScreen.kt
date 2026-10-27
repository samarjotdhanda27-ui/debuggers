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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debuggers.reloop.data.model.MaterialItem
import com.debuggers.reloop.ui.components.MaterialCard
import com.debuggers.reloop.ui.theme.*

@Composable
fun DiscoverScreen(
    materials: List<MaterialItem>,
    onOpenMaterial: (MaterialItem) -> Unit,
    onFindMatches: (MaterialItem) -> Unit,
    onViewPassport: (MaterialItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedRegion by remember { mutableStateOf("All Regions") }
    var sortBy by remember { mutableStateOf("Default") }
    var regionDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf("All", "Mineral", "Foundry", "Metal", "Process", "Slag")
    val regions = listOf("All Regions", "Punjab", "Haryana", "Chandigarh")

    val filtered = materials.filter { item ->
        val textMatch = query.isBlank() ||
                item.materialName.contains(query, true) ||
                item.sellerName.contains(query, true) ||
                item.city.contains(query, true)
        val catMatch = selectedCategory == "All" ||
                item.materialName.contains(selectedCategory, true) ||
                item.sourceIndustry.contains(selectedCategory, true) ||
                item.shortCategory.contains(selectedCategory, true)
        val regMatch = selectedRegion == "All Regions" || item.state.contains(selectedRegion, true) || item.city.contains(selectedRegion, true)
        textMatch && catMatch && regMatch
    }.sortedWith { a, b ->
        when (sortBy) {
            "Quantity: High to Low" -> b.quantityTonnes.compareTo(a.quantityTonnes)
            "Quantity: Low to High" -> a.quantityTonnes.compareTo(b.quantityTonnes)
            "Verified First" -> b.isSynthetic.compareTo(a.isSynthetic) // false comes before true
            else -> 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReLoopPageBg)
    ) {
        // Sticky Header with Search & Filter
        Surface(
            color = Color.White,
            border = BorderStroke(0.5.dp, ReLoopBorder),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Material Marketplace",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )

                // Search Bar
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by-product type, specifications…", fontSize = 13.5.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, null, tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = ReLoopPageBg,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = ReLoopBorder,
                        focusedBorderColor = BrandForest
                    )
                )

                // Category Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandLime,
                                selectedLabelColor = BrandLimeDark,
                                containerColor = ReLoopSurfaceSubtle,
                                labelColor = TextMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == cat,
                                borderColor = Color.Transparent,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                // Region & Sort Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        OutlinedButton(
                            onClick = { regionDropdownExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            border = BorderStroke(1.dp, ReLoopBorder)
                        ) {
                            Icon(Icons.Default.FilterList, null, modifier = Modifier.size(14.dp), tint = BrandForest)
                            Spacer(Modifier.width(6.dp))
                            Text(selectedRegion, fontSize = 12.sp, color = TextDark)
                        }

                        DropdownMenu(
                            expanded = regionDropdownExpanded,
                            onDismissRequest = { regionDropdownExpanded = false }
                        ) {
                            regions.forEach { reg ->
                                DropdownMenuItem(
                                    text = { Text(reg, fontSize = 13.sp) },
                                    onClick = {
                                        selectedRegion = reg
                                        regionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "${filtered.size} available listings",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Listings Feed
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
                    Icon(Icons.Default.SearchOff, null, tint = TextSubtle, modifier = Modifier.size(48.dp))
                    Text("No Matching Materials", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Text("Try broadening your search query or reset the filters.", color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    MaterialCard(
                        item = item,
                        onClick = { onOpenMaterial(item) },
                        onFindMatches = { onFindMatches(item) },
                        onViewPassport = { onViewPassport(item) }
                    )
                }
            }
        }
    }
}
