package com.example.ui.screens.transfers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.BeneficiaryEntity
import com.example.data.model.BillEntity
import com.example.data.model.BillerEntity
import com.example.data.model.UserEntity
import com.example.data.repository.BankRepository
import com.example.ui.components.AKDisclaimerNotice
import com.example.ui.components.AKPinVerificationDialog
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.BankViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    viewModel: BankViewModel,
    currentUser: UserEntity?,
    accounts: List<AccountEntity>,
    beneficiaries: List<BeneficiaryEntity>,
    billers: List<BillerEntity>,
    bills: List<BillEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Send Money", "Bill Pay", "Top-Up", "QR Pay", "Receive", "Beneficiaries")

    var pinDialogOpen by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

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
            0 -> SendMoneyTab(
                viewModel = viewModel,
                accounts = accounts,
                beneficiaries = beneficiaries,
                onRequestPin = { action ->
                    pendingAction = action
                    pinDialogOpen = true
                }
            )
            1 -> BillPaymentTab(
                viewModel = viewModel,
                billers = billers,
                bills = bills,
                onRequestPin = { action ->
                    pendingAction = action
                    pinDialogOpen = true
                }
            )
            2 -> MobileTopupTab(
                viewModel = viewModel,
                onRequestPin = { action ->
                    pendingAction = action
                    pinDialogOpen = true
                }
            )
            3 -> QRPaymentTab(
                viewModel = viewModel,
                onRequestPin = { action ->
                    pendingAction = action
                    pinDialogOpen = true
                }
            )
            4 -> ReceiveMoneyTab(accounts = accounts, currentUser = currentUser)
            5 -> BeneficiariesTab(viewModel = viewModel, beneficiaries = beneficiaries)
        }
    }

    if (pinDialogOpen) {
        AKPinVerificationDialog(
            isOpen = pinDialogOpen,
            onDismiss = {
                pinDialogOpen = false
                pendingAction = null
            },
            onConfirm = { enteredPin ->
                pinDialogOpen = false
                pendingAction?.invoke()
            }
        )
    }
}

// -------------------------------------------------------------
// 1. SEND MONEY TAB
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyTab(
    viewModel: BankViewModel,
    accounts: List<AccountEntity>,
    beneficiaries: List<BeneficiaryEntity>,
    onRequestPin: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    var transferType by remember { mutableStateOf("AK_TRANSFER") } // AK_TRANSFER, OTHER_BANK, OWN_ACCOUNT
    var recipientName by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf("AK Bank") }
    var destinationAccount by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("Family Support") }
    var showConfirmation by remember { mutableStateOf(false) }

    val pakistaniBanks = listOf(
        "AK Bank",
        "Meezan Bank Limited",
        "Habib Bank Limited (HBL)",
        "United Bank Limited (UBL)",
        "Bank Alfalah Limited",
        "MCB Bank Limited",
        "Standard Chartered Bank",
        "Faysal Bank",
        "Askari Bank"
    )

    var bankDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Transfer Type",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = transferType == "AK_TRANSFER",
                    onClick = {
                        transferType = "AK_TRANSFER"
                        selectedBank = "AK Bank"
                    },
                    label = { Text("To AK Bank") }
                )
                FilterChip(
                    selected = transferType == "OTHER_BANK",
                    onClick = {
                        transferType = "OTHER_BANK"
                        selectedBank = "Meezan Bank Limited"
                    },
                    label = { Text("Other Bank") }
                )
                FilterChip(
                    selected = transferType == "OWN_ACCOUNT",
                    onClick = {
                        transferType = "OWN_ACCOUNT"
                        selectedBank = "AK Bank"
                        if (accounts.size > 1) {
                            destinationAccount = accounts[1].accountNumber
                            recipientName = "Self (Savings)"
                        }
                    },
                    label = { Text("Own Account") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Beneficiary Quick Pick
            if (beneficiaries.isNotEmpty() && transferType != "OWN_ACCOUNT") {
                Text(
                    text = "Or Choose Saved Beneficiary",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(beneficiaries) { b ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                recipientName = b.name
                                selectedBank = b.bankName
                                destinationAccount = b.accountNumber
                            }
                        ) {
                            Text(
                                text = "${b.name} (${b.bankName.take(6)})",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bank Selector for Other Bank
            if (transferType == "OTHER_BANK") {
                ExposedDropdownMenuBox(
                    expanded = bankDropdownExpanded,
                    onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedBank,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Destination Bank") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false }
                    ) {
                        pakistaniBanks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank) },
                                onClick = {
                                    selectedBank = bank
                                    bankDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Recipient Account / IBAN
            OutlinedTextField(
                value = destinationAccount,
                onValueChange = { destinationAccount = it },
                label = { Text("Account Number or IBAN") },
                placeholder = { Text("e.g. 001234567890 or PK78AKBK...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transfer_account_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recipient Title
            OutlinedTextField(
                value = recipientName,
                onValueChange = { recipientName = it },
                label = { Text("Recipient Account Title / Name") },
                placeholder = { Text("e.g. Muhammad Ali") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transfer_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                label = { Text("Amount (PKR)") },
                prefix = { Text("Rs. ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transfer_amount_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Purpose
            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("Purpose of Transfer") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val amtDouble = amount.toDoubleOrNull() ?: 0.0
                    if (amtDouble <= 0) {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    } else if (destinationAccount.isBlank() || recipientName.isBlank()) {
                        Toast.makeText(context, "Please fill in recipient details", Toast.LENGTH_SHORT).show()
                    } else {
                        showConfirmation = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("transfer_proceed_button")
            ) {
                Text("Review Transfer Details", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    // Confirmation Bottom Sheet / Dialog
    if (showConfirmation) {
        val amt = amount.toDoubleOrNull() ?: 0.0
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirm Transfer", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Please review transfer summary before authorizing:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Recipient: $recipientName", fontWeight = FontWeight.Bold)
                            Text("Bank: $selectedBank", fontSize = 12.sp)
                            Text("Account/IBAN: $destinationAccount", fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Transfer Amount: Rs. ${BankRepository.formatCurrency(amt)}", fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Text("Interbank Fee: Rs. 0.00 (Demo Free)", fontSize = 11.sp, color = MintDark)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = false
                        onRequestPin {
                            viewModel.transferMoney(
                                recipientName = recipientName,
                                bankName = selectedBank,
                                destinationAccount = destinationAccount,
                                amount = amt,
                                purpose = purpose,
                                pin = "1234",
                                transferType = transferType,
                                onSuccess = {
                                    recipientName = ""
                                    destinationAccount = ""
                                    amount = ""
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Authorize with PIN", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 2. BILL PAYMENT TAB
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillPaymentTab(
    viewModel: BankViewModel,
    billers: List<BillerEntity>,
    bills: List<BillEntity>,
    onRequestPin: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("ELECTRICITY") }
    var selectedBiller by remember { mutableStateOf<BillerEntity?>(null) }
    var consumerNumber by remember { mutableStateOf("0400012345678") }
    var fetchedBill by remember { mutableStateOf<BillEntity?>(null) }
    var customAmount by remember { mutableStateOf("") }

    val categories = listOf("ELECTRICITY", "GAS", "WATER", "INTERNET", "MOBILE", "EDUCATION")
    val filteredBillers = billers.filter { it.category == selectedCategory }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Select Utility Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            selectedBiller = null
                            fetchedBill = null
                        },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Select Provider / Biller",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredBillers) { b ->
                    val isSel = selectedBiller?.id == b.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            selectedBiller = b
                            fetchedBill = null
                        }
                    ) {
                        Text(
                            text = b.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = consumerNumber,
                onValueChange = { consumerNumber = it },
                label = { Text("Consumer / Reference Number") },
                placeholder = { Text("e.g. 0400012345678") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("consumer_number_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val matching = bills.find { it.consumerNumber == consumerNumber }
                    if (matching != null) {
                        fetchedBill = matching
                        customAmount = matching.amount.toString()
                        Toast.makeText(context, "Bill fetched successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Generate mock bill on the fly
                        val bill = BillEntity(
                            billerId = selectedBiller?.id ?: 1L,
                            consumerNumber = consumerNumber,
                            title = "${selectedBiller?.name ?: "Utility"} Bill",
                            amount = 8750.0,
                            dueDate = "2024-10-28"
                        )
                        fetchedBill = bill
                        customAmount = "8750.0"
                        Toast.makeText(context, "Bill found: Rs. 8,750.00", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MintSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fetch_bill_button")
            ) {
                Text("Fetch Bill Details", color = NavyPrimary, fontWeight = FontWeight.Bold)
            }

            if (fetchedBill != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = fetchedBill?.title ?: "Utility Bill",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Due Date: ${fetchedBill?.dueDate} • Status: UNPAID",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Payable Amount: Rs. ${BankRepository.formatCurrency(customAmount.toDoubleOrNull() ?: 0.0)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val amt = customAmount.toDoubleOrNull() ?: 0.0
                                onRequestPin {
                                    viewModel.payBill(
                                        billerName = selectedBiller?.name ?: "Utility Provider",
                                        consumerNumber = consumerNumber,
                                        amount = amt,
                                        pin = "1234",
                                        onSuccess = {
                                            fetchedBill = null
                                            Toast.makeText(context, "Bill Paid Successfully!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pay Bill Now", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. MOBILE TOP-UP TAB
// -------------------------------------------------------------
@Composable
fun MobileTopupTab(
    viewModel: BankViewModel,
    onRequestPin: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    var selectedNetwork by remember { mutableStateOf("Jazz") }
    var phoneNumber by remember { mutableStateOf("03001234567") }
    var selectedAmount by remember { mutableStateOf("500") }

    val networks = listOf("Jazz", "Telenor", "Zong", "Ufone")
    val quickAmounts = listOf("100", "250", "500", "1000", "2000")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text("Select Telecom Network", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                networks.forEach { net ->
                    FilterChip(
                        selected = selectedNetwork == net,
                        onClick = { selectedNetwork = net },
                        label = { Text(net) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Mobile Number") },
                placeholder = { Text("0300XXXXXXX") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Quick Recharge Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickAmounts.forEach { amt ->
                    FilterChip(
                        selected = selectedAmount == amt,
                        onClick = { selectedAmount = amt },
                        label = { Text("Rs. $amt") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val amtDouble = selectedAmount.toDoubleOrNull() ?: 0.0
                    onRequestPin {
                        viewModel.mobileTopup(
                            network = selectedNetwork,
                            phone = phoneNumber,
                            amount = amtDouble,
                            pin = "1234",
                            onSuccess = {
                                Toast.makeText(context, "Top-Up successful!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Recharge Rs. $selectedAmount", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 4. QR PAYMENT TAB
// -------------------------------------------------------------
@Composable
fun QRPaymentTab(
    viewModel: BankViewModel,
    onRequestPin: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    var merchantName by remember { mutableStateOf("Hyperstar Karachi") }
    var amount by remember { mutableStateOf("1250") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        // Simulated Camera Viewfinder
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.9f))
                .border(2.dp, MintSecondary, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scanner Viewfinder",
                    tint = MintSecondary,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Point camera at merchant QR",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = "[ Simulated QR Reader Active ]",
                    color = MintSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = merchantName,
            onValueChange = { merchantName = it },
            label = { Text("Detected Merchant Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Payment Amount (PKR)") },
            prefix = { Text("Rs. ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                onRequestPin {
                    viewModel.qrPayment(
                        merchant = merchantName,
                        amount = amt,
                        pin = "1234",
                        onSuccess = {
                            Toast.makeText(context, "QR Payment Completed", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Pay Merchant with QR", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// 5. RECEIVE MONEY TAB
// -------------------------------------------------------------
@Composable
fun ReceiveMoneyTab(
    accounts: List<AccountEntity>,
    currentUser: UserEntity?
) {
    val context = LocalContext.current
    val acc = accounts.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RECEIVE PAYMENT QR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // QR Graphic representation
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Payment QR",
                        tint = NavyPrimary,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentUser?.fullName ?: "Customer Name",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Account: ${acc?.accountNumber ?: "001234567890"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "IBAN: ${acc?.iban ?: "PK78AKBK0012345678901234"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("IBAN", acc?.iban ?: ""))
                            Toast.makeText(context, "IBAN copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy IBAN", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Sharing payment details...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. BENEFICIARIES TAB
// -------------------------------------------------------------
@Composable
fun BeneficiariesTab(
    viewModel: BankViewModel,
    beneficiaries: List<BeneficiaryEntity>
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    var bName by remember { mutableStateOf("") }
    var bNick by remember { mutableStateOf("") }
    var bBank by remember { mutableStateOf("Meezan Bank") }
    var bAcc by remember { mutableStateOf("") }

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
                Text(
                    text = "Manage Beneficiaries (${beneficiaries.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New", fontSize = 12.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(beneficiaries) { b ->
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
                        Text(text = b.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "${b.bankName} • ${b.accountNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (b.nickname.isNotEmpty()) {
                            Text(text = "Tag: ${b.nickname}", fontSize = 11.sp, color = MintDark)
                        }
                    }
                    IconButton(onClick = { viewModel.deleteBeneficiary(b) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Beneficiary", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = bName,
                        onValueChange = { bName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bNick,
                        onValueChange = { bNick = it },
                        label = { Text("Nickname / Tag") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bBank,
                        onValueChange = { bBank = it },
                        label = { Text("Bank Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bAcc,
                        onValueChange = { bAcc = it },
                        label = { Text("Account Number or IBAN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bName.isNotBlank() && bAcc.isNotBlank()) {
                            viewModel.addBeneficiary(
                                name = bName,
                                nickname = bNick,
                                bank = bBank,
                                accNumber = bAcc,
                                iban = if (bAcc.startsWith("PK")) bAcc else "PK78${bBank.take(4).uppercase()}$bAcc"
                            )
                            showAddDialog = false
                            bName = ""
                            bNick = ""
                            bAcc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
