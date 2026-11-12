package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.PaymentStatus
import com.example.data.model.RateUnit
import com.example.data.model.WorkCategory
import com.example.data.model.WorkEntry
import com.example.ui.components.WorkCategoryGridCard
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingOrange
import com.example.ui.theme.TractorGold
import com.example.ui.theme.TractorGreenDark
import com.example.ui.theme.TractorGreenLight
import com.example.ui.theme.TractorGreenPrimary
import com.example.ui.viewmodel.TractorViewModel
import com.example.util.FormatUtils
import com.example.util.LocaleHelper
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditWorkScreen(
    viewModel: TractorViewModel,
    existingEntry: WorkEntry? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isHindi by viewModel.isHindi.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val categories by viewModel.categories.collectAsState()

    // Handle Android system back
    BackHandler { onNavigateBack() }

    // Form states
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var selectedCategory by remember { mutableStateOf<WorkCategory?>(null) }

    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(20) } // Default 20 mins for fast entry

    var customRateText by remember { mutableStateOf("15") }
    var selectedRateUnit by remember { mutableStateOf(RateUnit.PER_MINUTE) }

    var paymentStatus by remember { mutableStateOf(PaymentStatus.PAID) }
    var amountReceivedText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }

    var workDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var notesText by remember { mutableStateOf("") }

    // Dialog state for adding a customer
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerPhone by remember { mutableStateOf("") }
    var newCustomerVillage by remember { mutableStateOf("") }

    // Customer picker sheet
    var showCustomerPicker by remember { mutableStateOf(false) }

    // Initialize if editing existing entry
    LaunchedEffect(existingEntry, customers, categories) {
        if (existingEntry != null) {
            selectedCustomer = customers.find { it.id == existingEntry.customerId }
                ?: Customer(id = existingEntry.customerId, name = existingEntry.customerName)
            selectedCategory = categories.find { it.key == existingEntry.categoryKey }
                ?: categories.firstOrNull()

            hours = existingEntry.hours
            minutes = existingEntry.minutes
            customRateText = if (existingEntry.rate % 1.0 == 0.0) existingEntry.rate.toInt().toString() else existingEntry.rate.toString()
            selectedRateUnit = existingEntry.getParsedUnit()
            paymentStatus = existingEntry.getParsedStatus()
            amountReceivedText = if (existingEntry.amountPaid > 0) existingEntry.amountPaid.toInt().toString() else ""
            workDateMillis = existingEntry.workDateMillis
            notesText = existingEntry.notes
        } else {
            if (selectedCustomer == null && customers.isNotEmpty()) {
                selectedCustomer = customers.first()
            }
            if (selectedCategory == null && categories.isNotEmpty()) {
                val defaultCat = categories.find { it.key == "rotavator" } ?: categories.first()
                selectedCategory = defaultCat
                customRateText = if (defaultCat.defaultRate % 1.0 == 0.0) defaultCat.defaultRate.toInt().toString() else defaultCat.defaultRate.toString()
                selectedRateUnit = defaultCat.getParsedUnit()
            }
        }
    }

    // Live Calculation
    val totalDurationMinutes by remember {
        derivedStateOf { (hours * 60) + minutes }
    }

    val parsedRate by remember {
        derivedStateOf { customRateText.toDoubleOrNull() ?: 0.0 }
    }

    val totalCalculatedAmount by remember {
        derivedStateOf {
            FormatUtils.calculateTotalAmount(totalDurationMinutes, parsedRate, selectedRateUnit)
        }
    }

    // Auto calculate pending based on payment status
    val amountReceived by remember {
        derivedStateOf {
            when (paymentStatus) {
                PaymentStatus.PAID -> totalCalculatedAmount
                PaymentStatus.UNPAID -> 0.0
                PaymentStatus.PARTIAL -> (amountReceivedText.toDoubleOrNull() ?: 0.0).coerceAtMost(totalCalculatedAmount)
            }
        }
    }

    val amountPending by remember {
        derivedStateOf {
            (totalCalculatedAmount - amountReceived).coerceAtLeast(0.0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9F6))
    ) {
        // Sticky Header: Top Bar with Back + Prominent Live Calculation Display
        Surface(
            color = TractorGreenPrimary,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_work_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = if (existingEntry != null) LocaleHelper.getString("edit_work_title", isHindi)
                        else LocaleHelper.getString("new_work_title", isHindi),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 19.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Prominent Calculation Result Banner (Large & High Contrast)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = LocaleHelper.getString("calculated_total", isHindi),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = when (selectedRateUnit) {
                                    RateUnit.PER_MINUTE -> "$totalDurationMinutes ${LocaleHelper.getString("minutes", isHindi)} × ₹$parsedRate"
                                    RateUnit.PER_HOUR -> "${String.format("%.1f", totalDurationMinutes / 60.0)} ${LocaleHelper.getString("hours", isHindi)} × ₹$parsedRate"
                                    RateUnit.FIXED -> "फिक्स दर (Fixed)"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Text(
                            text = FormatUtils.formatCurrency(totalCalculatedAmount),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TractorGreenPrimary,
                                fontSize = 28.sp
                            ),
                            modifier = Modifier.testTag("live_total_amount_text")
                        )
                    }
                }
            }
        }

        // Form Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STEP 1: CUSTOMER SELECTION
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LocaleHelper.getString("step_customer", isHindi),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TractorGreenPrimary
                            )
                        )

                        TextButton(
                            onClick = { showAddCustomerDialog = true },
                            modifier = Modifier.testTag("add_customer_dialog_btn")
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = LocaleHelper.getString("add_new_customer", isHindi),
                                fontWeight = FontWeight.Bold,
                                color = TractorGreenPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Selected Customer Card / Selector
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F8E9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TractorGreenPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomerPicker = true }
                            .testTag("selected_customer_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(TractorGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedCustomer?.name ?: (if (isHindi) "ग्राहक चुनें..." else "Select Customer..."),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                )
                                if (selectedCustomer?.village?.isNotBlank() == true || selectedCustomer?.phone?.isNotBlank() == true) {
                                    Text(
                                        text = "${selectedCustomer?.village} ${if (selectedCustomer?.phone?.isNotBlank() == true) "• ${selectedCustomer?.phone}" else ""}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                    )
                                }
                            }

                            Text(
                                text = if (isHindi) "बदलें ▾" else "Change ▾",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TractorGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // STEP 2: AGRICULTURAL WORK CATEGORY (2-column grid)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = LocaleHelper.getString("step_category", isHindi),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2-Column Grid of 8 default categories
                    val chunked = categories.chunked(2)
                    chunked.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (cat in pair) {
                                WorkCategoryGridCard(
                                    category = cat,
                                    isSelected = selectedCategory?.key == cat.key,
                                    isHindi = isHindi,
                                    onSelect = {
                                        selectedCategory = cat
                                        // Update custom rate with this category's default rate and unit
                                        customRateText = if (cat.defaultRate % 1.0 == 0.0) cat.defaultRate.toInt().toString() else cat.defaultRate.toString()
                                        selectedRateUnit = cat.getParsedUnit()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // STEP 3: WORKING DURATION (Hours and Minutes)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = LocaleHelper.getString("step_duration", isHindi),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hours Spinner
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = LocaleHelper.getString("hours", isHindi),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF1F8E9),
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable { if (hours > 0) hours-- }
                                        .testTag("hours_decrement_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "-1 Hr", tint = TractorGreenPrimary)
                                    }
                                }

                                Text(
                                    text = "$hours",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 26.sp,
                                        color = TractorGreenPrimary
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .width(55.dp)
                                        .testTag("hours_value_text")
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF1F8E9),
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable { hours++ }
                                        .testTag("hours_increment_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "+1 Hr", tint = TractorGreenPrimary)
                                    }
                                }
                            }
                        }

                        // Minutes Spinner
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = LocaleHelper.getString("minutes", isHindi),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF1F8E9),
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable {
                                            minutes = if (minutes >= 5) (minutes - 5) else 0
                                        }
                                        .testTag("minutes_decrement_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "-5 Min", tint = TractorGreenPrimary)
                                    }
                                }

                                Text(
                                    text = "$minutes",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 26.sp,
                                        color = TractorGreenPrimary
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .width(55.dp)
                                        .testTag("minutes_value_text")
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF1F8E9),
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable {
                                            minutes = (minutes + 5).coerceAtMost(59)
                                        }
                                        .testTag("minutes_increment_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "+5 Min", tint = TractorGreenPrimary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fast Duration Preset Chips (Common agricultural field durations)
                    Text(
                        text = if (isHindi) "त्वरित समय चुनें (Quick Add):" else "Quick Presets:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            Pair("15m", 15),
                            Pair("20m", 20),
                            Pair("30m", 30),
                            Pair("45m", 45),
                            Pair("1h", 60),
                            Pair("1h 30m", 90),
                            Pair("2h", 120),
                            Pair("3h", 180)
                        )
                        presets.forEach { (label, totalMins) ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (totalDurationMinutes == totalMins) TractorGreenPrimary else Color(0xFFEFF3EE),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        hours = totalMins / 60
                                        minutes = totalMins % 60
                                    }
                            ) {
                                Text(
                                    text = label,
                                    color = if (totalDurationMinutes == totalMins) Color.White else Color(0xFF1B5E20),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // STEP 4: CUSTOM RATE SYSTEM (NO FIXED PRICE LIMIT)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = LocaleHelper.getString("step_rate", isHindi),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary
                        )
                    )
                    Text(
                        text = if (isHindi) "कोई भी दर अपनी मर्जी से डालें (₹5, ₹10, ₹15, ₹50, ₹800 आदि)"
                        else "Enter any custom rate without limit (₹5, ₹15, ₹50, ₹800 etc)",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rate Input & Unit Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customRateText,
                            onValueChange = { customRateText = it },
                            label = { Text(LocaleHelper.getString("custom_rate", isHindi)) },
                            prefix = { Text("₹", fontWeight = FontWeight.Bold, color = TractorGreenPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("custom_rate_input")
                        )

                        // Rate Unit Toggle Chips
                        Column(modifier = Modifier.weight(1.8f)) {
                            RateUnit.entries.forEach { unit ->
                                val isSelected = selectedRateUnit == unit
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) TractorGreenPrimary else Color(0xFFF1F5EE),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedRateUnit = unit }
                                        .testTag("rate_unit_${unit.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else Color.Transparent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isHindi) unit.labelHi else unit.labelEn,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) Color.White else Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Rate Short-Cuts
                    Text(
                        text = if (isHindi) "अक्सर इस्तेमाल दरें:" else "Quick Rates:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val ratePresets = listOf("10", "12", "15", "18", "20", "22", "500", "800", "900", "1000", "1200")
                        ratePresets.forEach { r ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (customRateText == r) TractorGold else Color(0xFFEFF3EE),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { customRateText = r }
                            ) {
                                Text(
                                    text = "₹$r",
                                    color = if (customRateText == r) Color.White else Color(0xFF333333),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // STEP 5: PAYMENT STATUS & RECEIVED AMOUNT
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = LocaleHelper.getString("step_payment", isHindi),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Payment Status Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // PAID
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (paymentStatus == PaymentStatus.PAID) StatusPaidGreen else StatusPaidBg,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    paymentStatus = PaymentStatus.PAID
                                    amountReceivedText = totalCalculatedAmount.toInt().toString()
                                }
                                .testTag("payment_status_paid")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = LocaleHelper.getString("paid", isHindi),
                                    color = if (paymentStatus == PaymentStatus.PAID) Color.White else StatusPaidGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "₹${totalCalculatedAmount.toInt()}",
                                    color = if (paymentStatus == PaymentStatus.PAID) Color.White.copy(alpha = 0.85f) else Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // PARTIAL
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (paymentStatus == PaymentStatus.PARTIAL) StatusPendingOrange else StatusPendingBg,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    paymentStatus = PaymentStatus.PARTIAL
                                    if (amountReceivedText.isBlank()) {
                                        amountReceivedText = (totalCalculatedAmount / 2).toInt().toString()
                                    }
                                }
                                .testTag("payment_status_partial")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = LocaleHelper.getString("partial", isHindi),
                                    color = if (paymentStatus == PaymentStatus.PARTIAL) Color.White else StatusPendingOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isHindi) "आधा या कुछ" else "Some Paid",
                                    color = if (paymentStatus == PaymentStatus.PARTIAL) Color.White.copy(alpha = 0.85f) else Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // UNPAID
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (paymentStatus == PaymentStatus.UNPAID) StatusOverdueRed else StatusOverdueBg,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    paymentStatus = PaymentStatus.UNPAID
                                    amountReceivedText = "0"
                                }
                                .testTag("payment_status_unpaid")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = LocaleHelper.getString("unpaid", isHindi),
                                    color = if (paymentStatus == PaymentStatus.UNPAID) Color.White else StatusOverdueRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isHindi) "बाकी उधार" else "All Due",
                                    color = if (paymentStatus == PaymentStatus.UNPAID) Color.White.copy(alpha = 0.85f) else Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // If Partial: Show amount input
                    AnimatedVisibility(visible = paymentStatus == PaymentStatus.PARTIAL) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(
                                value = amountReceivedText,
                                onValueChange = { amountReceivedText = it },
                                label = { Text(LocaleHelper.getString("amount_received", isHindi)) },
                                prefix = { Text("₹", fontWeight = FontWeight.Bold) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("amount_received_input")
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${LocaleHelper.getString("balance_pending", isHindi)}:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = FormatUtils.formatCurrency(amountPending),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StatusOverdueRed
                                    )
                                )
                            }
                        }
                    }

                    // Payment Method selector (if paid or partial)
                    if (paymentStatus != PaymentStatus.UNPAID) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isHindi) "माध्यम:" else "Method:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            listOf("CASH" to (if (isHindi) "नगद (Cash)" else "Cash"), "UPI" to "UPI (PhonePe/GPay)").forEach { (m, label) ->
                                val isSelected = paymentMethod == m
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) TractorGreenPrimary else Color(0xFFEFF3EE),
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { paymentMethod = m }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else Color.DarkGray,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // STEP 6: NOTES & WORK DATE
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isHindi) "६. तारीख व नोट" else "6. Date & Notes",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date display & quick picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = TractorGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = FormatUtils.formatDateWithDay(workDateMillis, isHindi),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Row {
                            TextButton(onClick = { workDateMillis = System.currentTimeMillis() - 86400000L }) {
                                Text(if (isHindi) "कल" else "Yesterday", fontSize = 12.sp)
                            }
                            TextButton(onClick = { workDateMillis = System.currentTimeMillis() }) {
                                Text(if (isHindi) "आज" else "Today", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = { Text(LocaleHelper.getString("notes_optional", isHindi)) },
                        singleLine = false,
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom Sticky Action Button: Large Save Work Button
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val cust = selectedCustomer
                        val cat = selectedCategory

                        if (cust == null) {
                            Toast.makeText(context, if (isHindi) "कृपया ग्राहक चुनें" else "Please select customer", Toast.LENGTH_SHORT).show()
                            showCustomerPicker = true
                            return@Button
                        }

                        if (cat == null) {
                            Toast.makeText(context, if (isHindi) "कृपया काम चुनें" else "Please select work category", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (totalDurationMinutes <= 0) {
                            Toast.makeText(context, if (isHindi) "कृपया समय (घंटा/मिनट) दर्ज करें" else "Please enter duration", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val entry = WorkEntry(
                            id = existingEntry?.id ?: 0L,
                            customerId = cust.id,
                            customerName = cust.name,
                            categoryId = cat.id,
                            categoryKey = cat.key,
                            categoryName = if (isHindi) "${cat.nameHi} (${cat.nameEn})" else "${cat.nameEn} (${cat.nameHi})",
                            categoryDrawable = cat.drawableName,
                            durationMinutes = totalDurationMinutes,
                            rate = parsedRate,
                            rateUnit = selectedRateUnit.id,
                            totalAmount = totalCalculatedAmount,
                            amountPaid = amountReceived,
                            amountPending = amountPending,
                            paymentStatus = paymentStatus.id,
                            workDateMillis = workDateMillis,
                            notes = notesText.trim()
                        )

                        viewModel.saveWork(entry, paymentMethod) {
                            Toast.makeText(
                                context,
                                if (isHindi) "✅ काम हिसाब सफलतापूर्वक सुरक्षित हुआ!" else "✅ Work record saved successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_work_btn")
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (existingEntry != null) LocaleHelper.getString("save_changes", isHindi)
                        else "${LocaleHelper.getString("save_work", isHindi)}  •  ${FormatUtils.formatCurrency(totalCalculatedAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }

    // Modal Sheet: Customer Picker
    if (showCustomerPicker) {
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "ग्राहक चुनें" else "Select Customer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = {
                        showCustomerPicker = false
                        showAddCustomerDialog = true
                    }) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add", tint = TractorGreenPrimary)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (customers.isEmpty()) {
                        Text(
                            text = if (isHindi) "कोई ग्राहक नहीं मिला। नया ग्राहक जोड़ें।" else "No customers found. Add a new customer.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        customers.forEach { c ->
                            val isSel = selectedCustomer?.id == c.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) Color(0xFFDCEDC8) else Color(0xFFF9FAF8),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedCustomer = c
                                        showCustomerPicker = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(TractorGreenPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = c.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = c.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (c.village.isNotBlank() || c.phone.isNotBlank()) {
                                            Text(
                                                text = "${c.village} ${if (c.phone.isNotBlank()) "• ${c.phone}" else ""}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCustomerPicker = false
                        showAddCustomerDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary)
                ) {
                    Text(LocaleHelper.getString("add_new_customer", isHindi))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomerPicker = false }) {
                    Text(LocaleHelper.getString("cancel", isHindi))
                }
            }
        )
    }

    // Dialog: Add New Customer
    if (showAddCustomerDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = {
                Text(
                    text = LocaleHelper.getString("add_new_customer", isHindi),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newCustomerName,
                        onValueChange = { newCustomerName = it },
                        label = { Text(LocaleHelper.getString("enter_customer_name", isHindi)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("new_customer_name_input")
                    )

                    OutlinedTextField(
                        value = newCustomerPhone,
                        onValueChange = { newCustomerPhone = it },
                        label = { Text(LocaleHelper.getString("customer_phone", isHindi)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("new_customer_phone_input")
                    )

                    OutlinedTextField(
                        value = newCustomerVillage,
                        onValueChange = { newCustomerVillage = it },
                        label = { Text(LocaleHelper.getString("customer_village", isHindi)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("new_customer_village_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustomerName.trim().isNotBlank()) {
                            viewModel.addCustomer(newCustomerName, newCustomerPhone, newCustomerVillage) { id ->
                                selectedCustomer = Customer(
                                    id = id,
                                    name = newCustomerName.trim(),
                                    phone = newCustomerPhone.trim(),
                                    village = newCustomerVillage.trim()
                                )
                                newCustomerName = ""
                                newCustomerPhone = ""
                                newCustomerVillage = ""
                                showAddCustomerDialog = false
                            }
                        } else {
                            Toast.makeText(context, if (isHindi) "कृपया नाम दर्ज करें" else "Please enter name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                    modifier = Modifier.testTag("save_new_customer_btn")
                ) {
                    Text(LocaleHelper.getString("save", isHindi))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text(LocaleHelper.getString("cancel", isHindi))
                }
            }
        )
    }
}
