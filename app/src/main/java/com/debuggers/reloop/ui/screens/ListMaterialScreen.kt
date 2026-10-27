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
    var selectedIndustry by remember { mutableStateOf(MaterialRepository.supportedIndustries.first()) }
    var quantityText by remember { mutableStateOf("") }
    var selectedCityKey by remember { mutableStateOf(MaterialRepository.regionalCoordinates.keys.first()) }
    var finenessText by remember { mutableStateOf("") }
    var moistureText by remember { mutableStateOf("") }

    val selectedDocs = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var industryDropdownExpanded by remember { mutableStateOf(false) }
    var cityDropdownExpanded by remember { mutableStateOf(false) }

    val commonSuggestions = listOf("Fly Ash", "Foundry Sand", "Ferrous Mill Scale", "Process Water", "Furnace Slag")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReLoopPageBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.dp, ReLoopBorder, RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = TextDark)
            }
            Column {
                Text(
                    text = "List Industrial By-Product",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Text(
                    text = "Create a live listing on the circular exchange",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Informational Callout
        Surface(
            color = BrandForest,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.CloudUpload, null, tint = BrandLime, modifier = Modifier.size(24.dp))
                Column {
                    Text(
                        text = "LIVE SUPABASE & AI MATCH ENGINE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandLime,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Your listing will immediately sync with the backend and become eligible for circular matching within a 300km radius.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section 1: Facility & Material
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ReLoopBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("FACILITY & MATERIAL IDENTITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                // Seller Facility Name
                Text("Generating Facility / Business Name *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = sellerName,
                    onValueChange = { sellerName = it },
                    placeholder = { Text("e.g. Rajpura Thermal Power Plant", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Material Name
                Text("Material / By-Product Name *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = materialName,
                    onValueChange = { materialName = it },
                    placeholder = { Text("e.g. Fly Ash Class F", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick suggestions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonSuggestions.take(3).forEach { suggestion ->
                        AssistChip(
                            onClick = { materialName = suggestion },
                            label = { Text(suggestion, fontSize = 11.sp) }
                        )
                    }
                }

                // Source Industry Dropdown
                Text("Source Industry Sector *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Box {
                    OutlinedButton(
                        onClick = { industryDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
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
            }
        }

        // Section 2: Quantity & Logistics
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ReLoopBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("QUANTITY & LOGISTICS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                Text("Available Quantity (Metric Tonnes) *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    placeholder = { Text("e.g. 500", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Collection Hub / City *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Box {
                    OutlinedButton(
                        onClick = { cityDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ReLoopBorder)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = BrandForest, modifier = Modifier.size(16.dp))
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
            }
        }

        // Section 3: Technical Specifications & Verification Documents
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ReLoopBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("TECHNICAL PROPERTIES & QUALITY DOCS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Fineness (m²/kg)", fontSize = 12.sp, color = TextMuted)
                        OutlinedTextField(
                            value = finenessText,
                            onValueChange = { finenessText = it },
                            placeholder = { Text("e.g. 320", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Moisture %", fontSize = 12.sp, color = TextMuted)
                        OutlinedTextField(
                            value = moistureText,
                            onValueChange = { moistureText = it },
                            placeholder = { Text("e.g. 2.5", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Text("Available Lab & Regulatory Documents:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
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
                        Text(doc, fontSize = 13.sp, color = TextDark)
                    }
                }
            }
        }

        // Status / Error banner
        statusMessage?.let { (isSuccess, msg) ->
            Surface(
                color = if (isSuccess) LiveGreenBg else AlertRedBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isSuccess) LiveGreen else AlertRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isSuccess) LiveGreen else AlertRed
                    )
                    Text(msg, color = if (isSuccess) LiveGreen else AlertRed, fontSize = 13.sp)
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                val qty = quantityText.trim().toDoubleOrNull()
                if (sellerName.isBlank()) {
                    statusMessage = Pair(false, "Please enter your facility or seller name.")
                    return@Button
                }
                if (materialName.isBlank()) {
                    statusMessage = Pair(false, "Please enter the by-product material name.")
                    return@Button
                }
                if (qty == null || qty <= 0) {
                    statusMessage = Pair(false, "Please enter a valid quantity in metric tonnes.")
                    return@Button
                }

                isSubmitting = true
                statusMessage = null

                val coords = MaterialRepository.regionalCoordinates[selectedCityKey] ?: Pair(30.478, 76.593)
                val cityParts = selectedCityKey.split(",")
                val city = cityParts.getOrElse(0) { selectedCityKey }.trim()
                val state = cityParts.getOrElse(1) { "Punjab" }.trim()

                val props = mutableMapOf<String, Any>()
                finenessText.toDoubleOrNull()?.let { props["fineness"] = it }
                moistureText.toDoubleOrNull()?.let { props["moisture"] = it }

                coroutineScope.launch {
                    val (success, message) = MaterialRepository.createMaterial(
                        sellerName = sellerName,
                        materialName = materialName,
                        sourceIndustry = selectedIndustry,
                        quantityTonnes = qty,
                        city = city,
                        state = state,
                        latitude = coords.first,
                        longitude = coords.second,
                        properties = props,
                        availableDocuments = selectedDocs.toList()
                    )
                    isSubmitting = false
                    statusMessage = Pair(success, message)
                    if (success) {
                        onSuccessCreated()
                    }
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandForest)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Publishing to ReLoop Network…", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Publish, null, tint = BrandLime)
                Spacer(Modifier.width(8.dp))
                Text("Publish Live Listing", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
