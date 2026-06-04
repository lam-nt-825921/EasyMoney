package com.example.easymoney.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easymoney.R
import com.example.easymoney.ui.common.loading.SkeletonBlock
import com.example.easymoney.ui.theme.LocalHomeColors

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.easymoney.domain.model.Banner
import com.example.easymoney.domain.model.LoanProduct
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.animation.core.tween

/**
 * Dynamic Banner Carousel with Auto-scroll
 */
@Composable
fun BannerCarousel(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit
) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll logic: 5s interval with 1s smooth transition
    // Fixed glitch: Changed key to 'banners' to prevent cancellation mid-animation
    LaunchedEffect(key1 = banners) {
        if (banners.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(5000)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 1000)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val banner = banners[page]
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onBannerClick(banner) }
            ) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Gradient Overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 300f
                            )
                        )
                )

                Text(
                    text = banner.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 32.dp, end = 20.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Pager Indicator
        Row(
            Modifier
                .height(24.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(banners.size) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val width = if (isSelected) 24.dp else 8.dp
                val color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

/**
 * Section for Points and Rewards
 */
@Composable
fun RewardsSection(
    points: Int,
    onRedeemClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.home_reward_points),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "$points ${stringResource(id = R.string.common_points_unit)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(
                onClick = onRedeemClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.home_redeem_gift))
            }
        }
    }
}

/**
 * Section for Hot Loan Promotions
 */
@Composable
fun HotLoansSection(
    loans: List<LoanProduct>,
    onLoanClick: (LoanProduct) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.home_hot_loan_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            TextButton(onClick = onSeeAllClick) {
                Text(
                    text = "Tất cả", // Should be in strings.xml
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp)
        ) {
            items(loans) { loan ->
                HotLoanCard(loan = loan, onClick = { onLoanClick(loan) })
            }
        }
    }
}

@Composable
private fun HotLoanCard(
    loan: LoanProduct,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (loan.badge != null) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = loan.badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = loan.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.home_loan_interest_rate, loan.interestRate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(id = R.string.home_loan_max_amount, formatAmount(loan.maxAmount)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatAmount(amount: Long): String = "%,dđ".format(amount).replace(',', '.')

/**
 * Alert box for incomplete eKYC
 */
@Composable
fun EKycAlertSection(
    message: String,
    onVerifyClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.home_ekyc_alert_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onVerifyClick) {
                Text(
                    text = stringResource(id = R.string.home_ekyc_alert_button),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Enum representing main features on the Home Screen.
 * Logic for colors is now handled via HomeTheme and CompositionLocal.
 */
enum class HomeFeature(
    val titleRes: Int,
    val iconRes: Int
) {
    MANAGE_LOAN(R.string.home_manage_loan, R.drawable.quanlykhoanvay),
    SUGGEST_LOAN(R.string.home_suggest_loan, R.drawable.goiykhoanvay),
    CONSULT_LOAN(R.string.home_consult_loan, R.drawable.tuvankhoanvay)
}

@Composable
fun HomeLoadingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SkeletonBlock(height = 180.dp, cornerRadius = 24.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonBlock(modifier = Modifier.weight(1f), height = 120.dp, cornerRadius = 20.dp)
            SkeletonBlock(modifier = Modifier.weight(1f), height = 120.dp, cornerRadius = 20.dp)
        }

        SkeletonBlock(height = 140.dp, cornerRadius = 24.dp)
    }
}

@Composable
fun MainBanner(
    title: String,
    description: String?,
    onRegistrationClick: () -> Unit
) {
    val homeColors = LocalHomeColors.current
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clickable { onRegistrationClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(homeColors.mainBannerGradient)
        ) {
            // Illustration with safe padding
            Image(
                painter = painterResource(id = R.drawable.vaytochuctindung),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.38f)
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, top = 20.dp, bottom = 20.dp),
                contentScale = ContentScale.Fit
            )
            
            // Text Content on the left
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.65f)
                    .padding(start = 24.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = homeColors.mainBannerTitle,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = homeColors.mainBannerTitle.copy(alpha = 0.78f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onRegistrationClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.primary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.home_main_banner_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GridSection(
    onManageLoanClick: () -> Unit,
    onSuggestLoanClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FeatureItem(
            feature = HomeFeature.MANAGE_LOAN,
            onClick = onManageLoanClick,
            modifier = Modifier.weight(1f)
        )
        FeatureItem(
            feature = HomeFeature.SUGGEST_LOAN,
            onClick = onSuggestLoanClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WideBanner(onClick: () -> Unit) {
    FeatureItem(
        feature = HomeFeature.CONSULT_LOAN,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(135.dp)
    )
}

@Composable
private fun FeatureItem(
    feature: HomeFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeColors = LocalHomeColors.current
    
    val (gradient, titleColor) = when(feature) {
        HomeFeature.MANAGE_LOAN -> homeColors.manageLoanGradient to homeColors.manageLoanTitle
        HomeFeature.SUGGEST_LOAN -> homeColors.suggestLoanGradient to homeColors.suggestLoanTitle
        HomeFeature.CONSULT_LOAN -> homeColors.consultLoanGradient to homeColors.consultLoanTitle
    }

    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text Content
                Text(
                    text = stringResource(id = feature.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = titleColor,
                    modifier = Modifier.weight(1.2f)
                )
                
                // Illustration
                Image(
                    painter = painterResource(id = feature.iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight()
                        .padding(vertical = 16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
