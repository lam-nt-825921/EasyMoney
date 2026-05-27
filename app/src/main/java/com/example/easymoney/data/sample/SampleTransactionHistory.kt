package com.example.easymoney.data.sample

import com.example.easymoney.domain.model.TransactionGroup
import com.example.easymoney.domain.model.TransactionItem

val SAMPLE_TRANSACTION_HISTORY: List<TransactionGroup> = listOf(
    TransactionGroup(
        date = "01/04/2026",
        items = listOf(
            TransactionItem("Nhận tiền từ ngân hàng", "2604750502176432", 41_652L, 132_376L, "07:45"),
            TransactionItem("GD thanh toán điện tử", "2604750502914339", -14_181L, 90_724L, "07:31"),
            TransactionItem("GD thanh toán điện tử", "2604750502680030", -50_000L, 104_905L, "06:31"),
            TransactionItem("Thanh toán EASY MONEY", "843397835046260475", 1_500L, 154_905L, "06:30")
        )
    ),
    TransactionGroup(
        date = "31/03/2026",
        items = listOf(
            TransactionItem("GD thanh toán điện tử", "2604750597686372", -19_868L, 153_405L, "13:44"),
            TransactionItem("GD thanh toán điện tử", "2604750695990866", -23_803L, 173_273L, "13:44"),
            TransactionItem("Nhận tiền chuyển khoản", "2604750695990977", 15_000L, 197_076L, "09:12")
        )
    ),
    TransactionGroup(
        date = "30/03/2026",
        items = listOf(
            TransactionItem("Giải ngân khoản vay", "2604750295880123", 5_000_000L, 212_076L, "14:00"),
            TransactionItem("Phí dịch vụ tháng 3", "2604750295880124", -12_000L, 207_076L, "10:30")
        )
    )
)
