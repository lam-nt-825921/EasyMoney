package com.example.easymoney.ui.onboarding

import com.example.easymoney.ui.theme.LocalDarkMode
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.easymoney.R
import com.example.easymoney.ui.common.loading.InlineButtonLoading
import com.example.easymoney.ui.common.loading.SkeletonBlock
import com.example.easymoney.ui.theme.EasyMoneyTheme
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.viewinterop.AndroidView
import com.example.easymoney.domain.model.LoanPackageModel
import com.example.easymoney.domain.model.LoanProviderInfoModel
import com.example.easymoney.ui.loan.formatCurrency

@Composable
fun OnboardingScreen(
	onContinueClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val viewModel: OnboardingViewModel = hiltViewModel()
	val uiState by viewModel.uiState.collectAsState()
	val isLoading = uiState.isLoading
	var isTermsAccepted by remember { mutableStateOf(true) }

	Scaffold(
		modifier = modifier.fillMaxSize(),
		containerColor = MaterialTheme.colorScheme.background,
		bottomBar = {
			OnboardingBottomSection(
				isTermsAccepted = isTermsAccepted,
				isLoading = isLoading,
				onTermsChanged = { isTermsAccepted = it },
				onContinueClick = onContinueClick
			)
		}
	) { innerPadding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(innerPadding)
				.padding(horizontal = 16.dp, vertical = 12.dp),
			verticalArrangement = Arrangement.spacedBy(14.dp)
		) {
			if (isLoading) {
				OnboardingLoadingContent()
			} else {
				HeroSection()
				WhyChooseSection()
				ProductInfoSection(productInfo = uiState.productInfo)
				ProviderInfoSection(providerInfo = uiState.providerInfo)
			}
			Spacer(modifier = Modifier.height(8.dp))
		}
	}
}

@Composable
private fun OnboardingLoadingContent() {
	Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
		SkeletonBlock(height = 150.dp, cornerRadius = 12.dp)
		SkeletonBlock(modifier = Modifier.fillMaxWidth(0.55f), height = 22.dp)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			SkeletonBlock(modifier = Modifier.weight(1f), height = 108.dp, cornerRadius = 12.dp)
			SkeletonBlock(modifier = Modifier.weight(1f), height = 108.dp, cornerRadius = 12.dp)
			SkeletonBlock(modifier = Modifier.weight(1f), height = 108.dp, cornerRadius = 12.dp)
		}
		SkeletonBlock(modifier = Modifier.fillMaxWidth(0.5f), height = 22.dp)
		SkeletonBlock(height = 110.dp, cornerRadius = 12.dp)
		SkeletonBlock(modifier = Modifier.fillMaxWidth(0.5f), height = 22.dp)
		SkeletonBlock(height = 160.dp, cornerRadius = 12.dp)
	}
}


@Composable
private fun HeroSection() {
	val isDarkMode = LocalDarkMode.current
	val illustrationRes = if (isDarkMode) R.drawable.part_dark else R.drawable.part_light

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background),
		contentAlignment = Alignment.Center
	) {
		Image(
			painter = painterResource(id = illustrationRes),
			contentDescription = stringResource(id = R.string.onboarding_hero_content_desc),
			modifier = Modifier
				.fillMaxWidth()
				.height(150.dp),
			contentScale = ContentScale.Fit
		)
	}
}

@Composable
private fun WhyChooseSection() {
	Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
		Text(
			text = stringResource(id = R.string.onboarding_reason_title),
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold
		)

		Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
			ReasonItem(
				iconRes = R.drawable.ic_feature_1_hand_money,
				textRes = R.string.onboarding_reason_fast,
				modifier = Modifier.weight(1f)
			)
			ReasonItem(
				iconRes = R.drawable.ic_feature_2_procedure,
				textRes = R.string.onboarding_reason_simple,
				modifier = Modifier.weight(1f)
			)
			ReasonItem(
				iconRes = R.drawable.ic_feature_3_trust,
				textRes = R.string.onboarding_reason_reliable,
				modifier = Modifier.weight(1f)
			)
		}
	}
}

@Composable
private fun ReasonItem(
	iconRes: Int,
	textRes: Int,
	modifier: Modifier = Modifier
) {
	Card(
		modifier = modifier,
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
		shape = RoundedCornerShape(10.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 8.dp, vertical = 10.dp),
			horizontalAlignment = Alignment.Start,
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Image(
				painter = painterResource(id = iconRes),
				contentDescription = null,
				modifier = Modifier.size(30.dp)
			)
			Text(
				text = stringResource(id = textRes),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}

@Composable
private fun ProductInfoSection(productInfo: LoanPackageModel?) {
	val maxAmountText = productInfo?.maxAmount?.let { formatCurrency(it) }
		?: stringResource(id = R.string.onboarding_product_limit_value)
	val maxTenor = productInfo?.getTenorList()?.maxOrNull()
	val tenorText = maxTenor?.let { "toi $it thang" }
		?: stringResource(id = R.string.onboarding_product_term_value)
	val interestText = productInfo?.interest?.let { "$it%/nam" }
		?: stringResource(id = R.string.onboarding_product_interest_value)

	Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
		Text(
			text = stringResource(id = R.string.onboarding_product_title),
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold
		)

		Card(
			modifier = Modifier.fillMaxWidth(),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
			shape = RoundedCornerShape(12.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					InfoLine(
						label = stringResource(id = R.string.onboarding_product_limit_label),
						value = maxAmountText
					)
					InfoLine(
						label = stringResource(id = R.string.onboarding_product_term_label),
						value = tenorText
					)
					InfoLine(
						label = stringResource(id = R.string.onboarding_product_interest_label),
						value = interestText
					)
				}

				Spacer(modifier = Modifier.width(12.dp))
				Image(
					painter = painterResource(id = R.drawable.img_4),
					contentDescription = null,
					modifier = Modifier
						.width(86.dp)
						.height(56.dp),
					contentScale = ContentScale.Fit
				)
			}
		}
	}
}

@Composable
private fun InfoLine(label: String, value: String) {
	Row {
		Text(
			text = label,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
		)
		Text(
			text = value,
			style = MaterialTheme.typography.bodySmall,
			fontWeight = FontWeight.Bold
		)
	}
}

@Composable
private fun ProviderInfoSection(providerInfo: LoanProviderInfoModel?) {
	val organizationName = providerInfo?.organizationName
		?: stringResource(id = R.string.onboarding_provider_org_value)
	val hotline = providerInfo?.hotline
		?: stringResource(id = R.string.onboarding_provider_business_line)
	val address = providerInfo?.address
		?: stringResource(id = R.string.onboarding_provider_address_value)

	Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
		Text(
			text = stringResource(id = R.string.onboarding_provider_title),
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold
		)

		Card(
			modifier = Modifier.fillMaxWidth(),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
			shape = RoundedCornerShape(12.dp)
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp),
				verticalArrangement = Arrangement.spacedBy(6.dp)
			) {
				Text(
					text = stringResource(id = R.string.onboarding_provider_org_label),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)
				Text(
					text = organizationName,
					style = MaterialTheme.typography.bodySmall,
					fontWeight = FontWeight.Bold
				)

				Text(
					text = stringResource(id = R.string.onboarding_provider_tax_line),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)

				Text(
					text = hotline,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)

				Text(
					text = stringResource(id = R.string.onboarding_provider_address_label),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)
				Text(
					text = address,
					style = MaterialTheme.typography.bodySmall,
					fontWeight = FontWeight.Bold
				)
			}
		}
	}
}

@Composable
private fun OnboardingBottomSection(
	isTermsAccepted: Boolean,
	isLoading: Boolean,
	onTermsChanged: (Boolean) -> Unit,
	onContinueClick: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
			.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		Row(verticalAlignment = Alignment.Top) {
			Checkbox(
				checked = isTermsAccepted,
				onCheckedChange = { if (!isLoading) onTermsChanged(it) },
				enabled = !isLoading,
				colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
			)
			Text(
				text = stringResource(id = R.string.onboarding_terms_text),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
				modifier = Modifier.padding(top = 12.dp)
			)
		}

		Button(
			onClick = onContinueClick,
			enabled = isTermsAccepted && !isLoading,
			modifier = Modifier
				.fillMaxWidth()
				.height(52.dp),
			shape = RoundedCornerShape(26.dp),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
			)
		) {
			if (isLoading) {
				InlineButtonLoading(label = "Dang tai")
			} else {
				Text(
					text = stringResource(id = R.string.onboarding_continue),
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Medium
				)
			}
		}
	}
}

@Preview(showBackground = true, showSystemUi = true, name = "Onboarding Light")
@Composable
private fun OnboardingScreenPreview() {
	EasyMoneyTheme(darkTheme = false) {
		OnboardingScreen(onContinueClick = {})
	}
}

@Preview(showBackground = true, showSystemUi = true, name = "Onboarding Dark")
@Composable
private fun OnboardingScreenDarkPreview() {
	EasyMoneyTheme(darkTheme = true) {
		OnboardingScreen(onContinueClick = {})
	}
}



