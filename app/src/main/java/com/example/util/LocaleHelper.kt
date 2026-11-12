package com.example.util

object LocaleHelper {
    // Clean English string provider
    fun getString(key: String, isHindi: Boolean = false): String {
        return enStrings[key] ?: key
    }

    private val enStrings = mapOf(
        "app_name" to "Tractor Hisab",
        "app_tagline" to "Work & Accounting for Tractor Owners",
        "tab_home" to "Home",
        "tab_add_work" to "Add Work",
        "tab_customers" to "Customers",
        "tab_reports" to "Reports",
        "tab_settings" to "Settings",
        "tab_rates" to "Rates",

        // Dashboard
        "today_summary" to "Today's Work Summary",
        "total_hours" to "Work Hours",
        "total_earnings" to "Total Earnings",
        "received_amount" to "Cash Received",
        "pending_balance" to "Pending Due",
        "customers_served" to "Customers",
        "work_entries_count" to "Entries",
        "daily_records" to "Work Records",
        "no_work_today" to "No tractor work recorded for this day",
        "tap_to_add_work" to "Tap below to record new field work",
        "prev_day" to "Previous Day",
        "next_day" to "Next Day",
        "today" to "Today",

        // Add Work
        "new_work_title" to "Record Tractor Work",
        "edit_work_title" to "Edit Work Record",
        "step_customer" to "1. Select Customer",
        "step_category" to "2. Select Agricultural Work",
        "step_duration" to "3. Enter Working Duration",
        "step_rate" to "4. Custom Rate & Amount",
        "step_payment" to "5. Payment Status",
        "hours" to "Hours",
        "minutes" to "Minutes",
        "custom_rate" to "Rate (₹)",
        "rate_unit" to "Rate Type",
        "calculated_total" to "Total Amount",
        "amount_received" to "Amount Received (₹)",
        "balance_pending" to "Balance Remaining",
        "save_work" to "SAVE WORK",
        "save_changes" to "SAVE CHANGES",
        "work_date" to "Work Date",
        "notes_optional" to "Field location / notes (optional)",
        "add_new_customer" to "+ Add Customer",
        "enter_customer_name" to "Customer Name",
        "customer_phone" to "Mobile Number",
        "customer_village" to "Village / Address",

        // Customers
        "search_customers" to "Search customers...",
        "total_customers" to "Total Customers",
        "total_due_all" to "Total Market Due",
        "record_payment" to "Record Payment",
        "call_customer" to "Call",
        "share_statement" to "Share Statement",
        "customer_history" to "Work & Payment History",
        "payment_history" to "Payments",
        "work_history" to "Work Entries",
        "no_customers_found" to "No customers found",

        // Rates
        "rate_management_title" to "Work Categories & Rates",
        "rate_management_sub" to "Manage default rates and add your own custom farm implements",
        "add_custom_category" to "+ Add Custom Work",
        "edit_rate" to "Edit Rate",
        "default_rate" to "Default Rate",

        // Reports
        "reports_title" to "Earnings & Work Reports",
        "daily" to "Daily",
        "weekly" to "Weekly (7 Days)",
        "monthly" to "Monthly (30 Days)",
        "export_pdf" to "Export PDF Report",
        "share_report" to "Share Summary",
        "category_breakdown" to "Work by Implement Category",

        // Settings
        "settings_title" to "Settings",
        "tractor_owner_profile" to "Tractor Owner Profile",
        "owner_name" to "Owner Name",
        "tractor_model" to "Tractor Model",
        "language_preference" to "App Language",
        "english" to "English",
        "data_backup_restore" to "Data Safety & Backup",
        "export_json" to "Backup All Data (JSON)",
        "import_json" to "Restore Backup",
        "reset_data" to "Reset to Default Data",
        "confirm_delete" to "Are you sure you want to delete this record?",
        "delete" to "Delete",
        "cancel" to "Cancel",
        "save" to "Save",
        "paid" to "Paid",
        "partial" to "Partial",
        "unpaid" to "Unpaid"
    )
}
