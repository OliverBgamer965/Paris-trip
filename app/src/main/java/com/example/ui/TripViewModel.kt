package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(
        val temperature: Double,
        val conditionText: String,
        val conditionIcon: String,
        val forecast: List<ForecastDay>
    ) : WeatherUiState
    data class Error(val message: String, val fallbackData: Success) : WeatherUiState
}

data class ForecastDay(
    val dayLabel: String,
    val maxTemp: Double,
    val minTemp: Double,
    val conditionText: String,
    val conditionIcon: String
)

class TripViewModel(
    application: Application,
    private val repository: ChecklistRepository
) : AndroidViewModel(application) {

    // Simulated Date & Time (Default to system current time, or July 12 2026 if today is before the trip, but we let users toggle/simulate!)
    private val _simulatedDateTime = MutableStateFlow<LocalDateTime>(LocalDateTime.now())
    val simulatedDateTime: StateFlow<LocalDateTime> = _simulatedDateTime.asStateFlow()

    // Flag indicating if time simulation is active
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    // Selected Itinerary Day Index (0 to 4 corresponding to Day 1 to Day 5)
    private val _selectedDayIndex = MutableStateFlow(0)
    val selectedDayIndex: StateFlow<Int> = _selectedDayIndex.asStateFlow()

    // Weather state
    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    // Interactive checklist from local DB (Day 1)
    val day1Checklist: StateFlow<List<ChecklistItem>> = repository.getItemsByCategory("day1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Interactive custom packing list from local DB
    val packingList: StateFlow<List<ChecklistItem>> = repository.getItemsByCategory("packing")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Ensure standard items exist in database
        viewModelScope.launch {
            repository.ensureDefaultItems()
        }

        // Initialize simulated date. If actual date is before trip starts, pre-set simulator to July 12 11:00 AM so the user sees data immediately, but allow toggles.
        val now = LocalDateTime.now()
        if (now.toLocalDate().isBefore(TripData.day1Date)) {
            // Pre-simulate Sunday July 12, 2026, at 11:00 AM to present a beautiful, live itinerary
            _simulatedDateTime.value = LocalDateTime.of(2026, 7, 12, 11, 0)
            _isSimulating.value = true
            _selectedDayIndex.value = 0
        } else if (now.toLocalDate().isAfter(TripData.day5Date)) {
            // Pre-set selected day to Day 5
            _selectedDayIndex.value = 4
            _simulatedDateTime.value = now
        } else {
            // We are within the trip! Automatically select correct day
            val idx = TripData.determineTripDayIndex(now.toLocalDate())
            if (idx in 0..4) {
                _selectedDayIndex.value = idx
            }
            _simulatedDateTime.value = now
        }

        fetchWeather()
    }

    fun fetchWeather() {
        _weatherState.value = WeatherUiState.Loading
        viewModelScope.launch {
            val fallbackForecast = listOf(
                ForecastDay("Sun 12", 26.5, 16.0, "Sunny", "☀️"),
                ForecastDay("Mon 13", 24.0, 15.2, "Partly Cloudy", "⛅"),
                ForecastDay("Tue 14", 22.1, 14.0, "Thunderstorms", "⛈️"),
                ForecastDay("Wed 15", 27.2, 17.5, "Sunny & Warm", "☀️"),
                ForecastDay("Thu 16", 28.0, 18.0, "Clear Sky", "☀️")
            )
            val fallbackSuccess = WeatherUiState.Success(
                temperature = 25.4,
                conditionText = "Sunny & Warm",
                conditionIcon = "☀️",
                forecast = fallbackForecast
            )

            try {
                val response = WeatherClient.api.getParisWeather()
                val current = response.currentWeather
                val daily = response.daily

                if (current != null && daily != null && daily.time != null && daily.tempMax != null && daily.tempMin != null && daily.weatherCode != null) {
                    val daysForecast = daily.time.mapIndexed { index, timeStr ->
                        val date = LocalDate.parse(timeStr)
                        val formattedLabel = date.format(DateTimeFormatter.ofPattern("E dd"))
                        val code = daily.weatherCode.getOrElse(index) { 0 }
                        val (text, icon) = mapWeatherCode(code)
                        ForecastDay(
                            dayLabel = formattedLabel,
                            maxTemp = daily.tempMax.getOrElse(index) { 24.0 },
                            minTemp = daily.tempMin.getOrElse(index) { 15.0 },
                            conditionText = text,
                            conditionIcon = icon
                        )
                    }

                    val (currText, currIcon) = mapWeatherCode(current.weatherCode)
                    _weatherState.value = WeatherUiState.Success(
                        temperature = current.temperature,
                        conditionText = currText,
                        conditionIcon = currIcon,
                        forecast = daysForecast.take(5)
                    )
                } else {
                    _weatherState.value = WeatherUiState.Error("Incomplete data received from weather server.", fallbackSuccess)
                }
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error("Offline mode: showing climate averages.", fallbackSuccess)
            }
        }
    }

    private fun mapWeatherCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> "Clear Sky" to "☀️"
            1, 2 -> "Partly Cloudy" to "⛅"
            3 -> "Overcast" to "☁️"
            45, 48 -> "Foggy" to "🌫️"
            51, 53, 55 -> "Light Drizzle" to "🌧️"
            61, 63, 65 -> "Showers" to "🌧️"
            71, 73, 75 -> "Light Snow" to "❄️"
            80, 81, 82 -> "Rain Showers" to "🌦️"
            95, 96, 99 -> "Thunderstorm" to "⛈️"
            else -> "Fine Weather" to "☀️"
        }
    }

    // Toggle simulated mode / update simulated date
    fun setSimulating(simulating: Boolean) {
        _isSimulating.value = simulating
        if (!simulating) {
            _simulatedDateTime.value = LocalDateTime.now()
            // Re-sync selected index to actual day if trip is running
            val idx = TripData.determineTripDayIndex(LocalDate.now())
            if (idx in 0..4) {
                _selectedDayIndex.value = idx
            }
        }
    }

    fun updateSimulatedDateTime(dateTime: LocalDateTime) {
        _simulatedDateTime.value = dateTime
    }

    fun selectDayIndex(index: Int) {
        if (index in 0..4) {
            _selectedDayIndex.value = index
        }
    }

    // Checklist Interactions
    fun toggleChecklistItem(item: ChecklistItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun addPackingItem(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertItem(ChecklistItem(category = "packing", name = name.trim(), isChecked = false))
        }
    }

    fun deletePackingItem(id: Int) {
        viewModelScope.launch {
            repository.deleteItemById(id)
        }
    }

    // Dynamic Computations based on current/simulated DateTime and selected Day Index
    fun getSelectedDayCompletedProgress(dayIndex: Int, currentDateTime: LocalDateTime): Float {
        val day = TripData.getItineraryDays()[dayIndex]
        val currentDate = currentDateTime.toLocalDate()

        if (currentDate.isBefore(day.localDate)) return 0.0f
        if (currentDate.isAfter(day.localDate)) return 1.0f

        // It is the same day! Compute completed proportion based on activities
        val activities = day.activities
        if (activities.isEmpty()) return 0.0f

        val firstAct = activities.first()
        val lastAct = activities.last()

        val firstTime = LocalDateTime.of(currentDate, firstAct.getLocalTime())
        val lastTime = LocalDateTime.of(currentDate, lastAct.getLocalTime())

        if (currentDateTime.isBefore(firstTime)) return 0.0f
        if (currentDateTime.isAfter(lastTime)) return 1.0f

        val totalMinutes = ChronoUnit.MINUTES.between(firstTime, lastTime)
        if (totalMinutes <= 0) return 1.0f

        val elapsedMinutes = ChronoUnit.MINUTES.between(firstTime, currentDateTime)
        return (elapsedMinutes.toFloat() / totalMinutes.toFloat()).coerceIn(0.0f, 1.0f)
    }

    fun getNextActivityCountdown(dayIndex: Int, currentDateTime: LocalDateTime): String {
        val day = TripData.getItineraryDays()[dayIndex]
        val currentDate = currentDateTime.toLocalDate()

        if (currentDate.isBefore(day.localDate)) {
            val daysDiff = ChronoUnit.DAYS.between(currentDate, day.localDate)
            return if (daysDiff == 1L) "Starts tomorrow!" else "Starts in $daysDiff days"
        }
        if (currentDate.isAfter(day.localDate)) {
            return "Day completed"
        }

        // Selected day is today!
        val currTime = currentDateTime.toLocalTime()
        val upcoming = day.activities.find { it.getLocalTime().isAfter(currTime) }

        return if (upcoming != null) {
            val minutesLeft = ChronoUnit.MINUTES.between(currTime, upcoming.getLocalTime())
            val hours = minutesLeft / 60
            val minutes = minutesLeft % 60
            if (hours > 0) {
                "Next: ${upcoming.title} in ${hours}h ${minutes}m"
            } else {
                "Next: ${upcoming.title} in ${minutes}m"
            }
        } else {
            "No more activities today"
        }
    }
}

class TripViewModelFactory(
    private val application: Application,
    private val repository: ChecklistRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
