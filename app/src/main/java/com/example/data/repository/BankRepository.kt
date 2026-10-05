package com.example.data.repository

import com.example.data.local.BankDao
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
import kotlinx.coroutines.flow.Flow
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class BankRepository(private val dao: BankDao) {

    // --- User & Auth ---
    suspend fun login(email: String, password: String):Result<UserEntity> {
        val user = dao.getUserByEmail(email.trim().lowercase())
        return if (user != null && user.passwordHash == password) {
            if (user.isSuspended) {
                Result.failure(Exception("This demo account has been suspended by the administrator."))
            } else {
                dao.insertAuditLog(
                    AuditLogEntity(
                        userId = user.id,
                        userEmail = user.email,
                        action = "LOGIN",
                        resource = "SESSION",
                        result = "SUCCESS"
                    )
                )
                Result.success(user)
            }
        } else {
            Result.failure(Exception("Invalid email or password."))
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        cnic: String,
        address: String,
        city: String,
        password: String
    ): Result<UserEntity> {
        val existing = dao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }

        val user = UserEntity(
            email = email.trim().lowercase(),
            passwordHash = password,
            fullName = fullName.trim(),
            phone = phone.trim(),
            cnic = cnic.trim(),
            address = address.trim(),
            city = city.trim(),
            role = "CUSTOMER",
            transactionPin = "1234"
        )
        val userId = dao.insertUser(user)
        val createdUser = user.copy(id = userId)

        // Automatically create a primary Current Account and a Demo Card for the new customer
        val accNumber = "00" + (1000000000L..9999999999L).random().toString()
        val iban = "PK78AKBK$accNumber"
        val accId = dao.insertAccount(
            AccountEntity(
                userId = userId,
                accountTitle = "$fullName - Current",
                accountNumber = accNumber,
                iban = iban,
                accountType = "CURRENT",
                balance = 50000.0,
                availableBalance = 50000.0,
                status = "ACTIVE"
            )
        )

        // Seed welcome deposit
        dao.insertTransaction(
            TransactionEntity(
                referenceNumber = "AKB-INIT-" + UUID.randomUUID().toString().take(6).uppercase(),
                senderAccountId = 0,
                receiverAccountId = accId,
                senderName = "AK Bank Welcome Bonus",
                receiverName = fullName,
                bankName = "AK Bank",
                amount = 50000.0,
                fee = 0.0,
                type = "DEPOSIT",
                category = "TRANSFERS",
                status = "COMPLETED",
                note = "Welcome to AK Bank Demo Account"
            )
        )

        // Create card
        val cardNum = "4532" + (100000000000L..999999999999L).random().toString()
        dao.insertCard(
            CardEntity(
                userId = userId,
                accountId = accId,
                cardNumber = cardNum,
                maskedNumber = "4532 •••• •••• " + cardNum.takeLast(4),
                cardHolderName = fullName.uppercase(),
                expiryMonth = 12,
                expiryYear = 29,
                cvv = (100..999).random().toString(),
                cardType = "DEBIT_VISA",
                spendingLimit = 100000.0,
                currentMonthSpent = 0.0
            )
        )

        dao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Welcome to AK Bank",
                message = "Your demo account $accNumber has been activated with Rs. 50,000 welcome balance.",
                type = "SYSTEM"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                userId = userId,
                userEmail = email,
                action = "REGISTER",
                resource = "USER",
                result = "SUCCESS"
            )
        )

        return Result.success(createdUser)
    }

    fun observeUser(userId: Long): Flow<UserEntity?> = dao.observeUserById(userId)
    fun observeAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers()
    suspend fun getUserById(userId: Long): UserEntity? = dao.getUserById(userId)

    suspend fun updateUserProfile(user: UserEntity): Result<Unit> {
        dao.updateUser(user)
        dao.insertAuditLog(
            AuditLogEntity(
                userId = user.id,
                userEmail = user.email,
                action = "PROFILE_UPDATE",
                resource = "USER_${user.id}",
                result = "SUCCESS"
            )
        )
        return Result.success(Unit)
    }

    suspend fun changePassword(userId: Long, oldPass: String, newPass: String): Result<Unit> {
        val user = dao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        if (user.passwordHash != oldPass) {
            return Result.failure(Exception("Current password is incorrect."))
        }
        dao.updateUser(user.copy(passwordHash = newPass))
        dao.insertAuditLog(
            AuditLogEntity(
                userId = userId,
                userEmail = user.email,
                action = "PASSWORD_CHANGE",
                resource = "AUTH",
                result = "SUCCESS"
            )
        )
        return Result.success(Unit)
    }

    suspend fun changeTransactionPin(userId: Long, newPin: String): Result<Unit> {
        val user = dao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        dao.updateUser(user.copy(transactionPin = newPin))
        return Result.success(Unit)
    }

    // --- Accounts & Transactions ---
    fun getAccountsForUser(userId: Long): Flow<List<AccountEntity>> = dao.getAccountsByUserId(userId)
    fun getAllAccounts(): Flow<List<AccountEntity>> = dao.getAllAccounts()
    fun getTransactionsForAccount(accountId: Long): Flow<List<TransactionEntity>> = dao.getTransactionsForAccount(accountId)
    fun getAllTransactions(): Flow<List<TransactionEntity>> = dao.getAllTransactions()

    suspend fun executeTransfer(
        senderUser: UserEntity,
        senderAccountId: Long,
        recipientName: String,
        bankName: String,
        destinationAccountOrIban: String,
        amount: Double,
        purpose: String,
        pin: String,
        transferType: String // "AK_TRANSFER", "OTHER_BANK", "OWN_ACCOUNT"
    ): Result<TransactionEntity> {
        if (senderUser.transactionPin != pin) {
            return Result.failure(Exception("Invalid Transaction PIN. Please check and try again."))
        }
        if (amount <= 0) {
            return Result.failure(Exception("Transfer amount must be greater than zero."))
        }

        val senderAcc = dao.getAccountById(senderAccountId)
            ?: return Result.failure(Exception("Sender account not found."))

        if (senderAcc.availableBalance < amount) {
            return Result.failure(Exception("Insufficient balance in account. Available: Rs. ${formatCurrency(senderAcc.availableBalance)}"))
        }

        // Generate Reference
        val refNumber = "AKB-" + System.currentTimeMillis().toString().takeLast(6) + "-" + (100..999).random()

        // Debit sender
        val updatedSender = senderAcc.copy(
            balance = senderAcc.balance - amount,
            availableBalance = senderAcc.availableBalance - amount
        )
        dao.updateAccount(updatedSender)

        // If transfer is to another AK Bank account in the system, credit that account
        val targetAcc = dao.getAccountByNumber(destinationAccountOrIban)
        var receiverAccId = 0L
        if (targetAcc != null) {
            receiverAccId = targetAcc.id
            val updatedTarget = targetAcc.copy(
                balance = targetAcc.balance + amount,
                availableBalance = targetAcc.availableBalance + amount
            )
            dao.updateAccount(updatedTarget)
            dao.insertNotification(
                NotificationEntity(
                    userId = targetAcc.userId,
                    title = "Money Received",
                    message = "Received Rs. ${formatCurrency(amount)} from ${senderUser.fullName} ($refNumber)",
                    type = "TRANSACTION"
                )
            )
        }

        // Create transaction record
        val tx = TransactionEntity(
            referenceNumber = refNumber,
            senderAccountId = senderAccountId,
            receiverAccountId = receiverAccId,
            senderName = senderUser.fullName,
            receiverName = recipientName,
            bankName = bankName,
            amount = amount,
            fee = 0.0,
            type = transferType,
            category = "TRANSFERS",
            status = "COMPLETED",
            note = purpose,
            timestamp = System.currentTimeMillis()
        )
        val txId = dao.insertTransaction(tx)

        // Notification for sender
        dao.insertNotification(
            NotificationEntity(
                userId = senderUser.id,
                title = "Money Transferred",
                message = "Successfully transferred Rs. ${formatCurrency(amount)} to $recipientName ($bankName). Ref: $refNumber",
                type = "TRANSACTION"
            )
        )

        // Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                userId = senderUser.id,
                userEmail = senderUser.email,
                action = "TRANSFER",
                resource = "TX_$refNumber",
                result = "SUCCESS"
            )
        )

        return Result.success(tx.copy(id = txId))
    }

    suspend fun payBill(
        senderUser: UserEntity,
        senderAccountId: Long,
        billId: Long,
        billerName: String,
        consumerNumber: String,
        amount: Double,
        pin: String
    ): Result<TransactionEntity> {
        if (senderUser.transactionPin != pin) {
            return Result.failure(Exception("Invalid Transaction PIN."))
        }
        val senderAcc = dao.getAccountById(senderAccountId)
            ?: return Result.failure(Exception("Account not found."))
        if (senderAcc.availableBalance < amount) {
            return Result.failure(Exception("Insufficient balance."))
        }

        val refNumber = "BILL-" + System.currentTimeMillis().toString().takeLast(6)

        val updatedSender = senderAcc.copy(
            balance = senderAcc.balance - amount,
            availableBalance = senderAcc.availableBalance - amount
        )
        dao.updateAccount(updatedSender)

        val tx = TransactionEntity(
            referenceNumber = refNumber,
            senderAccountId = senderAccountId,
            receiverAccountId = 0,
            senderName = senderUser.fullName,
            receiverName = billerName,
            bankName = "Utility Payment",
            amount = amount,
            fee = 0.0,
            type = "BILL_PAYMENT",
            category = "BILLS",
            status = "COMPLETED",
            note = "Consumer: $consumerNumber",
            timestamp = System.currentTimeMillis()
        )
        val txId = dao.insertTransaction(tx)

        // Mark bill paid if in DB
        val bill = dao.getAllBills() // or direct lookup
        // Update bill status
        dao.insertNotification(
            NotificationEntity(
                userId = senderUser.id,
                title = "Bill Paid Successfully",
                message = "Paid Rs. ${formatCurrency(amount)} to $billerName. Ref: $refNumber",
                type = "BILL"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                userId = senderUser.id,
                userEmail = senderUser.email,
                action = "BILL_PAY",
                resource = "BILL_$consumerNumber",
                result = "SUCCESS"
            )
        )

        return Result.success(tx.copy(id = txId))
    }

    suspend fun executeMobileTopup(
        senderUser: UserEntity,
        senderAccountId: Long,
        networkName: String,
        phoneNumber: String,
        amount: Double,
        pin: String
    ): Result<TransactionEntity> {
        if (senderUser.transactionPin != pin) {
            return Result.failure(Exception("Invalid Transaction PIN."))
        }
        val senderAcc = dao.getAccountById(senderAccountId)
            ?: return Result.failure(Exception("Account not found."))
        if (senderAcc.availableBalance < amount) {
            return Result.failure(Exception("Insufficient balance."))
        }

        val refNumber = "TOP-" + System.currentTimeMillis().toString().takeLast(6)
        val updatedSender = senderAcc.copy(
            balance = senderAcc.balance - amount,
            availableBalance = senderAcc.availableBalance - amount
        )
        dao.updateAccount(updatedSender)

        val tx = TransactionEntity(
            referenceNumber = refNumber,
            senderAccountId = senderAccountId,
            receiverAccountId = 0,
            senderName = senderUser.fullName,
            receiverName = "$networkName ($phoneNumber)",
            bankName = "Mobile Top-Up",
            amount = amount,
            fee = 0.0,
            type = "TOPUP",
            category = "BILLS",
            status = "COMPLETED",
            note = "Mobile Recharge to $phoneNumber",
            timestamp = System.currentTimeMillis()
        )
        val txId = dao.insertTransaction(tx)

        dao.insertNotification(
            NotificationEntity(
                userId = senderUser.id,
                title = "Mobile Recharge Successful",
                message = "Recharged Rs. ${formatCurrency(amount)} on $phoneNumber ($networkName).",
                type = "TRANSACTION"
            )
        )

        return Result.success(tx.copy(id = txId))
    }

    suspend fun executeQRPayment(
        senderUser: UserEntity,
        senderAccountId: Long,
        merchantName: String,
        amount: Double,
        pin: String
    ): Result<TransactionEntity> {
        if (senderUser.transactionPin != pin) {
            return Result.failure(Exception("Invalid Transaction PIN."))
        }
        val senderAcc = dao.getAccountById(senderAccountId)
            ?: return Result.failure(Exception("Account not found."))
        if (senderAcc.availableBalance < amount) {
            return Result.failure(Exception("Insufficient balance."))
        }

        val refNumber = "QR-" + System.currentTimeMillis().toString().takeLast(6)
        val updatedSender = senderAcc.copy(
            balance = senderAcc.balance - amount,
            availableBalance = senderAcc.availableBalance - amount
        )
        dao.updateAccount(updatedSender)

        val tx = TransactionEntity(
            referenceNumber = refNumber,
            senderAccountId = senderAccountId,
            receiverAccountId = 0,
            senderName = senderUser.fullName,
            receiverName = merchantName,
            bankName = "AK QR Pay",
            amount = amount,
            fee = 0.0,
            type = "QR_PAYMENT",
            category = "SHOPPING",
            status = "COMPLETED",
            note = "Scan & Pay at $merchantName",
            timestamp = System.currentTimeMillis()
        )
        val txId = dao.insertTransaction(tx)

        dao.insertNotification(
            NotificationEntity(
                userId = senderUser.id,
                title = "QR Payment Successful",
                message = "Paid Rs. ${formatCurrency(amount)} to $merchantName via QR Code.",
                type = "TRANSACTION"
            )
        )

        return Result.success(tx.copy(id = txId))
    }

    // --- Beneficiaries ---
    fun getBeneficiaries(userId: Long): Flow<List<BeneficiaryEntity>> = dao.getBeneficiariesByUserId(userId)
    suspend fun addBeneficiary(beneficiary: BeneficiaryEntity): Long {
        val id = dao.insertBeneficiary(beneficiary)
        dao.insertAuditLog(
            AuditLogEntity(
                userId = beneficiary.userId,
                userEmail = "",
                action = "BENEFICIARY_ADD",
                resource = "BENEFICIARY_${beneficiary.accountNumber}",
                result = "SUCCESS"
            )
        )
        return id
    }
    suspend fun deleteBeneficiary(beneficiary: BeneficiaryEntity) = dao.deleteBeneficiary(beneficiary)
    suspend fun updateBeneficiary(beneficiary: BeneficiaryEntity) = dao.updateBeneficiary(beneficiary)

    // --- Cards ---
    fun getCards(userId: Long): Flow<List<CardEntity>> = dao.getCardsByUserId(userId)
    fun getAllCards(): Flow<List<CardEntity>> = dao.getAllCards()
    suspend fun updateCard(card: CardEntity) {
        dao.updateCard(card)
        dao.insertAuditLog(
            AuditLogEntity(
                userId = card.userId,
                userEmail = "",
                action = if (card.isFrozen) "CARD_FREEZE" else "CARD_UNFREEZE",
                resource = "CARD_${card.maskedNumber}",
                result = "SUCCESS"
            )
        )
    }

    // --- Bills & Billers ---
    fun getAllBillers(): Flow<List<BillerEntity>> = dao.getAllBillers()
    fun getAllBills(): Flow<List<BillEntity>> = dao.getAllBills()
    suspend fun getBillByConsumerNumber(billerId: Long, consumerNumber: String): BillEntity? {
        return dao.getBillByConsumerNumber(billerId, consumerNumber)
    }

    // --- Notifications ---
    fun getNotifications(userId: Long): Flow<List<NotificationEntity>> = dao.getNotificationsByUserId(userId)
    suspend fun markAllNotificationsAsRead(userId: Long) = dao.markAllNotificationsAsRead(userId)
    suspend fun deleteNotification(id: Long) = dao.deleteNotification(id)

    // --- Support ---
    fun getTicketsForUser(userId: Long): Flow<List<SupportTicketEntity>> = dao.getTicketsByUserId(userId)
    fun getAllTickets(): Flow<List<SupportTicketEntity>> = dao.getAllTickets()
    fun getTicketMessages(ticketId: Long): Flow<List<SupportMessageEntity>> = dao.getMessagesForTicket(ticketId)
    suspend fun createTicket(ticket: SupportTicketEntity, initialMessage: String): Long {
        val ticketId = dao.insertTicket(ticket)
        dao.insertMessage(
            SupportMessageEntity(
                ticketId = ticketId,
                senderRole = "CUSTOMER",
                senderName = ticket.customerName,
                message = initialMessage
            )
        )
        return ticketId
    }
    suspend fun sendTicketReply(ticketId: Long, role: String, senderName: String, text: String) {
        dao.insertMessage(
            SupportMessageEntity(
                ticketId = ticketId,
                senderRole = role,
                senderName = senderName,
                message = text
            )
        )
        val ticket = dao.getTicketById(ticketId)
        if (ticket != null) {
            dao.updateTicket(ticket.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    suspend fun updateTicketStatus(ticketId: Long, status: String) {
        val ticket = dao.getTicketById(ticketId)
        if (ticket != null) {
            dao.updateTicket(ticket.copy(status = status, updatedAt = System.currentTimeMillis()))
        }
    }

    // --- Admin & Audit ---
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()
    suspend fun toggleUserSuspension(userId: Long, adminEmail: String): Boolean {
        val user = dao.getUserById(userId) ?: return false
        val newStatus = !user.isSuspended
        dao.updateUser(user.copy(isSuspended = newStatus))
        dao.insertAuditLog(
            AuditLogEntity(
                userId = user.id,
                userEmail = adminEmail,
                action = if (newStatus) "ADMIN_ACCOUNT_SUSPEND" else "ADMIN_ACCOUNT_ACTIVATE",
                resource = "USER_${user.email}",
                result = "SUCCESS"
            )
        )
        return newStatus
    }

    suspend fun markTransactionReviewed(txId: Long, adminEmail: String) {
        dao.insertAuditLog(
            AuditLogEntity(
                userId = 0,
                userEmail = adminEmail,
                action = "ADMIN_TRANSACTION_REVIEW",
                resource = "TX_$txId",
                result = "SUCCESS"
            )
        )
    }

    // --- AI Banking Assistant Engine ---
    suspend fun queryAssistant(query: String, user: UserEntity): String {
        val lower = query.trim().lowercase()
        val allTx = dao.getAllTransactionsDirect()
        val userAccounts = dao.getAccountsByUserIdDirect(user.id)
        val accountIds = userAccounts.map { it.id }.toSet()

        val userTx = allTx.filter { tx ->
            accountIds.contains(tx.senderAccountId) || accountIds.contains(tx.receiverAccountId)
        }

        return when {
            lower.contains("spend") && lower.contains("month") || lower.contains("monthly spending") -> {
                val expenseTotal = userTx.filter { it.type != "DEPOSIT" }.sumOf { it.amount }
                "Based on your recent transactions, your total spending this month is Rs. ${formatCurrency(expenseTotal)}. This includes utility bills, grocery shopping, and debit card orders."
            }
            lower.contains("highest") || lower.contains("biggest") -> {
                val highest = userTx.filter { it.type != "DEPOSIT" }.maxByOrNull { it.amount }
                if (highest != null) {
                    "Your highest expense was Rs. ${formatCurrency(highest.amount)} paid to ${highest.receiverName} on ${formatDate(highest.timestamp)} (Category: ${highest.category})."
                } else {
                    "No expense transactions found on your account."
                }
            }
            lower.contains("recent") || lower.contains("last transaction") -> {
                val recent = userTx.take(3)
                if (recent.isNotEmpty()) {
                    val list = recent.joinToString("\n• ") {
                        "${it.receiverName}: Rs. ${formatCurrency(it.amount)} (${it.status})"
                    }
                    "Here are your latest transactions:\n• $list"
                } else {
                    "You do not have any recent transactions."
                }
            }
            lower.contains("receive") || lower.contains("income") || lower.contains("credited") -> {
                val incomeTotal = userTx.filter { it.type == "DEPOSIT" }.sumOf { it.amount }
                "You have received a total of Rs. ${formatCurrency(incomeTotal)} in credits this month, primarily from salary and incoming remittances."
            }
            lower.contains("category") -> {
                val byCategory = userTx.filter { it.type != "DEPOSIT" }.groupBy { it.category }
                val maxCategory = byCategory.maxByOrNull { entry -> entry.value.sumOf { it.amount } }
                if (maxCategory != null) {
                    val amount = maxCategory.value.sumOf { it.amount }
                    "Your highest spending category is ${maxCategory.key} with a total of Rs. ${formatCurrency(amount)}."
                } else {
                    "You don't have enough categorized expenses yet."
                }
            }
            lower.contains("summary") || lower.contains("overview") -> {
                val income = userTx.filter { it.type == "DEPOSIT" }.sumOf { it.amount }
                val expense = userTx.filter { it.type != "DEPOSIT" }.sumOf { it.amount }
                val balance = userAccounts.sumOf { it.balance }
                "Financial Summary for ${user.fullName}:\n• Total Balance: Rs. ${formatCurrency(balance)}\n• Total Income: Rs. ${formatCurrency(income)}\n• Total Expenses: Rs. ${formatCurrency(expense)}\n• Net Savings Rate: ${if (income > 0) ((income - expense) / income * 100).toInt() else 0}%"
            }
            lower.contains("balance") -> {
                val balance = userAccounts.sumOf { it.balance }
                "Your current combined balance across all AK Bank accounts is Rs. ${formatCurrency(balance)}."
            }
            else -> {
                "Hello ${user.fullName}! I can analyze your demo banking data. You can ask me:\n• 'How much did I spend this month?'\n• 'What was my highest expense?'\n• 'Show my recent transactions'\n• 'How much money did I receive?'\n• 'What category do I spend the most on?'\n• 'Give me a summary of my spending'"
            }
        }
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            return formatter.format(amount)
        }

        fun formatDate(timestamp: Long): String {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
            return sdf.format(java.util.Date(timestamp))
        }
    }
}
