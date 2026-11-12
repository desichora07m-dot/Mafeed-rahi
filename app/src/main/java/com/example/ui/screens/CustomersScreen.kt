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
    val isHindi by viewModel.isHindi.collectAsState()
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
                        placeholder = { Text(LocaleHelper.getString("search_customers", isHindi)) },
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
                            .testTag("customer_search_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Market Due Card
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
                                Column {
                                    Text(
                                        text = LocaleHelper.getString("total_due_all", isHindi),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = FormatUtils.formatCurrency(totalMarketDue),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (totalMarketDue > 0) StatusOverdueRed else StatusPaidGreen,
                                            fontSize = 24.sp
                                        )
                                    )
                                }

                                Surface(
                                    color = Color(0xFFEFF3EE),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${ledgers.size} ${LocaleHelper.getString("total_customers", isHindi)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TractorGreenPrimary
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${if (isHindi) "कुल काम बिल:" else "Total Billed:"} ${FormatUtils.formatCurrency(totalMarketBilled)}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                )
                                Text(
                                    text = "${if (isHindi) "कुल जमा:" else "Total Paid:"} ${FormatUtils.formatCurrency(totalMarketReceived)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = StatusPaidGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Customers List
            if (ledgers.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = LocaleHelper.getString("no_customers_found", isHindi),
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }
            } else {
                items(ledgers, key = { it.customer.id }) { ledger ->
                    val customer = ledger.customer
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { selectedLedgerForProfile = ledger }
                            .testTag("customer_item_${customer.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(TractorGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = customer.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Name, Village, and Stats
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (customer.village.isNotBlank() || customer.phone.isNotBlank()) {
                                    Text(
                                        text = "${customer.village} ${if (customer.phone.isNotBlank()) "• ${customer.phone}" else ""}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "${FormatUtils.formatDuration(ledger.totalWorkMinutes, isHindi)} (${ledger.totalWorkEntries} ${if (isHindi) "काम" else "works"})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TractorGreenPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            // Balance Badge
                            Column(horizontalAlignment = Alignment.End) {
                                if (ledger.remainingBalance > 0) {
                                    Surface(
                                        color = StatusOverdueBg,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "${if (isHindi) "बाकी " else "Due "}${FormatUtils.formatCurrency(ledger.remainingBalance)}",
                                            color = StatusOverdueRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = StatusPaidBg,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = if (isHindi) "चुका दिया ✓" else "Settled ✓",
                                            color = StatusPaidGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${if (isHindi) "बिल: " else "Bill: "}${FormatUtils.formatCurrency(ledger.totalBillAmount)}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button: Add New Customer
        FloatingActionButton(
            onClick = { showAddCustomerDialog = true },
            containerColor = TractorGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(60.dp)
                .testTag("add_customer_fab")
        ) {
            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Customer", modifier = Modifier.size(28.dp))
        }
    }

    // Customer Profile & Ledger Sheet (Work & Payment History, Direct Call, WhatsApp share)
    if (selectedLedgerForProfile != null) {
        val ledger = selectedLedgerForProfile!!
        val customer = ledger.customer
        val customerEntries = allWorkEntries.filter { it.customerId == customer.id }
        val customerPayments = allPayments.filter { it.customerId == customer.id }

        var selectedTab by remember { mutableIntStateOf(0) }

        ModalBottomSheet(
            onDismissRequest = { selectedLedgerForProfile = null },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                // Customer Profile Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(TractorGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        )
                        if (customer.village.isNotBlank()) {
                            Text(
                                text = "🏡 ${customer.village}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                            )
                        }
                        if (customer.phone.isNotBlank()) {
                            Text(
                                text = "📞 ${customer.phone}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                            )
                        }
                    }

                    // Direct Call Button
                    if (customer.phone.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.testTag("call_customer_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = TractorGreenPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Financial Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Billed
                    Surface(
                        color = Color(0xFFEFF3EE),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(if (isHindi) "कुल काम" else "Total Work", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                            Text(FormatUtils.formatCurrency(ledger.totalBillAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    // Total Paid
                    Surface(
                        color = StatusPaidBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(if (isHindi) "कुल जमा" else "Paid", style = MaterialTheme.typography.labelSmall.copy(color = StatusPaidGreen))
                            Text(FormatUtils.formatCurrency(ledger.totalPaidAmount), fontWeight = FontWeight.Bold, color = StatusPaidGreen, fontSize = 14.sp)
                        }
                    }
                    // Net Balance Due
                    Surface(
                        color = if (ledger.remainingBalance > 0) StatusOverdueBg else StatusPaidBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(if (isHindi) "बाकी बकाया" else "Due Balance", style = MaterialTheme.typography.labelSmall.copy(color = if (ledger.remainingBalance > 0) StatusOverdueRed else StatusPaidGreen))
                            Text(FormatUtils.formatCurrency(ledger.remainingBalance), fontWeight = FontWeight.ExtraBold, color = if (ledger.remainingBalance > 0) StatusOverdueRed else StatusPaidGreen, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: + Record Payment & Share WhatsApp Statement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            ledgerForPayment = ledger
                            paymentAmountText = if (ledger.remainingBalance > 0) ledger.remainingBalance.toInt().toString() else ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f).testTag("record_payment_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "रुपये जमा करें" else "Record Payment",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val statement = FormatUtils.buildCustomerStatementText(
                                ledger,
                                customerEntries,
                                customerPayments,
                                ownerName,
                                isHindi
                            )
                            FormatUtils.shareText(context, statement, "Customer Statement")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("share_statement_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "खाता भेजें" else "Share Bill",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Work & Payment History Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = TractorGreenPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "${LocaleHelper.getString("work_history", isHindi)} (${customerEntries.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "${LocaleHelper.getString("payment_history", isHindi)} (${customerPayments.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (selectedTab == 0) {
                        // Work Entries List
                        if (customerEntries.isEmpty()) {
                            Text(
                                text = if (isHindi) "कोई काम दर्ज नहीं है" else "No work entries found",
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            customerEntries.forEach { entry ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAF8)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = ImplementImageHelper.getDrawableRes(entry.categoryDrawable)),
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(entry.categoryName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(
                                                "${FormatUtils.formatDate(entry.workDateMillis, isHindi)} • ${FormatUtils.formatDuration(entry.durationMinutes, isHindi)}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                            )
                                        }
                                        Text(
                                            FormatUtils.formatCurrency(entry.totalAmount),
                                            fontWeight = FontWeight.Bold,
                                            color = TractorGreenPrimary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Payment Transactions List
                        if (customerPayments.isEmpty()) {
                            Text(
                                text = if (isHindi) "कोई भुगतान इतिहास नहीं मिला" else "No payment transactions recorded",
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            customerPayments.forEach { payment ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAF8)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(StatusPaidBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Money,
                                                contentDescription = null,
                                                tint = StatusPaidGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${payment.paymentMethod} Payment",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "${FormatUtils.formatDate(payment.paymentDateMillis, isHindi)}${if (payment.notes.isNotBlank()) " • ${payment.notes}" else ""}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                            )
                                        }
                                        Text(
                                            text = "+${FormatUtils.formatCurrency(payment.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            color = StatusPaidGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Edit / Delete Customer buttons at bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            customerToEdit = customer
                            selectedLedgerForProfile = null
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "ग्राहक विवरण बदलें" else "Edit Customer")
                    }

                    TextButton(
                        onClick = {
                            customerToDelete = customer
                            selectedLedgerForProfile = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "ग्राहक हटाएं" else "Delete Customer")
                    }
                }
            }
        }
    }

    // Modal: Record Payment from Customer Profile
    if (ledgerForPayment != null) {
        val ledger = ledgerForPayment!!
        AlertDialog(
            onDismissRequest = { ledgerForPayment = null },
            title = {
                Text(
                    text = "${if (isHindi) "रुपये जमा करें" else "Record Payment"} - ${ledger.customer.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${if (isHindi) "वर्तमान बकाया:" else "Current Balance Due:"} ${FormatUtils.formatCurrency(ledger.remainingBalance)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StatusOverdueRed
                        )
                    )

                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text(if (isHindi) "मिली राशि (₹)" else "Amount Received (₹)") },
                        prefix = { Text("₹", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                    )

                    // Method toggle (Cash / UPI)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "माध्यम:" else "Method:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        listOf("CASH" to (if (isHindi) "नगद" else "Cash"), "UPI" to "UPI", "BANK" to "Bank").forEach { (m, label) ->
                            val isSel = paymentMethod == m
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) TractorGreenPrimary else Color(0xFFEFF3EE),
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clickable { paymentMethod = m }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else Color.DarkGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = paymentNoteText,
                        onValueChange = { paymentNoteText = it },
                        label = { Text(if (isHindi) "नोट (ऐच्छिक)" else "Note (Optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountText.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            viewModel.recordPayment(
                                customerId = ledger.customer.id,
                                customerName = ledger.customer.name,
                                amount = amount,
                                method = paymentMethod,
                                dateMillis = System.currentTimeMillis(),
                                notes = paymentNoteText.trim()
                            ) {
                                Toast.makeText(
                                    context,
                                    if (isHindi) "₹$amount जमा दर्ज हुआ!" else "Payment of ₹$amount recorded!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                ledgerForPayment = null
                            }
                        } else {
                            Toast.makeText(context, if (isHindi) "कृपया सही राशि दर्ज करें" else "Please enter valid amount", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                    modifier = Modifier.testTag("confirm_record_payment_btn")
                ) {
                    Text(LocaleHelper.getString("save", isHindi))
                }
            },
            dismissButton = {
                TextButton(onClick = { ledgerForPayment = null }) {
                    Text(LocaleHelper.getString("cancel", isHindi))
                }
            }
        )
    }

    // Dialog: Add Customer
    if (showAddCustomerDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var village by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = { Text(LocaleHelper.getString("add_new_customer", isHindi), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(LocaleHelper.getString("enter_customer_name", isHindi)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_customer_name_dialog_input")
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(LocaleHelper.getString("customer_phone", isHindi)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = village,
                        onValueChange = { village = it },
                        label = { Text(LocaleHelper.getString("customer_village", isHindi)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.trim().isNotBlank()) {
                            viewModel.addCustomer(name, phone, village) {
                                showAddCustomerDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary)
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

    // Dialog: Edit Customer
    if (customerToEdit != null) {
        val cust = customerToEdit!!
        var editName by remember { mutableStateOf(cust.name) }
        var editPhone by remember { mutableStateOf(cust.phone) }
        var editVillage by remember { mutableStateOf(cust.village) }

        AlertDialog(
            onDismissRequest = { customerToEdit = null },
            title = { Text(if (isHindi) "ग्राहक विवरण बदलें" else "Edit Customer", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(LocaleHelper.getString("enter_customer_name", isHindi)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text(LocaleHelper.getString("customer_phone", isHindi)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editVillage,
                        onValueChange = { editVillage = it },
                        label = { Text(LocaleHelper.getString("customer_village", isHindi)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.trim().isNotBlank()) {
                            viewModel.updateCustomer(cust.copy(name = editName.trim(), phone = editPhone.trim(), village = editVillage.trim())) {
                                customerToEdit = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary)
                ) {
                    Text(LocaleHelper.getString("save", isHindi))
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToEdit = null }) {
                    Text(LocaleHelper.getString("cancel", isHindi))
                }
            }
        )
    }

    // Dialog: Confirm Delete Customer
    if (customerToDelete != null) {
        val cust = customerToDelete!!
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text(if (isHindi) "ग्राहक हटाएं?" else "Delete Customer?") },
            text = {
                Text(
                    text = if (isHindi)
                        "क्या आप ${cust.name} और उनके सभी काम और भुगतान हिसाब हटाना चाहते हैं? यह क्रिया वापस नहीं की जा सकती।"
                    else
                        "Are you sure you want to delete ${cust.name} and all their work entries and payment history?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(cust)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(LocaleHelper.getString("delete", isHindi), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text(LocaleHelper.getString("cancel", isHindi))
                }
            }
        )
    }
}
