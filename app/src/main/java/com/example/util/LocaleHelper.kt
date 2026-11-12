package com.example.util

object LocaleHelper {
    // Strings map for English & Hindi
    fun getString(key: String, isHindi: Boolean): String {
        return if (isHindi) (hiStrings[key] ?: enStrings[key] ?: key)
        else (enStrings[key] ?: key)
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
        "edit_work_title" to "Edit Tractor Work",
        "step_customer" to "1. Select Customer",
        "step_category" to "2. Select Agricultural Work",
        "step_duration" to "3. Enter Working Duration",
        "step_rate" to "4. Custom Rate & Amount",
        "step_payment" to "5. Payment Status",
        "hours" to "Hours",
        "minutes" to "Minutes",
        "custom_rate" to "Enter Rate (₹)",
        "rate_unit" to "Rate Unit",
        "calculated_total" to "Total Amount",
        "amount_received" to "Amount Received (₹)",
        "balance_pending" to "Balance Remaining",
        "save_work" to "SAVE WORK RECORD",
        "save_changes" to "SAVE CHANGES",
        "work_date" to "Work Date",
        "notes_optional" to "Field location / notes (optional)",
        "add_new_customer" to "+ Add New Customer",
        "enter_customer_name" to "Customer Full Name",
        "customer_phone" to "Mobile Number (Optional)",
        "customer_village" to "Village / Address (Optional)",

        // Customers
        "search_customers" to "Search customer name or village...",
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
        "rate_management_sub" to "Customize default rates and add your own farm implements",
        "add_custom_category" to "+ Add Custom Category",
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
        "settings_title" to "Settings & Profile",
        "tractor_owner_profile" to "Tractor Owner Profile",
        "owner_name" to "Owner Name",
        "tractor_model" to "Tractor Model / No.",
        "language_preference" to "App Language",
        "english" to "English",
        "hindi" to "हिंदी",
        "data_backup_restore" to "Data Backup & Security",
        "export_json" to "Backup All Data (JSON)",
        "import_json" to "Restore Backup",
        "reset_data" to "Reset to Default Data",
        "confirm_delete" to "Are you sure you want to delete this record?",
        "delete" to "Delete",
        "cancel" to "Cancel",
        "save" to "Save"
    )

    private val hiStrings = mapOf(
        "app_name" to "ट्रैक्टर हिसाब",
        "app_tagline" to "ट्रैक्टर मालिकों के लिए दैनिक काम व खाता",
        "tab_home" to "होम",
        "tab_add_work" to "नया काम",
        "tab_customers" to "ग्राहक / खाता",
        "tab_reports" to "रिपोर्ट",
        "tab_settings" to "सेटिंग्स",
        "tab_rates" to "दर प्रबंधन",

        // Dashboard
        "today_summary" to "आज का काम और कमाई",
        "total_hours" to "काम का समय",
        "total_earnings" to "कुल कमाई",
        "received_amount" to "नगद जमा",
        "pending_balance" to "बाकी बकाया",
        "customers_served" to "कुल ग्राहक",
        "work_entries_count" to "काम प्रविष्टि",
        "daily_records" to "आज का काम हिसाब",
        "no_work_today" to "इस दिन कोई ट्रैक्टर का काम दर्ज नहीं है",
        "tap_to_add_work" to "नया काम दर्ज करने के लिए नीचे बटन दबाएं",
        "prev_day" to "पिछला दिन",
        "next_day" to "अगला दिन",
        "today" to "आज",

        // Add Work
        "new_work_title" to "नया काम दर्ज करें",
        "edit_work_title" to "काम हिसाब बदलें",
        "step_customer" to "१. ग्राहक चुनें",
        "step_category" to "२. कृषि यंत्र / काम चुनें",
        "step_duration" to "३. काम का समय (घंटा/मिनट)",
        "step_rate" to "४. अपनी दर और कुल राशि",
        "step_payment" to "५. भुगतान स्थिति (रुपये मिले या बाकी)",
        "hours" to "घंटा",
        "minutes" to "मिनट",
        "custom_rate" to "दर दर्ज करें (₹)",
        "rate_unit" to "दर का प्रकार",
        "calculated_total" to "कुल हिसाब राशि",
        "amount_received" to "मिली हुई राशि (₹)",
        "balance_pending" to "बाकी उधार राशि",
        "save_work" to "काम हिसाब सुरक्षित करें (SAVE)",
        "save_changes" to "परिवर्तन सहेजें",
        "work_date" to "काम की तारीख",
        "notes_optional" to "खेत का विवरण या नोट (ऐच्छिक)",
        "add_new_customer" to "+ नया ग्राहक जोड़ें",
        "enter_customer_name" to "ग्राहक का नाम",
        "customer_phone" to "मोबाइल नंबर (ऐच्छिक)",
        "customer_village" to "गांव / पता (ऐच्छिक)",

        // Customers
        "search_customers" to "ग्राहक या गांव खोजें...",
        "total_customers" to "कुल ग्राहक",
        "total_due_all" to "कुल बाजार उधारी",
        "record_payment" to "रुपये जमा करें",
        "call_customer" to "फोन लगाएं",
        "share_statement" to "खाता भेजें",
        "customer_history" to "काम व भुगतान इतिहास",
        "payment_history" to "जमा इतिहास",
        "work_history" to "काम का हिसाब",
        "no_customers_found" to "कोई ग्राहक नहीं मिला",

        // Rates
        "rate_management_title" to "यंत्र व तय दरें",
        "rate_management_sub" to "प्रत्येक यंत्र की सामान्य दर तय करें या नया यंत्र जोड़ें",
        "add_custom_category" to "+ नया कृषि काम जोड़ें",
        "edit_rate" to "दर बदलें",
        "default_rate" to "सामान्य दर",

        // Reports
        "reports_title" to "कमाई और काम रिपोर्ट",
        "daily" to "दैनिक",
        "weekly" to "साप्ताहिक (७ दिन)",
        "monthly" to "मासिक (३० दिन)",
        "export_pdf" to "PDF रिपोर्ट बनाएं",
        "share_report" to "रिपोर्ट शेयर करें",
        "category_breakdown" to "यंत्र-वार काम विवरण",

        // Settings
        "settings_title" to "सेटिंग्स व मालिक प्रोफाइल",
        "tractor_owner_profile" to "ट्रैक्टर मालिक का विवरण",
        "owner_name" to "मालिक का नाम",
        "tractor_model" to "ट्रैक्टर मॉडल / नंबर",
        "language_preference" to "ऐप की भाषा",
        "english" to "English",
        "hindi" to "हिंदी",
        "data_backup_restore" to "डेटा बैकअप व सुरक्षा",
        "export_json" to "पूरा डेटा बैकअप लें (JSON)",
        "import_json" to "डेटा रिस्टोर करें",
        "reset_data" to "डिफ़ॉल्ट डेटा रीसेट करें",
        "confirm_delete" to "क्या आप इस रिकॉर्ड को हटाना चाहते हैं?",
        "delete" to "हटाएं",
        "cancel" to "रद्द करें",
        "save" to "सहेजें"
    )
}
