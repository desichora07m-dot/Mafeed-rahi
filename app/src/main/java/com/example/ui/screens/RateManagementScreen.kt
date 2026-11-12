package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import com.example.data.model.RateUnit
import com.example.data.model.WorkCategory
import com.example.ui.components.ImplementImageHelper
import com.example.ui.theme.TractorGold
import com.example.ui.theme.TractorGreenPrimary
import com.example.ui.viewmodel.TractorViewModel
import com.example.util.LocaleHelper

@Composable
fun RateManagementScreen(
    viewModel: TractorViewModel
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()

    var categoryToEdit by remember { mutableStateOf<WorkCategory?>(null) }
    var showAddCustomCategoryDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<WorkCategory?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9F6))
                .padding(bottom = 80.dp)
                .testTag("rate_management_scroll")
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Work Categories & Rates",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage default rates and add your own custom farm implements",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
            }

            items(categories, key = { it.id }) { cat ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("rate_item_${cat.key}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Visual Implement Icon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F8E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = ImplementImageHelper.getDrawableRes(cat.drawableName)),
                                contentDescription = cat.nameEn,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Category Name & Default Rate
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cat.nameEn,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                if (cat.isCustom) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = TractorGold.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Custom",
                                            color = Color(0xFF8D4B00),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Default Rate: ₹${cat.defaultRate.toInt()} / ${cat.getParsedUnit().labelEn}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TractorGreenPrimary
                                )
                            )
                        }

                        // Edit Rate Button
                        IconButton(
                            onClick = { categoryToEdit = cat },
                            modifier = Modifier.testTag("edit_rate_btn_${cat.key}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Rate",
                                tint = TractorGreenPrimary
                            )
                        }

                        // Delete button (if custom)
                        if (cat.isCustom) {
                            IconButton(
                                onClick = { categoryToDelete = cat },
                                modifier = Modifier.testTag("delete_category_btn_${cat.key}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Category",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Custom Category FAB
        FloatingActionButton(
            onClick = { showAddCustomCategoryDialog = true },
            containerColor = TractorGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("add_custom_category_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Custom Work", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal: Edit Rate Dialog
    categoryToEdit?.let { cat ->
        var editRateText by remember { mutableStateOf(if (cat.defaultRate % 1.0 == 0.0) cat.defaultRate.toInt().toString() else cat.defaultRate.toString()) }
        var editUnit by remember { mutableStateOf(cat.getParsedUnit()) }

        AlertDialog(
            onDismissRequest = { categoryToEdit = null },
            title = { Text("Edit Rate: ${cat.nameEn}") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editRateText,
                        onValueChange = { editRateText = it },
                        label = { Text("Default Rate") },
                        prefix = { Text("₹", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("edit_rate_input")
                    )

                    Text("Rate Type:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RateUnit.entries.forEach { unit ->
                            val sel = editUnit == unit
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (sel) TractorGreenPrimary else Color(0xFFEFF3EE),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { editUnit = unit }
                            ) {
                                Text(
                                    text = unit.labelEn,
                                    color = if (sel) Color.White else Color.DarkGray,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
                        val newRate = editRateText.toDoubleOrNull() ?: cat.defaultRate
                        viewModel.saveCategoryRate(cat, newRate, editUnit)
                        Toast.makeText(context, "✅ Rate updated successfully!", Toast.LENGTH_SHORT).show()
                        categoryToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                    modifier = Modifier.testTag("save_edit_rate_btn")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Add Custom Category Dialog
    if (showAddCustomCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        var newCatRate by remember { mutableStateOf("15") }
        var newCatUnit by remember { mutableStateOf(RateUnit.PER_MINUTE) }

        AlertDialog(
            onDismissRequest = { showAddCustomCategoryDialog = false },
            title = { Text("Add Custom Work Category") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Work Name") },
                        placeholder = { Text("e.g. Deep Ploughing, Bund Maker") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("new_cat_name_en")
                    )

                    OutlinedTextField(
                        value = newCatRate,
                        onValueChange = { newCatRate = it },
                        label = { Text("Default Rate (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Unit Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RateUnit.entries.forEach { unit ->
                            val sel = newCatUnit == unit
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (sel) TractorGreenPrimary else Color(0xFFEFF3EE),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { newCatUnit = unit }
                            ) {
                                Text(
                                    text = unit.labelEn,
                                    color = if (sel) Color.White else Color.DarkGray,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
                        if (newCatName.trim().isNotBlank()) {
                            val rateVal = newCatRate.toDoubleOrNull() ?: 15.0
                            viewModel.addCustomCategory(
                                name = newCatName.trim(),
                                rate = rateVal,
                                unit = newCatUnit
                            )
                            Toast.makeText(context, "✅ Category added successfully!", Toast.LENGTH_SHORT).show()
                            showAddCustomCategoryDialog = false
                        } else {
                            Toast.makeText(context, "Please enter work name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary)
                ) {
                    Text("Add Category")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Delete Custom Category
    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category?") },
            text = { Text("Are you sure you want to delete '${cat.nameEn}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategory(cat)
                        categoryToDelete = null
                        Toast.makeText(context, "Category removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
