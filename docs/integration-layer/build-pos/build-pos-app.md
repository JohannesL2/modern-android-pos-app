# Quick start for integrating your own POS app

This page shows the minimum recommended startup flow when building your own POS application. Details about available
terminal configurations can be found in [Configuration](configure-terminal.md).

## Prerequisites

Before starting, make sure the required SDKs are installed:

- Verifone Payment SDK (PSDK)
- Epson ePOS SDK (for off-device printing)

See [Setup](../run-pos/run-pos-app.md) for installation instructions for these dependencies.

## 1. Add the integration module as dependency

```kotlin
dependencies {
    implementation(project(":api"))
}
```

## 2. Initialize the integration layer

```kotlin
class App : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        ApiModule.setUseEmulatedTerminal(BuildConfig.USE_EMULATED_TERMINAL)

        if (!BuildConfig.USE_EMULATED_TERMINAL) {
            ApiModule.setTerminalConnectionConfig(
                if (BuildConfig.USE_LOCAL_TERMINAL) {
                    TerminalConnectionConfig.OnDevice
                } else {
                    TerminalConnectionConfig.TcpIpClient(
                        address = BuildConfig.OFF_DEVICE_TERMINAL_IP,
                        networkConfiguration = NetworkConfiguration.STATIC,
                        forgetPersistedDevice = true,
                    )
                }
            )
        }

        ApiModule.initialize(this)
        ApiModule.start(appScope)
    }
}
```

`ApiModule.start(scope)` is asynchronous. Read readiness via
[State and flows](state-and-flows.md).

## 3. Use the terminal

```kotlin
class PaymentViewModel : ViewModel() {
    private val terminal = ApiModule.terminal

    val terminalReady = terminal.terminalReady

    suspend fun pay(amountMinorUnits: Int): PaymentResult {
        return terminal.pay(amountMinorUnits)
    }
}
```

Public methods and models are described in [TerminalApi](terminal-api.md).
