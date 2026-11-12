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
    val isHindi by viewModel.isHindi.collectAsState()
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
                        text = LocaleHelper.getString("rate_management_title", isHindi),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LocaleHelper.getString("rate_management_sub", isHindi),
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
                                    text = if (isHindi) cat.nameHi else cat.nameEn,
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
                                            text = if (isHindi) "कस्टम" else "Custom",
                                            color = Color(0xFF8D4B00),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (isHindi) cat.nameEn else cat.nameHi,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${LocaleHelper.getString("default_rate", isHindi)}: ₹${cat.defaultRate.toInt()} / ${if (isHindi) cat.getParsedUnit().shortUnitHi else cat.getParsedUnit().shortUnitEn}",
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
                                    contentDescription = "Delete",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB: Add Custom Category
        FloatingActionButton(
            onClick = { showAddCustomCategoryDialog = true },
            containerColor = TractorGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(60.dp)
                .testTag("add_custom_category_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category", modifier = Modifier.size(28.dp))
        }
    }

    // Dialog: Edit Default Rate
    if (categoryToEdit != null) {
        val cat = categoryToEdit!!
        var rateText by remember { mutableStateOf(if (cat.defaultRate % 1.0 == 0.0) cat.defaultRate.toInt().toString() else cat.defaultRate.toString()) }
        var selectedUnit by remember { mutableStateOf(cat.getParsedUnit()) }

        AlertDialog(
            onDismissRequest = { categoryToEdit = null },
            title = {
                Text(
                    text = "${LocaleHelper.getString("edit_rate", isHindi)} - ${if (isHindi) cat.nameHi else cat.nameEn}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text(LocaleHelper.getString("default_rate", isHindi)) },
                        prefix = { Text("₹", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("edit_rate_input")
                    )

                    Text(
                        text = LocaleHelper.getString("rate_unit", isHindi),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    RateUnit.entries.forEach { unit ->
                        val isSel = selectedUnit == unit
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) TractorGreenPrimary else Color(0xFFEFF3EE),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedUnit = unit }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSel) Icons.Default.Check else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isSel) Color.White else Color.Transparent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isHindi) unit.labelHi else unit.labelEn,
                                    color = if (isSel) Color.White else Color.DarkGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newRate = rateText.toDoubleOrNull()
                        if (newRate != null && newRate > 0) {
                            viewModel.saveCategoryRate(cat, newRate, selectedUnit)
                            Toast.makeText(context, if (isHindi) "दर सुरक्षित की गई" else "Rate updated", Toast.LENGTH_SHORT).show()
                            categoryToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                    modifier = Modifier.testTag("save_edited_rate_btn")
                ) {
                    Text(LocaleHelper.getString("save", isHindi))
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToEdit = null }) {
                    Text(LocaleHelper.getString("cancel", isHindi))
                }
            }
        )
    }

    // Dialog: Add Custom Work Category
    if (showAddCustomCategoryDialog) {
        var nameEn by remember { mutableStateOf("") }
        var nameHi by remember { mutableStateOf("") }
        var rateText by remember { mutableStateOf("15") }
        var selectedUnit by remember { mutableStateOf(RateUnit.PER_MINUTE) }
        var selectedDrawable by remember { mutableStateOf("ic_work_custom") }

        AlertDialog(
            onDismissRequest = { showAddCustomCategoryDialog = false },
            title = {
                Text(
                    text = LocaleHelper.getString("add_custom_category", isHindi),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("Category Name (English)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("custom_cat_name_en")
                    )

                    OutlinedTextField(
                        value = nameHi,
                        onValueChange = { nameHi = it },
                        label = { Text("काम का नाम (हिन्दी)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("custom_cat_name_hi")
                    )

                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text(LocaleHelper.getString("default_rate", isHindi)) },
                        prefix = { Text("₹", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = LocaleHelper.getString("rate_unit", isHindi),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RateUnit.entries.forEach { unit ->
                            val isSel = selectedUnit == unit
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) TractorGreenPrimary else Color(0xFFEFF3EE),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedUnit = unit }
                            ) {
                                Text(
                                    text = if (isHindi) unit.shortUnitHi else unit.shortUnitEn,
                                    color = if (isSel) Color.White else Color.DarkGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = rateText.toDoubleOrNull() ?: 15.0
                        if (nameEn.isNotBlank() || nameHi.isNotBlank()) {
                            viewModel.addCustomCategory(nameEn, nameHi, rate, selectedUnit, selectedDrawable)
                            showAddCustomCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TractorGreenPrimary),
                    modifier = Modifier.testTag("save_custom_category_btn")
                ) {
                    Text(LocaleHelper.getString("save", isHindi))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomCategoryDialog = false }) {
                    Text(LocaleHelper.getString("cancel", isHindi))
                }
            }
        )
    }

    // Dialog: Delete Category Confirmation
    if (categoryToDelete != null) {
        val cat = categoryToDelete!!
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text(if (isHindi) "यंत्र हटाएं?" else "Delete Category?") },
            text = {
                Text(
                    text = if (isHindi) "क्या आप '${cat.nameHi}' को हटाना चाहते हैं?"
                    else "Are you sure you want to delete '${cat.nameEn}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategory(cat)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(LocaleHelper.getString("delete", isHindi), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text(LocaleHelper.getString("cancel", isHindi))
                }
            }
        )
    }
}
