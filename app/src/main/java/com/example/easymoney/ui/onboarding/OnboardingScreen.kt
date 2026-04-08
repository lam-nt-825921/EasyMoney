package com.example.easymoney.ui.onboarding

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.easymoney.R
import com.example.easymoney.ui.theme.EasyMoneyTheme

@Composable
fun OnboardingScreen(
	onContinueClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	var isTermsAccepted by remember { mutableStateOf(true) }

	Scaffold(
		modifier = modifier.fillMaxSize(),
		containerColor = MaterialTheme.colorScheme.background,
		bottomBar = {
			OnboardingBottomSection(
				isTermsAccepted = isTermsAccepted,
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
			HeroSection()
			WhyChooseSection()
			ProductInfoSection()
			ProviderInfoSection()
			Spacer(modifier = Modifier.height(8.dp))
		}
	}
}

@Composable
private fun HeroSection() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background),
		contentAlignment = Alignment.Center
	) {
		Image(
			painter = painterResource(id = R.drawable.img),
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
				iconRes = R.drawable.img_1,
				textRes = R.string.onboarding_reason_fast,
				modifier = Modifier.weight(1f)
			)
			ReasonItem(
				iconRes = R.drawable.img_2,
				textRes = R.string.onboarding_reason_simple,
				modifier = Modifier.weight(1f)
			)
			ReasonItem(
				iconRes = R.drawable.img_3,
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
private fun ProductInfoSection() {
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
						value = stringResource(id = R.string.onboarding_product_limit_value)
					)
					InfoLine(
						label = stringResource(id = R.string.onboarding_product_term_label),
						value = stringResource(id = R.string.onboarding_product_term_value)
					)
					InfoLine(
						label = stringResource(id = R.string.onboarding_product_interest_label),
						value = stringResource(id = R.string.onboarding_product_interest_value)
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
private fun ProviderInfoSection() {
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
					text = stringResource(id = R.string.onboarding_provider_org_value),
					style = MaterialTheme.typography.bodySmall,
					fontWeight = FontWeight.Bold
				)

				Text(
					text = stringResource(id = R.string.onboarding_provider_tax_line),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)

				Text(
					text = stringResource(id = R.string.onboarding_provider_business_line),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)

				Text(
					text = stringResource(id = R.string.onboarding_provider_address_label),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)
				Text(
					text = stringResource(id = R.string.onboarding_provider_address_value),
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
				onCheckedChange = onTermsChanged,
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
			enabled = isTermsAccepted,
			modifier = Modifier
				.fillMaxWidth()
				.height(52.dp),
			shape = RoundedCornerShape(26.dp),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
			)
		) {
			Text(
				text = stringResource(id = R.string.onboarding_continue),
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Medium
			)
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



