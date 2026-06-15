package com.thanhtung.androidweatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thanhtung.androidweatherapp.ui.theme.AndroidWeatherAppTheme
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidWeatherAppTheme {
                WeatherApp()
            }
        }
    }
}

private data class WeatherUiState(
    val cityInput: String = "Ho Chi Minh City",
    val weather: WeatherInfo? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

private data class WeatherInfo(
    val cityName: String,
    val country: String,
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
)

private data class CityLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
)

private val SuggestedCities = listOf(
    "Ho Chi Minh City",
    "Hanoi",
    "Da Nang",
    "Bangkok",
    "Tokyo",
)

@Composable
private fun WeatherApp() {
    var uiState by remember { mutableStateOf(WeatherUiState()) }
    val coroutineScope = rememberCoroutineScope()

    fun searchWeather() {
        val city = uiState.cityInput.trim()
        if (city.isEmpty()) {
            uiState = uiState.copy(errorMessage = "Please enter a city name.")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)
        coroutineScope.launch {
            uiState = try {
                val weather = fetchWeather(city)
                uiState.copy(weather = weather, isLoading = false, errorMessage = null)
            } catch (exception: Exception) {
                uiState.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Unable to load weather data.",
                )
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            WeatherHeader()
            SearchPanel(
                cityInput = uiState.cityInput,
                isLoading = uiState.isLoading,
                onCityInputChange = { uiState = uiState.copy(cityInput = it) },
                onQuickCitySelected = { city -> uiState = uiState.copy(cityInput = city) },
                onSearch = ::searchWeather,
            )

            when {
                uiState.isLoading -> LoadingState()
                uiState.errorMessage != null -> ErrorState(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = ::searchWeather,
                )

                uiState.weather != null -> WeatherCard(weather = uiState.weather!!)
                else -> WelcomeState()
            }
        }
    }
}

@Composable
private fun WeatherHeader() {
    Column {
        Text(
            text = "Thanh Tung Weather",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Search current weather by city using a public API.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SearchPanel(
    cityInput: String,
    isLoading: Boolean,
    onCityInputChange: (String) -> Unit,
    onQuickCitySelected: (String) -> Unit,
    onSearch: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            OutlinedTextField(
                value = cityInput,
                onValueChange = onCityInputChange,
                label = { Text("City name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                enabled = !isLoading,
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isLoading) "Searching..." else "Search weather")
            }
            QuickCityRow(onQuickCitySelected = onQuickCitySelected)
        }
    }
}

@Composable
private fun QuickCityRow(onQuickCitySelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Quick cities",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            SuggestedCities.forEach { city ->
                TextButton(onClick = { onQuickCitySelected(city) }) {
                    Text(city)
                }
            }
        }
    }
}

@Composable
private fun WeatherCard(weather: WeatherInfo) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = weather.cityName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = weather.country,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                WeatherBadge(code = weather.weatherCode)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "${weather.temperature.toInt()} C",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = weatherDescription(weather.weatherCode),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WeatherMetric(
                    label = "Humidity",
                    value = "${weather.humidity}%",
                    modifier = Modifier.weight(1f),
                )
                WeatherMetric(
                    label = "Wind",
                    value = "${weather.windSpeed.toInt()} km/h",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WeatherBadge(code: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        contentColor = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
        modifier = Modifier.size(76.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = weatherLabel(code),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun WeatherMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(28.dp),
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text("Loading weather data...")
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Could not load weather",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(message)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text("Try again")
            }
        }
    }
}

@Composable
private fun WelcomeState() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Start with a city search",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Example: Ho Chi Minh City, Hanoi, Bangkok, Tokyo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private suspend fun fetchWeather(city: String): WeatherInfo {
    return withContext(Dispatchers.IO) {
        val location = fetchLocation(city)
        val forecastUrl =
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${location.latitude}" +
                "&longitude=${location.longitude}" +
                "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code" +
                "&timezone=auto"

        val forecastJson = JSONObject(readTextFromUrl(forecastUrl))
        val current = forecastJson.getJSONObject("current")

        WeatherInfo(
            cityName = location.name,
            country = location.country,
            temperature = current.getDouble("temperature_2m"),
            humidity = current.getInt("relative_humidity_2m"),
            windSpeed = current.getDouble("wind_speed_10m"),
            weatherCode = current.getInt("weather_code"),
        )
    }
}

private fun fetchLocation(city: String): CityLocation {
    val encodedCity = URLEncoder.encode(city, "UTF-8")
    val locationUrl =
        "https://geocoding-api.open-meteo.com/v1/search" +
            "?name=$encodedCity&count=1&language=en&format=json"
    val locationJson = JSONObject(readTextFromUrl(locationUrl))
    val results = locationJson.optJSONArray("results")
        ?: throw IllegalStateException("No city found. Try a different city name.")

    if (results.length() == 0) {
        throw IllegalStateException("No city found. Try a different city name.")
    }

    val firstResult = results.getJSONObject(0)
    return CityLocation(
        name = firstResult.getString("name"),
        country = firstResult.optString("country", "Unknown country"),
        latitude = firstResult.getDouble("latitude"),
        longitude = firstResult.getDouble("longitude"),
    )
}

private fun readTextFromUrl(url: String): String {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.connectTimeout = 10_000
    connection.readTimeout = 10_000

    return try {
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("Request failed with code ${connection.responseCode}.")
        }
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

private fun weatherDescription(code: Int): String {
    return when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Foggy"
        51, 53, 55, 56, 57 -> "Drizzle"
        61, 63, 65, 66, 67 -> "Rain"
        71, 73, 75, 77 -> "Snow"
        80, 81, 82 -> "Rain showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Weather code $code"
    }
}

private fun weatherLabel(code: Int): String {
    return when (code) {
        0 -> "SUN"
        1, 2, 3 -> "CLOUD"
        45, 48 -> "FOG"
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> "RAIN"
        71, 73, 75, 77 -> "SNOW"
        95, 96, 99 -> "STORM"
        else -> "INFO"
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherAppPreview() {
    AndroidWeatherAppTheme {
        Surface(modifier = Modifier.size(width = 390.dp, height = 820.dp)) {
            WeatherApp()
        }
    }
}
