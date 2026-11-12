package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Customer
import com.example.data.model.CustomerLedgerSummary
import com.example.data.model.PaymentTransaction
import com.example.data.model.WorkEntry
import com.example.ui.components.ImplementImageHelper
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingOrange
import com.example.ui.theme.TractorGold
import com.example.ui.theme.TractorGreenPrimary
import com.example.ui.viewmodel.TractorViewModel
import com.example.util.FormatUtils
import com.example.util.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: TractorViewModel
) {
    val context = LocalContext.current
    val ownerName by viewModel.ownerName.collectAsState()
    val ledgers by viewModel.customerLedgers.collectAsState()
    val searchQuery by viewModel.customerSearchQuery.collectAsState()
    val allWorkEntries by viewModel.allWorkEntries.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()

    var selectedLedgerForProfile by remember { mutableStateOf<CustomerLedgerSummary?>(null) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    // Payment recording modal
    var ledgerForPayment by remember { mutableStateOf<CustomerLedgerSummary?>(null) }
    var paymentAmountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var paymentNoteText by remember { mutableStateOf("") }

    // Summary of total market dues across all customers
    val totalMarketDue = remember(ledgers) { ledgers.sumOf { it.remainingBalance } }
    val totalMarketBilled = remember(ledgers) { ledgers.sumOf { it.totalBillAmount } }
    val totalMarketReceived = remember(ledgers) { ledgers.sumOf { it.totalPaidAmount } }

    // Form field for customer name
    var newCustomerNameInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9F6))
                .padding(bottom = 80.dp)
                .testTag("customers_screen_scroll")
        ) {
            // Search Bar & Market Overview
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setCustomerSearch(it) },
                        placeholder = { Text("Search customers...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TractorGreenPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setCustomerSearch("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_customers_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Overall Market Ledger Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL MARKET BALANCE",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                )
                                Text(
                                    text = "${ledgers.size} Customers",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = FormatUtils.formatCurrency(totalMarketDue),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (totalMarketDue > 0) StatusOverdueRed else Color(0xFF2E7D32),
                                    fontSize = 28.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF1F1F1))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Total Billed", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                    Text(
                                        text = FormatUtils.formatCurrency(totalMarketBilled),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Column {
                                    Text(text = "Total Received", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                    Text(
                                        text = FormatUtils.formatCurrency(totalMarketReceived),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = StatusPaidGreen)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Customer List
            if (ledgers.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No customers match your search" else "No customers added yet",
                            style = MaterialTheme.typography.titleMedium.copy(color = Color.DarkGray, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button to add a customer",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }
            } else {
                items(ledgers, key = { it.customer.id }) { item ->
                    Card(
                        onClick = { selectedLedgerForProfile = item },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("customer_item_${item.customer.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(TractorGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.customer.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.customer.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (item.customer.village.isNotBlank() || item.customer.phone.isNotBlank()) {
                                    Text(
                                        text = listOfNotNull(
                                            item.customer.village.takeIf { it.isNotBlank() },
                                            item.customer.phone.takeIf { it.isNotBlank() }
                                        ).joinToString(" • "),
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "${item.totalWorkEntries} work entries • ${FormatUtils.formatDuration(item.totalWorkMinutes)}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                                )
                            }

                            // Balance
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FormatUtils.formatCurrency(item.remainingBalance),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (item.remainingBalance > 0) StatusOverdueRed else StatusPaidGreen,
                                        fontSize = 17.sp
                                    )
                                )
                                Text(
                                    text = if (item.remainingBalance > 0) "Due" else "Settled",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (item.remainingBalance > 0) StatusOverdueRed else StatusPaidGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Customer FAB
        FloatingActionButton(
            onClick = {
                newCustomerNameInput = ""
                showAddCustomerDialog = true
            },
            containerColor = TractorGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("add_customer_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Customer")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Customer", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal Sheet: Customer Full Profile & Statement
    selectedLedgerForProfile?.let { ledger ->
        val customerEntries = remember(allWorkEntries, ledger) {
            allWorkEntries.filter { it.customerId == ledger.customer.id }
        }
        val customerPayments = remember(allPayments, ledger) {
            allPayments.filter { it.customerId == ledger.customer.id }
        }

        var activeTab by remember { mutableIntStateOf(0) } // 0 = Work Entries, 1 = Payments

        AlertDialog(
            onDismissRequest = { selectedLedgerForProfile = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ledger.customer.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        if (ledger.customer.village.isNotBlank() || ledger.customer.phone.isNotBlank()) {
                            Text(
                                text = listOfNotNull(
                                    ledger.customer.village.takeIf { it.isNotBlank() },
                                    ledger.customer.phone.takeIf { it.isNotBlank() }
                                ).joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    }
                    IconButton(onClick = { selectedLedgerForProfile = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {
                    // Summary Balance Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (ledger.remainingBalance > 0) StatusPendingBg else StatusPaidBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (ledger.remainingBalance > 0) "Pending Balance" else "Account Settled",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = FormatUtils.formatCurrency(ledger.remainingBalance),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (ledger.remainingBalance > 0) StatusOverdueRed else StatusPaidGreen
                                    )
                                )
                            }

                            // Record Payment Action
                            Button(
                                onClick = {
                                    ledgerForPayment = ledger
                                    paymentAmountText = if (ledger.remainingBalance > 0) ledger.remainingBalance.toInt().toString() else ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Receive Payment")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Row: Work Entries vs Payments
                    TabRow(selectedTabIndex = activeTab) {
                        Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                            Text("Work (${customerEntries.size})", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                            Text("Payments (${customerPayments.size})", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tab Content
                    Box(modifier = Modifier.weight(1f)) {
                        if (activeTab == 0) {
                            if (customerEntries.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No work entries for this customer", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(customerEntries) { e ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF9FAF8),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = e.categoryName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text(
                                                        text = "${FormatUtils.formatDate(e.workDateMillis)} • ${FormatUtils.formatDuration(e.durationMinutes)}",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = FormatUtils.formatCurrency(e.totalAmount),
                                                        fontWeight = FontWeight.Bold,
                                                        color = TractorGreenPrimary
                                                    )
                                                    Text(
                                                        text = if (e.amountPending > 0) "Due: ${FormatUtils.formatCurrency(e.amountPending)}" else "Paid",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = if (e.amountPending > 0) StatusOverdueRed else StatusPaidGreen,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (customerPayments.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No payment transactions recorded", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(customerPayments) { p ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF1F8E9),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(text = "Received via ${p.paymentMethod}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(
                                                        text = FormatUtils.formatDate(p.paymentDateMillis),
                                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                                    )
                                                }
                                                Text(
                                                    text = FormatUtils.formatCurrency(p.amount),
                                                    fontWeight = FontWeight.Bold,
                                                    color = StatusPaidGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                // Share Statement
                Button(
                    onClick = {
                        val statement = FormatUtils.buildCustomerStatementText(
                            summary = ledger,
                            entries = customerEntries,
                            payments = customerPayments,
                            ownerName = ownerName,
                            isHindi = false
                        )
                        FormatUtils.shareText(context, statement, "Account Statement - ${ledger.customer.name}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Statement")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLedgerForProfile = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal: Record Payment Dialog
    ledgerForPayment?.let { ledger ->
        AlertDialog(
            onDismissRequest = { ledgerForPayment = null },
            title = { Text("Receive Payment from ${ledger.customer.name}") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Current Balance Due: ${FormatUtils.formatCurrency(ledger.remainingBalance)}",
                        fontWeight = FontWeight.Bold,
                        color = StatusOverdueRed
                    )

                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text("Amount Received (₹)") },
                        prefix = { Text("₹", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("CASH" to "Cash", "UPI" to "UPI / Online").forEach { (m, label) ->
                            val sel = paymentMethod == m
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (sel) TractorGreenPrimary else Color(0xFFEFF3EE),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { paymentMethod = m }
                            ) {
                                Text(
                                    text = label,
                                    color = if (sel) Color.White else Color.DarkGray,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = paymentNoteText,
                        onValueChange = { paymentNoteText = it },
                        placeholder = { Text("Notes (optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = paymentAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.recordPayment(
                                customerId = ledger.customer.id,
                                customerName = ledger.customer.name,
                                amount = amt,
                                method = paymentMethod,
                                dateMillis = System.currentTimeMillis(),
                                notes = paymentNoteText.trim()
                            ) {
                                Toast.makeText(context, "✅ Payment of ₹$amt recorded!", Toast.LENGTH_SHORT).show()
                                ledgerForPayment = null
                                selectedLedgerForProfile = null
                            }
                        } else {
                            Toast.makeText(context, "Please enter valid payment amount", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary)
                ) {
                    Text("Save Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { ledgerForPayment = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Add Customer (ONLY Customer Name - Phase 4)
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
                        value = newCustomerNameInput,
                        onValueChange = { newCustomerNameInput = it },
                        label = { Text("Customer Name") },
                        placeholder = { Text("e.g. Ramesh Patel") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_customer_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustomerNameInput.trim().isNotBlank()) {
                            viewModel.addCustomer(newCustomerNameInput.trim()) {
                                Toast.makeText(context, "✅ Customer added successfully!", Toast.LENGTH_SHORT).show()
                                newCustomerNameInput = ""
                                showAddCustomerDialog = false
                            }
                        } else {
                            Toast.makeText(context, "Please enter customer name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                    modifier = Modifier.testTag("submit_customer_btn")
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
}
