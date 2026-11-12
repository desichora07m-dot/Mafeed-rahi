package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.data.model.WorkCategory
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
import com.example.util.FormatUtils
import com.example.util.LocaleHelper

enum class AppScreen(val route: String, val icon: ImageVector) {
    HOME("home", Icons.Default.Home),
    ADD_WORK("add_work", Icons.Default.AddCircle),
    CUSTOMERS("customers", Icons.Default.People),
    RATES("rates", Icons.Default.Tune),
    REPORTS("reports", Icons.Default.Assessment),
    SETTINGS("settings", Icons.Default.Settings)
}

object ImplementImageHelper {
    fun getDrawableRes(drawableName: String): Int {
        return when (drawableName.lowercase().trim()) {
            "img_cultivator", "ic_work_cultivator" -> R.drawable.img_cultivator
            "img_kartal", "ic_work_kartal" -> R.drawable.img_kartal
            "img_rotavator", "ic_work_rotavator" -> R.drawable.img_rotavator
            "img_trolley", "ic_work_trolley" -> R.drawable.img_trolley
            "img_mitti_cutting", "ic_work_mitti_karna" -> R.drawable.img_mitti_cutting
            "ic_work_land_leveler" -> R.drawable.ic_work_land_leveler
            "ic_work_paddy_thresher" -> R.drawable.ic_work_paddy_thresher
            "ic_work_maize_thresher" -> R.drawable.ic_work_maize_thresher
            else -> R.drawable.ic_work_custom
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TractorTopBar(
    title: String = "Tractor Hisab",
    subtitle: String? = "Tractor Work & Accounting",
    isHindi: Boolean = false,
    onToggleLanguage: (() -> Unit)? = null,
    onNavigateSettings: () -> Unit
) {
    Surface(
        color = TractorGreenPrimary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Tractor Logo Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(
                onClick = onNavigateSettings,
                modifier = Modifier.testTag("settings_top_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun TractorBottomBar(
    currentScreen: AppScreen,
    isHindi: Boolean = false,
    onScreenSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val items = listOf(
            Triple(AppScreen.HOME, "Home", Icons.Default.Home),
            Triple(AppScreen.ADD_WORK, "Add Work", Icons.Default.AddCircle),
            Triple(AppScreen.CUSTOMERS, "Customers", Icons.Default.People),
            Triple(AppScreen.RATES, "Rates", Icons.Default.Tune),
            Triple(AppScreen.REPORTS, "Reports", Icons.Default.Assessment)
        )

        items.forEach { (screen, label, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(if (screen == AppScreen.ADD_WORK) 28.dp else 24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TractorGreenPrimary,
                    selectedTextColor = TractorGreenPrimary,
                    indicatorColor = TractorGreenPrimary.copy(alpha = 0.12f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                ),
                modifier = Modifier.testTag("nav_${screen.route}")
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: PaymentStatus,
    amountPending: Double = 0.0,
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon) = when (status) {
        PaymentStatus.PAID -> Quad(
            StatusPaidBg,
            StatusPaidGreen,
            "Paid",
            Icons.Default.CheckCircle
        )
        PaymentStatus.PARTIAL -> Quad(
            StatusPendingBg,
            StatusPendingOrange,
            "Partial" + if (amountPending > 0) " (${FormatUtils.formatCurrency(amountPending)})" else "",
            Icons.Default.HourglassEmpty
        )
        PaymentStatus.UNPAID -> Quad(
            StatusOverdueBg,
            StatusOverdueRed,
            "Unpaid" + if (amountPending > 0) " (${FormatUtils.formatCurrency(amountPending)})" else "",
            Icons.Default.HourglassBottom
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun WorkCategoryGridCard(
    category: WorkCategory,
    isSelected: Boolean,
    isHindi: Boolean = false,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TractorGreenPrimary.copy(alpha = 0.08f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) TractorGreenPrimary else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("category_card_${category.key}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Realistic Photography Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFFE8ECE5)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = ImplementImageHelper.getDrawableRes(category.drawableName)),
                    contentDescription = category.nameEn,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(TractorGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Category Name
                Text(
                    text = category.nameEn,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isSelected) TractorGreenPrimary else Color(0xFF212121)
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Default rate badge
                Surface(
                    color = TractorGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "₹${category.defaultRate.toInt()} / ${category.getParsedUnit().shortUnitEn}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8D4B00),
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OtherWorkGridCard(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TractorGreenPrimary.copy(alpha = 0.08f) else Color(0xFFF9FAF8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.5.dp else 1.5.dp,
                color = if (isSelected) TractorGreenPrimary else TractorGreenPrimary.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("category_card_other_work")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(TractorGreenPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Other Work",
                        tint = TractorGreenPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(TractorGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Other Work",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TractorGreenPrimary
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = TractorGreenPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "+ Custom Work",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TractorGreenPrimary,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
