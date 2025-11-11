

<!--<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=FFD700&height=100&section=header&text=Koursa%20🚖&fontSize=45&fontColor=fff&animation=twinkling"/>
</p>-->

<p align="center">
  <img src="https://github.com/BouglaceMarouane/Koursa/blob/bbf627c01395ef39d3c6d79a513fc0a566acad60/Koursa%20-%20Edited.png" width="100%" alt="Taxi Animation"/>
</p>

<div align="center">

### 💛 *A Real-Time Taxi Meter App built with Kotlin & Google Maps*

<em>Koursa helps taxi drivers track distance, time, and fare dynamically — with a clean UI, live GPS tracking, and smart notifications.</em>  
<!--<br>

![Kotlin](https://img.shields.io/badge/Kotlin-Android-blueviolet?style=for-the-badge&logo=kotlin&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-IDE-green?style=for-the-badge&logo=androidstudio&logoColor=white)
![Google Maps](https://img.shields.io/badge/Google%20Maps-API-red?style=for-the-badge&logo=googlemaps&logoColor=white)
![EasyPermissions](https://img.shields.io/badge/EasyPermissions-Library-lightgrey?style=for-the-badge)
![Notifications](https://img.shields.io/badge/Android-Notifications-blue?style=for-the-badge&logo=android&logoColor=white)
![Localization](https://img.shields.io/badge/Multilingual-FR|EN|AR-yellow?style=for-the-badge)
-->
<br>

  [![GitHub stars](https://img.shields.io/github/stars/bouglacemarouane/Koursa?style=social)](https://github.com/bouglacemarouane/Koursa)
  [![GitHub forks](https://img.shields.io/github/forks/bouglacemarouane/Koursa?style=social)](https://github.com/bouglacemarouane/Koursa)
  [![GitHub issues](https://img.shields.io/github/issues/bouglacemarouane/Koursa)](https://github.com/bouglacemarouane/Koursa/issues)

</div>

---

## ✨ About Koursa

**Koursa** is a smart **Android taxi meter** app designed to simulate real-world taxi tracking and fare calculation. It combines **real-time GPS tracking**, **dynamic pricing**, and **driver profile management** — wrapped in a smooth and professional design.


<div align="center"> 
  <img src="https://github.com/BouglaceMarouane/Koursa/blob/118d5371ed8c4caaa1d3bbb7381b9bb878ee7863/presentation.jpg" width="100%" alt="Taxi Animation"/><br>
  <a href="https://www.canva.com/design/DAG4NOwueX4/3lBH5ujW-0nd9vfKwA2tdA/edit?utm_content=DAG4NOwueX4&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton" target="_blank"> 
    <img src="https://img.shields.io/badge/📊%20View%20the%20Presentation-00C4CC?style=for-the-badge&logo=canva&logoColor=white" alt="View Presentation"/> 
  </a> 
</div>

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

### 👨🚕 **Driver Profile + QR Code**
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
```

## 🧰 Technologies Used

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white) ![Google Maps API](https://img.shields.io/badge/Google_Maps-API-4285F4?logo=googlemaps&logoColor=white) ![Fused Location](https://img.shields.io/badge/Fused_Location-GPS-34A853?logo=googleplay&logoColor=white) ![Room](https://img.shields.io/badge/Room-Database-42A5F5?logo=sqlite&logoColor=white) ![Retrofit](https://img.shields.io/badge/Retrofit-Networking-square?logo=square&logoColor=white) <br> ![Notifications](https://img.shields.io/badge/Notifications-Local_Push-FF9800?logo=android&logoColor=white) ![LottieFiles](https://img.shields.io/badge/LottieFiles-Animations-00DDB3?logo=lottiefiles&logoColor=white) ![ZXing](https://img.shields.io/badge/ZXing-QR_Code-000000?logo=android&logoColor=white)

</div>

## 🎨 User Interface

| Element | Description |
|----------|-------------|
| ⚫ **Primary Color:** | `#000000` (Black) |
| 🟡 **Secondary Color:** | `#FFD700` (Taxi Yellow) |
| ⚪ **Accent Color:** | `#FFFFFF` (White) |
| 🔵 **Highlight:** | `#007BFF` (Blue) |
| 🟢 **Success:** | `#28A745` (Green) |
| 🔴 **Alert:** | `#FF0000` (Red) |
