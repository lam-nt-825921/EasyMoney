package com.example.easymoney.data.sample

import com.example.easymoney.domain.model.RewardCatalogItem

/**
 * Sample data dùng cho MOCK mode. Workflow #7 — moved from `RewardViewModel`.
 */
val SAMPLE_REWARD_CATALOG: List<RewardCatalogItem> = listOf(
    RewardCatalogItem(
        id = "1",
        title = "Voucher giảm 1% lãi suất",
        points = 500,
        description = "Áp dụng cho khoản vay tiêu dùng",
        category = "Tài chính"
    ),
    RewardCatalogItem(
        id = "2",
        title = "Voucher giảm 50k phí dịch vụ",
        points = 200,
        description = "Áp dụng cho mọi khoản vay",
        category = "Tài chính"
    ),
    RewardCatalogItem(
        id = "3",
        title = "Thẻ cào 50k",
        points = 500,
        description = "Mã thẻ Viettel/Vinaphone",
        category = "Viễn thông"
    ),
    RewardCatalogItem(
        id = "4",
        title = "Hoàn tiền 100k",
        points = 1000,
        description = "Cộng trực tiếp vào số dư ví",
        category = "Cashback"
    )
)
