package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CashPayment
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWeekend
import com.example.ui.theme.StatusWeekendContainer
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun SmartCalendarDialog(
    currentSelectedDate: String,
    totalStudentsCount: Int,
    monthlyPayments: List<CashPayment>,
    onSelectDate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var viewingYearMonth by remember {
        mutableStateOf(currentSelectedDate.substring(0, 7))
    }

    val daysInMonth = remember(viewingYearMonth) {
        DateUtils.getAllDaysInMonth(viewingYearMonth)
    }

    val todayIso = remember { DateUtils.getTodayIso() }

    // Payments mapped by date
    val paymentsByDate = remember(monthlyPayments) {
        monthlyPayments.filter { it.isPaid }.groupBy { it.date }
    }

    val dayNamesHeader = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

    // Calculate leading empty spaces for first day of month (0 = Sunday, 1 = Monday, etc.)
    val firstDayOffset = remember(viewingYearMonth) {
        try {
            val parts = viewingYearMonth.split("-")
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, parts[0].toInt())
            cal.set(Calendar.MONTH, parts[1].toInt() - 1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
        } catch (e: Exception) {
            0
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = GeoPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Pantau Kalender Kas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Deteksi Otomatis Hari Sekolah vs Libur",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Month Navigation
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            viewingYearMonth = DateUtils.addMonths(viewingYearMonth, -1)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Bulan Lalu", tint = GeoPrimary)
                        }

                        Text(
                            text = DateUtils.formatToMonthYear(viewingYearMonth),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        IconButton(onClick = {
                            viewingYearMonth = DateUtils.addMonths(viewingYearMonth, 1)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Bulan Depan", tint = GeoPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Day Names (Min, Sen, Sel, Rab, Kam, Jum, Sab)
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayNamesHeader.forEachIndexed { index, name ->
                        val isWeekendCol = index == 0 || index == 6
                        Text(
                            text = name,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWeekendCol) StatusWeekend else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Calendar Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Empty leading slots
                    items(firstDayOffset) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    }

                    // Days in month
                    items(daysInMonth) { dateIso ->
                        val isWeekend = DateUtils.isWeekend(dateIso)
                        val isSelected = dateIso == currentSelectedDate
                        val isToday = dateIso == todayIso
                        val dayNum = DateUtils.getDayNumber(dateIso)

                        val paidCountForDay = paymentsByDate[dateIso]?.size ?: 0
                        val hasUnpaid = !isWeekend && paidCountForDay < totalStudentsCount && totalStudentsCount > 0
                        val isAllPaid = !isWeekend && paidCountForDay >= totalStudentsCount && totalStudentsCount > 0

                        val bgColor = when {
                            isSelected -> GeoPrimary
                            isToday -> MaterialTheme.colorScheme.primaryContainer
                            isWeekend -> Color(0xFFF1F3F9)
                            isAllPaid -> StatusSuccessContainer
                            hasUnpaid && paidCountForDay > 0 -> Color(0xFFFFE082).copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val textColor = when {
                            isSelected -> Color.White
                            isToday -> MaterialTheme.colorScheme.primary
                            isWeekend -> StatusWeekend
                            isAllPaid -> StatusSuccess
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .border(
                                    width = if (isSelected) 2.dp else if (isToday) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) GeoPrimary else if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onSelectDate(dateIso)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = textColor
                                )
                                if (isWeekend) {
                                    Text(
                                        text = "Libur",
                                        fontSize = 8.sp,
                                        color = StatusWeekend,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (totalStudentsCount > 0) {
                                    Text(
                                        text = if (isAllPaid) "Lunas" else "$paidCountForDay/$totalStudentsCount",
                                        fontSize = 8.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else if (isAllPaid) StatusSuccess else Color(0xFFC05621),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Legend
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Keterangan Kalender:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(StatusWeekend))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sabtu/Minggu (Libur)", fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(StatusSuccess))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("100% Lunas", fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE53E3E)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ada Nunggak", fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        onSelectDate(todayIso)
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pilih Hari Ini")
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Tutup")
                    }
                }
            }
        }
    }
}
