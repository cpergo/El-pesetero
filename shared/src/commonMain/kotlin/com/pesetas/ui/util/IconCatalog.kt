package com.pesetas.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object IconCatalog {

    private val icons: Map<String, ImageVector> = linkedMapOf(
        "restaurant" to Icons.Filled.Restaurant,
        "fastfood" to Icons.Filled.Fastfood,
        "local_cafe" to Icons.Filled.LocalCafe,
        "local_bar" to Icons.Filled.LocalBar,
        "local_pizza" to Icons.Filled.LocalPizza,
        "lunch_dining" to Icons.Filled.LunchDining,
        "cake" to Icons.Filled.Cake,
        "local_grocery_store" to Icons.Filled.LocalGroceryStore,
        "directions_car" to Icons.Filled.DirectionsCar,
        "directions_bus" to Icons.Filled.DirectionsBus,
        "train" to Icons.Filled.Train,
        "flight" to Icons.Filled.Flight,
        "local_gas_station" to Icons.Filled.LocalGasStation,
        "two_wheeler" to Icons.Filled.TwoWheeler,
        "directions_bike" to Icons.AutoMirrored.Filled.DirectionsBike,
        "local_taxi" to Icons.Filled.LocalTaxi,
        "home" to Icons.Filled.Home,
        "chair" to Icons.Filled.Chair,
        "weekend" to Icons.Filled.Weekend,
        "bed" to Icons.Filled.Bed,
        "lightbulb" to Icons.Filled.Lightbulb,
        "water_drop" to Icons.Filled.WaterDrop,
        "bolt" to Icons.Filled.Bolt,
        "wifi" to Icons.Filled.Wifi,
        "shopping_cart" to Icons.Filled.ShoppingCart,
        "shopping_bag" to Icons.Filled.ShoppingBag,
        "checkroom" to Icons.Filled.Checkroom,
        "storefront" to Icons.Filled.Storefront,
        "redeem" to Icons.Filled.Redeem,
        "local_mall" to Icons.Filled.LocalMall,
        "medical_services" to Icons.Filled.MedicalServices,
        "favorite" to Icons.Filled.Favorite,
        "fitness_center" to Icons.Filled.FitnessCenter,
        "spa" to Icons.Filled.Spa,
        "medication" to Icons.Filled.Medication,
        "healing" to Icons.Filled.Healing,
        "sports_esports" to Icons.Filled.SportsEsports,
        "movie" to Icons.Filled.Movie,
        "music_note" to Icons.Filled.MusicNote,
        "sports_soccer" to Icons.Filled.SportsSoccer,
        "sports_basketball" to Icons.Filled.SportsBasketball,
        "beach_access" to Icons.Filled.BeachAccess,
        "camera_alt" to Icons.Filled.CameraAlt,
        "book" to Icons.Filled.Book,
        "menu_book" to Icons.AutoMirrored.Filled.MenuBook,
        "palette" to Icons.Filled.Category,
        "receipt_long" to Icons.AutoMirrored.Filled.ReceiptLong,
        "receipt" to Icons.Filled.Receipt,
        "payments" to Icons.Filled.Payments,
        "savings" to Icons.Filled.Savings,
        "account_balance" to Icons.Filled.AccountBalance,
        "account_balance_wallet" to Icons.Filled.AccountBalanceWallet,
        "credit_card" to Icons.Filled.CreditCard,
        "attach_money" to Icons.Filled.AttachMoney,
        "euro" to Icons.Filled.Euro,
        "request_quote" to Icons.Filled.RequestQuote,
        "currency_exchange" to Icons.Filled.CurrencyExchange,
        "price_check" to Icons.Filled.PriceCheck,
        "work" to Icons.Filled.Work,
        "school" to Icons.Filled.School,
        "business_center" to Icons.Filled.BusinessCenter,
        "laptop" to Icons.Filled.Laptop,
        "science" to Icons.Filled.Science,
        "computer" to Icons.Filled.Computer,
        "smartphone" to Icons.Filled.Smartphone,
        "phone_android" to Icons.Filled.PhoneAndroid,
        "pets" to Icons.Filled.Pets,
        "child_friendly" to Icons.Filled.ChildFriendly,
        "family_restroom" to Icons.Filled.FamilyRestroom,
        "face" to Icons.Filled.Face,
        "build" to Icons.Filled.Build,
        "cleaning_services" to Icons.Filled.CleaningServices,
        "local_laundry_service" to Icons.Filled.LocalLaundryService,
        "park" to Icons.Filled.Park,
        "card_giftcard" to Icons.Filled.CardGiftcard,
        "star" to Icons.Filled.Star,
        "flag" to Icons.Filled.Flag,
        "volunteer_activism" to Icons.Filled.VolunteerActivism,
        "subscriptions" to Icons.Filled.Subscriptions,
        "category" to Icons.Filled.Category,
    )

    val keys: List<String> = icons.keys.toList()

    val default: String = "category"

    fun iconFor(key: String): ImageVector = icons[key] ?: Icons.Filled.Category
}
