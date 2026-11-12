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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    val isHindi by viewModel.isHindi.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()
    val tractorModel by viewModel.tractorModel.collectAsState()
    val ownerPhone by viewModel.ownerPhone.collectAsState()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    val todayEntries by viewModel.todayEntries.collectAsState()
    val summary by viewModel.dashboardSummary.collectAsState()

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
                            text = if (isHindi) "नमस्ते, $ownerName 🌾" else "Welcome, $ownerName 🌾",
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
                                        imageVector = Icons.Default.ArrowBackIosNew,
                                        contentDescription = "Prev Day",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        viewModel.setSelectedDate(System.currentTimeMillis())
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = TractorGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = FormatUtils.formatDateWithDay(selectedDateMillis, isHindi),
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
                                                text = LocaleHelper.getString("today", isHindi),
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
                                        imageVector = Icons.Default.ArrowForwardIos,
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
                        text = LocaleHelper.getString("today_summary", isHindi),
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
                                    text = LocaleHelper.getString("total_hours", isHindi),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = FormatUtils.formatDuration(summary.totalWorkingMinutes, isHindi),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TractorGreenPrimary,
                                        fontSize = 18.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${summary.totalWorkEntriesCount} ${LocaleHelper.getString("work_entries_count", isHindi)}",
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
                                    text = LocaleHelper.getString("total_earnings", isHindi),
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
                                    text = "${summary.totalCustomersServed} ${LocaleHelper.getString("customers_served", isHindi)}",
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
                                    text = LocaleHelper.getString("received_amount", isHindi),
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
                                    text = LocaleHelper.getString("pending_balance", isHindi),
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
                                    text = if (isHindi) "आज" else "Today",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                                Text(
                                    text = FormatUtils.formatDuration(summary.totalWorkingMinutes, isHindi),
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
                                    text = if (isHindi) "७ दिन (सप्ताह)" else "7-Day Week",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                                Text(
                                    text = FormatUtils.formatDuration(summary.weeklyWorkingMinutes, isHindi),
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
                                    text = if (isHindi) "३० दिन (महीना)" else "30-Day Month",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                                Text(
                                    text = FormatUtils.formatDuration(summary.monthlyWorkingMinutes, isHindi),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Daily Work Records Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${LocaleHelper.getString("daily_records", isHindi)} (${todayEntries.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    )

                    TextButton(
                        onClick = onNavigateToAddWork,
                        modifier = Modifier.testTag("quick_add_work_link")
                    ) {
                        Text(
                            text = "+ " + LocaleHelper.getString("tab_add_work", isHindi),
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary
                        )
                    }
                }
            }

            // If empty
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
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_work_rotavator),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = LocaleHelper.getString("no_work_today", isHindi),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = LocaleHelper.getString("tap_to_add_work", isHindi),
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToAddWork,
                                colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("add_work_empty_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LocaleHelper.getString("tab_add_work", isHindi),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items(todayEntries, key = { it.id }) { entry ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { selectedEntryForOptions = entry }
                            .testTag("work_entry_card_${entry.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category visual implement icon
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F8E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = ImplementImageHelper.getDrawableRes(entry.categoryDrawable)),
                                    contentDescription = entry.categoryName,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Customer & Work Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.customerName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1A1C19)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${entry.categoryName} • ${FormatUtils.formatDuration(entry.durationMinutes, isHindi)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.DarkGray,
                                        fontSize = 12.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(
                                        status = entry.getParsedStatus(),
                                        amountPending = entry.amountPending,
                                        isHindi = isHindi
                                    )
                                    if (entry.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "• ${entry.notes}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Total Amount
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FormatUtils.formatCurrency(entry.totalAmount),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TractorGreenPrimary,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "₹${entry.rate}/${entry.getParsedUnit().shortUnitHi}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add Work
        FloatingActionButton(
            onClick = onNavigateToAddWork,
            containerColor = TractorGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(60.dp)
                .testTag("home_add_work_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Work",
                modifier = Modifier.size(32.dp)
            )
        }
    }

    // Modal BottomSheet for Entry Actions (Share, PDF, Edit, Delete)
    if (selectedEntryForOptions != null) {
        val entry = selectedEntryForOptions!!
        ModalBottomSheet(
            onDismissRequest = { selectedEntryForOptions = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = ImplementImageHelper.getDrawableRes(entry.categoryDrawable)),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = entry.customerName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${entry.categoryName} • ${FormatUtils.formatDuration(entry.durationMinutes, isHindi)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = FormatUtils.formatCurrency(entry.totalAmount),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Action 1: Share WhatsApp Receipt
                Button(
                    onClick = {
                        val text = FormatUtils.buildWorkReceiptText(entry, ownerName, tractorModel, isHindi)
                        FormatUtils.shareText(context, text, "Tractor Hisab Bill")
                        selectedEntryForOptions = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("share_receipt_btn")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "व्हाट्सएप / SMS रसीद भेजें" else "Share WhatsApp / SMS Receipt",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action 2: Generate PDF Bill
                OutlinedButton(
                    onClick = {
                        FormatUtils.generateAndSharePdfInvoice(context, entry, ownerName, tractorModel, ownerPhone)
                        selectedEntryForOptions = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("generate_pdf_btn")
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFC62828))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "PDF बिल बनाएं व शेयर करें" else "Generate & Share PDF Bill",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action 3: Edit Work Record
                OutlinedButton(
                    onClick = {
                        val toEdit = entry
                        selectedEntryForOptions = null
                        onNavigateToEditWork(toEdit)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_entry_btn")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = TractorGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "काम विवरण बदलें (Edit)" else "Edit Work Entry",
                        fontWeight = FontWeight.Bold,
                        color = TractorGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action 4: Delete Record
                OutlinedButton(
                    onClick = {
                        entryToDelete = entry
                        selectedEntryForOptions = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    modifier = Modifier.fillMaxWidth().testTag("delete_entry_btn")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "काम हिसाब हटाएं (Delete)" else "Delete Work Record",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Delete confirmation dialog
    if (entryToDelete != null) {
        val entry = entryToDelete!!
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = {
                Text(text = if (isHindi) "काम हिसाब हटाएं?" else "Delete Work Entry?")
            },
            text = {
                Text(
                    text = if (isHindi)
                        "क्या आप ${entry.customerName} का ${entry.categoryName} (${FormatUtils.formatCurrency(entry.totalAmount)}) हिसाब हटाना चाहते हैं? यह स्थायी रूप से हट जाएगा।"
                    else
                        "Are you sure you want to delete the record for ${entry.customerName} - ${entry.categoryName} (${FormatUtils.formatCurrency(entry.totalAmount)})?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWork(entry)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.testTag("confirm_delete_btn")
                ) {
                    Text(text = LocaleHelper.getString("delete", isHindi), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text(text = LocaleHelper.getString("cancel", isHindi))
                }
            }
        )
    }
}
