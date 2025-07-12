# GlowCorner Mobile App 🌟

A modern Android e-commerce application for cosmetics and beauty products with professional payment processing and user management.

## ✨ Features

- **User Authentication**: Secure login/register with JWT tokens
- **Product Catalog**: Browse cosmetics with categories and search
- **Shopping Cart**: Add/remove products with discount support
- **Payment System**: Professional checkout with card payment simulation
- **Order Management**: Track orders with real-time status updates
- **Profile Management**: User profiles with order history
- **Discount System**: Apply promotional codes and discounts
- **Camera Integration**: Take profile photos
- **Modern UI**: Material Design with CardViews and smooth animations

## 🛠️ Technology Stack

- **Android SDK**: Native Android development
- **Java**: Primary programming language
- **Retrofit**: REST API client for backend communication
- **Material Design**: Modern UI components
- **SharedPreferences**: Local data storage
- **RecyclerView**: Efficient list displays
- **JWT Authentication**: Secure user sessions

## 📱 Screenshots

[Add screenshots of your app here]

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 21+
- Java 8+

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/glowcorner-mobile.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Update the API base URL in `ApiClient.java`:
   ```java
   private static final String BASE_URL = "http://your-backend-url/";
   ```

5. Build and run the app on your device or emulator

## 🔧 Configuration

### API Configuration
Update the backend URL in `app/src/main/java/com/example/mobile/Api/ApiClient.java`:
```java
private static final String BASE_URL = "http://localhost:8080/";
```

### Payment Configuration
The app uses a simulated payment system. For real payments, integrate with:
- Stripe
- PayPal
- Other payment providers

## 📦 Project Structure

```
app/src/main/java/com/example/mobile/
├── Activities/           # Main app screens
├── Adapters/            # RecyclerView adapters
├── Api/                 # API interfaces and clients
├── Models/              # Data models
└── Utils/               # Utility classes
```

## 🔗 Related Projects

- **Backend API**: [GlowCorner Backend](https://github.com/yourusername/glowcorner-backend)

## 🤝 Contributing

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

- **Project Link**: [https://github.com/yourusername/glowcorner-mobile](https://github.com/yourusername/glowcorner-mobile)
- **Backend API**: [https://github.com/yourusername/glowcorner-backend](https://github.com/yourusername/glowcorner-backend)

## 🙏 Acknowledgments

- Material Design for UI inspiration
- Android Developer Documentation
- Retrofit for seamless API integration
