package com.example.easymoney.ui.loan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easymoney.data.model.LoanPackage
import com.example.easymoney.ui.theme.EasyMoneyTheme
import java.text.NumberFormat
import java.util.*

@Composable
@Suppress("UNUSED_PARAMETER")
fun LoanScreen(
    viewModel: LoanViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LoanContent(
        uiState = uiState,
        onAmountChanged = viewModel::onAmountChanged,
        onTenorSelected = viewModel::onTenorSelected,
        onInsuranceToggled = viewModel::onInsuranceToggled,
        onNextStep = viewModel::onNextStep
    )
}

@Composable
fun LoanScreenMock(
    mockPackage: LoanPackage,
    initialAmount: Long = mockPackage.minAmount,
    initialTenor: Int = mockPackage.getTenorList().firstOrNull() ?: 6,
    initialInsuranceSelected: Boolean = true
) {
    var uiState by remember {
        mutableStateOf(
            calculatePreviewState(
                LoanUiState(
                    selectedPackage = mockPackage,
                    loanAmount = initialAmount,
                    selectedTenorMonth = initialTenor,
                    isInsuranceSelected = initialInsuranceSelected
                )
            )
        )
    }

    LoanContent(
        uiState = uiState,
        onAmountChanged = { amount ->
            uiState = calculatePreviewState(uiState.copy(loanAmount = amount))
        },
        onTenorSelected = { tenor ->
            val validTenors = uiState.selectedPackage?.getTenorList().orEmpty()
            if (tenor in validTenors) {
                uiState = calculatePreviewState(uiState.copy(selectedTenorMonth = tenor))
            }
        },
        onInsuranceToggled = { selected ->
            uiState = calculatePreviewState(uiState.copy(isInsuranceSelected = selected))
        },
        onNextStep = {
            uiState = uiState.copy(currentStep = (uiState.currentStep + 1).coerceAtMost(3))
        }
    )
}

private enum class LoanSheetType {
    TENOR,
    BREAKDOWN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanContent(
    uiState: LoanUiState,
    onAmountChanged: (Long) -> Unit,
    onTenorSelected: (Int) -> Unit,
    onInsuranceToggled: (Boolean) -> Unit,
    onNextStep: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeSheet by remember { mutableStateOf<LoanSheetType?>(null) }

    val availableTenors = uiState.selectedPackage?.getTenorList().orEmpty().ifEmpty { listOf(6, 12, 18, 24) }
    val minAmount = uiState.selectedPackage?.minAmount ?: 6_000_000L
    val maxAmount = uiState.selectedPackage?.maxAmount ?: 100_000_000L
    val safeAmount = uiState.loanAmount.coerceIn(minAmount, maxAmount)
    val safeTenor = uiState.selectedTenorMonth.takeIf { it > 0 } ?: availableTenors.first()

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Checkbox(
                        checked = uiState.isInsuranceSelected,
                        onCheckedChange = { onInsuranceToggled(it) },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "Bảo hiểm người vay",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Red.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(18.dp)
                    )
                }
                Button(
                    onClick = onNextStep,
                    enabled = uiState.isInsuranceSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Text("Tiếp tục", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Stepper
            LoanStepper(currentStep = uiState.currentStep)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Chọn khoản vay mong muốn",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp,
                modifier = Modifier.padding(bottom = 28.dp)
            )

            LoanAmountSection(
                amount = safeAmount,
                minAmount = minAmount,
                maxAmount = maxAmount,
                onAmountChange = onAmountChanged
            )

            Spacer(modifier = Modifier.height(24.dp))

            TenorSelector(
                selectedTenor = safeTenor,
                onClick = { activeSheet = LoanSheetType.TENOR }
            )

            Spacer(modifier = Modifier.height(32.dp))

            LoanSummaryCard(
                state = uiState,
                onInfoClick = { activeSheet = LoanSheetType.BREAKDOWN }
            )
        }
    }

    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = sheetState,
            containerColor = Color.White,
            scrimColor = Color.Black.copy(alpha = 0.55f)
        ) {
            when (activeSheet) {
                LoanSheetType.TENOR -> {
                    TenorBottomSheetContent(
                        tenors = availableTenors,
                        selectedTenor = safeTenor,
                        onTenorSelected = {
                            onTenorSelected(it)
                            activeSheet = null
                        }
                    )
                }

                LoanSheetType.BREAKDOWN -> {
                    LoanBreakdownBottomSheet(
                        state = uiState,
                        onDismiss = { activeSheet = null }
                    )
                }

                null -> Unit
            }
        }
    }
}

@Composable
fun LoanStepper(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        StepItem(1, "Chọn khoản vay", currentStep >= 1)
        StepDivider(isDone = currentStep > 1)
        StepItem(2, "Điền thông tin", currentStep >= 2)
        StepDivider(isDone = currentStep > 2)
        StepItem(3, "Xác nhận", currentStep >= 3)
    }
}

@Composable
fun StepItem(step: Int, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isActive) MaterialTheme.colorScheme.primary else Color(0xFFD0D5DD)),
            contentAlignment = Alignment.Center
        ) {
            Text(step.toString(), color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = if (isActive) MaterialTheme.colorScheme.primary else Color(0xFF667085),
            lineHeight = 14.sp
        )
    }
}

@Composable
fun StepDivider(isDone: Boolean) {
    Box(
        modifier = Modifier
            .padding(top = 16.dp)
            .width(40.dp)
            .height(2.dp)
            .background(if (isDone) MaterialTheme.colorScheme.primary else Color(0xFFEAECF0))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanAmountSection(amount: Long, minAmount: Long, maxAmount: Long, onAmountChange: (Long) -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    var sliderSize by remember { mutableStateOf(IntSize.Zero) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .onSizeChanged { sliderSize = it }
        ) {
            val range = (maxAmount - minAmount).toFloat()
            val progress = if (range > 0) ((amount - minAmount).toFloat() / range).coerceIn(0f, 1f) else 0f

            val bubbleWidth = 68.dp
            val bubbleOffset = with(density) {
                val bubbleWidthPx = bubbleWidth.toPx()
                val travelPx = (sliderSize.width - bubbleWidthPx).coerceAtLeast(0f)
                (travelPx * progress).toDp()
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(bubbleWidth)
                    .offset(x = bubbleOffset)
            ) {
                Surface(
                    color = primaryColor,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        formatCompactAmount(amount),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Canvas(modifier = Modifier.size(10.dp, 6.dp)) {
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2, size.height)
                        close()
                    }
                    drawPath(path, color = primaryColor)
                }
            }

            Slider(
                value = amount.toFloat(),
                onValueChange = {
                    val snapped = ((it / 100_000f).toInt() * 100_000L).coerceIn(minAmount, maxAmount)
                    onAmountChange(snapped)
                },
                valueRange = minAmount.toFloat()..maxAmount.toFloat(),
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = primaryColor,
                    activeTrackColor = primaryColor,
                    inactiveTrackColor = Color(0xFFEAECF0)
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(4.dp),
                        colors = SliderDefaults.colors(
                            activeTrackColor = primaryColor,
                            inactiveTrackColor = Color(0xFFEAECF0)
                        ),
                        drawStopIndicator = null,
                        thumbTrackGapSize = 0.dp
                    )
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatCompactAmount(minAmount), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(formatCompactAmount(maxAmount), fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
fun TenorSelector(selectedTenor: Int, onClick: () -> Unit) {
    OutlinedSelector(
        label = "Chọn kỳ hạn vay",
        value = "$selectedTenor tháng",
        onClick = onClick
    )
}

@Composable
fun OutlinedSelector(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }.padding(vertical = 8.dp)) {
        Text(label, fontSize = 14.sp, color = Color(0xFF667085))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF667085))
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(thickness = 1.dp, color = Color(0xFFEAECF0))
    }
}

@Composable
fun LoanSummaryCard(state: LoanUiState, onInfoClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SummaryRow("Số tiền thực nhận", formatCurrency(state.actualReceivedAmount))
            SummaryRow("Tiền trả hàng tháng", formatCurrency(state.monthlyPayment))
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = Color(0xFFEAECF0))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tổng tiền phải trả", color = Color(0xFF667085), fontSize = 14.sp)
                        IconButton(
                            onClick = onInfoClick,
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Chi tiết tổng tiền phải trả",
                                tint = Color(0xFFFDB022),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text("(tạm tính)", color = Color(0xFF667085), fontSize = 12.sp)
                }
                Text(
                    formatCurrency(state.totalPayment),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF667085), fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 16.sp)
    }
}

@Composable
fun TenorBottomSheetContent(
    tenors: List<Int>,
    selectedTenor: Int,
    onTenorSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Kỳ hạn vay",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        HorizontalDivider()
        
        tenors.forEach { tenor ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTenorSelected(tenor) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$tenor tháng", fontSize = 16.sp)
                if (tenor == selectedTenor) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    RadioButton(
                        selected = false,
                        onClick = { onTenorSelected(tenor) },
                        colors = RadioButtonDefaults.colors(unselectedColor = Color(0xFFB3B3B3))
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFEAECF0))
        }
    }
}

@Composable
fun LoanBreakdownBottomSheet(state: LoanUiState, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Tổng tiền tạm tính",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )
        HorizontalDivider(color = Color(0xFFEAECF0))

        Column(modifier = Modifier.padding(20.dp)) {
            SummaryRow(label = "Số tiền thực nhận", value = formatCurrency(state.actualReceivedAmount))
            if (state.insuranceFee > 0) {
                SummaryRow(label = "Phí bảo hiểm", value = formatCurrency(state.insuranceFee))
            }
            SummaryRow(label = "Tiền lãi", value = formatCurrency(state.interestAmount))

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                thickness = 1.dp,
                color = Color(0xFFEAECF0)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Tổng tiền phải trả\n(tạm tính)",
                    color = Color(0xFF667085),
                    fontSize = 16.sp,
                    lineHeight = 20.sp
                )
                Text(
                    text = formatCurrency(state.totalPayment),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 12.dp)
            ) {
                Text("Đóng")
            }
        }
    }
}

fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)}đ"
}

fun formatCompactAmount(amount: Long): String = "${amount / 1_000_000}tr"

private const val PREVIEW_INSURANCE_RATE = 0.01

private fun calculatePreviewState(state: LoanUiState): LoanUiState {
    val pkg = state.selectedPackage ?: return state
    val tenor = state.selectedTenorMonth.coerceAtLeast(1)
    val amount = state.loanAmount.coerceIn(pkg.minAmount, pkg.maxAmount)
    val insuranceFee = if (state.isInsuranceSelected) (amount * PREVIEW_INSURANCE_RATE).toLong() else 0L
    val interestAmount = (amount * (pkg.interest / 100.0) * tenor / 12.0).toLong()
    val totalPayment = amount + insuranceFee + interestAmount
    val monthlyPayment = (totalPayment / tenor.toDouble()).toLong()
    val actualReceived = (amount - insuranceFee).coerceAtLeast(0L)

    return state.copy(
        loanAmount = amount,
        insuranceFee = insuranceFee,
        interestAmount = interestAmount,
        monthlyPayment = monthlyPayment,
        totalPayment = totalPayment,
        actualReceivedAmount = actualReceived
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoanScreenPreview() {
    val mockPackage = LoanPackage(
        id = "1",
        packageName = "Vay Nhanh",
        tenorRange = "6,12,18,24",
        minAmount = 6_000_000,
        maxAmount = 100_000_000,
        interest = 12.0,
        overdueCost = 5.0,
        eligibleCreditScore = 600
    )

    EasyMoneyTheme {
        LoanScreenMock(
            mockPackage = mockPackage,
            initialAmount = 70_000_000,
            initialTenor = 6,
            initialInsuranceSelected = true
        )
    }
}
