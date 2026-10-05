package com.example.ui.screens.admin

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.SupportTicketEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.repository.BankRepository
import com.example.ui.components.AKDisclaimerNotice
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.BankViewModel

@Composable
fun AdminScreen(
    viewModel: BankViewModel,
    users: List<UserEntity>,
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    auditLogs: List<AuditLogEntity>,
    supportTickets: List<SupportTicketEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Customers", "Transactions", "Audit Trail", "Support Desk")

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
            0 -> AdminDashboardTab(
                users = users,
                accounts = accounts,
                transactions = transactions,
                supportTickets = supportTickets
            )
            1 -> AdminCustomersTab(
                users = users,
                accounts = accounts,
                onToggleSuspend = { userId -> viewModel.toggleSuspendCustomer(userId) }
            )
            2 -> AdminTransactionsTab(
                transactions = transactions,
                onReview = { txId -> viewModel.reviewTransaction(txId) }
            )
            3 -> AdminAuditTrailTab(auditLogs = auditLogs)
            4 -> AdminSupportDeskTab(
                tickets = supportTickets,
                onReply = { id, text -> viewModel.replyToTicket(id, text) }
            )
        }
    }
}

// -------------------------------------------------------------
// 1. DASHBOARD OVERVIEW
// -------------------------------------------------------------
@Composable
fun AdminDashboardTab(
    users: List<UserEntity>,
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    supportTickets: List<SupportTicketEntity>
) {
    val totalSystemBalance = accounts.sumOf { it.balance }
    val customersCount = users.filter { it.role == "CUSTOMER" }.size
    val activeCustomers = users.filter { it.role == "CUSTOMER" && !it.isSuspended }.size
    val pendingTickets = supportTickets.filter { it.status != "RESOLVED" && it.status != "CLOSED" }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text(
                text = "System Metrics Overview",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Live telemetry from local Room database",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "Total Customers",
                    value = "$customersCount ($activeCustomers Active)",
                    icon = Icons.Default.People,
                    color = NavyPrimary,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "System Liquidity",
                    value = "Rs. ${BankRepository.formatCurrency(totalSystemBalance)}",
                    icon = Icons.Default.AccountBalance,
                    color = MintDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "Total Transactions",
                    value = "${transactions.size} Completed",
                    icon = Icons.Default.Receipt,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Open Support Tickets",
                    value = "$pendingTickets Tickets",
                    icon = Icons.Default.SupportAgent,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Security & Compliance Status",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MintDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Audit Trail Logging: Active (Zero-Trust Enforced)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Every transfer, card state update, and account status adjustment is appended to immutable audit logs.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// -------------------------------------------------------------
// 2. CUSTOMER MANAGEMENT
// -------------------------------------------------------------
@Composable
fun AdminCustomersTab(
    users: List<UserEntity>,
    accounts: List<AccountEntity>,
    onToggleSuspend: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = users.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.cnic.contains(searchQuery)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Name, Email, or CNIC") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(filtered) { u ->
            val userAccounts = accounts.filter { it.userId == u.id }
            val balance = userAccounts.sumOf { it.balance }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(u.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (u.isSuspended) Color(0xFFEF4444).copy(alpha = 0.2f) else MintSecondary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (u.isSuspended) "SUSPENDED" else u.role,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (u.isSuspended) Color(0xFFDC2626) else MintDark,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("${u.email} • CNIC: ${u.cnic}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Total Balance: Rs. ${BankRepository.formatCurrency(balance)} (${userAccounts.size} Accounts)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                        }

                        if (u.role == "CUSTOMER") {
                            Button(
                                onClick = { onToggleSuspend(u.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (u.isSuspended) MintSecondary else Color(0xFFEF4444)
                                )
                            ) {
                                Text(
                                    text = if (u.isSuspended) "Activate" else "Suspend",
                                    fontSize = 11.sp,
                                    color = if (u.isSuspended) NavyPrimary else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. TRANSACTION MANAGEMENT
// -------------------------------------------------------------
@Composable
fun AdminTransactionsTab(
    transactions: List<TransactionEntity>,
    onReview: (Long) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val filtered = transactions.filter {
        it.referenceNumber.contains(query, ignoreCase = true) ||
                it.receiverName.contains(query, ignoreCase = true) ||
                it.senderName.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search by Ref, Sender, Receiver") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(filtered) { tx ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ref: ${tx.referenceNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${tx.senderName} ➔ ${tx.receiverName}", fontSize = 12.sp)
                        Text("Rs. ${BankRepository.formatCurrency(tx.amount)} • ${tx.type}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                        Text(BankRepository.formatDate(tx.timestamp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            onReview(tx.id)
                            Toast.makeText(context, "Transaction ${tx.referenceNumber} marked reviewed", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Review", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. AUDIT TRAIL TAB
// -------------------------------------------------------------
@Composable
fun AdminAuditTrailTab(auditLogs: List<AuditLogEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text("System Audit Logs (${auditLogs.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(auditLogs) { log ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NavyPrimary
                            ) {
                                Text(
                                    text = log.action,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(log.userEmail.ifEmpty { "system@akbank.demo" }, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("Resource: ${log.resource} • IP: ${log.ipAddress}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(BankRepository.formatDate(log.timestamp), fontSize = 9.sp, color = Color.Gray)
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MintSecondary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = log.result,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. SUPPORT DESK (AGENT VIEW)
// -------------------------------------------------------------
@Composable
fun AdminSupportDeskTab(
    tickets: List<SupportTicketEntity>,
    onReply: (Long, String) -> Unit
) {
    val context = LocalContext.current
    var replyDialogTicket by remember { mutableStateOf<SupportTicketEntity?>(null) }
    var replyText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text("Help Desk & Customer Inquiries (${tickets.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(tickets) { t ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(t.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (t.status == "RESOLVED") MintSecondary.copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = t.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (t.status == "RESOLVED") MintDark else Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(t.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Customer: ${t.customerName} (${t.customerEmail})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { replyDialogTicket = t },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Reply as Support", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (replyDialogTicket != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { replyDialogTicket = null },
            title = { Text("Reply to Ticket #${replyDialogTicket?.id}") },
            text = {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Agent Response") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ticket = replyDialogTicket
                        if (ticket != null && replyText.isNotBlank()) {
                            onReply(ticket.id, replyText)
                            Toast.makeText(context, "Reply dispatched to customer", Toast.LENGTH_SHORT).show()
                            replyDialogTicket = null
                            replyText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Send Reply", color = Color.White)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { replyDialogTicket = null }) { Text("Cancel") }
            }
        )
    }
}
