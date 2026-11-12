package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PaymentStatus
import com.example.data.model.WorkEntry
import com.example.ui.components.ImplementImageHelper
import com.example.ui.components.StatusBadge
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TractorViewModel,
    onNavigateToAddWork: () -> Unit,
    onNavigateToEditWork: (WorkEntry) -> Unit
) {
    val context = LocalContext.current
    val ownerName by viewModel.ownerName.collectAsState()
    val tractorModel by viewModel.tractorModel.collectAsState()
    val ownerPhone by viewModel.ownerPhone.collectAsState()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    val todayEntries by viewModel.todayEntries.collectAsState()
    val summary by viewModel.dashboardSummary.collectAsState()

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var selectedEntryForOptions by remember { mutableStateOf<WorkEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<WorkEntry?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9F6))
                .padding(bottom = 80.dp)
                .testTag("home_screen_scroll")
        ) {
            // Hero Banner with farm photography
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_farm_hero),
                        contentDescription = "Farm Hero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        TractorGreenDark.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    // Content inside hero
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Welcome, $ownerName 🌾",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = if (tractorModel.isNotBlank()) "🚜 $tractorModel" else "🚜 Agricultural Tractor",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TractorGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Date navigation bar
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.Black.copy(alpha = 0.45f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { viewModel.changeSelectedDate(-1) },
                                    modifier = Modifier.size(32.dp).testTag("prev_day_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                        contentDescription = "Prev Day",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { showDatePickerDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = TractorGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = FormatUtils.formatDateWithDay(selectedDateMillis),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (FormatUtils.isSameDay(selectedDateMillis, System.currentTimeMillis())) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = TractorGreenLight,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "Today",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.changeSelectedDate(1) },
                                    modifier = Modifier.size(32.dp).testTag("next_day_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = "Next Day",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Summary Stats Grid (Today's Hours, Earnings, Received, Pending)
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Today's Work Summary",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 1: Work Hours & Total Earnings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Work Hours Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Work Hours",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = FormatUtils.formatDuration(summary.totalWorkingMinutes),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TractorGreenPrimary,
                                        fontSize = 18.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${summary.totalWorkEntriesCount} Entries",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                            }
                        }

                        // Total Earnings Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Total Earnings",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = FormatUtils.formatCurrency(summary.totalEarnings),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        fontSize = 18.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${summary.totalCustomersServed} Customers",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Cash Received & Pending Balance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Received
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StatusPaidBg),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Cash Received",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = StatusPaidGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = FormatUtils.formatCurrency(summary.totalPaymentsReceived),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StatusPaidGreen
                                    )
                                )
                            }
                        }

                        // Pending
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (summary.totalPendingPayments > 0) StatusPendingBg else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Pending Due",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (summary.totalPendingPayments > 0) StatusPendingOrange else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = FormatUtils.formatCurrency(summary.totalPendingPayments),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (summary.totalPendingPayments > 0) StatusPendingOrange else Color.Gray
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Working Hours Overview (Today, Weekly, Monthly)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF4EC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Today",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                                Text(
                                    text = FormatUtils.formatDuration(summary.totalWorkingMinutes),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TractorGreenPrimary
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(Color.LightGray)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "7-Day Week",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                                Text(
                                    text = FormatUtils.formatDuration(summary.weeklyWorkingMinutes),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(Color.LightGray)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "30-Day Month",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                                Text(
                                    text = FormatUtils.formatDuration(summary.monthlyWorkingMinutes),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Daily Records Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Work Records (${todayEntries.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    )

                    Text(
                        text = FormatUtils.formatDate(selectedDateMillis),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
            }

            // Entries List or Empty State
            if (todayEntries.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = null,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No tractor work recorded for this day",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the + button below to record field work",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToAddWork,
                                colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Work Now")
                            }
                        }
                    }
                }
            } else {
                items(todayEntries, key = { it.id }) { entry ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("work_entry_card_${entry.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Visual Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFEFF3EE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = ImplementImageHelper.getDrawableRes(entry.categoryDrawable)),
                                            contentDescription = entry.categoryName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = entry.customerName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF212121)
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${entry.categoryName} • ${FormatUtils.formatDuration(entry.durationMinutes)}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                StatusBadge(
                                    status = entry.getParsedStatus(),
                                    amountPending = entry.amountPending,
                                    isHindi = false
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF1F1F1))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Rate: ₹${entry.rate} / ${entry.getParsedUnit().shortUnitEn}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                    )
                                    Text(
                                        text = "Total Bill: ${FormatUtils.formatCurrency(entry.totalAmount)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TractorGreenPrimary
                                        )
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Share Text Receipt
                                    IconButton(
                                        onClick = {
                                            val text = FormatUtils.buildWorkReceiptText(entry, ownerName, tractorModel, false)
                                            FormatUtils.shareText(context, text, "Tractor Bill - ${entry.customerName}")
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = TractorGreenPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Generate & Share PDF
                                    IconButton(
                                        onClick = {
                                            FormatUtils.generateAndSharePdfInvoice(context, entry, ownerName, tractorModel, ownerPhone)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = "PDF",
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // More options (Edit / Delete)
                                    IconButton(
                                        onClick = { selectedEntryForOptions = entry },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            if (entry.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📝 ${entry.notes}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.DarkGray,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button: Add Work
        FloatingActionButton(
            onClick = onNavigateToAddWork,
            containerColor = TractorGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("home_add_work_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Work")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Work", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal: DatePicker Dialog for Home Screen
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.setSelectedDate(it)
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

    // Options Bottom Sheet for an entry
    selectedEntryForOptions?.let { entry ->
        ModalBottomSheet(
            onDismissRequest = { selectedEntryForOptions = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "${entry.customerName} - ${entry.categoryName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${FormatUtils.formatDuration(entry.durationMinutes)} • ${FormatUtils.formatCurrency(entry.totalAmount)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Button
                OutlinedButton(
                    onClick = {
                        selectedEntryForOptions = null
                        onNavigateToEditWork(entry)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_entry_btn")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = TractorGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Work Record", color = TractorGreenPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Share Text
                Button(
                    onClick = {
                        selectedEntryForOptions = null
                        val text = FormatUtils.buildWorkReceiptText(entry, ownerName, tractorModel, false)
                        FormatUtils.shareText(context, text, "Tractor Bill - ${entry.customerName}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share WhatsApp / SMS Bill")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Delete Button
                OutlinedButton(
                    onClick = {
                        entryToDelete = entry
                        selectedEntryForOptions = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("delete_entry_btn")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Record", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Work Record?") },
            text = { Text("Are you sure you want to delete this record for ${entry.customerName} (${entry.categoryName})?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWork(entry) {
                            entryToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
