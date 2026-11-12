package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Customer
import com.example.data.model.CustomerLedgerSummary
import com.example.data.model.DashboardSummary
import com.example.data.model.PaymentTransaction
import com.example.data.model.RateUnit
import com.example.data.model.WorkCategory
import com.example.data.model.WorkEntry
import com.example.data.repository.TractorRepository
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TractorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TractorRepository.getRepository(application)

    // Language state: true = Hindi, false = English
    private val _isHindi = MutableStateFlow(true)
    val isHindi: StateFlow<Boolean> = _isHindi.asStateFlow()

    // Owner profile
    private val _ownerName = MutableStateFlow("Rajesh Patel (राजेश पटेल)")
    val ownerName: StateFlow<String> = _ownerName.asStateFlow()

    private val _tractorModel = MutableStateFlow("Mahindra 575 DI")
    val tractorModel: StateFlow<String> = _tractorModel.asStateFlow()

    private val _ownerPhone = MutableStateFlow("9876501234")
    val ownerPhone: StateFlow<String> = _ownerPhone.asStateFlow()

    // Selected date for Daily Dashboard
    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    // Customers Search
    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    // Active edit entry
    private val _editingWorkEntry = MutableStateFlow<WorkEntry?>(null)
    val editingWorkEntry: StateFlow<WorkEntry?> = _editingWorkEntry.asStateFlow()

    // Data Streams
    val categories: StateFlow<List<WorkCategory>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerLedgers: StateFlow<List<CustomerLedgerSummary>> = combine(
        repository.customerLedgers,
        _customerSearchQuery
    ) { ledgers, query ->
        if (query.isBlank()) {
            ledgers
        } else {
            val q = query.trim().lowercase()
            ledgers.filter {
                it.customer.name.lowercase().contains(q) ||
                        it.customer.village.lowercase().contains(q) ||
                        it.customer.phone.contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkEntries: StateFlow<List<WorkEntry>> = repository.allWorkEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentTransaction>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected day entries
    val todayEntries: StateFlow<List<WorkEntry>> = combine(
        allWorkEntries,
        _selectedDateMillis
    ) { entries, dateMillis ->
        entries.filter { FormatUtils.isSameDay(it.workDateMillis, dateMillis) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Dashboard Summary
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        allWorkEntries,
        allPayments,
        _selectedDateMillis
    ) { entries, payments, selectedDate ->
        val dayEntries = entries.filter { FormatUtils.isSameDay(it.workDateMillis, selectedDate) }
        val dayPayments = payments.filter { FormatUtils.isSameDay(it.paymentDateMillis, selectedDate) }

        val todayWorkMinutes = dayEntries.sumOf { it.durationMinutes }
        val todayEarnings = dayEntries.sumOf { it.totalAmount }
        val todayCustomers = dayEntries.map { it.customerId }.distinct().size
        val todayReceived = dayPayments.sumOf { it.amount }
        val todayPending = dayEntries.sumOf { it.amountPending }
        val todayCount = dayEntries.size

        // Calculate 7-day weekly stats
        val weekAgoMillis = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        val weekEntries = entries.filter { it.workDateMillis >= weekAgoMillis }
        val weekMinutes = weekEntries.sumOf { it.durationMinutes }
        val weekEarnings = weekEntries.sumOf { it.totalAmount }

        // Calculate 30-day monthly stats
        val monthAgoMillis = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val monthEntries = entries.filter { it.workDateMillis >= monthAgoMillis }
        val monthMinutes = monthEntries.sumOf { it.durationMinutes }
        val monthEarnings = monthEntries.sumOf { it.totalAmount }

        DashboardSummary(
            totalWorkingMinutes = todayWorkMinutes,
            totalEarnings = todayEarnings,
            totalCustomersServed = todayCustomers,
            totalPaymentsReceived = todayReceived,
            totalPendingPayments = todayPending,
            totalWorkEntriesCount = todayCount,
            weeklyWorkingMinutes = weekMinutes,
            monthlyWorkingMinutes = monthMinutes,
            weeklyEarnings = weekEarnings,
            monthlyEarnings = monthEarnings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    fun toggleLanguage() {
        _isHindi.value = !_isHindi.value
    }

    fun setLanguage(hindi: Boolean) {
        _isHindi.value = hindi
    }

    fun updateOwnerProfile(name: String, model: String, phone: String) {
        _ownerName.value = name
        _tractorModel.value = model
        _ownerPhone.value = phone
    }

    fun setCustomerSearch(query: String) {
        _customerSearchQuery.value = query
    }

    fun setEditingWorkEntry(entry: WorkEntry?) {
        _editingWorkEntry.value = entry
    }

    fun changeSelectedDate(offsetDays: Int) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = _selectedDateMillis.value
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        _selectedDateMillis.value = cal.timeInMillis
    }

    fun setSelectedDate(millis: Long) {
        _selectedDateMillis.value = millis
    }

    fun saveWork(entry: WorkEntry, paymentMethod: String = "CASH", onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveWorkEntry(entry, paymentMethod)
            _editingWorkEntry.value = null
            onComplete()
        }
    }

    fun deleteWork(entry: WorkEntry, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteWorkEntry(entry)
            if (_editingWorkEntry.value?.id == entry.id) {
                _editingWorkEntry.value = null
            }
            onComplete()
        }
    }

    fun addCustomer(name: String, phone: String, village: String, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val customer = Customer(name = name.trim(), phone = phone.trim(), village = village.trim())
            val id = repository.insertCustomer(customer)
            onComplete(id)
        }
    }

    fun updateCustomer(customer: Customer, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            onComplete()
        }
    }

    fun deleteCustomer(customer: Customer, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            onComplete()
        }
    }

    fun recordPayment(
        customerId: Long,
        customerName: String,
        amount: Double,
        method: String,
        dateMillis: Long,
        notes: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.addPayment(customerId, customerName, amount, method, dateMillis, notes)
            onComplete()
        }
    }

    fun deletePayment(payment: PaymentTransaction, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            onComplete()
        }
    }

    fun saveCategoryRate(category: WorkCategory, newRate: Double, newUnit: RateUnit) {
        viewModelScope.launch {
            val updated = category.copy(
                defaultRate = newRate,
                defaultUnit = newUnit.id
            )
            repository.saveCategory(updated)
        }
    }

    fun addCustomCategory(nameEn: String, nameHi: String, rate: Double, unit: RateUnit, drawableName: String) {
        viewModelScope.launch {
            val key = "custom_${System.currentTimeMillis()}"
            val newCategory = WorkCategory(
                key = key,
                nameEn = nameEn.trim(),
                nameHi = if (nameHi.isBlank()) nameEn.trim() else nameHi.trim(),
                drawableName = drawableName,
                defaultRate = rate,
                defaultUnit = unit.id,
                isCustom = true
            )
            repository.saveCategory(newCategory)
        }
    }

    fun deleteCategory(category: WorkCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun resetData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.resetDatabase()
            onComplete()
        }
    }

    suspend fun exportBackupJson(): String {
        return repository.exportDataAsJson()
    }

    fun restoreBackupJson(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.importDataFromJson(json)
            onResult(result)
        }
    }
}
