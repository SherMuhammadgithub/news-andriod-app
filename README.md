# NewsFlash

An Android news app built with Jetpack Compose that fetches and displays the latest headlines using the [GNews API](https://gnews.io).

## Features

- Latest top headlines with real-time fetch
- Search news by keyword
- Filter by country and language (27 languages supported)
- Full article detail screen
- Opens original article in browser
- Splash screen with branded icon

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + StateFlow |
| Networking | Retrofit + Gson |
| Image Loading | Coil |
| Navigation | Navigation Compose |

## Screens

| Screen | Description |
|---|---|
| Splash | Branded launch screen |
| Home | Headlines feed with search and filters |
| Detail | Full article view with source, date, and content |

## Getting Started

1. Clone the repository
2. Get a free API key from [gnews.io](https://gnews.io)
3. Replace the key in `NewsApiService.kt`
4. Build and run on Android 7.0+ (API 24+)

## Download

Latest debug APK available in [Releases](../../releases).
