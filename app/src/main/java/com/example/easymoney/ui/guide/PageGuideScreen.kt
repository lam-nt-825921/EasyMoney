package com.example.easymoney.ui.guide

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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

    // 1. LẤY MÀU CHỮ TỪ THEME COMPOSE (Tự động đổi Đen/Trắng theo EasyMoneyTheme)
    val composeTextColor = MaterialTheme.colorScheme.onSurface.toArgb()

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
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground // Ép màu chữ Compose
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            // Dùng surface để Card lấy màu Trắng (Light) hoặc Xám đậm (Dark) từ Theme của bạn
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        LayoutInflater.from(ctx).inflate(
                            resolvedXml.resourceId,
                            null,
                            false
                        )
                    },
                    update = { view ->
                        // 2. LỘT BỎ NỀN CỦA XML ĐỂ LỘ NỀN CỦA COMPONENT CARD
                        view.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                        // 3. HÀM ĐỆ QUY: TÌM VÀ ÉP MÀU TOÀN BỘ TEXTVIEW
                        fun applyComposeThemeToXml(v: View) {
                            if (v is TextView) {
                                v.setTextColor(composeTextColor)
                            } else if (v is ViewGroup) {
                                for (i in 0 until v.childCount) {
                                    applyComposeThemeToXml(v.getChildAt(i))
                                }
                            }
                        }

                        // Kích hoạt hàm đệ quy cho toàn bộ cấu trúc file XML
                        applyComposeThemeToXml(view)
                    }
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
    context: Context
): Int {
    if (xmlName.isBlank()) return 0
    @Suppress("DiscouragedApi")
    return context.resources.getIdentifier(xmlName, "layout", packageName)
}