package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AKBottomNavigationBar
import com.example.ui.components.AKReceiptDialog
import com.example.ui.components.AKTopBar
import com.example.ui.screens.admin.AdminScreen
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.assistant.AssistantScreen
import com.example.ui.screens.auth.AuthContainer
import com.example.ui.screens.cards.CardsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.landing.LandingScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.search.GlobalSearchDialog
import com.example.ui.screens.search.NotificationsDialog
import com.example.ui.screens.search.RoleSwitcherDialog
import com.example.ui.screens.transfers.TransfersScreen
import com.example.ui.theme.AKBankTheme
import com.example.viewmodel.BankViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BankViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            AKBankTheme(darkTheme = isDarkMode) {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: BankViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isBalanceHidden by viewModel.isBalanceHidden.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val activeReceipt by viewModel.activeReceipt.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()

    // Data streams
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val beneficiaries by viewModel.beneficiaries.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val billers by viewModel.billers.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val supportTickets by viewModel.supportTickets.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    // Dialogs state
    var showSearchDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showRoleSwitcherDialog by remember { mutableStateOf(false) }
    var isLandingPageActive by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    // Back button handling
    if (currentTab != "HOME" && isLoggedIn) {
        BackHandler {
            viewModel.navigateTo("HOME")
        }
    }

    if (isLandingPageActive) {
        LandingScreen(onEnterApp = { isLandingPageActive = false })
        return
    }

    if (!isLoggedIn || currentUser == null) {
        AuthContainer(viewModel = viewModel)
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AKTopBar(
                currentUser = currentUser,
                isDarkMode = isDarkMode,
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onOpenSearch = { showSearchDialog = true },
                onOpenNotifications = { showNotificationsDialog = true },
                onRoleSwitchClick = { showRoleSwitcherDialog = true },
                onProfileClick = { viewModel.navigateTo("PROFILE") }
            )
        },
        bottomBar = {
            AKBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { tab -> viewModel.navigateTo(tab) },
                userRole = currentUser?.role ?: "CUSTOMER"
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "HOME" -> HomeScreen(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    accounts = accounts,
                    transactions = transactions,
                    beneficiaries = beneficiaries,
                    selectedAccountId = selectedAccountId,
                    isBalanceHidden = isBalanceHidden,
                    onNavigate = { tab -> viewModel.navigateTo(tab) }
                )
                "TRANSFERS" -> TransfersScreen(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    accounts = accounts,
                    beneficiaries = beneficiaries,
                    billers = billers,
                    bills = bills
                )
                "CARDS" -> CardsScreen(
                    viewModel = viewModel,
                    cards = cards
                )
                "ANALYTICS" -> AnalyticsScreen(
                    accounts = accounts,
                    transactions = transactions
                )
                "ASSISTANT" -> AssistantScreen(
                    viewModel = viewModel,
                    chatMessages = chatMessages
                )
                "PROFILE" -> ProfileScreen(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    supportTickets = supportTickets
                )
                "ADMIN" -> AdminScreen(
                    viewModel = viewModel,
                    users = allUsers,
                    accounts = accounts,
                    transactions = transactions,
                    auditLogs = auditLogs,
                    supportTickets = supportTickets
                )
                "SUPPORT_PORTAL" -> AdminScreen(
                    viewModel = viewModel,
                    users = allUsers,
                    accounts = accounts,
                    transactions = transactions,
                    auditLogs = auditLogs,
                    supportTickets = supportTickets
                )
                else -> HomeScreen(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    accounts = accounts,
                    transactions = transactions,
                    beneficiaries = emptyList(),
                    selectedAccountId = selectedAccountId,
                    isBalanceHidden = isBalanceHidden,
                    onNavigate = { tab -> viewModel.navigateTo(tab) }
                )
            }
        }
    }

    // Active Digital Receipt Dialog
    activeReceipt?.let { receipt ->
        AKReceiptDialog(
            receipt = receipt,
            onDismiss = { viewModel.closeReceipt() }
        )
    }

    // Global Search Modal
    GlobalSearchDialog(
        isOpen = showSearchDialog,
        transactions = transactions,
        beneficiaries = beneficiaries,
        billers = billers,
        onDismiss = { showSearchDialog = false },
        onSelectTransaction = { tx ->
            // Could navigate to details or show receipt
        }
    )

    // Notifications Dialog
    NotificationsDialog(
        isOpen = showNotificationsDialog,
        viewModel = viewModel,
        onDismiss = { showNotificationsDialog = false }
    )

    // Role Switcher Dialog
    RoleSwitcherDialog(
        isOpen = showRoleSwitcherDialog,
        currentRole = currentUser?.role ?: "CUSTOMER",
        onDismiss = { showRoleSwitcherDialog = false },
        onSelectRole = { email -> viewModel.quickSwitchRole(email) }
    )
}
