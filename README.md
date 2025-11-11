# 🚖 Koursa — Smart Taxi Meter App 🌍

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=FFD700&height=100&section=header&text=Koursa%20🚖&fontSize=45&fontColor=fff&animation=twinkling"/>
</p>

<p align="center">
  <img src="https://media.giphy.com/media/fxSwrPOlY3l3I/giphy.gif" width="200px" alt="Taxi Animation"/>
</p>

<div align="center">

### 💛 *A Real-Time Taxi Meter App built with Kotlin & Google Maps*

<em>Koursa helps taxi drivers track distance, time, and fare dynamically — with a clean UI, live GPS tracking, and smart notifications.</em>  
<br>

![Kotlin](https://img.shields.io/badge/Kotlin-Android-blueviolet?style=for-the-badge&logo=kotlin&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-IDE-green?style=for-the-badge&logo=androidstudio&logoColor=white)
![Google Maps](https://img.shields.io/badge/Google%20Maps-API-red?style=for-the-badge&logo=googlemaps&logoColor=white)
![EasyPermissions](https://img.shields.io/badge/EasyPermissions-Library-lightgrey?style=for-the-badge)
![Notifications](https://img.shields.io/badge/Android-Notifications-blue?style=for-the-badge&logo=android&logoColor=white)
![Localization](https://img.shields.io/badge/Multilingual-FR|EN|AR-yellow?style=for-the-badge)

---

[![GitHub stars](https://img.shields.io/github/stars/yourusername/Koursa?style=social)](https://github.com/yourusername/Koursa)
[![GitHub forks](https://img.shields.io/github/forks/yourusername/Koursa?style=social)](https://github.com/yourusername/Koursa)
[![GitHub issues](https://img.shields.io/github/issues/yourusername/Koursa)](https://github.com/yourusername/Koursa/issues)

</div>

---

## ✨ About Koursa

**Koursa** is a smart **Android taxi meter** app designed to simulate real-world taxi tracking and fare calculation.  
It combines **real-time GPS tracking**, **dynamic pricing**, and **driver profile management** — wrapped in a smooth and professional design.

<p align="center">
  <img src="https://media.giphy.com/media/xUOwGfpWxeCAd0Y1fi/giphy.gif" width="600px" alt="Map Animation"/>
</p>

---

## 🚀 Features

### 🚗 **Real-Time Taxi Counter**
- Calculates **fare** based on **distance (km)** and **time (min)**  
- Parameters:
  - **Base fare:** 2.5 DH  
  - **Per km:** 1.5 DH  
  - **Per minute:** 0.5 DH  
- Updates instantly when the driver moves.

### 🗺️ **Live Google Map**
- Displays the **driver’s position in real time**
- Integrated with **FusedLocationProviderClient**
- Smooth marker animation and live camera updates

### 🔔 **Smart Notifications**
- Alerts the driver when the trip ends  
- Displays **total fare**, **distance**, and **duration**
- Custom notification layout using `NotificationCompat`

### 👨‍✈️ **Driver Profile + QR Code**
- Shows driver info (name, age, license type)
- Generates a **QR Code** containing driver data
- Professional profile card design

### 🔐 **Permission Handling**
- Uses **EasyPermissions** for simple runtime permissions
- Handles foreground & background location
- Smooth UX for permission requests

### 🌍 **Multilingual Interface**
- Available in:
  - 🇫🇷 **Français**
  - 🇬🇧 **English**
  - 🇸🇦 **العربية**
- Automatic language detection based on system locale

---

## 🧮 Fare Calculation Formula

```text
Total Fare = Base Fare + (Distance × RatePerKm) + (Time × RatePerMinute)

Example:
Total Fare = 2.5 + (10 × 1.5) + (20 × 0.5)
Total Fare = 27.5 DH
