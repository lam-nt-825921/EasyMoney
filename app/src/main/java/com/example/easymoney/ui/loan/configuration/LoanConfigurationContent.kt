package com.example.easymoney.ui.loan.configuration

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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easymoney.ui.loan.LoanUiState
import com.example.easymoney.ui.loan.components.LoanBottomButton
import com.example.easymoney.ui.loan.formatCompactAmount
import com.example.easymoney.ui.loan.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanConfigurationContent(
    uiState: LoanUiState,
    onAmountChanged: (Long) -> Unit,
    onTenorSelected: (Int) -> Unit,
    onInsuranceToggled: (Boolean) -> Unit,
    onNextStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeSheet by remember { mutableStateOf<LoanSheetType?>(null) }

    val availableTenors = uiState.selectedPackage?.getTenorList().orEmpty().ifEmpty { listOf(6, 12, 18, 24) }
    val minAmount = uiState.selectedPackage?.minAmount ?: 6_000_000L
    val maxAmount = uiState.selectedPackage?.maxAmount ?: 100_000_000L
    val safeAmount = uiState.loanAmount.coerceIn(minAmount, maxAmount)
    val safeTenor = uiState.selectedTenorMonth.takeIf { it > 0 } ?: availableTenors.first()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            LoanBottomButton(
                isInsuranceSelected = uiState.isInsuranceSelected,
                onInsuranceToggled = onInsuranceToggled,
                onNextClick = onNextStep
            )
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

private enum class LoanSheetType { TENOR, BREAKDOWN }

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
private fun StepItem(step: Int, label: String, isActive: Boolean) {
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
private fun StepDivider(isDone: Boolean) {
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
private fun OutlinedSelector(label: String, value: String, onClick: () -> Unit) {
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
private fun SummaryRow(label: String, value: String) {
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
