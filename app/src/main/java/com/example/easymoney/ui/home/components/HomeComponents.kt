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
fun MainBanner(onRegistrationClick: () -> Unit) {
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
                    text = stringResource(id = R.string.home_main_banner_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = homeColors.mainBannerTitle,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
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
fun GridSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FeatureItem(
            feature = HomeFeature.MANAGE_LOAN,
            modifier = Modifier.weight(1f)
        )
        FeatureItem(
            feature = HomeFeature.SUGGEST_LOAN,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WideBanner() {
    FeatureItem(
        feature = HomeFeature.CONSULT_LOAN,
        modifier = Modifier
            .fillMaxWidth()
            .height(135.dp)
    )
}

@Composable
private fun FeatureItem(
    feature: HomeFeature,
    modifier: Modifier = Modifier
) {
    val homeColors = LocalHomeColors.current
    
    val (gradient, titleColor) = when(feature) {
        HomeFeature.MANAGE_LOAN -> homeColors.manageLoanGradient to homeColors.manageLoanTitle
        HomeFeature.SUGGEST_LOAN -> homeColors.suggestLoanGradient to homeColors.suggestLoanTitle
        HomeFeature.CONSULT_LOAN -> homeColors.consultLoanGradient to homeColors.consultLoanTitle
    }

    Card(
        modifier = modifier.height(120.dp),
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
