package com.debuggers.reloop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debuggers.reloop.data.repository.MaterialRepository
import com.debuggers.reloop.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ListMaterialScreen(
    onBack: () -> Unit,
    onSuccessCreated: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var sellerName by remember { mutableStateOf("") }
    var materialName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Minerals & By-Products") }
    var selectedIndustry by remember { mutableStateOf(MaterialRepository.supportedIndustries.first()) }
    var quantityText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("tonnes") }
    var priceQuotePreference by remember { mutableStateOf("Quote on Request") } // or "Fixed Price"
    var priceText by remember { mutableStateOf("") }
    var gradeText by remember { mutableStateOf("") }
    var conditionText by remember { mutableStateOf("") }
    var selectedCityKey by remember { mutableStateOf(MaterialRepository.regionalCoordinates.keys.first()) }
    var descriptionText by remember { mutableStateOf("") }

    val selectedDocs = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }

    // In-line validation error states
    var sellerError by remember { mutableStateOf<String?>(null) }
    var materialError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var submissionError by remember { mutableStateOf<String?>(null) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var industryDropdownExpanded by remember { mutableStateOf(false) }
    var cityDropdownExpanded by remember { mutableStateOf(false) }

    val categoryList = listOf("Minerals & By-Products", "Metals", "Construction", "Plastics", "Chemicals", "Paper")

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
                        text = "List Material / By-Product",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Create an industrial B2B marketplace listing",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Form Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Basic Information
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("MATERIAL IDENTITY", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                    // Material Name
                    Text("Material Name *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    OutlinedTextField(
                        value = materialName,
                        onValueChange = {
                            materialName = it
                            if (it.isNotBlank()) materialError = null
                        },
                        placeholder = { Text("e.g. High Purity Fly Ash, Recycled HDPE Regrind", fontSize = 13.sp) },
                        isError = materialError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    materialError?.let { Text(it, color = AlertRed, fontSize = 11.sp) }

                    // Category
                    Text("Material Category *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Box {
                        OutlinedButton(
                            onClick = { categoryDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ReLoopBorder)
                        ) {
                            Text(selectedCategory, modifier = Modifier.weight(1f), color = TextDark, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categoryList.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, fontSize = 13.sp) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Generating Industry
                    Text("Source Industry Sector *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Box {
                        OutlinedButton(
                            onClick = { industryDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ReLoopBorder)
                        ) {
                            Text(selectedIndustry, modifier = Modifier.weight(1f), color = TextDark, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                        DropdownMenu(
                            expanded = industryDropdownExpanded,
                            onDismissRequest = { industryDropdownExpanded = false }
                        ) {
                            MaterialRepository.supportedIndustries.forEach { ind ->
                                DropdownMenuItem(
                                    text = { Text(ind, fontSize = 13.sp) },
                                    onClick = {
                                        selectedIndustry = ind
                                        industryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Seller Name
                    Text("Seller / Facility Business Name *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    OutlinedTextField(
                        value = sellerName,
                        onValueChange = {
                            sellerName = it
                            if (it.isNotBlank()) sellerError = null
                        },
                        placeholder = { Text("e.g. Rajpura Thermal Hub, Vardhman Steel", fontSize = 13.sp) },
                        isError = sellerError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    sellerError?.let { Text(it, color = AlertRed, fontSize = 11.sp) }
                }
            }

            // Section 2: Quantity & Pricing Basis
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("QUANTITY & COMMERCIAL TERMS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(Modifier.weight(1.5f)) {
                            Text("Available Quantity *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = {
                                    quantityText = it
                                    if (it.toDoubleOrNull() != null) quantityError = null
                                },
                                placeholder = { Text("e.g. 500", fontSize = 13.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = quantityError != null,
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(Modifier.weight(1f)) {
                            Text("Unit", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    quantityError?.let { Text(it, color = AlertRed, fontSize = 11.sp) }

                    // Pricing Preference
                    Text("Pricing Basis", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Quote on Request", "Fixed Price / MT").forEach { pref ->
                            val isSelected = priceQuotePreference == pref
                            Surface(
                                color = if (isSelected) BrandForest else ReLoopPageBg,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) BrandForest else ReLoopBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { priceQuotePreference = pref }
                            ) {
                                Text(
                                    text = pref,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextDark,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    if (priceQuotePreference == "Fixed Price / MT") {
                        Text("Price in INR (per unit) *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = {
                                priceText = it
                                if (it.toDoubleOrNull() != null) priceError = null
                            },
                            placeholder = { Text("e.g. 420 or 34500", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = priceError != null,
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        priceError?.let { Text(it, color = AlertRed, fontSize = 11.sp) }
                    }
                }
            }

            // Section 3: Specifications & Origin Location
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("SPECIFICATIONS & LOGISTICS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                    Text("Material Grade / Standard (Optional)", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    OutlinedTextField(
                        value = gradeText,
                        onValueChange = { gradeText = it },
                        placeholder = { Text("e.g. Class F (IS 3812), HMS 1/2", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Physical Condition (Optional)", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    OutlinedTextField(
                        value = conditionText,
                        onValueChange = { conditionText = it },
                        placeholder = { Text("e.g. Dry Silo Powder, Clean Flakes, Baled", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Collection Origin Hub / City *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Box {
                        OutlinedButton(
                            onClick = { cityDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ReLoopBorder)
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = BrandForest, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(selectedCityKey, modifier = Modifier.weight(1f), color = TextDark, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                        }
                        DropdownMenu(
                            expanded = cityDropdownExpanded,
                            onDismissRequest = { cityDropdownExpanded = false }
                        ) {
                            MaterialRepository.regionalCoordinates.keys.forEach { cityKey ->
                                DropdownMenuItem(
                                    text = { Text(cityKey, fontSize = 13.sp) },
                                    onClick = {
                                        selectedCityKey = cityKey
                                        cityDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text("Listing Description & Material Notes", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        placeholder = { Text("Describe application suitability, handling instructions, loading logistics…", fontSize = 12.5.sp) },
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Section 4: Available Documentation
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("VERIFICATION DOCUMENTS AVAILABLE", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)
                    Text("Check documents your facility can provide for this lot:", fontSize = 12.sp, color = TextMuted)

                    MaterialRepository.standardDocumentsList.forEach { doc ->
                        val isChecked = selectedDocs.contains(doc)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedDocs.remove(doc) else selectedDocs.add(doc)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) selectedDocs.add(doc) else selectedDocs.remove(doc)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = BrandForest)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(doc, fontSize = 12.5.sp, color = TextDark)
                        }
                    }
                }
            }

            // Submission Error Banner if any
            submissionError?.let { err ->
                Surface(
                    color = AlertRedBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(err, color = AlertRed, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // Submit Button
            Button(
                onClick = {
                    var hasError = false
                    val qty = quantityText.trim().toDoubleOrNull()
                    val price = priceText.trim().toDoubleOrNull()

                    if (materialName.isBlank()) {
                        materialError = "Please enter the material name."
                        hasError = true
                    }
                    if (sellerName.isBlank()) {
                        sellerError = "Please enter your facility name."
                        hasError = true
                    }
                    if (qty == null || qty <= 0) {
                        quantityError = "Please enter a valid quantity."
                        hasError = true
                    }
                    if (priceQuotePreference == "Fixed Price / MT" && (price == null || price <= 0)) {
                        priceError = "Please enter a valid price."
                        hasError = true
                    }

                    if (hasError) return@Button

                    isSubmitting = true
                    submissionError = null

                    val coords = MaterialRepository.regionalCoordinates[selectedCityKey] ?: Pair(30.478, 76.593)
                    val cityParts = selectedCityKey.split(",")
                    val city = cityParts.getOrElse(0) { selectedCityKey }.trim()
                    val state = cityParts.getOrElse(1) { "Punjab" }.trim()

                    val props = mutableMapOf<String, Any>(
                        "unit" to unit.trim(),
                        "category" to selectedCategory,
                        "pricing_basis" to if (priceQuotePreference == "Fixed Price / MT") "Ex-Works Fixed" else "Quote on Request",
                        "availability" to "In Stock · Dispatch Ready"
                    )
                    if (gradeText.isNotBlank()) props["grade"] = gradeText.trim()
                    if (conditionText.isNotBlank()) props["condition"] = conditionText.trim()
                    if (descriptionText.isNotBlank()) props["description"] = descriptionText.trim()
                    if (price != null) props["price_per_tonne"] = price

                    coroutineScope.launch {
                        val (success, message) = MaterialRepository.createMaterial(
                            sellerName = sellerName,
                            materialName = materialName,
                            sourceIndustry = selectedIndustry,
                            quantityTonnes = qty ?: 0.0,
                            city = city,
                            state = state,
                            latitude = coords.first,
                            longitude = coords.second,
                            properties = props,
                            availableDocuments = selectedDocs.toList()
                        )
                        isSubmitting = false
                        if (success) {
                            onSuccessCreated()
                        } else {
                            submissionError = message
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Submitting to ReLoop Network…", fontSize = 14.sp)
                } else {
                    Icon(Icons.Default.CloudUpload, null, tint = BrandLime, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Publish Material Listing", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
