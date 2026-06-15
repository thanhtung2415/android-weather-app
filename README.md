# Android Weather App

A simple Android weather app built with Kotlin and Jetpack Compose.

## Overview

This project is part of my mobile app development portfolio. It focuses on REST API integration, JSON parsing, loading/error states, and a clean mobile UI.

The app uses the public Open-Meteo APIs, so it does not require an API key.

## Features

- Search weather by city name
- Select a quick city shortcut for common test locations
- Display city, country, temperature, weather condition, humidity, and wind speed
- Show loading state while requesting data
- Show error state when the city cannot be found or the request fails
- Use a public geocoding API to find latitude and longitude
- Use a public forecast API to load current weather

## How to Run

1. Open the project in Android Studio.
2. Wait for Gradle sync to finish.
3. Start an Android emulator or connect a real device.
4. Run the `app` configuration.
5. Search for a city such as `Ho Chi Minh City`, `Hanoi`, or `Tokyo`.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Open-Meteo API
- Android Studio
- Git/GitHub

## API

- Geocoding: `https://geocoding-api.open-meteo.com`
- Forecast: `https://api.open-meteo.com`

## What I Practiced

- Building UI with Jetpack Compose
- Handling user input
- Adding quick city shortcuts
- Running network requests outside the main thread
- Parsing JSON with `JSONObject`
- Managing loading, success, and error states
- Preparing a project for GitHub portfolio use

## Planned Improvements

- Add recent searched cities
- Add better weather icons
- Add daily forecast
- Add screenshots and demo video
