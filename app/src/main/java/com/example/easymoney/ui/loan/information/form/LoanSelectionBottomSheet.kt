package com.example.easymoney.ui.loan.information.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easymoney.domain.model.MasterDataItem

/**
 * Template 1: Chọn thông thường (Nghề nghiệp, Hôn nhân, ...)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSelectionBottomSheet(
    title: String,
    items: List<MasterDataItem>,
    selectedId: String?,
    onItemSelected: (MasterDataItem) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                items(items) { item ->
                    SelectionRow(
                        label = item.name,
                        isSelected = item.id == selectedId,
                        onClick = { onItemSelected(item) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

/**
 * Template 2: Chọn có phân cấp & tìm kiếm (Tỉnh/Thành -> Quận/Huyện -> Phường/Xã)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HierarchicalSelectionBottomSheet(
    title: String,
    items: List<MasterDataItem>,
    selectedId: String?,
    onItemSelected: (MasterDataItem) -> Unit,
    onBack: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    searchPlaceholder: String = "Tìm kiếm..."
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = items.filter { it.name.contains(searchQuery, ignoreCase = true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            // Header với nút Back rõ ràng
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary // Sử dụng màu Primary để nổi bật nút điều hướng
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Thanh tìm kiếm chuẩn Material 3
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(searchPlaceholder) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                }
            )

            // Danh sách kết quả
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                items(filteredItems) { item ->
                    SelectionRow(
                        label = item.name,
                        isSelected = item.id == selectedId,
                        onClick = { onItemSelected(item) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        } else {
            RadioButton(
                selected = false,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    unselectedColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}
