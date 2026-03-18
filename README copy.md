# FXRatesApp

Android app to view HKD/CNY/USD exchange rates (EOD, free API) and set rate alerts.

## Data Source
Uses the free Frankfurter API (ECB reference rates). Daily updates around 16:00 CET.

## Features
- Latest rate
- 24h and 7d line charts (daily points)
- Daily K and Monthly K (derived from daily EOD)
- Local alert rules with notifications

## Build
Open the `FXRatesApp` folder in Android Studio and run the app module.

## Notes
- Alerts run via WorkManager (minimum 15-minute interval).
- This uses daily EOD rates, so alerts update on daily refresh.
