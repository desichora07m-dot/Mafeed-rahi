package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.TractorGold
import com.example.ui.theme.TractorGreenPrimary
import com.example.ui.viewmodel.TractorViewModel
import com.example.util.FormatUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TractorViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ownerName by viewModel.ownerName.collectAsState()
    val tractorModel by viewModel.tractorModel.collectAsState()
    val ownerPhone by viewModel.ownerPhone.collectAsState()
    val lastBackupTimestamp by viewModel.lastBackupTimeMillis.collectAsState()

    BackHandler { onNavigateBack() }

    var editOwnerName by remember { mutableStateOf(ownerName) }
    var editTractorModel by remember { mutableStateOf(tractorModel) }
    var editOwnerPhone by remember { mutableStateOf(ownerPhone) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9F6))
    ) {
        // Top Header
        Surface(
            color = TractorGreenPrimary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_btn")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Settings & Data Safety",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Data Safety and Backup (Phase 15 - Most Important)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = TractorGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Data Safety & Backup",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "100% offline local database. Your data is stored safely on your phone without requiring internet.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Last Backup Status Banner
                    val backupFormatted = if (lastBackupTimestamp > 0) {
                        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
                        "Last backup: " + sdf.format(Date(lastBackupTimestamp))
                    } else {
                        "No backup created yet. Tap below to create your first backup."
                    }
                    val isBackupRecent = lastBackupTimestamp > (System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isBackupRecent) StatusPaidBg else StatusOverdueBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isBackupRecent) Icons.Default.Check else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isBackupRecent) StatusPaidGreen else StatusOverdueRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = backupFormatted,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isBackupRecent) StatusPaidGreen else StatusOverdueRed
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Backup Data (JSON)
                    Button(
                        onClick = {
                            scope.launch {
                                val json = viewModel.exportBackupJson()
                                FormatUtils.shareText(context, json, "Tractor Hisab Data Backup")
                                Toast.makeText(context, "✅ Backup created successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("export_backup_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. Backup All Data (JSON)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Export Records in CSV Format
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val csv = viewModel.exportWorkEntriesCsv()
                                FormatUtils.shareCsvFile(context, csv, "tractor_work_records.csv", "Tractor Work Records (CSV)")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("export_csv_btn")
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = TractorGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2. Export Work Records (CSV for Excel)", color = TractorGreenPrimary, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Export Customer Ledger in CSV Format
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val csv = viewModel.exportCustomerLedgersCsv()
                                FormatUtils.shareCsvFile(context, csv, "customer_ledgers.csv", "Customer Ledgers (CSV)")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = TractorGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3. Export Customer Statements (CSV)", color = TractorGreenPrimary, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Restore Backup Data
                    OutlinedButton(
                        onClick = { showRestoreDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("restore_backup_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("4. Restore from Backup", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 2: Owner Profile
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = TractorGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tractor Owner Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editOwnerName,
                        onValueChange = { editOwnerName = it },
                        label = { Text("Owner Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("owner_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editTractorModel,
                        onValueChange = { editTractorModel = it },
                        label = { Text("Tractor Model / Plate No.") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("tractor_model_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editOwnerPhone,
                        onValueChange = { editOwnerPhone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("owner_phone_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.updateOwnerProfile(editOwnerName.trim(), editTractorModel.trim(), editOwnerPhone.trim())
                            Toast.makeText(context, "✅ Profile saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_profile_btn")
                    ) {
                        Text("Save Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 3: App Information
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF3EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tractor Hisab",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TractorGreenPrimary)
                    )
                    Text(
                        text = "Version 1.0 • 100% Offline-First",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dedicated to Indian tractor owners and agricultural operators.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                    )
                }
            }
        }
    }

    // Modal: Restore Confirmation Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Data Backup") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Paste the exported JSON backup text below to restore your customer accounts and work entries:",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                    )
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        placeholder = { Text("Paste JSON backup here...") },
                        maxLines = 8,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(150.dp).testTag("restore_json_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonText.trim().isNotBlank()) {
                            viewModel.restoreBackupJson(restoreJsonText.trim()) { success ->
                                if (success) {
                                    Toast.makeText(context, "✅ Data restored successfully!", Toast.LENGTH_SHORT).show()
                                    restoreJsonText = ""
                                    showRestoreDialog = false
                                } else {
                                    Toast.makeText(context, "❌ Invalid backup format. Please check.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please paste JSON backup text", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary)
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
