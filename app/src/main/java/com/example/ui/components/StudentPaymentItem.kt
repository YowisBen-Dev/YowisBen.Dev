package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentDayPaymentState
import com.example.ui.UserRole
import com.example.ui.theme.GeoLightOutlineVariant
import com.example.ui.theme.GeoLightSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.util.FormatUtils

@Composable
fun StudentPaymentItem(
    state: StudentDayPaymentState,
    dailyAmount: Long,
    userRole: UserRole,
    onTogglePay: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPaid = state.isPaidToday
    val student = state.student

    val cardBgColor by animateColorAsState(
        targetValue = if (isPaid) Color(0xFFF1F0F4) else Color.White,
        label = "cardBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onCardClick)
            .testTag("student_item_${student.absenNumber}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = if (isPaid) null else BorderStroke(1.dp, GeoLightOutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Accent Bar for Paid Items
            if (isPaid) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(68.dp)
                        .background(GeoPrimary)
                )
            } else {
                Spacer(modifier = Modifier.width(5.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Circular Badge with Initials / Absen
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (isPaid) GeoPrimary else Color(0xFFBDBDBD),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = student.name.split(" ")
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .joinToString("")
                        .ifEmpty { "${student.absenNumber}" }

                    Text(
                        text = initials,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Middle: Name & Subtitle
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${student.absenNumber}. ${student.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaid) Color(0xFF1A1C1E) else Color(0xFF1A1C1E).copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Gender Pill
                        Surface(
                            color = if (student.gender.equals("L", true)) Color(0xFFD6E3FF) else Color(0xFFFFD8E4),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = student.gender,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (student.gender.equals("L", true)) Color(0xFF004786) else Color(0xFF705575),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = if (isPaid) "Lunas: ${FormatUtils.formatRupiah(state.amountPaidToday.takeIf { it > 0 } ?: dailyAmount)}" else "Belum Bayar: ${FormatUtils.formatRupiah(dailyAmount)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isPaid) GeoPrimary else Color(0xFF44474E)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Checkbox / Toggle Pay
                if (userRole == UserRole.TREASURER) {
                    IconButton(
                        onClick = onTogglePay,
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                color = if (isPaid) GeoPrimary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .testTag("toggle_pay_${student.absenNumber}")
                    ) {
                        if (isPaid) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Tandai Belum Bayar",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(2.dp, GeoLightOutlineVariant),
                                color = Color.Transparent,
                                modifier = Modifier.size(24.dp)
                            ) {}
                        }
                    }
                } else {
                    IconButton(
                        onClick = onCardClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Detail Siswa",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

