package com.debuggers.reloop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
fun PostRequirementScreen(
    onBack: () -> Unit,
    onSuccessSubmitted: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var buyerName by remember { mutableStateOf("") }
    var selectedIndustry by remember { mutableStateOf("Cement Manufacturing") }
    var requiredMaterial by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var selectedCityKey by remember { mutableStateOf(MaterialRepository.regionalCoordinates.keys.first()) }
    var maxMoistureText by remember { mutableStateOf("") }

    val selectedDocs = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var industryDropdownExpanded by remember { mutableStateOf(false) }
    var cityDropdownExpanded by remember { mutableStateOf(false) }

    val buyerIndustries = listOf(
        "Cement Manufacturing",
        "Brick & Block Production",
        "Concrete Products & RMC",
        "Road & Highway Construction",
        "Steel & Foundry",
        "Chemical Processing",
        "Other Manufacturing"
    )

    val commonFeedstocks = listOf("Fly Ash", "Foundry Sand", "Mill Scale", "Secondary Aggregate")

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
                    text = "Post Feedstock Demand",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Text(
                    text = "Broadcast required industrial by-products to suppliers",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Info Banner
        Surface(
            color = InfoBlueBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, InfoBlue.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.ShareLocation, null, tint = InfoBlue, modifier = Modifier.size(24.dp))
                Column {
                    Text(
                        text = "INTELLIGENT SPATIAL MATCHING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = InfoBlue,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "The ReLoop matching engine scans regional sellers and scores compatibility based on transit distance and quality specs.",
                        fontSize = 12.sp,
                        color = TextDark,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section 1: Buyer Information
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ReLoopBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("BUYER FACILITY DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                Text("Procuring Business / Buyer Name *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    placeholder = { Text("e.g. Ambala Green Cement Works", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Buyer Industry Sector *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
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

        // Section 2: Material Demand & Intake Location
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ReLoopBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("FEEDSTOCK SPECIFICATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 0.5.sp)

                Text("Target Raw Material / Feedstock *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = requiredMaterial,
                    onValueChange = { requiredMaterial = it },
                    placeholder = { Text("e.g. Fly Ash", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonFeedstocks.forEach { feed ->
                        AssistChip(
                            onClick = { requiredMaterial = feed },
                            label = { Text(feed, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Required Monthly Intake (Tonnes) *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    placeholder = { Text("e.g. 300", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Receiving Facility City *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Box {
                    OutlinedButton(
                        onClick = { cityDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ReLoopBorder)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = InfoBlue, modifier = Modifier.size(16.dp))
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

                Text("Maximum Permissible Moisture %", fontSize = 12.5.sp, color = TextMuted)
                OutlinedTextField(
                    value = maxMoistureText,
                    onValueChange = { maxMoistureText = it },
                    placeholder = { Text("e.g. 5.0", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Text("Mandatory Verification Documents Required:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                MaterialRepository.standardDocumentsList.take(3).forEach { doc ->
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
                            colors = CheckboxDefaults.colors(checkedColor = InfoBlue)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(doc, fontSize = 13.sp, color = TextDark)
                    }
                }
            }
        }

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
                if (buyerName.isBlank()) {
                    statusMessage = Pair(false, "Please enter your buyer facility name.")
                    return@Button
                }
                if (requiredMaterial.isBlank()) {
                    statusMessage = Pair(false, "Please specify the required material.")
                    return@Button
                }
                if (qty == null || qty <= 0) {
                    statusMessage = Pair(false, "Please enter a valid intake demand quantity.")
                    return@Button
                }

                isSubmitting = true
                statusMessage = null

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
                        requiredQuantityTonnes = qty,
                        city = city,
                        state = state,
                        latitude = coords.first,
                        longitude = coords.second,
                        requiredProperties = props,
                        requiredDocuments = selectedDocs.toList()
                    )
                    isSubmitting = false
                    statusMessage = Pair(success, message)
                    if (success) {
                        onSuccessSubmitted()
                    }
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Registering Demand in Engine…", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.PostAdd, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Post Live Demand", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
