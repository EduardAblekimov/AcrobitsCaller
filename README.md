## Architecture & Design Philosophy
The application is built on Clean Architecture, separating concerns into Presentation, Domain, and Data layers.

1. **Presentation Layer**

   Built with Jetpack Compose and MVVM (with some MVI elements), this layer uses `ViewModels` to expose state via Kotlin `Flow`s in a Unidirectional Data Flow pattern.
   This layer contains 3 screens: welcome screen with permission handling via Google's Accompanist, Dialer screen with account picker, number input and validation, and a Call Screen with basic caller info as well as the ability to hangup, mute or hold the call.
   A type-safe Compose navigation approach is used to connect those 3 screens.

2. **Domain Layer**

   This layer contains core business logic, independent of the Android framework. A `SipRepository` interface defines the data contract for the Data layer. It uses its own `CallState` and `RegistrationState` models to decouple the app from SDK-specific data structures.

3. **Data Layer**

   The `SipRepositoryImpl` class implements the domain's `SipRepository` interface. It communicates with a dedicated `SipSdk` wrapper, which is the only class that handles any low-level interactions with the SDK and then translates its models into the app's domain models.
   This abstraction makes the data layer highly testable.

While this prototype is robust, a production-ready application would benefit from the following improvements:

* **Dependency Injection with Hilt**: Replace the manual DI container (AppModule) with Hilt for standardized, boilerplate-free dependency injection.

* **Multi-Call State Management**: Refactor the state management in `SipSdkImpl` from tracking a single active call to managing a map of calls. This would properly support concurrent calls, call waiting, and conference scenarios.

* **Expanded Testing**: Augment the existing unit tests with integration tests to verify the interactions between the data and domain layers, and UI tests to validate Composable behavior and navigation.