package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripAppContent(viewModel: TripViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State collections
    val simulatedDateTime by viewModel.simulatedDateTime.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()
    val selectedDayIndex by viewModel.selectedDayIndex.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()

    val day1Items by viewModel.day1Checklist.collectAsState()
    val packingItems by viewModel.packingList.collectAsState()

    // View categories / Sheet overrides
    var activeSubPage by remember { mutableStateOf("timeline") } // "timeline", "packing", "hotel", "emergency"

    // Horizontal Pager for Days
    val pagerState = rememberPagerState(pageCount = { 5 })

    // Sync Pager and ViewModel day index
    LaunchedEffect(selectedDayIndex) {
        if (pagerState.currentPage != selectedDayIndex) {
            pagerState.animateScrollToPage(selectedDayIndex)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectDayIndex(pagerState.currentPage)
    }

    // Dynamic date and status labels
    val formattedSimulatedTime = simulatedDateTime.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy, HH:mm"))
    val currentDayIndex = TripData.determineTripDayIndex(simulatedDateTime.toLocalDate())

    // Theme backgrounds & linear gradients
    val backgroundBrush = if (isSystemInDarkTheme()) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A), // Slate 900
                Color(0xFF020617)  // Slate 950
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF1F5F9), // Slate 100
                Color(0xFFE2E8F0)  // Slate 200
            )
        )
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🇫🇷 Paris Trip 2026",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            modifier = Modifier.testTag("app_title")
                        )
                    }
                },
                actions = {
                    // Refresh weather
                    IconButton(
                        onClick = { viewModel.fetchWeather() },
                        modifier = Modifier.testTag("refresh_weather_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Weather")
                    }
                    // Test notification trigger
                    IconButton(
                        onClick = {
                            NotificationScheduler.triggerInstantTestNotification(context)
                            Toast.makeText(context, "Notification will arrive in 2s!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("test_notif_button")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Test Notification")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Column {
                // Bottom Tab selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabNavigationButton(
                        label = "Itinerary",
                        icon = Icons.Default.CalendarToday,
                        active = activeSubPage == "timeline",
                        onClick = { activeSubPage = "timeline" },
                        testTag = "nav_itinerary"
                    )
                    TabNavigationButton(
                        label = "Packing",
                        icon = Icons.Default.Luggage,
                        active = activeSubPage == "packing",
                        onClick = { activeSubPage = "packing" },
                        testTag = "nav_packing"
                    )
                    TabNavigationButton(
                        label = "Hotel",
                        icon = Icons.Default.Hotel,
                        active = activeSubPage == "hotel",
                        onClick = { activeSubPage = "hotel" },
                        testTag = "nav_hotel"
                    )
                    TabNavigationButton(
                        label = "Emergency",
                        icon = Icons.Default.LocalHospital,
                        active = activeSubPage == "emergency",
                        onClick = { activeSubPage = "emergency" },
                        testTag = "nav_emergency"
                    )
                }

                // If on itinerary view, display Day Select Navigation
                if (activeSubPage == "timeline") {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        tonalElevation = 8.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        val days = TripData.getItineraryDays()
                        days.forEachIndexed { index, day ->
                            NavigationBarItem(
                                selected = selectedDayIndex == index,
                                onClick = { viewModel.selectDayIndex(index) },
                                label = { Text("Day ${day.dayNumber}", fontWeight = FontWeight.SemiBold) },
                                icon = {
                                    Icon(
                                        imageVector = getDayIcon(day.dayNumber),
                                        contentDescription = "Day ${day.dayNumber}"
                                    )
                                },
                                modifier = Modifier.testTag("day_button_$index")
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time simulation status banner
                item {
                    SimulationStatusCard(
                        simulatedTime = formattedSimulatedTime,
                        isSimulating = isSimulating,
                        currentDayIndex = currentDayIndex,
                        onSimulateToggle = { viewModel.setSimulating(it) },
                        onPresetClick = { dateTime -> viewModel.updateSimulatedDateTime(dateTime) }
                    )
                }

                // Dynamic progress / countdown widget
                item {
                    DynamicTripStatusCard(
                        viewModel = viewModel,
                        selectedDayIndex = selectedDayIndex,
                        simulatedDateTime = simulatedDateTime
                    )
                }

                // Weather section
                item {
                    WeatherWidget(state = weatherState)
                }

                // Render active category page
                when (activeSubPage) {
                    "timeline" -> {
                        item {
                            Text(
                                text = "Daily Itineraries",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "Swipe left/right to browse days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        item {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 400.dp, max = 1500.dp)
                                    .testTag("itinerary_pager")
                            ) { pageIndex ->
                                val day = TripData.getItineraryDays()[pageIndex]
                                ItineraryDayPage(
                                    day = day,
                                    day1Checklist = day1Items,
                                    onChecklistToggle = { viewModel.toggleChecklistItem(it) }
                                )
                            }
                        }
                    }
                    "packing" -> {
                        item {
                            PackingListSection(
                                items = packingItems,
                                onToggle = { viewModel.toggleChecklistItem(it) },
                                onAdd = { viewModel.addPackingItem(it) },
                                onDelete = { viewModel.deletePackingItem(it) }
                            )
                        }
                    }
                    "hotel" -> {
                        item {
                            HotelInfoSection()
                        }
                    }
                    "emergency" -> {
                        item {
                            EmergencyContactsSection()
                        }
                    }
                }

                // Extra safety padding at the bottom of list
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun TabNavigationButton(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .width(64.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SimulationStatusCard(
    simulatedTime: String,
    isSimulating: Boolean,
    currentDayIndex: Int,
    onSimulateToggle: (Boolean) -> Unit,
    onPresetClick: (LocalDateTime) -> Unit
) {
    var expandedPresets by remember { mutableStateOf(false) }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("simulation_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSimulating) "🕰️ Simulation Mode Active" else "📅 Local Device Time",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isSimulating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = simulatedTime,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                // Dynamic Current Day Status indicator
                Spacer(modifier = Modifier.height(4.dp))
                val statusText = when (currentDayIndex) {
                    -1 -> "Your Paris trip hasn't started yet."
                    -2 -> "Your Paris trip has finished."
                    in 0..4 -> "Currently on Day ${currentDayIndex + 1} of the trip!"
                    else -> "No active trip date"
                }
                Text(
                    text = statusText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = if (currentDayIndex >= 0) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = isSimulating,
                    onCheckedChange = onSimulateToggle,
                    modifier = Modifier.testTag("simulate_switch")
                )
            }
        }

        if (isSimulating) {
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedPresets = !expandedPresets },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fast-Travel Simulation Presets",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Icon(
                    imageVector = if (expandedPresets) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Presets",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expandedPresets) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Choose a trip activity to set the clock to its start time:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    PresetRow(
                        label = "Day 1: Meet (01:30)",
                        onClick = { onPresetClick(LocalDateTime.of(2026, 7, 12, 1, 30)) }
                    )
                    PresetRow(
                        label = "Day 2: Louvre Museum (13:00)",
                        onClick = { onPresetClick(LocalDateTime.of(2026, 7, 13, 13, 0)) }
                    )
                    PresetRow(
                        label = "Day 3: Asterix Park (10:00)",
                        onClick = { onPresetClick(LocalDateTime.of(2026, 7, 14, 10, 0)) }
                    )
                    PresetRow(
                        label = "Day 4: Eiffel Tower (18:45)",
                        onClick = { onPresetClick(LocalDateTime.of(2026, 7, 15, 18, 45)) }
                    )
                    PresetRow(
                        label = "Day 5: Ferry Home (14:20)",
                        onClick = { onPresetClick(LocalDateTime.of(2026, 7, 16, 14, 20)) }
                    )
                }
            }
        }
    }
}

@Composable
fun PresetRow(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Schedule, contentDescription = null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DynamicTripStatusCard(
    viewModel: TripViewModel,
    selectedDayIndex: Int,
    simulatedDateTime: LocalDateTime
) {
    val countdown = viewModel.getNextActivityCountdown(selectedDayIndex, simulatedDateTime)
    val progress = viewModel.getSelectedDayCompletedProgress(selectedDayIndex, simulatedDateTime)
    val animatedProgress by animateFloatAsState(targetValue = progress)

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dynamic_status_card")
    ) {
        Text(
            text = "Trip Status (Day ${selectedDayIndex + 1})",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Countdown Text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = countdown,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Day Progress",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun WeatherWidget(state: WeatherUiState) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weather_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Paris Weather",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                when (state) {
                    is WeatherUiState.Loading -> {
                        Text("Retrieving current weather...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    is WeatherUiState.Success -> {
                        Text("${state.conditionText} in Paris", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    is WeatherUiState.Error -> {
                        Text(state.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        Text("${state.fallbackData.conditionText} (Average)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Temperature Circle
            val temp = when (state) {
                is WeatherUiState.Loading -> 24.5
                is WeatherUiState.Success -> state.temperature
                is WeatherUiState.Error -> state.fallbackData.temperature
            }
            val icon = when (state) {
                is WeatherUiState.Loading -> "☀️"
                is WeatherUiState.Success -> state.conditionIcon
                is WeatherUiState.Error -> state.fallbackData.conditionIcon
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${temp.toInt()}°C",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        Spacer(modifier = Modifier.height(10.dp))

        // Forecast days horizontally
        val forecast = when (state) {
            is WeatherUiState.Loading -> emptyList()
            is WeatherUiState.Success -> state.forecast
            is WeatherUiState.Error -> state.fallbackData.forecast
        }

        if (forecast.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                forecast.forEach { day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(day.dayLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(day.conditionIcon, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${day.maxTemp.toInt()}°", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${day.minTemp.toInt()}°", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun ItineraryDayPage(
    day: DayInfo,
    day1Checklist: List<ChecklistItem>,
    onChecklistToggle: (ChecklistItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Day Page Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Day ${day.dayNumber}: ${day.title}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = day.dateLabel,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    imageVector = getDayIcon(day.dayNumber),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activities list
            Text(
                text = "Timeline & Schedule",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                day.activities.forEach { activity ->
                    ActivityItem(activity = activity)
                }
            }

            // If Day 1, display Day 1 Checklist
            if (day.dayNumber == 1) {
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "🚨 Required Checklist Day 1",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Ensure all essentials are checked off before departure",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                day1Checklist.forEach { item ->
                    ChecklistRow(item = item, onToggle = { onChecklistToggle(item) })
                }
            }
        }
    }
}

@Composable
fun ActivityItem(activity: TripActivity) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Badge
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = activity.timeLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (activity.description.isNotEmpty()) {
                    Text(
                        text = activity.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = activity.location,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Map button
            IconButton(
                onClick = { openMap(context, activity.mapsQuery) },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    .testTag("map_btn_${activity.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Open in Maps",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChecklistRow(item: ChecklistItem, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
            color = if (item.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PackingListSection(
    items: List<ChecklistItem>,
    onToggle: (ChecklistItem) -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (Int) -> Unit
) {
    var textState by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("packing_list_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "💼 General Packing List",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Customized list saved offline",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(Icons.Default.CardTravel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Input field to add custom items
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textState,
                onValueChange = { textState = it },
                placeholder = { Text("Add custom item...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_item_input"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (textState.isNotBlank()) {
                        onAdd(textState)
                        textState = ""
                        keyboardController?.hide()
                    }
                }),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (textState.isNotBlank()) {
                        onAdd(textState)
                        textState = ""
                        keyboardController?.hide()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("add_item_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Your packing list is empty! Add items above.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onToggle(item) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { onToggle(item) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.name,
                                fontSize = 14.sp,
                                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (item.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Allow deleting custom items
                        IconButton(
                            onClick = { onDelete(item.id) },
                            modifier = Modifier.testTag("delete_${item.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HotelInfoSection() {
    val context = LocalContext.current
    val hotel = TripData.hotelInfo

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hotel_info_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🏨 Hotel Accommodations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = hotel.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        Spacer(modifier = Modifier.height(12.dp))

        HotelDetailRow(label = "Address", value = hotel.address, icon = Icons.Default.Place, clickable = true, onClick = {
            openMap(context, hotel.address)
        })
        HotelDetailRow(label = "Phone", value = hotel.phone, icon = Icons.Default.Phone, clickable = true, onClick = {
            dialPhone(context, hotel.phone)
        })
        HotelDetailRow(label = "Check-In", value = hotel.checkInTime, icon = Icons.Default.Login)
        HotelDetailRow(label = "Check-Out", value = hotel.checkOutTime, icon = Icons.Default.Logout)

        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, contentDescription = "Wifi", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Complimentary High-Speed Wi-Fi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Network: ${hotel.wifiSsid}", fontSize = 12.sp)
                Text("Password: ${hotel.wifiPassword}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = hotel.details,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            lineHeight = 16.sp
        )
    }
}

@Composable
fun HotelDetailRow(label: String, value: String, icon: ImageVector, clickable: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (clickable) TextDecoration.Underline else TextDecoration.None,
                color = if (clickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmergencyContactsSection() {
    val context = LocalContext.current
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("emergency_contacts_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📞 Emergency Contact Directory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Tap to call directly from app",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        Spacer(modifier = Modifier.height(12.dp))

        TripData.emergencyContacts.forEach { contact ->
            Surface(
                onClick = { dialPhone(context, contact.phone) },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (contact.name.contains("Emergency")) Icons.Default.LocalHospital else Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = if (contact.name.contains("Emergency")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${contact.role} • ${contact.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        if (contact.details.isNotEmpty()) {
                            Text(contact.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Contact",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Utility Helpers
fun getDayIcon(dayNumber: Int): ImageVector {
    return when (dayNumber) {
        1 -> Icons.Default.DirectionsBus
        2 -> Icons.Default.PhotoCamera
        3 -> Icons.Default.Attractions
        4 -> Icons.Default.Castle
        5 -> Icons.Default.Home
        else -> Icons.Default.CalendarToday
    }
}

fun openMap(context: Context, query: String) {
    val encodedQuery = Uri.encode(query)
    val geoUri = "geo:0,0?q=$encodedQuery"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)).apply {
        `package` = "com.google.android.apps.maps"
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val webUri = "https://www.google.com/maps/search/?api=1&query=$encodedQuery"
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri))
        try {
            context.startActivity(webIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, "No maps application or web browser found.", Toast.LENGTH_SHORT).show()
        }
    }
}

fun dialPhone(context: Context, phone: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot initiate call on this device.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
