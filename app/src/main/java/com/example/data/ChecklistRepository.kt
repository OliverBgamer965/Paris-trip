package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ChecklistRepository(private val checklistDao: ChecklistDao) {
    val allItems: Flow<List<ChecklistItem>> = checklistDao.getAllItemsFlow()

    fun getItemsByCategory(category: String): Flow<List<ChecklistItem>> =
        checklistDao.getItemsByCategoryFlow(category)

    suspend fun insertItem(item: ChecklistItem): Long = checklistDao.insertItem(item)

    suspend fun updateItem(item: ChecklistItem) = checklistDao.updateItem(item)

    suspend fun deleteItemById(id: Int) = checklistDao.deleteItemById(id)

    suspend fun ensureDefaultItems() {
        val existing = checklistDao.getAllItemsFlow().first()
        if (existing.isEmpty()) {
            val defaultDay1 = listOf(
                "Passport", "GHIC Card", "Phone", "Charger", "Water Bottle", "Spending Money", "Medication"
            ).map { ChecklistItem(category = "day1", name = it) }

            val defaultPacking = listOf(
                "Comfortable walking shoes", "Raincoat / Umbrella", "Power bank", "Adaptor plug (Euro)",
                "Toiletries", "Sunscreen", "Sunglasses", "Euro cash", "Light jacket", "Pyjamas"
            ).map { ChecklistItem(category = "packing", name = it) }

            checklistDao.insertItems(defaultDay1 + defaultPacking)
        }
    }
}
