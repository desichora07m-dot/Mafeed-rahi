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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.rememberDatePickerState
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
import com.example.ui.components.OtherWorkGridCard
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
    val customers by viewModel.customers.collectAsState()
    val visibleCategories by viewModel.visibleCategories.collectAsState()

    // Handle Android system back
    BackHandler { onNavigateBack() }

    // Form states
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var selectedCategory by remember { mutableStateOf<WorkCategory?>(null) }

    // Manual Working Time (Hours and Minutes)
    var hoursText by remember { mutableStateOf("0") }
    var minutesText by remember { mutableStateOf("20") }

    // Completely Flexible Rate
    var customRateText by remember { mutableStateOf("15") }
    var selectedRateUnit by remember { mutableStateOf(RateUnit.PER_MINUTE) }

    // Payment details
    var paymentStatus by remember { mutableStateOf(PaymentStatus.PAID) }
    var amountReceivedText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }

    // Date & Notes
    var workDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf("") }

    // Dialog state for adding a customer (Only Name required)
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var newCustomerName by remember { mutableStateOf("") }

    // Customer picker sheet
    var showCustomerPicker by remember { mutableStateOf(false) }

    // Dialog for adding custom "Other Work"
    var showOtherWorkDialog by remember { mutableStateOf(false) }
    var customWorkName by remember { mutableStateOf("") }
    var customWorkRate by remember { mutableStateOf("15") }
    var customWorkUnit by remember { mutableStateOf(RateUnit.PER_MINUTE) }

    // Double-tap submission guard
    var isSaving by remember { mutableStateOf(false) }

    // Initialize if editing existing entry
    LaunchedEffect(existingEntry, customers, visibleCategories) {
        if (existingEntry != null) {
            selectedCustomer = customers.find { it.id == existingEntry.customerId }
                ?: Customer(id = existingEntry.customerId, name = existingEntry.customerName)
            selectedCategory = visibleCategories.find { it.key == existingEntry.categoryKey }
                ?: WorkCategory(
                    key = existingEntry.categoryKey,
                    nameEn = existingEntry.categoryName,
                    nameHi = existingEntry.categoryName,
                    drawableName = existingEntry.categoryDrawable,
                    defaultRate = existingEntry.rate,
                    defaultUnit = existingEntry.rateUnit
                )

            hoursText = existingEntry.hours.toString()
            minutesText = existingEntry.minutes.toString()
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
            if (selectedCategory == null && visibleCategories.isNotEmpty()) {
                val rotavator = visibleCategories.find { it.key == "rotavator" } ?: visibleCategories.first()
                selectedCategory = rotavator
                customRateText = if (rotavator.defaultRate % 1.0 == 0.0) rotavator.defaultRate.toInt().toString() else rotavator.defaultRate.toString()
                selectedRateUnit = rotavator.getParsedUnit()
            }
        }
    }

    // Parsed Duration in Minutes
    val totalDurationMinutes by remember {
        derivedStateOf {
            val h = hoursText.toIntOrNull() ?: 0
            val m = minutesText.toIntOrNull() ?: 0
            (h * 60) + m
        }
    }

    val parsedRate by remember {
        derivedStateOf { customRateText.toDoubleOrNull() ?: 0.0 }
    }

    // Live Automatic Earning Calculation
    val totalCalculatedAmount by remember {
        derivedStateOf {
            FormatUtils.calculateTotalAmount(totalDurationMinutes, parsedRate, selectedRateUnit)
        }
    }

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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (existingEntry != null) "Edit Work Record" else "Record Tractor Work",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Prominent Calculation Result Banner
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
                                text = "CALCULATED TOTAL",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = when (selectedRateUnit) {
                                    RateUnit.PER_MINUTE -> "$totalDurationMinutes min × ₹$parsedRate"
                                    RateUnit.PER_HOUR -> "${String.format("%.2f", totalDurationMinutes / 60.0)} hr × ₹$parsedRate"
                                    RateUnit.FIXED -> "Fixed Amount"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.SemiBold
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
            // STEP 1: SELECT CUSTOMER
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
                            text = "1. Select Customer",
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
                                text = "+ Add Customer",
                                fontWeight = FontWeight.Bold,
                                color = TractorGreenPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Selected Customer Selector
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(TractorGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedCustomer?.name ?: "Select Customer...",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                )
                                if (selectedCustomer?.village?.isNotBlank() == true || selectedCustomer?.phone?.isNotBlank() == true) {
                                    Text(
                                        text = listOfNotNull(
                                            selectedCustomer?.village.takeIf { it?.isNotBlank() == true },
                                            selectedCustomer?.phone.takeIf { it?.isNotBlank() == true }
                                        ).joinToString(" • "),
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                    )
                                }
                            }

                            Text(
                                text = "Change ▾",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TractorGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // STEP 2: AGRICULTURAL WORK CATEGORY (5 Defaults + Custom + Other Work)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "2. Select Agricultural Work",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary
                        )
                    )
                    Text(
                        text = "Realistic photography for all 5 farm operations",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2-Column Grid
                    val itemsToDisplay = visibleCategories
                    val totalCards = itemsToDisplay.size + 1 // +1 for "Other Work"
                    val pairs = mutableListOf<List<Any>>()
                    var i = 0
                    while (i < totalCards) {
                        val row = mutableListOf<Any>()
                        if (i < itemsToDisplay.size) {
                            row.add(itemsToDisplay[i])
                        } else {
                            row.add("OTHER_WORK")
                        }
                        if (i + 1 < totalCards) {
                            if (i + 1 < itemsToDisplay.size) {
                                row.add(itemsToDisplay[i + 1])
                            } else {
                                row.add("OTHER_WORK")
                            }
                        }
                        pairs.add(row)
                        i += 2
                    }

                    pairs.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (item in pair) {
                                if (item is WorkCategory) {
                                    WorkCategoryGridCard(
                                        category = item,
                                        isSelected = selectedCategory?.key == item.key,
                                        onSelect = {
                                            selectedCategory = item
                                            customRateText = if (item.defaultRate % 1.0 == 0.0) item.defaultRate.toInt().toString() else item.defaultRate.toString()
                                            selectedRateUnit = item.getParsedUnit()
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    OtherWorkGridCard(
                                        isSelected = false,
                                        onClick = { showOtherWorkDialog = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // STEP 3: WORK DATE
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "3. Select Work Date",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF9FAF8),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePickerDialog = true }
                            .testTag("work_date_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = TractorGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = FormatUtils.formatDateWithDay(workDateMillis),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1B5E20)
                                )
                            }

                            Row {
                                TextButton(onClick = { workDateMillis = System.currentTimeMillis() - 86400000L }) {
                                    Text("Yesterday", fontSize = 12.sp)
                                }
                                TextButton(onClick = { workDateMillis = System.currentTimeMillis() }) {
                                    Text("Today", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // STEP 4: MANUAL WORKING TIME ENTRY (HOURS & MINUTES)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "4. Enter Working Duration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary
                        )
                    )
                    Text(
                        text = "Enter any exact hours and minutes (e.g. 0h 17m, 1h 20m, 2h 45m)",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hours Input Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hours",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF1F8E9),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clickable {
                                            val current = hoursText.toIntOrNull() ?: 0
                                            if (current > 0) hoursText = (current - 1).toString()
                                        }
                                        .testTag("hours_minus_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "-1 Hr", tint = TractorGreenPrimary)
                                    }
                                }

                                OutlinedTextField(
                                    value = hoursText,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() }) hoursText = input
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = MaterialTheme.typography.titleLarge.copy(
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = TractorGreenPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp)
                                        .testTag("hours_input_field")
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF1F8E9),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clickable {
                                            val current = hoursText.toIntOrNull() ?: 0
                                            hoursText = (current + 1).toString()
                                        }
                                        .testTag("hours_plus_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "+1 Hr", tint = TractorGreenPrimary)
                                    }
                                }
                            }
                        }

                        // Minutes Input Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Minutes",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF1F8E9),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clickable {
                                            val current = minutesText.toIntOrNull() ?: 0
                                            if (current >= 5) minutesText = (current - 5).toString()
                                            else minutesText = "0"
                                        }
                                        .testTag("minutes_minus_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "-5 Min", tint = TractorGreenPrimary)
                                    }
                                }

                                OutlinedTextField(
                                    value = minutesText,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() }) minutesText = input
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = MaterialTheme.typography.titleLarge.copy(
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = TractorGreenPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp)
                                        .testTag("minutes_input_field")
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF1F8E9),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clickable {
                                            val current = minutesText.toIntOrNull() ?: 0
                                            minutesText = (current + 5).toString()
                                        }
                                        .testTag("minutes_plus_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "+5 Min", tint = TractorGreenPrimary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Total Duration: ${FormatUtils.formatDuration(totalDurationMinutes)} ($totalDurationMinutes minutes)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // STEP 5: COMPLETELY FLEXIBLE RATE SYSTEM
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "5. Select Rate Type & Enter Rate",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary
                        )
                    )
                    Text(
                        text = "Enter any rate without restriction (₹5, ₹15, ₹50, ₹800, ₹1500 etc.)",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rate Type Chips (Per Minute, Per Hour, Fixed Amount)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RateUnit.entries.forEach { unit ->
                            val isSelected = selectedRateUnit == unit
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) TractorGreenPrimary else Color(0xFFF1F5EE),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedRateUnit = unit }
                                    .testTag("rate_unit_${unit.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = unit.labelEn,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom Rate Manual Input
                    OutlinedTextField(
                        value = customRateText,
                        onValueChange = { customRateText = it },
                        label = { Text("Enter Custom Rate") },
                        prefix = { Text("₹", fontWeight = FontWeight.Bold, color = TractorGreenPrimary) },
                        suffix = {
                            Text(
                                text = " / ${selectedRateUnit.labelEn}",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_rate_input")
                    )
                }
            }

            // STEP 6: PAYMENT STATUS & SUMMARY
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "6. Payment Status",
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
                                    text = "Paid (Full)",
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
                                    text = "Partial",
                                    color = if (paymentStatus == PaymentStatus.PARTIAL) Color.White else StatusPendingOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Some Paid",
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
                                    text = "Unpaid",
                                    color = if (paymentStatus == PaymentStatus.UNPAID) Color.White else StatusOverdueRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "All Due",
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
                                label = { Text("Amount Received (₹)") },
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
                                    text = "Remaining Due:",
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
                                text = "Method:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            listOf("CASH" to "Cash", "UPI" to "UPI / Online").forEach { (m, label) ->
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
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = { Text("Field location / notes (optional)") },
                        singleLine = false,
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_input")
                    )
                }
            }

            // SUMMARY CARD
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                border = androidx.compose.foundation.BorderStroke(1.dp, TractorGreenPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "WORK SUMMARY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TractorGreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Customer:", color = Color.Gray, fontSize = 13.sp)
                        Text(selectedCustomer?.name ?: "-", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Work Category:", color = Color.Gray, fontSize = 13.sp)
                        Text(selectedCategory?.nameEn ?: "-", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Working Duration:", color = Color.Gray, fontSize = 13.sp)
                        Text(FormatUtils.formatDuration(totalDurationMinutes), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rate:", color = Color.Gray, fontSize = 13.sp)
                        Text("₹$parsedRate / ${selectedRateUnit.shortUnitEn}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFC5E1A5))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Bill:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(FormatUtils.formatCurrency(totalCalculatedAmount), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TractorGreenPrimary)
                    }
                    if (amountPending > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pending Balance Due:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusOverdueRed)
                            Text(FormatUtils.formatCurrency(amountPending), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusOverdueRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // STEP 7: PROMINENT SAVE BUTTON
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
                        if (isSaving) return@Button
                        val cust = selectedCustomer
                        val cat = selectedCategory

                        if (cust == null) {
                            Toast.makeText(context, "Please select customer", Toast.LENGTH_SHORT).show()
                            showCustomerPicker = true
                            return@Button
                        }

                        if (cat == null) {
                            Toast.makeText(context, "Please select work category", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (totalDurationMinutes <= 0) {
                            Toast.makeText(context, "Please enter working duration (hours/minutes)", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSaving = true
                        val entry = WorkEntry(
                            id = existingEntry?.id ?: 0L,
                            customerId = cust.id,
                            customerName = cust.name,
                            categoryId = cat.id,
                            categoryKey = cat.key,
                            categoryName = cat.nameEn,
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
                            Toast.makeText(context, "✅ Work record saved successfully!", Toast.LENGTH_SHORT).show()
                            isSaving = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_work_btn")
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (existingEntry != null) "SAVE CHANGES"
                        else "SAVE WORK  •  ${FormatUtils.formatCurrency(totalCalculatedAmount)}",
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

    // Modal: DatePicker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = workDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            workDateMillis = it
                        }
                        showDatePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary)
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
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
                        text = "Select Customer",
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
                            text = "No customers found. Tap + Add Customer to create one.",
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
                                            .size(34.dp)
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
                                                text = listOfNotNull(
                                                    c.village.takeIf { it.isNotBlank() },
                                                    c.phone.takeIf { it.isNotBlank() }
                                                ).joinToString(" • "),
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
                    Text("+ Add Customer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomerPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Add New Customer (ONLY CUSTOMER NAME - Phase 4)
    if (showAddCustomerDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = {
                Text(
                    text = "Add Customer",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter customer name to add:",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
                    )
                    OutlinedTextField(
                        value = newCustomerName,
                        onValueChange = { newCustomerName = it },
                        label = { Text("Customer Name") },
                        placeholder = { Text("e.g. Ramesh Patel") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("new_customer_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustomerName.trim().isNotBlank()) {
                            viewModel.addCustomer(newCustomerName.trim()) { id ->
                                selectedCustomer = Customer(
                                    id = id,
                                    name = newCustomerName.trim()
                                )
                                newCustomerName = ""
                                showAddCustomerDialog = false
                            }
                        } else {
                            Toast.makeText(context, "Please enter customer name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                    modifier = Modifier.testTag("save_new_customer_btn")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Other Work (Custom Work Category - Phase 5)
    if (showOtherWorkDialog) {
        AlertDialog(
            onDismissRequest = { showOtherWorkDialog = false },
            title = {
                Text(
                    text = "Add Custom Work Category",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter any agricultural work name and default rate. It will be saved for future use.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    OutlinedTextField(
                        value = customWorkName,
                        onValueChange = { customWorkName = it },
                        label = { Text("Work Name") },
                        placeholder = { Text("e.g. Deep Ploughing, Bund Maker") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("custom_work_name_input")
                    )

                    OutlinedTextField(
                        value = customWorkRate,
                        onValueChange = { customWorkRate = it },
                        label = { Text("Default Rate (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Rate Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RateUnit.entries.forEach { u ->
                            val sel = customWorkUnit == u
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (sel) TractorGreenPrimary else Color(0xFFF1F5EE),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { customWorkUnit = u }
                            ) {
                                Text(
                                    text = u.labelEn,
                                    color = if (sel) Color.White else Color(0xFF1B5E20),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customWorkName.trim().isNotBlank()) {
                            val rateVal = customWorkRate.toDoubleOrNull() ?: 15.0
                            viewModel.addCustomCategory(
                                name = customWorkName.trim(),
                                rate = rateVal,
                                unit = customWorkUnit
                            ) { newCat ->
                                selectedCategory = newCat
                                customRateText = if (rateVal % 1.0 == 0.0) rateVal.toInt().toString() else rateVal.toString()
                                selectedRateUnit = customWorkUnit
                                customWorkName = ""
                                showOtherWorkDialog = false
                            }
                        } else {
                            Toast.makeText(context, "Please enter work name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary)
                ) {
                    Text("Add & Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtherWorkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
