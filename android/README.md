# Feather Wallet Android - Monero Кошелек для Android

## Обзор

Feather Wallet Android - это адаптация десктопного кошелька Feather Wallet для платформы Android. Приложение предоставляет безопасный и удобный способ управления Monero на мобильных устройствах.

## Архитектура

Проект использует современную архитектуру Android с разделением на слои:

### Слои приложения:

1. **Presentation Layer (UI)**
   - Jetpack Compose для UI
   - ViewModel для управления состоянием
   - Hilt для Dependency Injection

2. **Domain Layer**
   - Use Cases (Interactors) для бизнес-логики
   - Модели домена
   - Интерфейсы репозиториев

3. **Data Layer**
   - Репозитории и их реализации
   - JNI слой для взаимодействия с C++ библиотекой Monero
   - Локальное хранилище данных

### Структура проекта:

```
app/src/main/java/org/monero/feather/
├── data/
│   ├── local/           # JNI wrapper и нативные вызовы
│   └── repository/      # Реализации репозиториев
├── di/                  # Hilt модули для DI
├── domain/
│   ├── model/           # Модели домена
│   ├── repository/      # Интерфейсы репозиториев
│   └── usecase/         # Use cases
└── ui/
    ├── navigation/      # Навигация
    ├── screens/         # Экраны приложения
    ├── theme/           # Тема и стили
    └── viewmodel/       # ViewModel
```

## Технологический стек

### Основные технологии:
- **Kotlin** - основной язык разработки
- **Jetpack Compose** - современный UI toolkit
- **Hilt** - dependency injection
- **Coroutines & Flow** - асинхронное программирование
- **Navigation Compose** - навигация между экранами
- **NDK/JNI** - интеграция с C++ библиотекой Monero

### Архитектурные паттерны:
- **Clean Architecture** - разделение на слои
- **MVVM** - Model-View-ViewModel
- **Repository** - абстракция доступа к данным
- **Use Case** - инкапсуляция бизнес-логики

## Функциональность

### Реализованные компоненты:

#### Use Cases:
- `CreateWalletUseCase` - создание нового кошелька
- `RestoreWalletUseCase` - восстановление из seed-фразы
- `OpenWalletUseCase` - открытие существующего кошелька
- `CloseWalletUseCase` - закрытие кошелька
- `GetBalanceUseCase` - получение баланса
- `GetAddressUseCase` - получение адреса кошелька
- `GetTransactionsUseCase` - история транзакций
- `SendTransactionUseCase` - отправка транзакции

#### ViewModels:
- `WelcomeViewModel` - экран приветствия
- `LoginViewModel` - вход в кошелек
- `CreateWalletViewModel` - создание кошелька
- `RestoreWalletViewModel` - восстановление кошелька
- `MainViewModel` - главный экран
- `SendViewModel` - отправка Monero
- `ReceiveViewModel` - получение Monero
- `HistoryViewModel` - история транзакций
- `SettingsViewModel` - настройки
- `CoinsViewModel` - управление монетами

#### Экраны:
- Welcome Screen - выбор языка, приветствие
- Login Screen - ввод пароля
- Create Wallet Screen - создание нового кошелька
- Restore Wallet Screen - восстановление из seed
- Main Screen - баланс, адрес, статус синхронизации
- Send Screen - отправка транзакции
- Receive Screen - QR-код и адрес для получения
- History Screen - список транзакций
- Settings Screen - настройки приложения
- Coins Screen - управление монетами (coin control)

## Сборка и запуск

### Требования:
- Android Studio Arctic Fox или новее
- Android SDK 26+
- NDK 21+
- Kotlin 1.9+

### Шаги сборки:

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd android
```

2. Откройте проект в Android Studio

3. Синхронизируйте Gradle зависимости

4. Соберите и запустите на устройстве или эмуляторе

### Компиляция нативной библиотеки:

Для работы с Monero требуется компиляция libwallet2 для Android:

```bash
# В директории jni/
mkdir build && cd build
cmake .. -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK/build/cmake/android.toolchain.cmake \
         -DANDROID_ABI=arm64-v8a \
         -DANDROID_PLATFORM=android-26
make
```

## Безопасность

### Меры безопасности:
- Шифрование данных кошелька с использованием Android Keystore
- Biometric authentication (отпечаток, Face ID)
- Secure storage для чувствительных данных
- Защита от скриншотов в режиме инкогнито
- Автоматическая блокировка при бездействии

### Разрешения:
- `INTERNET` - синхронизация с сетью Monero
- `CAMERA` - сканирование QR-кодов
- `USE_BIOMETRIC` - биометрическая аутентификация
- `VIBRATE` - тактильная отдача

## Roadmap

### Ближайшие планы:
1. Интеграция реальной Monero библиотеки через JNI
2. Полная синхронизация с сетью
3. Поддержка нескольких кошельков
4. Tor integration для приватности
5. Hardware wallet support

### Долгосрочные цели:
- Поддержка других криптовалют
- DeFi интеграции
- P2P обмен
- Lightning Network (для BTC)

## Вклад в проект

Мы приветствуем вклад в развитие проекта! Пожалуйста:

1. Forkните репозиторий
2. Создайте feature branch (`git checkout -b feature/amazing-feature`)
3. Закоммитьте изменения (`git commit -m 'Add amazing feature'`)
4. Запушьте branch (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## Лицензия

Этот проект распространяется под лицензией MIT. См. файл LICENSE для деталей.

## Контакты

- Website: https://featherwallet.org
- Telegram: @featherwallet
- Email: support@featherwallet.org

## Благодарности

- Original Feather Wallet team
- Monero core developers
- Android community
- Все контрибьюторы проекта

---

**Отказ от ответственности**: Это программное обеспечение предоставляется "как есть", без каких-либо гарантий. Используйте на свой страх и риск. Авторы не несут ответственности за любые потери средств.
