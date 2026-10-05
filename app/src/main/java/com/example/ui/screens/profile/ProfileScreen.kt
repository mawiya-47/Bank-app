package com.example.ui.screens.profile

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.data.model.SupportTicketEntity
import com.example.data.model.UserEntity
import com.example.data.repository.BankRepository
import com.example.ui.components.AKDisclaimerNotice
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.BankViewModel

@Composable
fun ProfileScreen(
    viewModel: BankViewModel,
    currentUser: UserEntity?,
    supportTickets: List<SupportTicketEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Profile", "Security Center", "Help & Support")

    var editProfileOpen by remember { mutableStateOf(false) }
    var changePasswordOpen by remember { mutableStateOf(false) }
    var changePinOpen by remember { mutableStateOf(false) }
    var newTicketOpen by remember { mutableStateOf(false) }

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
            0 -> ProfileDetailsTab(
                user = currentUser,
                onEditProfile = { editProfileOpen = true },
                onLogout = { viewModel.logout() }
            )
            1 -> SecurityCenterTab(
                user = currentUser,
                onChangePassword = { changePasswordOpen = true },
                onChangePin = { changePinOpen = true }
            )
            2 -> SupportTicketsTab(
                user = currentUser,
                tickets = supportTickets.filter { it.userId == (currentUser?.id ?: 0L) },
                onNewTicket = { newTicketOpen = true },
                onReply = { ticketId, text -> viewModel.replyToTicket(ticketId, text) }
            )
        }
    }

    // Edit Profile Dialog
    if (editProfileOpen && currentUser != null) {
        var name by remember { mutableStateOf(currentUser.fullName) }
        var phone by remember { mutableStateOf(currentUser.phone) }
        var address by remember { mutableStateOf(currentUser.address) }
        var city by remember { mutableStateOf(currentUser.city) }

        AlertDialog(
            onDismissRequest = { editProfileOpen = false },
            title = { Text("Edit Customer Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Residential Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = currentUser.copy(
                            fullName = name,
                            phone = phone,
                            address = address,
                            city = city
                        )
                        viewModel.updateCardLimit(
                            com.example.data.model.CardEntity(
                                userId = currentUser.id,
                                accountId = 1,
                                cardNumber = "",
                                maskedNumber = "",
                                cardHolderName = name
                            ),
                            100000.0
                        )
                        editProfileOpen = false
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editProfileOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Change Password Dialog
    if (changePasswordOpen && currentUser != null) {
        var currentPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { changePasswordOpen = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPass == currentUser.passwordHash && newPass.length >= 6) {
                            changePasswordOpen = false
                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Incorrect current password or new password too short", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Update Password", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { changePasswordOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Change PIN Dialog
    if (changePinOpen && currentUser != null) {
        var newPin by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { changePinOpen = false },
            title = { Text("Change Transaction PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Set a new 4-digit PIN for transaction authorizations:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPin = it },
                        label = { Text("New 4-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            changePinOpen = false
                            Toast.makeText(context, "Transaction PIN updated to $newPin", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Update PIN", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { changePinOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Create Support Ticket Dialog
    if (newTicketOpen) {
        var category by remember { mutableStateOf("TRANSFER") }
        var subject by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { newTicketOpen = false },
            title = { Text("Create Support Ticket", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        placeholder = { Text("e.g. Issue with transfer") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Detailed Message") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subject.isNotBlank() && message.isNotBlank()) {
                            viewModel.createSupportTicket(category, subject, message) {
                                newTicketOpen = false
                                Toast.makeText(context, "Support ticket submitted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Submit Ticket", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { newTicketOpen = false }) { Text("Cancel") }
            }
        )
    }
}

// -------------------------------------------------------------
// 1. PROFILE DETAILS TAB
// -------------------------------------------------------------
@Composable
fun ProfileDetailsTab(
    user: UserEntity?,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.fullName ?: "Customer").take(2).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = user?.fullName ?: "Muhammad Mawiya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = user?.email ?: "demo@akbank.demo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onEditProfile,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Profile Details", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Personal Information", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    ProfileInfoRow(label = "CNIC (Demo KYC)", value = user?.cnic ?: "42101-5829143-7")
                    ProfileInfoRow(label = "Phone", value = user?.phone ?: "+92 300 1234567")
                    ProfileInfoRow(label = "Address", value = user?.address ?: "PECHS Block 6")
                    ProfileInfoRow(label = "City", value = user?.city ?: "Karachi, Pakistan")
                    ProfileInfoRow(label = "Role", value = user?.role ?: "CUSTOMER")
                    ProfileInfoRow(label = "Customer ID", value = "AKB-CUST-${user?.id ?: 1}")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out of Session", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// -------------------------------------------------------------
// 2. SECURITY CENTER TAB
// -------------------------------------------------------------
@Composable
fun SecurityCenterTab(
    user: UserEntity?,
    onChangePassword: () -> Unit,
    onChangePin: () -> Unit
) {
    var biometricEnabled by remember { mutableStateOf(user?.isBiometricEnabled ?: true) }
    var twoFactorEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Authentication & Passwords", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    SecurityActionItem(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        subtitle = "Last changed 30 days ago",
                        onClick = onChangePassword
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SecurityActionItem(
                        icon = Icons.Default.Security,
                        title = "Transaction PIN",
                        subtitle = "4-digit authorization PIN for transfers",
                        onClick = onChangePin
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Biometrics & Two-Factor", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Biometric Login", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Use simulated Fingerprint / Face ID", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Two-Factor (2FA) SMS/Email", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Require OTP for sensitive transfers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = twoFactorEnabled,
                            onCheckedChange = { twoFactorEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Active Login Sessions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Android Studio Virtual Device (This Device)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Karachi, Pakistan • IP: 192.168.1.101 • Active Now", fontSize = 11.sp, color = MintDark)
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = MintSecondary.copy(alpha = 0.2f)) {
                            Text("CURRENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MintDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = NavyPrimary)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

// -------------------------------------------------------------
// 3. SUPPORT TICKETS TAB
// -------------------------------------------------------------
@Composable
fun SupportTicketsTab(
    user: UserEntity?,
    tickets: List<SupportTicketEntity>,
    onNewTicket: () -> Unit,
    onReply: (Long, String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    var selectedTicketId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Customer Support Tickets", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(
                    onClick = onNewTicket,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("+ New Ticket", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (tickets.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                    Text("No support tickets created yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        } else {
            items(tickets) { t ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = t.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        Text(text = t.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Category: ${t.category} • Ref #${t.id}", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
