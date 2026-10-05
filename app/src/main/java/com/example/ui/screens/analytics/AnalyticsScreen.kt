package com.example.ui.screens.analytics

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.BankRepository
import com.example.ui.components.AKDisclaimerNotice
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary

@Composable
fun AnalyticsScreen(
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Spending Charts", "Account Statements")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NavyPrimary,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        AKDisclaimerNotice()

        when (selectedTab) {
            0 -> SpendingChartsTab(transactions = transactions)
            1 -> StatementsTab(accounts = accounts, transactions = transactions)
        }
    }
}

// -------------------------------------------------------------
// 1. SPENDING CHARTS TAB
// -------------------------------------------------------------
@Composable
fun SpendingChartsTab(transactions: List<TransactionEntity>) {
    val totalIncome = transactions.filter { it.type == "DEPOSIT" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type != "DEPOSIT" }.sumOf { it.amount }

    // Dynamic Category grouping
    val categoryTotals = transactions.filter { it.type != "DEPOSIT" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val categoryColors = listOf(
        Pair("SHOPPING", Color(0xFF3B82F6)),
        Pair("BILLS", Color(0xFFF59E0B)),
        Pair("TRANSFERS", Color(0xFF10B981)),
        Pair("FOOD", Color(0xFFEC4899)),
        Pair("TRANSPORT", Color(0xFF8B5CF6)),
        Pair("OTHER", Color(0xFF64748B))
    )

    val highestCategory = categoryTotals.maxByOrNull { it.value }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Smart Financial Insights Card
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MintSecondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Insights",
                            tint = MintSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SMART FINANCIAL INSIGHTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (highestCategory != null) {
                                "${highestCategory.key} is your highest expense category at Rs. ${BankRepository.formatCurrency(highestCategory.value)}."
                            } else {
                                "Your spending increased by 12% compared to last month."
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Net savings rate is healthy with Rs. ${BankRepository.formatCurrency(totalIncome - totalExpense)} saved.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Income vs Expense Comparison Bar Chart
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Income vs Expenses",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val maxAmount = maxOf(totalIncome, totalExpense, 1.0)
                    val incomeRatio = (totalIncome / maxAmount).toFloat()
                    val expenseRatio = (totalExpense / maxAmount).toFloat()

                    // Canvas Bar Visualization
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val barWidth = 60.dp.toPx()
                        val canvasHeight = size.height - 30.dp.toPx()
                        val canvasWidth = size.width

                        // Income Bar (Left)
                        val incomeHeight = canvasHeight * incomeRatio
                        drawRoundRect(
                            color = MintDark,
                            topLeft = Offset(canvasWidth * 0.25f - barWidth / 2, canvasHeight - incomeHeight),
                            size = Size(barWidth, incomeHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                        )

                        // Expense Bar (Right)
                        val expenseHeight = canvasHeight * expenseRatio
                        drawRoundRect(
                            color = Color(0xFFEF4444),
                            topLeft = Offset(canvasWidth * 0.75f - barWidth / 2, canvasHeight - expenseHeight),
                            size = Size(barWidth, expenseHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                        )

                        // Baseline
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(20f, canvasHeight),
                            end = Offset(canvasWidth - 20f, canvasHeight),
                            strokeWidth = 2f
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Income", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Rs. ${BankRepository.formatCurrency(totalIncome)}", fontWeight = FontWeight.Bold, color = MintDark)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Expenses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Rs. ${BankRepository.formatCurrency(totalExpense)}", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }

        // Category Breakdown Donut / Progress List
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Spending by Category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    categoryColors.forEach { (catName, color) ->
                        val catAmt = categoryTotals[catName] ?: 0.0
                        val percentage = if (totalExpense > 0) ((catAmt / totalExpense) * 100).toInt() else 0

                        if (catAmt > 0) {
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = catName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Text(
                                        text = "Rs. ${BankRepository.formatCurrency(catAmt)} ($percentage%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = (percentage / 100f).coerceIn(0f, 1f))
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. STATEMENTS TAB
// -------------------------------------------------------------
@Composable
fun StatementsTab(
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("CURRENT_MONTH") }
    val periods = listOf("Current Month", "Previous Month", "Quarter 3")

    val acc = accounts.firstOrNull()
    val totalCredits = transactions.filter { it.type == "DEPOSIT" }.sumOf { it.amount }
    val totalDebits = transactions.filter { it.type != "DEPOSIT" }.sumOf { it.amount }
    val openingBalance = (acc?.balance ?: 145000.0) - totalCredits + totalDebits
    val closingBalance = acc?.balance ?: 145000.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Select Statement Period", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                periods.forEach { p ->
                    FilterChip(
                        selected = selectedPeriod == p,
                        onClick = { selectedPeriod = p },
                        label = { Text(p) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Professional Statement Sheet Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AK BANK",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = NavyPrimary
                            )
                            Text(
                                text = "Official E-Account Statement",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "OCTOBER 2024",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(text = "Account Title: ${acc?.accountTitle ?: "Muhammad Mawiya"}", fontSize = 12.sp)
                    Text(text = "Account Number: ${acc?.accountNumber ?: "001234567890"}", fontSize = 12.sp)
                    Text(text = "IBAN: ${acc?.iban ?: "PK78AKBK0012345678901234"}", fontSize = 12.sp)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    StatementRow(label = "Opening Balance", value = "Rs. ${BankRepository.formatCurrency(openingBalance)}")
                    StatementRow(label = "Total Credits", value = "+ Rs. ${BankRepository.formatCurrency(totalCredits)}", color = MintDark)
                    StatementRow(label = "Total Debits", value = "- Rs. ${BankRepository.formatCurrency(totalDebits)}", color = Color(0xFFEF4444))
                    StatementRow(label = "Federal Excise / Fees", value = "Rs. 0.00")
                    StatementRow(label = "Closing Balance", value = "Rs. ${BankRepository.formatCurrency(closingBalance)}", isBold = true)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Exporting CSV statement...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download CSV", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Generating PDF statement...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download PDF", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatementRow(label: String, value: String, color: Color = Color.Unspecified, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
    }
}
