package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val phone: String,
    val cnic: String,
    val address: String,
    val city: String,
    val role: String = "CUSTOMER", // CUSTOMER, ADMIN, SUPPORT
    val transactionPin: String = "1234",
    val isBiometricEnabled: Boolean = true,
    val isSuspended: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val accountTitle: String,
    val accountNumber: String,
    val iban: String,
    val accountType: String, // CURRENT, SAVINGS
    val balance: Double,
    val availableBalance: Double,
    val status: String = "ACTIVE", // ACTIVE, DORMANT, SUSPENDED
    val openingDate: String = "2024-01-15"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceNumber: String,
    val senderAccountId: Long,
    val receiverAccountId: Long = 0,
    val senderName: String,
    val receiverName: String,
    val bankName: String,
    val amount: Double,
    val fee: Double = 0.0,
    val type: String, // AK_TRANSFER, OTHER_BANK, OWN_ACCOUNT, BILL_PAYMENT, TOPUP, QR_PAYMENT, DEPOSIT
    val category: String, // FOOD, SHOPPING, TRANSPORT, BILLS, ENTERTAINMENT, EDUCATION, HEALTH, TRANSFERS, OTHER
    val status: String = "COMPLETED", // COMPLETED, PENDING, FAILED, REVERSED
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "beneficiaries")
data class BeneficiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val nickname: String,
    val bankName: String,
    val accountNumber: String,
    val iban: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val accountId: Long,
    val cardNumber: String,
    val maskedNumber: String,
    val cardHolderName: String,
    val expiryMonth: Int = 12,
    val expiryYear: Int = 28,
    val cvv: String = "842",
    val cardType: String = "DEBIT_VISA",
    val isFrozen: Boolean = false,
    val isOnlinePaymentEnabled: Boolean = true,
    val isIntlPaymentEnabled: Boolean = false,
    val spendingLimit: Double = 150000.0,
    val currentMonthSpent: Double = 32450.0,
    val cardPin: String = "1234"
)

@Entity(tableName = "billers")
data class BillerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // ELECTRICITY, GAS, WATER, INTERNET, MOBILE, EDUCATION, GOVERNMENT, OTHER
    val name: String,
    val code: String
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billerId: Long,
    val consumerNumber: String,
    val title: String,
    val amount: Double,
    val dueDate: String,
    val isPaid: Boolean = false,
    val paidDate: String? = null,
    val referenceNumber: String? = null
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val type: String = "TRANSACTION",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val customerName: String,
    val customerEmail: String,
    val category: String, // ACCOUNT, TRANSFER, CARD, PAYMENT, LOGIN, SECURITY, OTHER
    val subject: String,
    val message: String,
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_messages")
data class SupportMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketId: Long,
    val senderRole: String, // CUSTOMER, SUPPORT, ADMIN
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userEmail: String,
    val action: String, // LOGIN, LOGOUT, TRANSFER, CARD_FREEZE, BENEFICIARY_ADD, etc.
    val resource: String,
    val result: String = "SUCCESS",
    val ipAddress: String = "192.168.1.42",
    val timestamp: Long = System.currentTimeMillis()
)
