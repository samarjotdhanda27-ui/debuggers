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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debuggers.reloop.data.repository.MaterialRepository
import com.debuggers.reloop.ui.components.reloopTextFieldColors
import com.debuggers.reloop.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PostRequirementScreen(
    onBack: () -> Unit,
    onSuccessSubmitted: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var buyerName by remember { mutableStateOf("") }
    var selectedIndustry by remember { mutableStateOf("Cement & Concrete Manufacturing") }
    var requiredMaterial by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var selectedCityKey by remember { mutableStateOf(MaterialRepository.regionalCoordinates.keys.first()) }
    var maxMoistureText by remember { mutableStateOf("") }

    val selectedDocs = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }

    var buyerError by remember { mutableStateOf<String?>(null) }
    var materialError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var submissionError by remember { mutableStateOf<String?>(null) }

    var industryDropdownExpanded by remember { mutableStateOf(false) }
    var cityDropdownExpanded by remember { mutableStateOf(false) }

    val buyerIndustries = listOf(
        "Cement & Concrete Manufacturing",
        "Brick & Masonry Units",
        "Road & Highway Construction",
        "Steel & Metallurgy",
        "Plastics & Compounding",
        "Chemical Processing",
        "Paper & Packaging"
    )

    val commonFeedstocks = listOf("Fly Ash", "Foundry Sand", "Steel Scrap", "Recycled Aggregate", "HDPE Regrind")

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
                        text = "Post Feedstock Requirement",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Broadcast industrial demand to regional suppliers",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Form Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Buyer Facility Information Card
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("BUYER FACILITY IDENTITY", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                    Text("Procuring Business / Buyer Facility Name *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    OutlinedTextField(
                        value = buyerName,
                        onValueChange = {
                            buyerName = it
                            if (it.isNotBlank()) buyerError = null
                        },
                        placeholder = { Text("e.g. Ambala Green Cement Works, Eco Pavers", fontSize = 13.sp, color = TextMuted) },
                        isError = buyerError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = reloopTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    buyerError?.let { Text(it, color = AlertRed, fontSize = 11.sp) }

                    Text("Buyer Industry Sector *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
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
                            buyerIndustries.forEach { ind ->
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
                }
            }

            // Material Demand Card
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("FEEDSTOCK SPECIFICATIONS & INTAKE", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                    Text("Target Raw Feedstock / Material *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    OutlinedTextField(
                        value = requiredMaterial,
                        onValueChange = {
                            requiredMaterial = it
                            if (it.isNotBlank()) materialError = null
                        },
                        placeholder = { Text("e.g. Fly Ash, Used Foundry Sand, Steel Scrap", fontSize = 13.sp, color = TextMuted) },
                        isError = materialError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = reloopTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    materialError?.let { Text(it, color = AlertRed, fontSize = 11.sp) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        commonFeedstocks.take(3).forEach { feed ->
                            AssistChip(
                                onClick = { requiredMaterial = feed },
                                label = {
                                    Text(
                                        text = feed,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = ReLoopSurfaceSubtle,
                                    labelColor = TextDark
                                ),
                                border = BorderStroke(1.dp, ReLoopBorder)
                            )
                        }
                    }

                    Text("Required Monthly Quantity (Tonnes) *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = {
                            quantityText = it
                            if (it.toDoubleOrNull() != null) quantityError = null
                        },
                        placeholder = { Text("e.g. 300", fontSize = 13.sp, color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = quantityError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = reloopTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    quantityError?.let { Text(it, color = AlertRed, fontSize = 11.sp) }

                    Text("Intake Plant Location / City *", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Box {
                        OutlinedButton(
                            onClick = { cityDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ReLoopBorder)
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = BrandPrimary, modifier = Modifier.size(15.dp))
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

                    Text("Max Permissible Moisture % (Optional)", fontSize = 12.sp, color = TextMuted)
                    OutlinedTextField(
                        value = maxMoistureText,
                        onValueChange = { maxMoistureText = it },
                        placeholder = { Text("e.g. 5.0", fontSize = 13.sp, color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = reloopTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Documents Required Checklist
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ReLoopBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("MANDATORY VENDOR CERTIFICATIONS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)
                    Text("Select documentation required from suppliers before dispatch:", fontSize = 12.sp, color = TextMuted)

                    MaterialRepository.standardDocumentsList.take(4).forEach { doc ->
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

                    if (buyerName.isBlank()) {
                        buyerError = "Please enter your buyer facility name."
                        hasError = true
                    }
                    if (requiredMaterial.isBlank()) {
                        materialError = "Please specify the required raw material."
                        hasError = true
                    }
                    if (qty == null || qty <= 0) {
                        quantityError = "Please enter a valid required quantity."
                        hasError = true
                    }

                    if (hasError) return@Button

                    isSubmitting = true
                    submissionError = null

                    val coords = MaterialRepository.regionalCoordinates[selectedCityKey] ?: Pair(30.378, 76.777)
                    val cityParts = selectedCityKey.split(",")
                    val city = cityParts.getOrElse(0) { selectedCityKey }.trim()
                    val state = cityParts.getOrElse(1) { "Punjab" }.trim()

                    val props = mutableMapOf<String, Any>()
                    maxMoistureText.toDoubleOrNull()?.let {
                        props["moisture"] = mapOf("max" to it)
                    }

                    coroutineScope.launch {
                        val (success, message) = MaterialRepository.createRequirement(
                            buyerName = buyerName,
                            buyerIndustry = selectedIndustry,
                            requiredMaterial = requiredMaterial,
                            requiredQuantityTonnes = qty ?: 0.0,
                            city = city,
                            state = state,
                            latitude = coords.first,
                            longitude = coords.second,
                            requiredProperties = props,
                            requiredDocuments = selectedDocs.toList()
                        )
                        isSubmitting = false
                        if (success) {
                            onSuccessSubmitted()
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
                    Text("Registering Demand in Engine…", fontSize = 14.sp)
                } else {
                    Icon(Icons.Default.PostAdd, null, tint = BrandLime, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Broadcast Feedstock Demand", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
