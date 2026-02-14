package com.example.eventfinder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.eventfinder.ui.components.EventCard
import com.example.eventfinder.viewmodel.SharedViewModel
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SharedViewModel,
    onBackClick: () -> Unit,
    onEventClick: (String) -> Unit
) {
    // Default values from ViewModel
    val savedKeyword by viewModel.searchKeyword.collectAsState()
    val savedLocation by viewModel.searchLocation.collectAsState()
    val savedDistance by viewModel.searchDistance.collectAsState()
    val savedCategory by viewModel.searchCategory.collectAsState()

    // Local state - use saved values or defaults
    var keyword by remember { mutableStateOf(savedKeyword.ifBlank { "" }) }
    var location by remember { mutableStateOf(savedLocation.ifBlank { "Current Location" }) }
    var distance by remember { mutableStateOf(savedDistance.ifBlank { "10" }) }
    var category by remember { mutableStateOf(savedCategory.ifBlank { "All" }) }
    
    // Validation states
    var isKeywordError by remember { mutableStateOf(false) }
    var keywordErrorText by remember { mutableStateOf("") }
    var isDistanceError by remember { mutableStateOf(false) }
    var distanceErrorText by remember { mutableStateOf("") }
    var skipSuggestionFetch by remember { mutableStateOf(false) }
    var skipLocationFetch by remember { mutableStateOf(false) }

    // Dropdown states
    var showKeywordSuggestions by remember { mutableStateOf(false) }
    var showLocationDropdown by remember { mutableStateOf(false) }
    var hasSearchedOnce by remember { mutableStateOf(false) }
    
    // Focus states - dropdowns only show when fields are focused
    var isKeywordFocused by remember { mutableStateOf(false) }
    var isLocationFocused by remember { mutableStateOf(false) }

    val results = viewModel.searchResults.collectAsState()
    val isSearching = viewModel.isSearching.collectAsState()
    val error = viewModel.searchError.collectAsState()
    val suggestions = viewModel.suggestions.collectAsState()
    val locationSuggestions = viewModel.locationSuggestions.collectAsState()
    val isLoadingLocationSuggestions = viewModel.isLoadingLocationSuggestions.collectAsState()
    val focusManager = LocalFocusManager.current

    val categories = listOf("All", "Music", "Sports", "Arts & Theatre", "Film", "Miscellaneous")
    val MAX_DISTANCE = 500

    // Auto-search when category changes (only if already searched once)
    LaunchedEffect(category) {
        if (hasSearchedOnce && keyword.isNotBlank()) {
            val dist = distance.toIntOrNull() ?: 10
            if (dist in 1..MAX_DISTANCE) {
                viewModel.searchEvents(keyword, dist, category, location)
            }
        }
    }

    // Fetch keyword suggestions when keyword changes (with debounce)
    LaunchedEffect(keyword) {
        if (skipSuggestionFetch) {
            skipSuggestionFetch = false
            return@LaunchedEffect
        }
        if (keyword.isNotEmpty()) {
            kotlinx.coroutines.delay(300)
            viewModel.fetchSuggestions(keyword)
        } else {
            viewModel.clearSuggestions()
        }
    }

    // Fetch location suggestions when location changes (with debounce)
    LaunchedEffect(location) {
        if (skipLocationFetch) {
            skipLocationFetch = false
            return@LaunchedEffect
        }
        if (location.isNotEmpty() && location != "Current Location") {
            kotlinx.coroutines.delay(300)
            viewModel.fetchLocationSuggestions(location)
        } else {
            viewModel.clearLocationSuggestions()
        }
    }

    // Update showKeywordSuggestions based on suggestions list AND focus state
    LaunchedEffect(suggestions.value, isKeywordFocused) {
        showKeywordSuggestions = suggestions.value.isNotEmpty() && keyword.isNotEmpty() && isKeywordFocused
    }

    fun validateAndSearch(): Boolean {
        // Keyword validation
        if (keyword.isBlank()) {
            isKeywordError = true
            keywordErrorText = "Please enter a keyword"
            return false
        } else {
            isKeywordError = false
            keywordErrorText = ""
        }

        // Distance validation
        val dist = distance.toIntOrNull()
        when {
            dist == null || distance.isBlank() -> {
                isDistanceError = true
                distanceErrorText = "Please enter a valid distance"
                return false
            }
            dist <= 0 -> {
                isDistanceError = true
                distanceErrorText = "Distance must be greater than 0"
                return false
            }
            dist > MAX_DISTANCE -> {
                isDistanceError = true
                distanceErrorText = "Distance cannot exceed $MAX_DISTANCE miles"
                return false
            }
            else -> {
                isDistanceError = false
                distanceErrorText = ""
            }
        }

        return true
    }

    fun performSearch() {
        if (!validateAndSearch()) return
        
        focusManager.clearFocus()
        viewModel.clearSuggestions()
        viewModel.clearLocationSuggestions()
        showKeywordSuggestions = false
        showLocationDropdown = false
        hasSearchedOnce = true
        
        val dist = distance.toIntOrNull() ?: 10
        viewModel.searchEvents(keyword, dist, category, location)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = keyword,
                        onValueChange = {
                            keyword = it
                            viewModel.updateSearchKeyword(it)
                            if (it.isNotEmpty()) {
                                isKeywordError = false
                                keywordErrorText = ""
                            }
                        },
                        placeholder = { Text("Search events...", color = Color.Gray) },
                        singleLine = true,
                        isError = isKeywordError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { performSearch() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            cursorColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                isKeywordFocused = focusState.isFocused
                                if (!focusState.isFocused) {
                                    showKeywordSuggestions = false
                                }
                            }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { performSearch() }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Keyword validation message
                if (isKeywordError && keywordErrorText.isNotBlank()) {
                    Text(
                        text = keywordErrorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Search Form Container
                Surface(color = MaterialTheme.colorScheme.primaryContainer) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Location & Distance Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Location icon
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Location input with dropdown
                            TextField(
                                value = location,
                                onValueChange = {
                                    location = it
                                    viewModel.updateSearchLocation(it)
                                    // Only show dropdown if field is focused
                                    if (isLocationFocused) {
                                        showLocationDropdown = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged { focusState ->
                                        isLocationFocused = focusState.isFocused
                                        if (focusState.isFocused) {
                                            showLocationDropdown = true
                                        } else {
                                            showLocationDropdown = false
                                        }
                                    },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    cursorColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            showLocationDropdown = !showLocationDropdown
                                        }
                                    ) {
                                        Icon(
                                            if (showLocationDropdown) Icons.Default.KeyboardArrowUp 
                                            else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Distance arrow
                            Text(
                                text = "↔",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Distance Input
                            TextField(
                                value = distance,
                                onValueChange = {
                                    val filtered = it.filter { c -> c.isDigit() }
                                    distance = filtered
                                    viewModel.updateSearchDistance(filtered)
                                    // Clear error when user types
                                    if (filtered.isNotBlank()) {
                                        val dist = filtered.toIntOrNull()
                                        if (dist != null && dist > 0 && dist <= MAX_DISTANCE) {
                                            isDistanceError = false
                                            distanceErrorText = ""
                                        }
                                    }
                                },
                                modifier = Modifier.width(70.dp),
                                singleLine = true,
                                isError = isDistanceError,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    cursorColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "mi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Distance validation error
                        if (isDistanceError && distanceErrorText.isNotBlank()) {
                            Text(
                                text = distanceErrorText,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Tabs
                        ScrollableTabRow(
                            selectedTabIndex = categories.indexOf(category).coerceAtLeast(0),
                            edgePadding = 0.dp,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary,
                            indicator = { tabPositions ->
                                val index = categories.indexOf(category).coerceAtLeast(0)
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            divider = {},
                            modifier = Modifier.padding(horizontal = 0.dp)
                        ) {
                            categories.forEach { cat ->
                                Tab(
                                    selected = category == cat,
                                    onClick = {
                                        if (category != cat) {
                                            category = cat
                                            viewModel.updateSearchCategory(cat)
                                        }
                                    },
                                    text = { Text(cat) }
                                )
                            }
                        }
                    }
                }

                // Results List
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    if (isSearching.value) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (!error.value.isNullOrEmpty()) {
                        Text(
                            text = error.value!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (results.value.isEmpty() && !isSearching.value) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (hasSearchedOnce) "No events found" else "Search for events",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    } else {
                        LazyColumn {
                            items(results.value) { event ->
                                EventCard(
                                    event = event,
                                    onClick = { onEventClick(event.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(event) }
                                )
                            }
                        }
                    }
                }
            }

            // Keyword Suggestions Dropdown (overlay)
            if (showKeywordSuggestions && suggestions.value.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 56.dp)
                        .zIndex(20f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column {
                        suggestions.value.take(5).forEach { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        skipSuggestionFetch = true
                                        keyword = suggestion
                                        viewModel.updateSearchKeyword(suggestion)
                                        viewModel.clearSuggestions()
                                        showKeywordSuggestions = false
                                        performSearch()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Click outside to dismiss dropdowns (invisible overlay) - MUST be before dropdowns
            if (showLocationDropdown || showKeywordSuggestions) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(15f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showLocationDropdown = false
                            showKeywordSuggestions = false
                            viewModel.clearSuggestions()
                            viewModel.clearLocationSuggestions()
                        }
                )
            }

            // Location Dropdown (overlay) - matches the screenshot design
            if (showLocationDropdown) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 100.dp)
                        .padding(top = if (isKeywordError) 100.dp else 76.dp)
                        .zIndex(20f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column {
                        // Current Location option with icon
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    skipLocationFetch = true
                                    location = "Current Location"
                                    viewModel.updateSearchLocation("Current Location")
                                    viewModel.clearLocationSuggestions()
                                    showLocationDropdown = false
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Current Location (Los Angeles, CA)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Loading indicator - match screenshot (spinner + text)
                        if (isLoadingLocationSuggestions.value) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Searching...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Location suggestions
                        locationSuggestions.value.forEach { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        skipLocationFetch = true
                                        location = suggestion
                                        viewModel.updateSearchLocation(suggestion)
                                        viewModel.clearLocationSuggestions()
                                        showLocationDropdown = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
