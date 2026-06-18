package com.example.data

object Countries {
    data class CountryItem(val name: String, val dial: String, val code: String, val flag: String)

    val list = listOf(
        CountryItem("India", "+91", "IN", "🇮🇳"),
        CountryItem("United States", "+1", "US", "🇺🇸"),
        CountryItem("United Kingdom", "+44", "GB", "🇬🇧"),
        CountryItem("Canada", "+1", "CA", "🇨🇦"),
        CountryItem("Australia", "+61", "AU", "🇦🇺"),
        CountryItem("Bangladesh", "+880", "BD", "🇧🇩"),
        CountryItem("Pakistan", "+92", "PK", "🇵🇰"),
        CountryItem("Nepal", "+977", "NP", "🇳🇵"),
        CountryItem("Sri Lanka", "+94", "LK", "🇱🇰"),
        CountryItem("United Arab Emirates", "+971", "AE", "🇦🇪"),
        CountryItem("Saudi Arabia", "+966", "SA", "🇸🇦"),
        CountryItem("Qatar", "+974", "QA", "🇶🇦"),
        CountryItem("Oman", "+968", "OM", "🇴🇲"),
        CountryItem("Kuwait", "+965", "KW", "🇰🇼"),
        CountryItem("Bahrain", "+973", "BH", "🇧🇭"),
        CountryItem("Malaysia", "+60", "MY", "🇲🇾"),
        CountryItem("Singapore", "+65", "SG", "🇸🇬"),
        CountryItem("Indonesia", "+62", "ID", "🇮🇩"),
        CountryItem("Philippines", "+63", "PH", "🇵🇭"),
        CountryItem("Thailand", "+66", "TH", "🇹🇭"),
        CountryItem("Vietnam", "+84", "VN", "🇻🇳")
    )
}
