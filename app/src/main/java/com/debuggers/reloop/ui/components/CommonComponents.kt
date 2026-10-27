package com.debuggers.reloop.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.debuggers.reloop.data.model.*
import com.debuggers.reloop.data.repository.MaterialRepository
import com.debuggers.reloop.ui.theme.*

@Composable
fun ReLoopTopBar(
    serverHealth: ServerHealth,
    currentLocation: String = "Punjab Hub",
    onLocationClick: () -> Unit,
    onHealthClick: () -> Unit,
    onProfileClick: () -> Unit
) {
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = BrandForest,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "ReLoop Logo",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ReLoop",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandForest,
                            letterSpacing = (-0.3).sp
                        )
                    }
                    Text(
                        text = "Industrial Material Exchange",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }
            }

            // Right: Location Chip, Cloud Status & Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Location Selector Pill
                Surface(
                    color = ReLoopSurfaceSubtle,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onLocationClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = BrandEmerald, modifier = Modifier.size(12.dp))
                        Text(
                            text = currentLocation,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }

                // Map Quick Access Button
                IconButton(
                    onClick = onLocationClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Industrial Corridor Map",
                        tint = BrandCobalt,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Cloud Status Dot
                IconButton(
                    onClick = onHealthClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = if (serverHealth.isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = "Cloud Engine Status",
                            tint = if (serverHealth.isOnline) BrandEmerald else AlertRed,
                            modifier = Modifier.size(19.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (serverHealth.isOnline) BrandEmerald else AlertRed)
                        )
                    }
                }

                // Profile Avatar Button
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Surface(
                        color = ReLoopSurfaceSubtle,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, ReLoopBorder),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = "Account", tint = TextDark, modifier = Modifier.size(17.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Universal, high-contrast text field styling ensuring 100% readability across all devices and themes.
 */
@Composable
fun reloopTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextDark,
    unfocusedTextColor = TextDark,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = BrandPrimary,
    unfocusedBorderColor = ReLoopBorder,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted,
    cursorColor = BrandPrimary
)

@Composable
fun MarketplaceSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search materials, grades, suppliers…",
    onFilterClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, fontSize = 13.5.sp, color = TextMuted) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
                if (onFilterClick != null) {
                    IconButton(onClick = onFilterClick) {
                        Icon(Icons.Default.Tune, contentDescription = "Filters", tint = BrandPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark,
            unfocusedContainerColor = ReLoopPageBg,
            focusedContainerColor = Color.White,
            unfocusedBorderColor = ReLoopBorder,
            focusedBorderColor = BrandPrimary,
            focusedPlaceholderColor = TextMuted,
            unfocusedPlaceholderColor = TextMuted,
            cursorColor = BrandPrimary
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun CategoryShortcutItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) BrandForest else Color.White,
        border = BorderStroke(1.dp, if (isSelected) BrandForest else ReLoopBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else BrandForest,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextDark
            )
        }
    }
}

/**
 * Clean B2B Supplier Portal action card that NEVER overflows or squeezes buttons.
 */
@Composable
fun SupplierPortalCard(
    onListMaterial: () -> Unit,
    onPostDemand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, ReLoopBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = BrandMintBg,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Storefront, null, tint = BrandForest, modifier = Modifier.size(16.dp))
                        }
                    }
                    Column {
                        Text("Industrial Supplier Portal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("List by-products or post procurement demand", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onListMaterial,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandForest),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("List Material", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onPostDemand,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ReLoopBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.PostAdd, null, modifier = Modifier.size(14.dp), tint = BrandForest)
                    Spacer(Modifier.width(4.dp))
                    Text("Post Demand", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                }
            }
        }
    }
}

/**
 * Modern Regional Materials Transit Corridor Banner with direct one-tap interactive map access.
 */
@Composable
fun RegionalCorridorBanner(
    activeHubsCount: Int = 12,
    activeLotsCount: Int,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BrandNavy,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BrandCobalt.copy(alpha = 0.3f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenMap)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = BrandEmeraldLight, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = "NORTH INDIA INDUSTRIAL CORRIDOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandEmeraldLight,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(BrandEmeraldLight))
                        Text(
                            text = "LIVE SATELLITE MAP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "Punjab, Haryana & NCR Materials Network",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Explore $activeLotsCount active industrial lots and procurement demands mapped across $activeHubsCount regional clusters.",
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 16.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Ludhiana", "Mandi Gobindgarh", "Rajpura", "Panipat", "Baddi").take(4).forEach { hub ->
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = hub,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "View Map",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Realistic B2B product listing card (Amazon Business / IndiaMART style) with real industrial photography and one-tap Map integration.
 */
@Composable
fun MaterialListingCard(
    item: MaterialItem,
    onClick: () -> Unit,
    onRequestQuote: () -> Unit,
    onViewOnMap: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ReLoopBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Top Section: Visual Photo Thumbnail on Left + Core Details on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // High-resolution visual product photo thumbnail with badges
                Box(
                    modifier = Modifier
                        .size(width = 108.dp, height = 112.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ReLoopSurfaceSubtle)
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.materialName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlaid Verification Pill on top-start
                    if (item.isVerifiedListing) {
                        Surface(
                            color = StatusVerified,
                            shape = RoundedCornerShape(bottomEnd = 6.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                Text(
                                    text = "VERIFIED",
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = StatusDeclared,
                            shape = RoundedCornerShape(bottomEnd = 6.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "DECLARED",
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Chemical/Material Symbol Badge on bottom-end
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(topStart = 6.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = item.symbol,
                            color = BrandEmeraldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Middle/Right Content Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Category Tag
                    Text(
                        text = item.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCobalt,
                        letterSpacing = 0.4.sp
                    )

                    // Material Name
                    Text(
                        text = item.materialName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp
                    )

                    // Seller Name & City with Location Pin
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = TextSubtle, modifier = Modifier.size(12.dp))
                        Text(
                            text = "${item.sellerName} · ${item.displayLocation}",
                            fontSize = 11.5.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Specification bullet
                    item.grade?.let { gr ->
                        Text(
                            text = "Spec: ${gr.removePrefix("Grade: ").removePrefix("Fineness: ")}",
                            fontSize = 11.5.sp,
                            color = TextDark,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    // Price & Stock Available Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = item.priceDisplay,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandNavy
                            )
                            Text(
                                text = item.pricingBasis,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = item.displayQuantity,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = item.availabilityStatus,
                                fontSize = 10.sp,
                                color = if (item.isVerifiedListing) StatusVerified else TextMuted
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = ReLoopBorder, thickness = 0.5.dp)

            // Action Row: "Request Quote" (Primary) + "Map" (Direct plant map locator) + "Details ➔"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRequestQuote,
                    modifier = Modifier.weight(1.3f).height(36.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCobalt),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(13.dp), tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Request Quote", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                if (onViewOnMap != null) {
                    OutlinedButton(
                        onClick = onViewOnMap,
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, ReLoopBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Place, null, modifier = Modifier.size(14.dp), tint = BrandCobalt)
                        Spacer(Modifier.width(3.dp))
                        Text("Map", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    }
                }

                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ReLoopBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("Details", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Spacer(Modifier.width(2.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(12.dp), tint = TextMuted)
                }
            }
        }
    }
}

/**
 * ReLoop Match Intelligence Card.
 * Displays:
 * 1. AI Compatibility Score (0 - 100%)
 * 2. Real-time Status Badge (e.g. HIGH MATCH · REGIONAL DEMAND)
 * 3. Specific explanations/reasons for the match
 * 4. Technical score breakdown points
 */
@Composable
fun ReLoopIntelligenceCard(
    compatibilityScore: Double,
    statusText: String,
    reasons: List<String>,
    scoreBreakdown: ScoreBreakdown? = null,
    onExploreMatches: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Sparkle + Label + Score Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = BrandMintBg,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(15.dp))
                        }
                    }
                    Text(
                        text = "CIRCULAR INTELLIGENCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    color = if (compatibilityScore >= 80) StatusVerifiedBg else StatusDeclaredBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, if (compatibilityScore >= 80) StatusVerified.copy(alpha = 0.4f) else StatusDeclared.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${compatibilityScore.toInt()}% FIT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (compatibilityScore >= 80) StatusVerified else StatusDeclared,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Status Banner
            Surface(
                color = ReLoopSurfaceSubtle,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(if (compatibilityScore >= 80) StatusVerified else StatusDeclared, CircleShape)
                    )
                    Text(
                        text = statusText.uppercase(),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        letterSpacing = 0.4.sp
                    )
                }
            }

            // Match Reasons Section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Key Compatibility Reasons:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )

                reasons.take(3).forEach { reason ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = reason,
                            fontSize = 12.sp,
                            color = TextDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Score Breakdown Bars if available
            scoreBreakdown?.let { sb ->
                HorizontalDivider(color = ReLoopBorder, thickness = 0.5.dp)
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Multi-Criteria Score Breakdown:", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                    BreakdownBar("Technical Specifications Fit", sb.propertyCompatibilityPoints, 35.0)
                    BreakdownBar("Spatial & Transit Proximity", sb.proximityPoints, 25.0)
                    BreakdownBar("Documentation & Regulatory Clearances", sb.documentationPoints, 25.0)
                    BreakdownBar("Intake Volume Fulfillment", sb.quantityPoints, 15.0)
                }
            }

            // Action to view detailed matches
            if (onExploreMatches != null) {
                Button(
                    onClick = onExploreMatches,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Explore Matching Buyer Facilities", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}

@Composable
private fun BreakdownBar(label: String, points: Double, maxPoints: Double) {
    val progress = (points / maxPoints).coerceIn(0.0, 1.0).toFloat()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = TextMuted, modifier = Modifier.weight(1f))
        Text("${points.toInt()} / ${maxPoints.toInt()} pts", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
    }
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
        color = BrandPrimary,
        trackColor = ReLoopSurfaceSubtle
    )
}

/**
 * Modern B2B Modal Bottom Sheet for Requesting a Quote (replaces clunky AlertDialog).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestQuoteBottomSheet(
    material: MaterialItem,
    onDismiss: () -> Unit,
    onSubmit: (quantity: Double, city: String, contact: String, message: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var quantityText by remember { mutableStateOf(material.quantityTonnes.toInt().toString()) }
    var destinationCity by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("We are interested in procuring this material. Please share your commercial terms and delivery timeline.") }
    var errorMessage by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = ReLoopBorderStrong) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Request Commercial Quote", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("Supplier: ${material.sellerName}", fontSize = 12.sp, color = TextMuted)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            // Material Summary Card
            Surface(
                color = ReLoopPageBg,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ReLoopBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    ) {
                        Text(
                            text = material.materialName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BrandPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${material.displayQuantity} available · ${material.priceDisplay}",
                            fontSize = 12.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        color = StatusVerifiedBg,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, StatusVerified.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = material.pricingBasis,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusVerified,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Quantity Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Procurement Quantity (${material.unit}) *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = reloopTextFieldColors()
                )
            }

            // Destination City
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Delivery Destination City *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = destinationCity,
                    onValueChange = { destinationCity = it },
                    placeholder = { Text("e.g. Ludhiana, Punjab", fontSize = 12.5.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = reloopTextFieldColors()
                )
            }

            // Contact Info
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Buyer Facility / Phone Number *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    placeholder = { Text("e.g. Eco Build Works · +91 98765 43210", fontSize = 12.5.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = reloopTextFieldColors()
                )
            }

            // Notes / Message
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Commercial Notes & Logistics Inquiry", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(8.dp),
                    colors = reloopTextFieldColors()
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = AlertRed, fontSize = 12.sp)
            }

            // Action Buttons
            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull()
                    if (qty == null || qty <= 0) {
                        errorMessage = "Please enter a valid procurement quantity."
                        return@Button
                    }
                    if (destinationCity.isBlank()) {
                        errorMessage = "Please enter your delivery city."
                        return@Button
                    }
                    if (contactInfo.isBlank()) {
                        errorMessage = "Please provide your facility name or contact number."
                        return@Button
                    }
                    onSubmit(qty, destinationCity, contactInfo, message)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
            ) {
                Text("Send Official RFQ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))
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
                    tint = if (health.isOnline) BrandForest else AlertRed
                )
                Text("Backend Infrastructure", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "ReLoop connects directly to a live FastAPI engine backed by Supabase PostgreSQL.",
                    fontSize = 12.5.sp,
                    color = TextMuted
                )
                HorizontalDivider(color = ReLoopBorder)
                DiagRow("Cloud Host", "reloop-ui.onrender.com")
                DiagRow("Server Status", if (health.isOnline) "ONLINE (Active)" else "CONNECTING / SLEEPING")
                DiagRow("Database", health.database)
                DiagRow("Network Latency", "${health.latencyMs} ms")
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
                Text("Ping Server")
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
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextDark)
    }
}
