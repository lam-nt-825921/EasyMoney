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
import androidx.compose.ui.res.stringResource
import com.example.easymoney.R
import com.example.easymoney.navigation.AppDestination

import androidx.compose.ui.unit.sp
import android.util.TypedValue
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun PageGuideScreen(
    xmlName: String?,
    title: String? = null
) {
    val context = LocalContext.current
    val resolvedXml = rememberResolvedXmlName(
        rawXmlName = xmlName,
        packageName = context.packageName
    )

    // 1. LẤY MÀU CHỮ TỪ THEME COMPOSE
    val composeTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    
    // TIÊU CHUẨN MOBILE: Cỡ chữ tối thiểu cho nội dung nên là 14sp-16sp, tiêu đề 18sp-22sp
    val minContentTextSizeSp = 16f // sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title ?: stringResource(id = R.string.guide_title_default),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        view.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                        fun applyMobileStandardsToXml(v: View) {
                            if (v is TextView) {
                                v.setTextColor(composeTextColor)
                                // Ép cỡ chữ tối thiểu nếu nhỏ hơn chuẩn
                                if (v.textSize < minContentTextSizeSp * context.resources.displayMetrics.scaledDensity) {
                                    v.setTextSize(TypedValue.COMPLEX_UNIT_SP, minContentTextSizeSp)
                                }
                                // Tăng line height cho dễ đọc trên mobile
                                v.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, context.resources.displayMetrics), 1.0f)
                            } else if (v is ViewGroup) {
                                for (i in 0 until v.childCount) {
                                    applyMobileStandardsToXml(v.getChildAt(i))
                                }
                            }
                        }

                        applyMobileStandardsToXml(view)
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
