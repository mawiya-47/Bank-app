package com.example.ui.screens.cards

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CardEntity
import com.example.data.repository.BankRepository
import com.example.ui.components.AKDisclaimerNotice
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.BankViewModel

@Composable
fun CardsScreen(
    viewModel: BankViewModel,
    cards: List<CardEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val card = cards.firstOrNull() ?: CardEntity(
        userId = 1,
        accountId = 1,
        cardNumber = "4532890123458842",
        maskedNumber = "4532 •••• •••• 8842",
        cardHolderName = "MUHAMMAD MAWIYA",
        expiryMonth = 12,
        expiryYear = 28,
        cvv = "842"
    )

    var showCvv by remember { mutableStateOf(false) }
    var changePinOpen by remember { mutableStateOf(false) }
    var reportCardOpen by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }
    var sliderValue by remember(card.spendingLimit) {
        mutableFloatStateOf(card.spendingLimit.toFloat().coerceIn(25000f, 500000f))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            AKDisclaimerNotice()
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Virtual Debit Cards",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Manage your digital card security and spending limits in real-time.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Virtual Card Graphic
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (card.isFrozen) {
                                listOf(Color(0xFF64748B), Color(0xFF334155))
                            } else {
                                listOf(NavyPrimary, Color(0xFF0D3256), NavyDark)
                            }
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (card.isFrozen) Color.LightGray else GoldAccent.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AK BANK",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.5.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (card.isFrozen) Color(0xFFEF4444) else MintSecondary
                        ) {
                            Text(
                                text = if (card.isFrozen) "FROZEN" else "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (card.isFrozen) Color.White else NavyDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Chip & Contactless Symbol
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 38.dp, height = 28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldAccent)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Contactless",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = card.maskedNumber,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "CARD HOLDER",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = card.cardHolderName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "EXPIRES",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${card.expiryMonth}/${card.expiryYear}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CVV",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { showCvv = !showCvv }
                            ) {
                                Text(
                                    text = if (showCvv) card.cvv else "•••",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (showCvv) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle CVV",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Text(
                            text = "DEMO VISA",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Quick Controls
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Card Controls",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Freeze Card Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Freeze Card", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(text = "Instantly lock card for all POS and ATM use", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = card.isFrozen,
                            onCheckedChange = { viewModel.toggleCardFreeze(card) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Online Shopping Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Online E-Commerce", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(text = "Enable web and app purchases", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = card.isOnlinePaymentEnabled,
                            onCheckedChange = { viewModel.toggleCardOnline(card) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // International Payments Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "International Transactions", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(text = "Enable foreign currency transactions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = card.isIntlPaymentEnabled,
                            onCheckedChange = { viewModel.toggleCardIntl(card) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
                        )
                    }
                }
            }
        }

        // Spending Limit Slider
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Daily Spending Limit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "Rs. ${BankRepository.formatCurrency(sliderValue.toDouble())}",
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = {
                            viewModel.updateCardLimit(card, sliderValue.toDouble())
                        },
                        valueRange = 25000f..500000f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = NavyPrimary,
                            activeTrackColor = MintSecondary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Rs. 25,000", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Rs. 500,000", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Action Buttons: Change PIN, Report Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { changePinOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Change PIN", fontSize = 12.sp, color = Color.White)
                }

                Button(
                    onClick = { reportCardOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Report / Replace", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }

    // Change Card PIN Dialog
    if (changePinOpen) {
        AlertDialog(
            onDismissRequest = { changePinOpen = false },
            title = { Text("Change Debit Card PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Set a new 4-digit ATM/POS PIN for your demo debit card:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPinText,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinText = it },
                        label = { Text("New 4-Digit PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinText.length == 4) {
                            changePinOpen = false
                            Toast.makeText(context, "Debit Card PIN updated to $newPinText", Toast.LENGTH_SHORT).show()
                            newPinText = ""
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

    // Report Card Dialog
    if (reportCardOpen) {
        AlertDialog(
            onDismissRequest = { reportCardOpen = false },
            title = { Text("Report Lost or Damaged Card", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your current virtual card ending in ${card.cardNumber.takeLast(4)} will be permanently deactivated and a new demo replacement card issued.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        reportCardOpen = false
                        viewModel.toggleCardFreeze(card)
                        Toast.makeText(context, "Card reported and deactivated. Replacement requested.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Block & Replace", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportCardOpen = false }) { Text("Cancel") }
            }
        )
    }
}
