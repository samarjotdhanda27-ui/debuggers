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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        Icon(Icons.Default.Tune, contentDescription = "Filters", tint = BrandForest, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = ReLoopPageBg,
            focusedContainerColor = Color.White,
            unfocusedBorderColor = ReLoopBorder,
            focusedBorderColor = BrandForest
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
 * Realistic B2B product listing card (IndiaMART / Amazon Business style) without AI clutter.
 */
@Composable
fun MaterialListingCard(
    item: MaterialItem,
    onClick: () -> Unit,
    onRequestQuote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ReLoopBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category tag and verification status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandForest,
                    letterSpacing = 0.5.sp
                )

                if (item.isVerifiedListing) {
                    Surface(
                        color = StatusVerifiedBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.Verified, null, tint = StatusVerified, modifier = Modifier.size(11.dp))
                            Text(
                                text = "VERIFIED LOT",
                                color = StatusVerified,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }
                } else {
                    Surface(
                        color = StatusDeclaredBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "SELLER DECLARED",
                            color = StatusDeclared,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }

            // Material Title and Seller
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = BrandMintBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (item.category) {
                                "Metals" -> Icons.Default.PrecisionManufacturing
                                "Minerals & By-Products" -> Icons.Default.Factory
                                "Plastics" -> Icons.Default.Recycling
                                "Construction" -> Icons.Default.Foundation
                                "Chemicals" -> Icons.Default.Science
                                "Paper" -> Icons.Default.Description
                                else -> Icons.Default.Inventory2
                            },
                            contentDescription = null,
                            tint = BrandForest,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.materialName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.sellerName} · ${item.displayLocation}",
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Specification Clean Bullet
            item.grade?.let { gr ->
                Text(
                    text = "Specification: ${gr.removePrefix("Grade: ").removePrefix("Fineness: ")}",
                    fontSize = 12.sp,
                    color = TextDark,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = ReLoopBorder, thickness = 0.5.dp)

            // Commercial Row: Price on left, Quantity on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.priceDisplay,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandForest
                    )
                    Text(
                        text = item.pricingBasis,
                        fontSize = 10.5.sp,
                        color = TextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = item.displayQuantity,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = item.availabilityStatus,
                        fontSize = 10.5.sp,
                        color = StatusVerified
                    )
                }
            }

            // Compact Action Row: "Request Quote" primary + "Details ➔"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRequestQuote,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
                ) {
                    Text("Request Quote", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ReLoopBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text("Details", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Spacer(Modifier.width(2.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(13.dp), tint = TextMuted)
                }
            }
        }
    }
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
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(material.materialName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandForest)
                        Text("${material.displayQuantity} available · ${material.priceDisplay}", fontSize = 12.sp, color = TextMuted)
                    }
                    Surface(color = StatusVerifiedBg, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = material.pricingBasis,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusVerified,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = ReLoopBorder,
                        focusedBorderColor = BrandForest
                    )
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
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = ReLoopBorder,
                        focusedBorderColor = BrandForest
                    )
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
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = ReLoopBorder,
                        focusedBorderColor = BrandForest
                    )
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
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = ReLoopBorder,
                        focusedBorderColor = BrandForest
                    )
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
