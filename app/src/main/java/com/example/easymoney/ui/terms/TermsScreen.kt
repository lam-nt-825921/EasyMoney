package com.example.easymoney.ui.terms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.easymoney.R

@Composable
fun TermsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.terms_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.terms_body_placeholder),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
