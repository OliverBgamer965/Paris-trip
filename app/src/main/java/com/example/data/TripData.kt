package com.example.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

data class TripActivity(
    val id: String,
    val title: String,
    val timeLabel: String,
    val hour: Int,
    val minute: Int,
    val location: String,
    val mapsQuery: String,
    val description: String = ""
) {
    fun getLocalTime(): LocalTime = LocalTime.of(hour, minute)
}

data class DayInfo(
    val dayNumber: Int,
    val title: String,
    val dateLabel: String,
    val localDate: LocalDate,
    val activities: List<TripActivity>
)

object TripData {
    val day1Date = LocalDate.of(2026, 7, 12)
    val day2Date = LocalDate.of(2026, 7, 13)
    val day3Date = LocalDate.of(2026, 7, 14)
    val day4Date = LocalDate.of(2026, 7, 15)
    val day5Date = LocalDate.of(2026, 7, 16)

    fun getItineraryDays(): List<DayInfo> = listOf(
        DayInfo(
            dayNumber = 1,
            title = "Travel Day",
            dateLabel = "Sunday 12 July 2026",
            localDate = day1Date,
            activities = listOf(
                TripActivity("1_1", "Meet at Saddleworth School", "01:30", 1, 30, "Saddleworth School", "Saddleworth School, Uppermill, UK", "Check-in and prepare for boarding the coach."),
                TripActivity("1_2", "Coach departs", "01:50", 1, 50, "Saddleworth School", "Saddleworth School, Uppermill, UK", "Departing for Dover Ferry Terminal."),
                TripActivity("1_3", "DFDS Ferry", "10:40", 10, 40, "Dover Port", "DFDS Ferry Terminal Dover", "Boarding DFDS Ferry to cross the English Channel."),
                TripActivity("1_4", "Arrive Calais", "13:10", 13, 10, "Port of Calais", "Calais, France", "Arriving in France! Travel onward to Paris."),
                TripActivity("1_5", "Travel to Paris", "14:00", 14, 0, "En Route to Paris", "Paris, France", "Scenic coach journey through northern France."),
                TripActivity("1_6", "Evening meal", "18:30", 18, 30, "Mitry-Mory Restaurant", "Mitry-Mory, France", "A delicious dinner on arrival near Paris."),
                TripActivity("1_7", "Arrive Ace Hotel Mitry Mory", "20:00", 20, 0, "Ace Hotel Mitry Mory", "Ace Hotel Mitry Mory, Rue de la Garenne, Mitry-Mory, France", "Check-in, collect room keys, and rest up.")
            )
        ),
        DayInfo(
            dayNumber = 2,
            title = "Sightseeing",
            dateLabel = "Monday 13 July 2026",
            localDate = day2Date,
            activities = listOf(
                TripActivity("2_1", "Breakfast", "07:30", 7, 30, "Ace Hotel Mitry Mory", "Ace Hotel Mitry Mory, France", "Start the day with a buffet breakfast at the hotel."),
                TripActivity("2_2", "Hypermarket", "09:30", 9, 30, "French Hypermarket", "Hypermarche, Mitry-Mory, France", "Experience a massive French supermarket for shopping & snacks."),
                TripActivity("2_3", "Lunch", "12:00", 12, 0, "Paris Bistro", "Paris, France", "Take a midday break for traditional French lunch."),
                TripActivity("2_4", "Louvre Museum", "13:00", 13, 0, "Musée du Louvre", "Louvre Museum, Paris, France", "Explore the world's largest art museum, home of the Mona Lisa."),
                TripActivity("2_5", "Louvre Pyramid", "14:30", 14, 30, "Louvre Courtyard", "Louvre Pyramid, Paris, France", "View and photograph the iconic glass pyramid structure."),
                TripActivity("2_6", "Musée de l'Orangerie", "15:30", 15, 30, "Jardin des Tuileries", "Musée de l'Orangerie, Paris, France", "Admire Monet's legendary water lilies paintings."),
                TripActivity("2_7", "Place du Tertre", "16:30", 16, 30, "Montmartre", "Place du Tertre, Paris, France", "Visit the famous artist square on Montmartre hill."),
                TripActivity("2_8", "Arc de Triomphe", "17:15", 17, 15, "Champs-Élysées", "Arc de Triomphe, Paris, France", "Stand beneath Napoleon's majestic triumphal arch."),
                TripActivity("2_9", "Dinner", "18:00", 18, 0, "Parisian Restaurant", "Paris, France", "Relax and dine in the heart of Paris."),
                TripActivity("2_10", "River Seine Cruise", "20:00", 20, 0, "Bateaux Parisiens", "Bateaux Parisiens, Paris, France", "An evening boat cruise showcasing illuminated Parisian monuments."),
                TripActivity("2_11", "Return to Hotel", "22:00", 22, 0, "Ace Hotel Mitry Mory", "Ace Hotel Mitry Mory, France", "Travel back to the hotel for a well-deserved sleep.")
            )
        ),
        DayInfo(
            dayNumber = 3,
            title = "Asterix Park",
            dateLabel = "Tuesday 14 July 2026",
            localDate = day3Date,
            activities = listOf(
                TripActivity("3_1", "Breakfast", "07:30", 7, 30, "Ace Hotel Mitry Mory", "Ace Hotel Mitry Mory, France", "Fuel up at the hotel breakfast buffet."),
                TripActivity("3_2", "Depart Hotel", "09:00", 9, 0, "Ace Hotel Mitry Mory", "Ace Hotel Mitry Mory, France", "Boarding the coach for Asterix Park."),
                TripActivity("3_3", "Day at Asterix Park", "10:00", 10, 0, "Parc Astérix", "Parc Astérix, Plailly, France", "Enjoy incredible rollercoasters, themed attractions, and shows!"),
                TripActivity("3_4", "Leave Park", "18:30", 18, 30, "Parc Astérix", "Parc Astérix, Plailly, France", "Board the coach to travel back for dinner."),
                TripActivity("3_5", "Evening Meal", "19:30", 19, 30, "Mitry-Mory Restaurant", "Mitry-Mory, France", "Hot dinner in Mitry-Mory."),
                TripActivity("3_6", "Return Hotel", "21:00", 21, 0, "Ace Hotel Mitry Mory", "Ace Hotel Mitry Mory, France", "Unwind and sleep.")
            )
        ),
        DayInfo(
            dayNumber = 4,
            title = "Versailles",
            dateLabel = "Wednesday 15 July 2026",
            localDate = day4Date,
            activities = listOf(
                TripActivity("4_1", "Breakfast", "08:30", 8, 30, "Ace Hotel Mitry Mory", "Ace Hotel Mitry Mory, France", "Morning hotel breakfast."),
                TripActivity("4_2", "Depart", "09:30", 9, 30, "Ace Hotel Mitry Mory", "Ace Hotel Mitry Mory, France", "Coach departs towards Versailles."),
                TripActivity("4_3", "Market", "11:00", 11, 0, "Marché Notre-Dame", "Versailles Market, Versailles, France", "Explore the vibrant historical market of Versailles."),
                TripActivity("4_4", "Palace of Versailles", "14:00", 14, 0, "Château de Versailles", "Palace of Versailles, France", "Step inside the grand royal residence and stroll the gorgeous gardens."),
                TripActivity("4_5", "Eiffel Tower", "18:45", 18, 45, "Champ de Mars", "Eiffel Tower, Paris, France", "Visit Paris's ultimate landmark, the Iron Lady, beautifully illuminated."),
                TripActivity("4_6", "Return Hotel", "21:15", 21, 15, "Ace Hotel Mitry Mory", "Ace Hotel Mitry Mory, France", "Boarding coach to return to hotel for our final night.")
            )
        ),
        DayInfo(
            dayNumber = 5,
            title = "Travel Home",
            dateLabel = "Thursday 16 July 2026",
            localDate = day5Date,
            activities = listOf(
                TripActivity("5_1", "Breakfast", "07:00", 7, 0, "Ace Hotel Mitry Mory", "Ace Hotel Mitry Mory, France", "Early breakfast and check-out of the hotel."),
                TripActivity("5_2", "Leave Hotel", "08:00", 8, 0, "Ace Hotel Mitry Mory", "Ace Hotel Mitry Mory, France", "Departing Paris to head north towards Calais."),
                TripActivity("5_3", "Arrive Calais", "12:20", 12, 20, "Port of Calais", "Port of Calais, France", "Checking in at Calais ferry terminal."),
                TripActivity("5_4", "Ferry", "14:20", 14, 20, "Calais Port", "DFDS Ferry Calais", "Boarding DFDS Ferry to cross back to Dover."),
                TripActivity("5_5", "Arrive Saddleworth School", "21:00", 21, 0, "Saddleworth School", "Saddleworth School, Uppermill, UK", "Welcome home! Meet parents at the school gates.")
            )
        )
    )

    fun determineTripDayIndex(date: LocalDate): Int {
        return when (date) {
            day1Date -> 0
            day2Date -> 1
            day3Date -> 2
            day4Date -> 3
            day5Date -> 4
            else -> {
                if (date.isBefore(day1Date)) -1 else -2
            }
        }
    }

    // Custom models for hotel info & emergencies
    data class Contact(val name: String, val role: String, val phone: String, val details: String = "")

    val emergencyContacts = listOf(
        Contact("Emergency Services", "Europe-wide", "112", "Free universal emergency number in France"),
        Contact("Ace Hotel Mitry Mory", "Hotel Front Desk", "+33 1 64 67 11 11", "Rue de la Garenne, 77290 Mitry-Mory"),
        Contact("Saddleworth School", "School Office (UK)", "+44 1457 872072", "High Street, Uppermill, Oldham OL3 6BU"),
        Contact("NHS Health Line", "Medical Advice", "+44 111", "For non-emergency UK health advice"),
        Contact("UK Global Health Insurance (GHIC)", "Emergency Medical Card", "+44 191 218 1999", "Ensure card is in your backpack!")
    )

    data class HotelDetails(
        val name: String,
        val address: String,
        val phone: String,
        val wifiSsid: String,
        val wifiPassword: String,
        val checkInTime: String,
        val checkOutTime: String,
        val details: String
    )

    val hotelInfo = HotelDetails(
        name = "Ace Hotel Mitry Mory",
        address = "Rue de la Garenne, 77290 Mitry-Mory, France",
        phone = "+33 1 64 67 11 11",
        wifiSsid = "ACE_HOTEL_GUEST",
        wifiPassword = "paristrip2026",
        checkInTime = "16:00 (Day 1: ~20:00 arrival)",
        checkOutTime = "11:00 (Day 5: 08:00 checkout)",
        details = "A modern, secure, and extremely comfortable hotel. Features free high-speed Wi-Fi, spacious rooms with private bathrooms, and a breakfast lounge. Located conveniently near Asterix Park and Paris transit points."
    )
}
