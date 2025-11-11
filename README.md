

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

---

## 📸 Screenshots

---

### **2. Login & Registration Pages**

| Login Page | Registration Page |
|:-----------------:|:-----------------:|
| <img src="https://github.com/BouglaceMarouane/Application-Gestion-Examen/blob/46fda12b1ae5c9713812f89aa893d78ddc66c053/images/login.png" alt="Login Page" width="300"/> | <img src="https://github.com/BouglaceMarouane/Application-Gestion-Examen/blob/46fda12b1ae5c9713812f89aa893d78ddc66c053/images/register.png" alt="Registration Page" width="300"/> |
| 🔑 *Login Page – Secure login for administrators, teachers, and students.* | 📝 *Registration Page – Allows students to register with their details.* |

---

---

## 🧰 Technologies Used

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white) ![Google Maps API](https://img.shields.io/badge/Google_Maps-API-4285F4?logo=googlemaps&logoColor=white) ![Fused Location](https://img.shields.io/badge/Fused_Location-GPS-34A853?logo=googleplay&logoColor=white) ![Room](https://img.shields.io/badge/Room-Database-42A5F5?logo=sqlite&logoColor=white) ![Retrofit](https://img.shields.io/badge/Retrofit-Networking-square?logo=square&logoColor=white) <br> ![Notifications](https://img.shields.io/badge/Notifications-Local_Push-FF9800?logo=android&logoColor=white) ![LottieFiles](https://img.shields.io/badge/LottieFiles-Animations-00DDB3?logo=lottiefiles&logoColor=white) ![ZXing](https://img.shields.io/badge/ZXing-QR_Code-000000?logo=android&logoColor=white)

</div>

---

## 🛠️ Installation & Setup

1️⃣ Clone the Repository
```bash
git clone [https://github.com/yourusername/Koursa.git](https://github.com/yourusername/Koursa.git)
```

### 2️⃣ Open in Android Studio
- Go to File → Open.
- Select the project folder.

### 3️⃣ Add Your API Key
Add your Google Maps API Key in AndroidManifest.xml (inside the <application> tag):
```code
<application>
   <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_API_KEY_HERE"/>
</application>
```

4️⃣ Run the App
- Connect an Android device or start an emulator.
- Click ▶️ Run to start Koursa.

---

## 🧭 Required Permissions

Ensure the following permissions are included in your AndroidManifest.xml:
```code
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.INTERNET"/>
```

---

## 🤝 Contributing

Contributions to this project are welcome! If you have suggestions, improvements, or bug fixes, please submit a pull request. Make sure to follow coding conventions and maintain consistent styles.

If you encounter issues or want to request a new feature, please open an issue in the repository with as much detail as possible.

### Ways to Contribute
- 🐛 **Report Bugs** - Found an issue? Let us know!
- 💡 **Suggest Features** - Have ideas? We'd love to hear them!
- 🔧 **Submit Pull Requests** - Code contributions are welcome
- 📖 **Improve Documentation** - Help make our docs better

### Getting Started
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## ⭐ Support

If you like this project, don't forget to leave a ⭐ on GitHub. Thank you and happy coding! 🚀

---

## 📬 Stay in Touch

<div align="center">

### 👨‍💻 **Marouane Bouglace** - *Project Creator*

[![Email](https://img.shields.io/badge/Email-bouglacemarouane@gmail.com-red?style=for-the-badge&logo=gmail&logoColor=white)](mailto:bouglacemarouane@gmail.com)
[![GitHub](https://img.shields.io/badge/GitHub-bouglacemarouane-black?style=for-the-badge&logo=github&logoColor=white)](https://github.com/bouglacemarouane)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Marouane%20Bouglace-blue?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/marouane-bouglace)

</div>

---

<div align="center">

**Thank you for visiting Koursa! 💬✨**

</div>

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&height=60&section=footer"/>
</p>
