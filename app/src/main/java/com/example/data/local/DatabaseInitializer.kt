package com.example.data.local

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

object DatabaseInitializer {

    suspend fun seedDatabaseIfEmpty(dao: BankDao) {
        val existingCustomer = dao.getUserByEmail("demo@akbank.demo")
        if (existingCustomer != null) {
            return
        }

        // 1. Create Default Users (Customer, Admin, Support)
        val customerId = dao.insertUser(
            UserEntity(
                email = "demo@akbank.demo",
                passwordHash = "Demo@12345", // Hashed/Verified in demo
                fullName = "Muhammad Mawiya",
                phone = "+92 300 1234567",
                cnic = "42101-5829143-7",
                address = "House 42, Block 6, PECHS",
                city = "Karachi",
                role = "CUSTOMER",
                transactionPin = "1234",
                isBiometricEnabled = true
            )
        )

        dao.insertUser(
            UserEntity(
                email = "admin@akbank.demo",
                passwordHash = "Admin@12345",
                fullName = "AK Bank System Admin",
                phone = "+92 321 9876543",
                cnic = "61101-1122334-1",
                address = "AK Bank Tower, Blue Area",
                city = "Islamabad",
                role = "ADMIN",
                transactionPin = "9999",
                isBiometricEnabled = true
            )
        )

        dao.insertUser(
            UserEntity(
                email = "support@akbank.demo",
                passwordHash = "Support@12345",
                fullName = "Ayesha Support Specialist",
                phone = "+92 333 4455667",
                cnic = "35201-9988776-5",
                address = "Gulberg III",
                city = "Lahore",
                role = "SUPPORT",
                transactionPin = "5555",
                isBiometricEnabled = true
            )
        )

        // 2. Customer Accounts
        val currentAccId = dao.insertAccount(
            AccountEntity(
                userId = customerId,
                accountTitle = "Muhammad Mawiya - Current",
                accountNumber = "001234567890",
                iban = "PK78AKBK0012345678901234",
                accountType = "CURRENT",
                balance = 145250.0,
                availableBalance = 145250.0,
                status = "ACTIVE",
                openingDate = "2024-01-15"
            )
        )

        val savingsAccId = dao.insertAccount(
            AccountEntity(
                userId = customerId,
                accountTitle = "Muhammad Mawiya - Asaan Savings",
                accountNumber = "009876543210",
                iban = "PK21AKBK0098765432109876",
                accountType = "SAVINGS",
                balance = 380000.0,
                availableBalance = 380000.0,
                status = "ACTIVE",
                openingDate = "2024-03-20"
            )
        )

        // 3. Transactions for current account (Income and expenses)
        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        val demoTransactions = listOf(
            TransactionEntity(
                referenceNumber = "AKB-2024-88410",
                senderAccountId = 0,
                receiverAccountId = currentAccId,
                senderName = "Tech Solutions Ltd (Salary)",
                receiverName = "Muhammad Mawiya",
                bankName = "AK Bank",
                amount = 220000.0,
                fee = 0.0,
                type = "DEPOSIT",
                category = "TRANSFERS",
                status = "COMPLETED",
                note = "Monthly Salary Credited",
                timestamp = now - (dayMs * 2)
            ),
            TransactionEntity(
                referenceNumber = "AKB-2024-88392",
                senderAccountId = currentAccId,
                receiverAccountId = 0,
                senderName = "Muhammad Mawiya",
                receiverName = "K-Electric Karachi",
                bankName = "K-Electric",
                amount = 14820.0,
                fee = 0.0,
                type = "BILL_PAYMENT",
                category = "BILLS",
                status = "COMPLETED",
                note = "Consumer #0400012345678",
                timestamp = now - (dayMs * 3)
            ),
            TransactionEntity(
                referenceNumber = "AKB-2024-88210",
                senderAccountId = currentAccId,
                receiverAccountId = 0,
                senderName = "Muhammad Mawiya",
                receiverName = "Metro Cash & Carry",
                bankName = "AK Debit Card",
                amount = 18450.0,
                fee = 0.0,
                type = "OTHER_BANK",
                category = "SHOPPING",
                status = "COMPLETED",
                note = "Household Groceries",
                timestamp = now - (dayMs * 4)
            ),
            TransactionEntity(
                referenceNumber = "AKB-2024-88104",
                senderAccountId = currentAccId,
                receiverAccountId = 0,
                senderName = "Muhammad Mawiya",
                receiverName = "Fatima Zahra",
                bankName = "Habib Bank Limited (HBL)",
                amount = 25000.0,
                fee = 0.0,
                type = "OTHER_BANK",
                category = "TRANSFERS",
                status = "COMPLETED",
                note = "Family Support Transfer",
                timestamp = now - (dayMs * 6)
            ),
            TransactionEntity(
                referenceNumber = "AKB-2024-87980",
                senderAccountId = currentAccId,
                receiverAccountId = 0,
                senderName = "Muhammad Mawiya",
                receiverName = "Shell Fuel Station",
                bankName = "AK Debit Card",
                amount = 8200.0,
                fee = 0.0,
                type = "OTHER_BANK",
                category = "TRANSPORT",
                status = "COMPLETED",
                note = "V-Power Petrol Refill",
                timestamp = now - (dayMs * 7)
            ),
            TransactionEntity(
                referenceNumber = "AKB-2024-87820",
                senderAccountId = currentAccId,
                receiverAccountId = 0,
                senderName = "Muhammad Mawiya",
                receiverName = "Foodpanda Pakistan",
                bankName = "AK Debit Card",
                amount = 2850.0,
                fee = 0.0,
                type = "OTHER_BANK",
                category = "FOOD",
                status = "COMPLETED",
                note = "Weekend Dinner Order",
                timestamp = now - (dayMs * 8)
            ),
            TransactionEntity(
                referenceNumber = "AKB-2024-87654",
                senderAccountId = currentAccId,
                receiverAccountId = 0,
                senderName = "Muhammad Mawiya",
                receiverName = "Nayatel Flash Fiber",
                bankName = "Nayatel",
                amount = 4500.0,
                fee = 0.0,
                type = "BILL_PAYMENT",
                category = "BILLS",
                status = "COMPLETED",
                note = "High Speed Internet",
                timestamp = now - (dayMs * 10)
            ),
            TransactionEntity(
                referenceNumber = "AKB-2024-87420",
                senderAccountId = 0,
                receiverAccountId = currentAccId,
                senderName = "Upwork Escrow Inc.",
                receiverName = "Muhammad Mawiya",
                bankName = "Standard Chartered",
                amount = 65000.0,
                fee = 0.0,
                type = "DEPOSIT",
                category = "TRANSFERS",
                status = "COMPLETED",
                note = "Freelance Mobile App Payment",
                timestamp = now - (dayMs * 12)
            ),
            TransactionEntity(
                referenceNumber = "AKB-2024-87301",
                senderAccountId = currentAccId,
                receiverAccountId = 0,
                senderName = "Muhammad Mawiya",
                receiverName = "Daraz.pk Online",
                bankName = "AK Debit Card",
                amount = 6800.0,
                fee = 0.0,
                type = "OTHER_BANK",
                category = "SHOPPING",
                status = "COMPLETED",
                note = "Home & Kitchen Appliances",
                timestamp = now - (dayMs * 14)
            )
        )

        for (tx in demoTransactions) {
            dao.insertTransaction(tx)
        }

        // 4. Beneficiaries
        val demoBeneficiaries = listOf(
            BeneficiaryEntity(
                userId = customerId,
                name = "Ali Raza",
                nickname = "Ali - Roommate",
                bankName = "Meezan Bank",
                accountNumber = "0102938475",
                iban = "PK56MEZN0102938475019283",
                isFavorite = true
            ),
            BeneficiaryEntity(
                userId = customerId,
                name = "Fatima Zahra",
                nickname = "Fatima Sister",
                bankName = "Habib Bank Limited (HBL)",
                accountNumber = "008472910482",
                iban = "PK99HABB0084729104820192",
                isFavorite = true
            ),
            BeneficiaryEntity(
                userId = customerId,
                name = "Ahmed Hassan",
                nickname = "Ahmed Colleague",
                bankName = "AK Bank",
                accountNumber = "004820194820",
                iban = "PK78AKBK0048201948201948",
                isFavorite = true
            ),
            BeneficiaryEntity(
                userId = customerId,
                name = "Zainab Malik",
                nickname = "Zainab - Architect",
                bankName = "Bank Alfalah",
                accountNumber = "009384710293",
                iban = "PK12ALFH0093847102938471",
                isFavorite = false
            ),
            BeneficiaryEntity(
                userId = customerId,
                name = "Hamza Tariq",
                nickname = "Hamza Cousin",
                bankName = "United Bank Limited (UBL)",
                accountNumber = "003928172635",
                iban = "PK44UNIL0039281726354829",
                isFavorite = false
            )
        )

        for (b in demoBeneficiaries) {
            dao.insertBeneficiary(b)
        }

        // 5. Virtual Cards
        dao.insertCard(
            CardEntity(
                userId = customerId,
                accountId = currentAccId,
                cardNumber = "4532890123458842",
                maskedNumber = "4532 •••• •••• 8842",
                cardHolderName = "MUHAMMAD MAWIYA",
                expiryMonth = 12,
                expiryYear = 28,
                cvv = "842",
                cardType = "DEBIT_VISA",
                isFrozen = false,
                isOnlinePaymentEnabled = true,
                isIntlPaymentEnabled = false,
                spendingLimit = 250000.0,
                currentMonthSpent = 46300.0,
                cardPin = "1234"
            )
        )

        // 6. Billers
        val kelectricId = dao.insertBiller(BillerEntity(category = "ELECTRICITY", name = "K-Electric Karachi", code = "KE"))
        val lescoId = dao.insertBiller(BillerEntity(category = "ELECTRICITY", name = "LESCO Lahore", code = "LESCO"))
        val ssgcId = dao.insertBiller(BillerEntity(category = "GAS", name = "Sui Southern Gas (SSGC)", code = "SSGC"))
        val sngplId = dao.insertBiller(BillerEntity(category = "GAS", name = "Sui Northern Gas (SNGPL)", code = "SNGPL"))
        val ptclId = dao.insertBiller(BillerEntity(category = "INTERNET", name = "PTCL Flash Fiber", code = "PTCL"))
        val nayatelId = dao.insertBiller(BillerEntity(category = "INTERNET", name = "Nayatel Internet", code = "NTL"))
        val jazzId = dao.insertBiller(BillerEntity(category = "MOBILE", name = "Jazz Mobile Postpaid", code = "JAZZ"))
        val lumsId = dao.insertBiller(BillerEntity(category = "EDUCATION", name = "LUMS University Fees", code = "LUMS"))

        // 7. Demo Bills
        dao.insertBill(
            BillEntity(
                billerId = kelectricId,
                consumerNumber = "0400012345678",
                title = "Electricity Bill - September",
                amount = 14820.0,
                dueDate = "2024-10-18",
                isPaid = false
            )
        )
        dao.insertBill(
            BillEntity(
                billerId = ptclId,
                consumerNumber = "1002345678",
                title = "PTCL High Speed Fiber",
                amount = 4250.0,
                dueDate = "2024-10-22",
                isPaid = false
            )
        )
        dao.insertBill(
            BillEntity(
                billerId = ssgcId,
                consumerNumber = "9876543210",
                title = "Domestic Gas Connection",
                amount = 2180.0,
                dueDate = "2024-10-25",
                isPaid = false
            )
        )

        // 8. Notifications
        val demoNotifications = listOf(
            NotificationEntity(
                userId = customerId,
                title = "Salary Credited",
                message = "Rs. 220,000.00 credited to account 001234567890 from Tech Solutions Ltd.",
                type = "TRANSACTION",
                isRead = false,
                timestamp = now - (dayMs * 2)
            ),
            NotificationEntity(
                userId = customerId,
                title = "Security Alert",
                message = "New login detected from Karachi, Pakistan on Android device.",
                type = "SECURITY",
                isRead = false,
                timestamp = now - (dayMs * 1)
            ),
            NotificationEntity(
                userId = customerId,
                title = "New Bill Generated",
                message = "K-Electric bill of Rs. 14,820.00 is due on October 18, 2024.",
                type = "BILL",
                isRead = true,
                timestamp = now - (dayMs * 3)
            )
        )
        for (n in demoNotifications) {
            dao.insertNotification(n)
        }

        // 9. Support Tickets
        val ticketId = dao.insertTicket(
            SupportTicketEntity(
                userId = customerId,
                customerName = "Muhammad Mawiya",
                customerEmail = "demo@akbank.demo",
                category = "CARD",
                subject = "Request for International E-Commerce Activation",
                message = "Hello AK Bank Support, I would like to verify if international transactions on my Visa Debit Card can be enabled for software subscriptions.",
                status = "IN_PROGRESS",
                createdAt = now - (dayMs * 2),
                updatedAt = now - (dayMs * 1)
            )
        )

        dao.insertMessage(
            SupportMessageEntity(
                ticketId = ticketId,
                senderRole = "CUSTOMER",
                senderName = "Muhammad Mawiya",
                message = "Hello AK Bank Support, I would like to verify if international transactions on my Visa Debit Card can be enabled for software subscriptions.",
                timestamp = now - (dayMs * 2)
            )
        )
        dao.insertMessage(
            SupportMessageEntity(
                ticketId = ticketId,
                senderRole = "SUPPORT",
                senderName = "Ayesha (Support Specialist)",
                message = "Dear Customer, you can toggle International Payments instantly under the Cards tab using your 4-digit PIN! Let us know if you need any further assistance.",
                timestamp = now - (dayMs * 1)
            )
        )

        // 10. Audit Logs
        dao.insertAuditLog(
            AuditLogEntity(
                userId = customerId,
                userEmail = "demo@akbank.demo",
                action = "LOGIN",
                resource = "AUTH_SERVICE",
                result = "SUCCESS",
                ipAddress = "192.168.1.101",
                timestamp = now - (dayMs * 1)
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                userId = customerId,
                userEmail = "demo@akbank.demo",
                action = "BENEFICIARY_ADD",
                resource = "BENEFICIARY_0102938475",
                result = "SUCCESS",
                ipAddress = "192.168.1.101",
                timestamp = now - (dayMs * 4)
            )
        )
    }
}
