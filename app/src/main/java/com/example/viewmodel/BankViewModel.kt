package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.model.AccountEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.BeneficiaryEntity
import com.example.data.model.BillEntity
import com.example.data.model.BillerEntity
import com.example.data.model.CardEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.SupportMessageEntity
import com.example.data.model.SupportTicketEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.repository.BankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "USER" or "ASSISTANT"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ReceiptData(
    val transactionId: String,
    val reference: String,
    val recipientName: String,
    val bankName: String,
    val destinationAccount: String,
    val amount: Double,
    val fee: Double,
    val timestamp: Long,
    val purpose: String,
    val status: String = "SUCCESSFUL"
)

class BankViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = BankRepository(db.bankDao())

    // --- Current Auth State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isBalanceHidden = MutableStateFlow(false)
    val isBalanceHidden: StateFlow<Boolean> = _isBalanceHidden.asStateFlow()

    // Screen Navigation
    // Values: "HOME", "TRANSFERS", "CARDS", "ANALYTICS", "ASSISTANT", "PROFILE", "ADMIN", "SUPPORT_PORTAL", "SEARCH", "NOTIFICATIONS", "SECURITY"
    private val _currentTab = MutableStateFlow("HOME")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Toast / Message Banner
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Receipt Modal
    private val _activeReceipt = MutableStateFlow<ReceiptData?>(null)
    val activeReceipt: StateFlow<ReceiptData?> = _activeReceipt.asStateFlow()

    // AI Assistant Chat History
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("ASSISTANT", "Salam & welcome to AK Assistant! I can help you analyze your spending, check income, or find transactions. Try asking: 'How much did I spend this month?'")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Selected account id for multi-account selector
    private val _selectedAccountId = MutableStateFlow<Long?>(null)
    val selectedAccountId: StateFlow<Long?> = _selectedAccountId.asStateFlow()

    // Database reactive streams
    val accounts: StateFlow<List<AccountEntity>> = _currentUser.combine(db.bankDao().getAllAccounts()) { user, allAccs ->
        if (user == null) emptyList()
        else if (user.role == "ADMIN") allAccs
        else allAccs.filter { it.userId == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = db.bankDao().getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val beneficiaries: StateFlow<List<BeneficiaryEntity>> = db.bankDao().getAllBeneficiaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cards: StateFlow<List<CardEntity>> = _currentUser.combine(db.bankDao().getAllCards()) { user, allCards ->
        if (user == null) emptyList()
        else allCards.filter { it.userId == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val billers: StateFlow<List<BillerEntity>> = db.bankDao().getAllBillers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<BillEntity>> = db.bankDao().getAllBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = db.bankDao().getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = db.bankDao().getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supportTickets: StateFlow<List<SupportTicketEntity>> = db.bankDao().getAllTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = db.bankDao().getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            DatabaseInitializer.seedDatabaseIfEmpty(db.bankDao())
            // Default login to demo customer for immediate high-fidelity testing
            val defaultCustomer = db.bankDao().getUserByEmail("demo@akbank.demo")
            if (defaultCustomer != null) {
                _currentUser.value = defaultCustomer
                _isLoggedIn.value = true
                val accs = db.bankDao().getAccountsByUserIdDirect(defaultCustomer.id)
                if (accs.isNotEmpty()) {
                    _selectedAccountId.value = accs.first().id
                }
            }
        }
    }

    // --- Authentication Actions ---
    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(email, pass)
            result.onSuccess { user ->
                _currentUser.value = user
                _isLoggedIn.value = true
                val accs = db.bankDao().getAccountsByUserIdDirect(user.id)
                if (accs.isNotEmpty()) {
                    _selectedAccountId.value = accs.first().id
                }
                _statusMessage.value = "Welcome back, ${user.fullName}!"
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Authentication failed")
            }
        }
    }

    fun quickSwitchRole(email: String) {
        viewModelScope.launch {
            val user = db.bankDao().getUserByEmail(email)
            if (user != null) {
                _currentUser.value = user
                _isLoggedIn.value = true
                val accs = db.bankDao().getAccountsByUserIdDirect(user.id)
                if (accs.isNotEmpty()) {
                    _selectedAccountId.value = accs.first().id
                }
                _currentTab.value = if (user.role == "ADMIN") "ADMIN" else if (user.role == "SUPPORT") "SUPPORT_PORTAL" else "HOME"
                _statusMessage.value = "Switched to ${user.role} mode (${user.fullName})"
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        phone: String,
        cnic: String,
        address: String,
        city: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.register(fullName, email, phone, cnic, address, city, pass)
            result.onSuccess { user ->
                _currentUser.value = user
                _isLoggedIn.value = true
                val accs = db.bankDao().getAccountsByUserIdDirect(user.id)
                if (accs.isNotEmpty()) {
                    _selectedAccountId.value = accs.first().id
                }
                _statusMessage.value = "Account created successfully! Welcome to AK Bank."
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        _currentTab.value = "HOME"
        _statusMessage.value = "You have been logged out safely."
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleHideBalance() {
        _isBalanceHidden.value = !_isBalanceHidden.value
    }

    fun selectAccount(accountId: Long) {
        _selectedAccountId.value = accountId
    }

    fun navigateTo(tab: String) {
        _currentTab.value = tab
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun closeReceipt() {
        _activeReceipt.value = null
    }

    // --- Transfer Action ---
    fun transferMoney(
        recipientName: String,
        bankName: String,
        destinationAccount: String,
        amount: Double,
        purpose: String,
        pin: String,
        transferType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: return onError("Please sign in first")
        val accountId = _selectedAccountId.value ?: return onError("No source account selected")

        viewModelScope.launch {
            val result = repository.executeTransfer(
                senderUser = user,
                senderAccountId = accountId,
                recipientName = recipientName,
                bankName = bankName,
                destinationAccountOrIban = destinationAccount,
                amount = amount,
                purpose = purpose,
                pin = pin,
                transferType = transferType
            )
            result.onSuccess { tx ->
                _activeReceipt.value = ReceiptData(
                    transactionId = tx.id.toString(),
                    reference = tx.referenceNumber,
                    recipientName = recipientName,
                    bankName = bankName,
                    destinationAccount = destinationAccount,
                    amount = amount,
                    fee = 0.0,
                    timestamp = tx.timestamp,
                    purpose = purpose
                )
                _statusMessage.value = "Transfer completed successfully!"
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Transfer failed")
            }
        }
    }

    // --- Pay Bill Action ---
    fun payBill(
        billerName: String,
        consumerNumber: String,
        amount: Double,
        pin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: return onError("Please sign in")
        val accountId = _selectedAccountId.value ?: return onError("No source account")

        viewModelScope.launch {
            val result = repository.payBill(
                senderUser = user,
                senderAccountId = accountId,
                billId = 0L,
                billerName = billerName,
                consumerNumber = consumerNumber,
                amount = amount,
                pin = pin
            )
            result.onSuccess { tx ->
                _activeReceipt.value = ReceiptData(
                    transactionId = tx.id.toString(),
                    reference = tx.referenceNumber,
                    recipientName = billerName,
                    bankName = "Utility Payment",
                    destinationAccount = consumerNumber,
                    amount = amount,
                    fee = 0.0,
                    timestamp = tx.timestamp,
                    purpose = "Utility Bill Payment"
                )
                _statusMessage.value = "Bill paid successfully!"
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Payment failed")
            }
        }
    }

    // --- Mobile Topup ---
    fun mobileTopup(
        network: String,
        phone: String,
        amount: Double,
        pin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: return onError("Please sign in")
        val accountId = _selectedAccountId.value ?: return onError("No source account")

        viewModelScope.launch {
            val result = repository.executeMobileTopup(
                senderUser = user,
                senderAccountId = accountId,
                networkName = network,
                phoneNumber = phone,
                amount = amount,
                pin = pin
            )
            result.onSuccess { tx ->
                _activeReceipt.value = ReceiptData(
                    transactionId = tx.id.toString(),
                    reference = tx.referenceNumber,
                    recipientName = "$network Recharge",
                    bankName = "Mobile Operator",
                    destinationAccount = phone,
                    amount = amount,
                    fee = 0.0,
                    timestamp = tx.timestamp,
                    purpose = "Mobile Airtime Top-Up"
                )
                _statusMessage.value = "Mobile top-up completed!"
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Top-up failed")
            }
        }
    }

    // --- QR Payment ---
    fun qrPayment(
        merchant: String,
        amount: Double,
        pin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: return onError("Please sign in")
        val accountId = _selectedAccountId.value ?: return onError("No source account")

        viewModelScope.launch {
            val result = repository.executeQRPayment(
                senderUser = user,
                senderAccountId = accountId,
                merchantName = merchant,
                amount = amount,
                pin = pin
            )
            result.onSuccess { tx ->
                _activeReceipt.value = ReceiptData(
                    transactionId = tx.id.toString(),
                    reference = tx.referenceNumber,
                    recipientName = merchant,
                    bankName = "AK QR Merchant",
                    destinationAccount = "QR-$merchant",
                    amount = amount,
                    fee = 0.0,
                    timestamp = tx.timestamp,
                    purpose = "Scan & Pay Merchant Purchase"
                )
                _statusMessage.value = "QR payment completed!"
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "QR payment failed")
            }
        }
    }

    // --- Card Settings ---
    fun toggleCardFreeze(card: CardEntity) {
        viewModelScope.launch {
            repository.updateCard(card.copy(isFrozen = !card.isFrozen))
            _statusMessage.value = if (!card.isFrozen) "Card frozen successfully" else "Card unfrozen"
        }
    }

    fun toggleCardOnline(card: CardEntity) {
        viewModelScope.launch {
            repository.updateCard(card.copy(isOnlinePaymentEnabled = !card.isOnlinePaymentEnabled))
            _statusMessage.value = "Online payments updated"
        }
    }

    fun toggleCardIntl(card: CardEntity) {
        viewModelScope.launch {
            repository.updateCard(card.copy(isIntlPaymentEnabled = !card.isIntlPaymentEnabled))
            _statusMessage.value = "International payments updated"
        }
    }

    fun updateCardLimit(card: CardEntity, limit: Double) {
        viewModelScope.launch {
            repository.updateCard(card.copy(spendingLimit = limit))
            _statusMessage.value = "Spending limit updated to Rs. ${BankRepository.formatCurrency(limit)}"
        }
    }

    // --- Beneficiaries ---
    fun addBeneficiary(name: String, nickname: String, bank: String, accNumber: String, iban: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.addBeneficiary(
                BeneficiaryEntity(
                    userId = user.id,
                    name = name,
                    nickname = nickname,
                    bankName = bank,
                    accountNumber = accNumber,
                    iban = iban
                )
            )
            _statusMessage.value = "Beneficiary $name added"
        }
    }

    fun deleteBeneficiary(b: BeneficiaryEntity) {
        viewModelScope.launch {
            repository.deleteBeneficiary(b)
            _statusMessage.value = "Beneficiary removed"
        }
    }

    // --- AI Assistant ---
    fun sendAssistantMessage(query: String) {
        val user = _currentUser.value ?: return
        val current = _chatMessages.value.toMutableList()
        current.add(ChatMessage("USER", query))
        _chatMessages.value = current

        viewModelScope.launch {
            val response = repository.queryAssistant(query, user)
            val updated = _chatMessages.value.toMutableList()
            updated.add(ChatMessage("ASSISTANT", response))
            _chatMessages.value = updated
        }
    }

    // --- Support Tickets ---
    fun createSupportTicket(category: String, subject: String, message: String, onSuccess: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.createTicket(
                SupportTicketEntity(
                    userId = user.id,
                    customerName = user.fullName,
                    customerEmail = user.email,
                    category = category,
                    subject = subject,
                    message = message
                ),
                initialMessage = message
            )
            _statusMessage.value = "Support ticket created successfully"
            onSuccess()
        }
    }

    fun replyToTicket(ticketId: Long, replyText: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.sendTicketReply(ticketId, user.role, user.fullName, replyText)
            _statusMessage.value = "Reply sent"
        }
    }

    // --- Admin Operations ---
    fun toggleSuspendCustomer(userId: Long) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val suspended = repository.toggleUserSuspension(userId, admin.email)
            _statusMessage.value = if (suspended) "Customer account suspended" else "Customer account activated"
        }
    }

    fun reviewTransaction(txId: Long) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.markTransactionReviewed(txId, admin.email)
            _statusMessage.value = "Transaction marked as reviewed"
        }
    }

    // --- Notifications ---
    fun markAllNotificationsRead() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(user.id)
            _statusMessage.value = "All notifications marked as read"
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }
}
