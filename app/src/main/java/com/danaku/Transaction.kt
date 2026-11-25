package com.danaku

import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val categoryId: String,
    val dateMillis: Long = System.currentTimeMillis()
)
