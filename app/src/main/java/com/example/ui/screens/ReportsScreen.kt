package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.WorkEntry
import com.example.ui.components.ImplementImageHelper
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

@Composable
fun ReportsScreen(
    viewModel: TractorViewModel
) {
    val context = LocalContext.current
    val isHindi by viewModel.isHindi.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()
    val allWorkEntries by viewModel.allWorkEntries.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()

    var selectedPeriodTab by remember { mutableIntStateOf(0) } // 0 = Daily, 1 = Weekly (7d), 2 = Monthly (30d)

    val now = System.currentTimeMillis()
    val filteredEntries = remember(allWorkEntries, selectedPeriodTab) {
        when (selectedPeriodTab) {
            0 -> allWorkEntries.filter { FormatUtils.isSameDay(it.workDateMillis, now) }
            1 -> {
                val weekAgo = now - (7L * 24 * 60 * 60 * 1000)
                allWorkEntries.filter { it.workDateMillis >= weekAgo }
            }
            else -> {
                val monthAgo = now - (30L * 24 * 60 * 60 * 1000)
                allWorkEntries.filter { it.workDateMillis >= monthAgo }
            }
        }
    }

    val totalHoursMinutes = remember(filteredEntries) { filteredEntries.sumOf { it.durationMinutes } }
    val totalEarnings = remember(filteredEntries) { filteredEntries.sumOf { it.totalAmount } }
    val totalReceived = remember(filteredEntries) { filteredEntries.sumOf { it.amountPaid } }
    val totalPending = remember(filteredEntries) { filteredEntries.sumOf { it.amountPending } }
    val uniqueCustomers = remember(filteredEntries) { filteredEntries.map { it.customerId }.distinct().size }

    // Category breakdown
    val categoryBreakdown = remember(filteredEntries) {
        filteredEntries.groupBy { it.categoryKey }
            .map { (key, entries) ->
                Triple(
                    entries.first().categoryName,
                    entries.first().categoryDrawable,
                    Pair(entries.sumOf { it.durationMinutes }, entries.sumOf { it.totalAmount })
                )
            }
            .sortedByDescending { it.third.second }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9F6))
            .padding(bottom = 80.dp)
            .testTag("reports_screen_scroll")
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = LocaleHelper.getString("reports_title", isHindi),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Period Tabs (Daily, Weekly, Monthly)
                TabRow(
                    selectedTabIndex = selectedPeriodTab,
                    containerColor = Color.White,
                    contentColor = TractorGreenPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("report_period_tabs")
                ) {
                    Tab(
                        selected = selectedPeriodTab == 0,
                        onClick = { selectedPeriodTab = 0 },
                        text = {
                            Text(
                                text = LocaleHelper.getString("daily", isHindi),
                                fontWeight = if (selectedPeriodTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedPeriodTab == 1,
                        onClick = { selectedPeriodTab = 1 },
                        text = {
                            Text(
                                text = LocaleHelper.getString("weekly", isHindi),
                                fontWeight = if (selectedPeriodTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedPeriodTab == 2,
                        onClick = { selectedPeriodTab = 2 },
                        text = {
                            Text(
                                text = LocaleHelper.getString("monthly", isHindi),
                                fontWeight = if (selectedPeriodTab == 2) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Big Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = LocaleHelper.getString("total_earnings", isHindi),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                                Text(
                                    text = FormatUtils.formatCurrency(totalEarnings),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TractorGreenPrimary,
                                        fontSize = 28.sp
                                    )
                                )
                            }

                            Surface(
                                color = Color(0xFFEFF3EE),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = FormatUtils.formatDuration(totalHoursMinutes, isHindi),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TractorGreenPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(14.dp))

                        // Received, Pending, and Customers Served Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(LocaleHelper.getString("received_amount", isHindi), style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                Text(FormatUtils.formatCurrency(totalReceived), fontWeight = FontWeight.Bold, color = StatusPaidGreen, fontSize = 15.sp)
                            }

                            Column {
                                Text(LocaleHelper.getString("pending_balance", isHindi), style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                Text(FormatUtils.formatCurrency(totalPending), fontWeight = FontWeight.Bold, color = if (totalPending > 0) StatusOverdueRed else Color.Gray, fontSize = 15.sp)
                            }

                            Column {
                                Text(LocaleHelper.getString("customers_served", isHindi), style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                Text("$uniqueCustomers", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 15.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Share / Export Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val periodName = when (selectedPeriodTab) {
                                0 -> if (isHindi) "दैनिक रिपोर्ट" else "Daily Report"
                                1 -> if (isHindi) "साप्ताहिक रिपोर्ट (7 दिन)" else "Weekly Report"
                                else -> if (isHindi) "मासिक रिपोर्ट (30 दिन)" else "Monthly Report"
                            }
                            val sb = StringBuilder()
                            sb.appendLine("🚜 *Tractor Hisab - $periodName*")
                            sb.appendLine("━━━━━━━━━━━━━━━━━━")
                            sb.appendLine("⏱️ काम समय: ${FormatUtils.formatDuration(totalHoursMinutes, isHindi)}")
                            sb.appendLine("💵 कुल कमाई: ${FormatUtils.formatCurrency(totalEarnings)}")
                            sb.appendLine("✅ कुल जमा: ${FormatUtils.formatCurrency(totalReceived)}")
                            sb.appendLine("⚠️ बाकी बकाया: ${FormatUtils.formatCurrency(totalPending)}")
                            sb.appendLine("👥 कुल ग्राहक: $uniqueCustomers")
                            sb.appendLine("━━━━━━━━━━━━━━━━━━")
                            sb.appendLine("यंत्र-वार काम:")
                            categoryBreakdown.forEach { (name, _, data) ->
                                sb.appendLine("• $name: ${FormatUtils.formatDuration(data.first, isHindi)} | ${FormatUtils.formatCurrency(data.second)}")
                            }
                            if (ownerName.isNotBlank()) {
                                sb.appendLine("──────────────────")
                                sb.appendLine("ट्रैक्टर मालिक: $ownerName")
                            }
                            FormatUtils.shareText(context, sb.toString(), "Tractor Hisab Report")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("share_report_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LocaleHelper.getString("share_report", isHindi),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Category Wise Work Breakdown
                Text(
                    text = LocaleHelper.getString("category_breakdown", isHindi),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                )
            }
        }

        if (categoryBreakdown.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isHindi) "इस अवधि में कोई काम दर्ज नहीं है" else "No work entries recorded for this period",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(categoryBreakdown) { (catName, catDrawable, data) ->
                val (mins, earnings) = data
                val percentage = if (totalEarnings > 0) (earnings / totalEarnings).toFloat() else 0f

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = ImplementImageHelper.getDrawableRes(catDrawable)),
                                contentDescription = null,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(catName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "${FormatUtils.formatDuration(mins, isHindi)} (${(percentage * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                            Text(
                                text = FormatUtils.formatCurrency(earnings),
                                fontWeight = FontWeight.ExtraBold,
                                color = TractorGreenPrimary,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { percentage },
                            color = TractorGreenPrimary,
                            trackColor = Color(0xFFEFF3EE),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}
