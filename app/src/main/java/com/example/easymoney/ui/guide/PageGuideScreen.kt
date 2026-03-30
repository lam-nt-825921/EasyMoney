package com.example.easymoney.ui.guide

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.easymoney.navigation.AppDestination

@Composable
fun PageGuideScreen(
    xmlName: String?
) {
    val context = LocalContext.current
    val resolvedXml = rememberResolvedXmlName(
        rawXmlName = xmlName,
        packageName = context.packageName
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Khung hướng dẫn trang",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Màn hình này là khung xem hướng dẫn.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "File XML đang gắn: ${resolvedXml.displayName}.xml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Nếu không chỉ định XML hoặc XML không tồn tại, hệ thống tự dùng file mặc định.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class ResolvedXml(
    val displayName: String,
    val resourceId: Int
)

@Composable
private fun rememberResolvedXmlName(
    rawXmlName: String?,
    packageName: String
): ResolvedXml {
    val context = LocalContext.current
    val requested = normalizeXmlName(rawXmlName)
    val requestedId = resolveXmlResourceId(
        xmlName = requested,
        packageName = packageName,
        context = context
    )

    if (requestedId != 0) {
        return ResolvedXml(displayName = requested, resourceId = requestedId)
    }

    val defaultId = resolveXmlResourceId(
        xmlName = AppDestination.PageGuide.DEFAULT_XML_NAME,
        packageName = packageName,
        context = context
    )

    return ResolvedXml(
        displayName = AppDestination.PageGuide.DEFAULT_XML_NAME,
        resourceId = defaultId
    )
}

private fun normalizeXmlName(rawXmlName: String?): String {
    if (rawXmlName.isNullOrBlank()) return ""
    return rawXmlName.trim().removeSuffix(".xml")
}

private fun resolveXmlResourceId(
    xmlName: String,
    packageName: String,
    context: android.content.Context
): Int {
    if (xmlName.isBlank()) return 0
    @Suppress("DiscouragedApi")
    return context.resources.getIdentifier(xmlName, "xml", packageName)
}



