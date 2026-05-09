package com.example.easymoney.ui.common.identity

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Shared Module for Reading Chip-based Identity Cards (CCCD) via NFC.
 * Note: Actual CCCD reading requires PACE/BAC protocols (often via external SDKs or JMRTD).
 */
@Composable
fun NfcReaderModule(
    onResult: (NfcResult) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    var status by remember { mutableStateOf("Đang chờ thẻ...") }

    if (nfcAdapter == null) {
        status = "Thiết bị không hỗ trợ NFC"
    } else if (!nfcAdapter.isEnabled) {
        status = "Vui lòng bật NFC trong cài đặt"
    }

    DisposableEffect(Unit) {
        val callback = NfcAdapter.ReaderCallback { tag: Tag ->
            val isoDep = IsoDep.get(tag)
            if (isoDep != null) {
                status = "Đã tìm thấy thẻ, đang đọc dữ liệu..."
                try {
                    isoDep.connect()
                    // Here we would perform the PACE/BAC handshake and read EF.COM, EF.SOD, EF.DG1, etc.
                    // This is a complex process often requiring a library like JMRTD.
                    
                    // Mock success result
                    onResult(NfcResult(rawData = "MOCK_CHIP_DATA", isSuccess = true))
                } catch (e: Exception) {
                    status = "Lỗi khi đọc thẻ: ${e.message}"
                } finally {
                    isoDep.close()
                }
            }
        }

        nfcAdapter?.enableReaderMode(
            context as android.app.Activity,
            callback,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )

        onDispose {
            nfcAdapter?.disableReaderMode(context as android.app.Activity)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = status, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    }
}
