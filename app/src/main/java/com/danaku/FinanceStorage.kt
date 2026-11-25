package com.danaku

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FinanceStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "danaku_prefs"
        private const val CATEGORIES_NAME = "categories_json"
        private const val TRANSACTIONS_NAME = "transactions_json"

        fun categoriesToJson(categories: List<Category>, gson: Gson = Gson()): String {
            return gson.toJson(categories)
        }

        fun categoriesFromJson(json: String?, gson: Gson = Gson()): List<Category> {
            if (json.isNullOrBlank()) return emptyList()
            val type = object : TypeToken<List<Category>>() {}.type
            return gson.fromJson(json, type) ?: emptyList()
        }

        fun transactionsToJson(transactions: List<Transaction>, gson: Gson = Gson()): String {
            return gson.toJson(transactions)
        }

        fun transactionsFromJson(json: String?, gson: Gson = Gson()): List<Transaction> {
            if (json.isNullOrBlank()) return emptyList()
            val type = object : TypeToken<List<Transaction>>() {}.type
            return gson.fromJson(json, type) ?: emptyList()
        }
    }

    fun getCategories(): List<Category> {
        val json = prefs.getString(CATEGORIES_NAME, null)
        return categoriesFromJson(json, gson)
    }

    fun getCategoriesByType(type: TransactionType): List<Category> {
        return getCategories().filter { it.type == type }
    }

    fun findCategoryById(categoryId: String): Category? {
        return getCategories().find { it.id == categoryId }
    }

    fun addCategory(name: String, type: TransactionType): Boolean {
        val trimmed = name.trim()
        val current = getCategories().toMutableList()
        val exists = current.any { it.type == type && it.name.equals(trimmed, ignoreCase = true) }
        if (exists) return false

        current.add(Category(name = trimmed, type = type))
        saveCategories(current)
        return true
    }

    fun updateCategory(updated: Category): Boolean {
        val trimmed = updated.name.trim()
        val current = getCategories().toMutableList()
        val exists = current.any {
            it.id != updated.id && it.type == updated.type && it.name.equals(trimmed, ignoreCase = true)
        }
        if (exists) return false

        val index = current.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            current[index] = updated.copy(name = trimmed)
            saveCategories(current)
            return true
        }
        return false
    }

    fun isCategoryUsed(categoryId: String): Boolean {
        return getTransactions().any { it.categoryId == categoryId }
    }

    fun deleteCategory(categoryId: String): Boolean {
        if (isCategoryUsed(categoryId)) {
            return false
        }
        val current = getCategories().toMutableList()
        val removed = current.removeAll { it.id == categoryId }
        if (removed) {
            saveCategories(current)
        }
        return removed
    }

    private fun saveCategories(categories: List<Category>) {
        val json = categoriesToJson(categories, gson)
        prefs.edit().putString(CATEGORIES_NAME, json).apply()
    }

    fun getTransactions(): List<Transaction> {
        val json = prefs.getString(TRANSACTIONS_NAME, null)
        return transactionsFromJson(json, gson)
    }

    fun getTransactionsByType(type: TransactionType): List<Transaction> {
        return getTransactions()
            .filter { it.type == type }
            .sortedByDescending { it.dateMillis }
    }

    fun addTransaction(transaction: Transaction) {
        val current = getTransactions().toMutableList()
        current.add(transaction)
        saveTransactions(current)
    }

    fun updateTransaction(updated: Transaction): Boolean {
        val current = getTransactions().toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            current[index] = updated
            saveTransactions(current)
            return true
        }
        return false
    }

    fun deleteTransaction(transactionId: String): Boolean {
        val current = getTransactions().toMutableList()
        val removed = current.removeAll { it.id == transactionId }
        if (removed) {
            saveTransactions(current)
        }
        return removed
    }

    fun getTotalByType(type: TransactionType): Long {
        return getTransactionsByType(type).sumOf { it.amount }
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        val json = transactionsToJson(transactions, gson)
        prefs.edit().putString(TRANSACTIONS_NAME, json).apply()
    }
}
