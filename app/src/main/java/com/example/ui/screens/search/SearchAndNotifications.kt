package com.example.ui.screens.search

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BeneficiaryEntity
import com.example.data.model.BillerEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.BankRepository
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.BankViewModel

@Composable
fun GlobalSearchDialog(
    isOpen: Boolean,
    transactions: List<TransactionEntity>,
    beneficiaries: List<BeneficiaryEntity>,
    billers: List<BillerEntity>,
    onDismiss: () -> Unit,
    onSelectTransaction: (TransactionEntity) -> Unit
) {
    if (!isOpen) return

    var query by remember { mutableStateOf("") }

    val matchedTransactions = if (query.isBlank()) emptyList() else transactions.filter {
        it.receiverName.contains(query, ignoreCase = true) ||
                it.referenceNumber.contains(query, ignoreCase = true) ||
                it.bankName.contains(query, ignoreCase = true)
    }

    val matchedBeneficiaries = if (query.isBlank()) emptyList() else beneficiaries.filter {
        it.name.contains(query, ignoreCase = true) ||
                it.nickname.contains(query, ignoreCase = true)
    }

    val matchedBillers = if (query.isBlank()) emptyList() else billers.filter {
        it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Global Search", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search transactions, payees, bills...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.height(280.dp)) {
                    if (query.isBlank()) {
                        item {
                            Text(
                                text = "Popular searches: K-Electric, Meezan, Salary, Foodpanda, Groceries",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (matchedTransactions.isNotEmpty()) {
                        item {
                            Text("Transactions (${matchedTransactions.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                        }
                        items(matchedTransactions) { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectTransaction(tx)
                                        onDismiss()
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tx.receiverName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Rs. ${BankRepository.formatCurrency(tx.amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (matchedBeneficiaries.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Beneficiaries (${matchedBeneficiaries.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                        }
                        items(matchedBeneficiaries) { b ->
                            Text("${b.name} (${b.bankName})", fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }

                    if (matchedBillers.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Billers (${matchedBillers.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                        }
                        items(matchedBillers) { biller ->
                            Text("${biller.name} (${biller.category})", fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun NotificationsDialog(
    isOpen: Boolean,
    viewModel: BankViewModel,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val sampleNotifications = listOf(
        Pair("Salary Credited", "Rs. 220,000.00 credited to account from Tech Solutions Ltd."),
        Pair("Security Login", "New login detected from Karachi on Android device."),
        Pair("Utility Bill Due", "K-Electric bill of Rs. 14,820.00 is due in 3 days."),
        Pair("Beneficiary Added", "Ali Raza (Meezan Bank) added to favorite recipients.")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                sampleNotifications.forEach { (title, desc) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.markAllNotificationsRead()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Mark All Read", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun RoleSwitcherDialog(
    isOpen: Boolean,
    currentRole: String,
    onDismiss: () -> Unit,
    onSelectRole: (String) -> Unit
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Switch Demo Role", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column {
                Text(
                    text = "AK Bank provides 3 fully implemented distinct user roles with dedicated interfaces and permissions:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                RoleOptionCard(
                    title = "Customer (Muhammad Mawiya)",
                    role = "CUSTOMER",
                    email = "demo@akbank.demo",
                    description = "Access accounts, transfers, cards, bill payments, and AI assistant.",
                    icon = Icons.Default.Person,
                    isSelected = currentRole == "CUSTOMER",
                    onClick = {
                        onSelectRole("demo@akbank.demo")
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                RoleOptionCard(
                    title = "System Administrator",
                    role = "ADMIN",
                    email = "admin@akbank.demo",
                    description = "Full admin dashboard, customer suspension, audit logs, liquidity metrics.",
                    icon = Icons.Default.AdminPanelSettings,
                    isSelected = currentRole == "ADMIN",
                    onClick = {
                        onSelectRole("admin@akbank.demo")
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                RoleOptionCard(
                    title = "Support Desk Agent",
                    role = "SUPPORT",
                    email = "support@akbank.demo",
                    description = "Customer lookup, ticket response, transaction lookup, communication notes.",
                    icon = Icons.Default.SupportAgent,
                    isSelected = currentRole == "SUPPORT",
                    onClick = {
                        onSelectRole("support@akbank.demo")
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RoleOptionCard(
    title: String,
    role: String,
    email: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MintSecondary else NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) NavyDark else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
