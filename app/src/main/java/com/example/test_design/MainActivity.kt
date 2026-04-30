package com.example.test_design

import com.example.test_design.data.db.AppDatabase
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.test_design.ui.theme.TestdesignTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import kotlinx.coroutines.launch
import com.jakewharton.threetenabp.AndroidThreeTen
import androidx.core.view.WindowCompat
import androidx.compose.runtime.rememberCoroutineScope
import com.example.test_design.components.overlays.SellerLogin
import com.example.test_design.viewmodels.PaymentViewModel
import kotlin.Int
import com.example.test_design.views.PinScreen
import com.example.test_design.views.PaymentConfirmation
import com.example.test_design.views.MainScreen
import com.example.test_design.views.LoadingOverlay
import com.example.test_design.views.LockLoadingScreen
import com.example.test_design.views.RefundScreen
import com.example.test_design.views.SecondScreen
import androidx.navigation.NavType
import com.example.test_design.views.FeedBackScreen
import com.example.test_design.views.FeedbackFlowType
import androidx.navigation.navArgument
import androidx.navigation.NavController
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.test_design.viewmodels.OrderViewModel
import kotlinx.coroutines.CoroutineScope
import android.widget.Toast
import com.example.test_design.views.AddProductScreen
import com.example.test_design.data.repository.ProductRepository
import com.example.test_design.viewmodels.CartViewModel
import androidx.compose.runtime.collectAsState
import com.example.test_design.viewmodels.RefundViewModel
import com.example.test_design.viewmodels.MainViewModel
import android.view.KeyEvent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test_design.performance.JankTracker
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.test_design.domain.models.CartItem
import com.example.test_design.states.SaleState
import com.example.test_design.views.AboutScreen
import android.content.Intent
import android.os.Build
import com.example.test_design.service.PaymentServerService // Se till att detta matchar din mappstruktur!
import androidx.core.content.ContextCompat
import android.content.Context
import com.example.test_design.components.common.feedback.Warning
import com.example.test_design.domain.usecases.PaymentOutcome
import com.example.test_design.domain.usecases.ProcessPaymentUseCase
import com.example.test_design.events.PaymentUiFeedback
import com.example.test_design.utils.toDisplayMessage
import com.example.test_design.views.SplitPaymentScreen

class MainActivity : AppCompatActivity() {

    private val jankTracker by lazy { JankTracker(this) }

    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var refundViewModel: RefundViewModel

    private var onDrawerTrigger: (() -> Unit)? = null
    private var onPayTrigger: (() -> Unit)? = null

    private var onClearCartTrigger: (() -> Unit)? = null

    fun startPayment(
        navController: NavController,
        cart: SnapshotStateList<CartItem>,
        paymentViewModel: PaymentViewModel,
        orderViewModel: OrderViewModel,
        discountAmount: Int,
        appliedCode: String,
        total: Int,
        scope: CoroutineScope,
        mainViewModel: MainViewModel
    ) {
        val context = this

        val processPayment = ProcessPaymentUseCase(paymentViewModel, orderViewModel)

        jankTracker.setContext("PaymentFlow", "Initializing")
        // Visar laddskärm först för att INTE visa andra skärmen igen efter köp och innan feedback

        mainViewModel.isLoading = true

        scope.launch {
            navController.navigate("payment_processing") {
                launchSingleTop = true
            }

            kotlinx.coroutines.yield()

            try {
                val outcome = processPayment(cart.toList(), total)

                val feedback : PaymentUiFeedback = when (outcome) {
                    is PaymentOutcome.Success -> {
                        navController.navigate(
                            "feedback_screen/payment/true?appliedCode=$appliedCode&discountAmount=$discountAmount&ean=${outcome.receiptNumber}"
                        ) {
                            popUpTo("main") { inclusive = false }
                            launchSingleTop = true
                        }
                        return@launch
                    }

                    is PaymentOutcome.Aborted -> PaymentUiFeedback(
                        context.getString(R.string.payment_error_aborted),
                        Toast.LENGTH_SHORT,
                        true
                    )

                    is PaymentOutcome.Failure -> PaymentUiFeedback(
                        outcome.error.toDisplayMessage(context),
                        Toast.LENGTH_SHORT,
                        true
                    )

                    is PaymentOutcome.UnknownFailure -> PaymentUiFeedback(
                        context.getString(R.string.payment_error_unknown),
                        Toast.LENGTH_LONG,
                        true
                    )
                }

                feedback.let {
                    if (it.navigateBack) {
                        navController.popBackStack("main", inclusive = false)
                    }

                    Toast.makeText(context, it.message, it.duration).show()
                }

            } catch (e: Exception) {
                navController.popBackStack("main", inclusive = false)
                Toast.makeText(
                    context,
                    context.getString(R.string.payment_error_unknown),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                mainViewModel.isLoading = false
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, PaymentServerService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        paymentViewModel = PaymentViewModel()
        refundViewModel = RefundViewModel()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        window.attributes = window.attributes.apply { screenBrightness = 1f }
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        WindowCompat.enableEdgeToEdge(window)
        AndroidThreeTen.init(this)

        //Om du vill lägga till nya produkter, uppdatera productdao.kt
        //Ta bort den gamla databasen
        //Kör denna kod nedan innan val db = AppDatabase.getInstance(this)
        this.deleteDatabase("app_database.db")
        val db = AppDatabase.getInstance(this)
        val dao = db.productDao()
        val paymentDao = db.paymentDao()
        val productRepository = ProductRepository(dao)
        val cartViewModel = CartViewModel(productRepository)
        val orderDao = db.orderDao()
        val orderViewModel = OrderViewModel(orderDao, paymentDao)

        enableEdgeToEdge()
        setContent {
            TestdesignTheme {
                val mainViewModel: MainViewModel = viewModel()
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                // Gör statet från ViewModel till Compose-state
                val saleState by paymentViewModel.saleState.collectAsState()
                val terminalConnected by paymentViewModel.terminalConnected.collectAsState()
                val terminalReady by paymentViewModel.terminalReady.collectAsState()
                val context = LocalContext.current // Viktigt för Toast och Context

                // 1. Hantera betalning vid uppstart (om appen var stängd när anropet kom)
                LaunchedEffect(Unit) {
                    handleRemotePaymentIntent(intent, navController, orderViewModel, mainViewModel, scope)
                }

                // 2. Hantera betalning när appen redan är öppen
                androidx.compose.runtime.DisposableEffect(Unit) {
                    val listener = androidx.core.util.Consumer<android.content.Intent> { newIntent ->
                        handleRemotePaymentIntent(newIntent, navController, orderViewModel, mainViewModel, scope)
                    }
                    addOnNewIntentListener(listener)
                    onDispose { removeOnNewIntentListener(listener) }
                }

                LaunchedEffect(saleState) {
                    when (val state = saleState) {
                        is SaleState.ProcessingPayment,
                        is SaleState.ProcessingSplitPayment -> {
                            navController.navigate("payment_processing") {
                                launchSingleTop = true
                            }
                        }

                        is SaleState.PaymentSuccess -> {

                            val route = if (state.receiptNumber.startsWith("SPLIT")) {
                                "feedback_screen/payment/true?ean=${state.receiptNumber}"
                            } else {
                                "feedback_screen/payment/true?appliedCode=${state.appliedCode}&discountAmount=${state.discountAmount}&ean=${state.receiptNumber}"
                            }

                            navController.navigate(route) {
                                popUpTo("main") { inclusive = false }
                            }

                            paymentViewModel.resetSale()
                        }
                        is SaleState.PaymentFailed -> {
                            Toast.makeText(context, context.getString(state.reasonRes), Toast.LENGTH_LONG).show()
                            // Gå tillbaka till huvudskärmen om vi var på laddskärmen
                            navController.popBackStack("main", inclusive = false)
                            paymentViewModel.resetSale()
                        }
                        else -> { /* Idle eller BuildingSale - gör ingenting */ }
                    }
                }

                LaunchedEffect(navController) {
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        jankTracker.setContext("CurrentScreen", destination.route ?: "unknown")
                    }
                }

                val products by productRepository.observeProducts().collectAsState(initial = emptyList())
                val subTotal = cartViewModel.totalSum
                val finalTotal = mainViewModel.getFinalTotal(subTotal)


                SideEffect {
                    onPayTrigger = {
                        if (cartViewModel.cart.isNotEmpty() && !mainViewModel.isLoading) {
                            startPayment(
                                navController,
                                cartViewModel.cart,
                                paymentViewModel,
                                orderViewModel,
                                mainViewModel.discountAmount,
                                mainViewModel.appliedCode,
                                finalTotal,
                                scope,
                                mainViewModel
                            )
                        }
                    }

                    onClearCartTrigger = {
                        if (cartViewModel.cart.isNotEmpty()) {
                            cartViewModel.cart.clear()
                            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                            Toast.makeText(this, "Varukorgen rensad", Toast.LENGTH_SHORT).show()
                        }
                    }
                }


                Box(Modifier.fillMaxSize()) {
                    if (!mainViewModel.isLocked) {
                        NavHost(
                            navController = navController,
                            startDestination = "main"
                        ) {
                            composable("main") {
                                MainScreen(
                                    onDrawerTrigger = { drawerState ->
                                        onDrawerTrigger = {
                                            scope.launch {
                                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                            }
                                        }
                                    },
                                    navController = navController,
                                    cartViewModel = cartViewModel,
                                    mainViewModel = mainViewModel,
                                    products = products,
                                    dao = dao,
                                    orderDao = orderDao,
                                    paymentDao = paymentDao,
                                    paymentViewModel = paymentViewModel,
                                    onPay = { discount, code, total ->
                                        startPayment(
                                            navController,
                                            cartViewModel.cart,
                                            paymentViewModel,
                                            orderViewModel,
                                            mainViewModel.discountAmount,
                                            mainViewModel.appliedCode,
                                            total,
                                            scope,
                                            mainViewModel)
                                    }
                                )
                            }

                            composable("second") {
                                SecondScreen(
                                    navController = navController, // 👈 FIXEN
                                    appliedAmount = mainViewModel.discountAmount,
                                    appliedCode = mainViewModel.appliedCode,
                                    onDiscountApplied = { amount, code ->
                                        mainViewModel.applyDiscount(amount, code)
                                    },
                                    cart = cartViewModel.cart,
                                    onPay = { discount, code, total ->
                                        startPayment(
                                            navController,
                                            cartViewModel.cart,
                                            paymentViewModel,
                                            orderViewModel,
                                            discount,
                                            code,
                                            total,
                                            scope,
                                            mainViewModel
                                        )
                                    }
                                )
                            }

                            composable("split_payment/{total}") { backStackEntry ->
                                val total = backStackEntry.arguments?.getString("total")?.toInt() ?: 0

                                SplitPaymentScreen(
                                    navController = navController,
                                    paymentViewModel = paymentViewModel,
                                    totalAmount = total,
                                    cart = cartViewModel.cart,
                                    orderViewModel = orderViewModel,
                                    onPaymentComplete = {
                                        // Navigation sker via SaleState.PaymentSuccess
                                    }
                                )
                            }

                            composable("payment_processing") {
                                LoadingOverlay()
                            }
                            composable("about") {
                                AboutScreen(
                                    paymentViewModel = paymentViewModel,
                                    onClose = {
                                        navController.navigate("main") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    }
                                )
                            }
                                composable("pinScreen") {
                                    PinScreen(
                                        navController = navController,
                                        cartViewModel.cart,
                                        dao = dao,
                                        onPinEntered = { pin ->
                                            println("PIN: $pin")
                                        }
                                    )
                                }
                                composable("confirmation")
                                {
                                    PaymentConfirmation(
                                        navController = navController,
                                        cartViewModel.cart
                                    )
                                }

                                composable(route = "refund") {
                                    RefundScreen(
                                        paymentViewModel = paymentViewModel,
                                        refundViewModel = refundViewModel,
                                        orderDao = db.orderDao(),
                                        paymentDao = db.paymentDao(),
                                        onRefundCompleted = {
                                            navController.navigate("feedback_screen/refund/true") {
                                                popUpTo("main") { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        },
                                        onClose = {
                                            navController.navigate("main") {
                                                popUpTo("main") { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable(route = "add_product") {
                                    AddProductScreen(
                                        navController = navController,
                                        dao = dao,
                                        onClose = {
                                            navController.navigate("main") {
                                                popUpTo("main") { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable(
                                    route = "feedback_screen/{flowType}/{success}?appliedCode={appliedCode}&discountAmount={discountAmount}&ean={ean}",
                                    arguments = listOf(
                                        navArgument("flowType") { type = NavType.StringType },
                                        navArgument("success") { type = NavType.BoolType },
                                        navArgument("appliedCode") {
                                            type = NavType.StringType; defaultValue = ""
                                        },
                                        navArgument("discountAmount") {
                                            type = NavType.IntType; defaultValue = 0
                                        },
                                        navArgument("ean") {
                                            type = NavType.StringType; defaultValue = ""
                                        } // default EAN!
                                    )
                                ) { backStackEntry ->
                                    val flowTypeArg =
                                        backStackEntry.arguments?.getString("flowType") ?: "payment"
                                    val isSuccess =
                                        backStackEntry.arguments?.getBoolean("success") ?: false
                                    val appliedCode =
                                        backStackEntry.arguments?.getString("appliedCode") ?: ""
                                    val discountAmount =
                                        backStackEntry.arguments?.getInt("discountAmount") ?: 0
                                    val ean13 = backStackEntry.arguments?.getString("ean") ?: ""
                                    val flowType = if (flowTypeArg == "refund") {
                                        FeedbackFlowType.REFUND
                                    } else {
                                        FeedbackFlowType.PAYMENT
                                    }



                                    FeedBackScreen(
                                        navController = navController,
                                        flowType = flowType,
                                        isSuccess = isSuccess,
                                        cart = cartViewModel.cart,
                                        appliedCode = appliedCode,
                                        discountAmount = discountAmount,
                                        paymentViewModel = paymentViewModel,
                                        refundViewModel = refundViewModel,
                                        onLock = { mainViewModel.lockApp() },
                                    )
                                }
                            }
                        }

                        if (mainViewModel.isLoading) {
                            LoadingOverlay()
                        }

                        if (mainViewModel.isLocked || mainViewModel.showSellerLogin) {
                            SellerLogin(
                                navController = navController,
                                onDismiss = { mainViewModel.showSellerLogin = false },
                                onPinVerified = { pin ->
                                    if (mainViewModel.verifyPin(pin)) {
                                        mainViewModel.unlockApp()
                                    }
                                },
                                isAlreadyLoggedIn = mainViewModel.isSellerLoggedIn,
                                onLock = { mainViewModel.lockApp() }
                            )
                        }

                        if (mainViewModel.showLockLoading) {
                            LockLoadingScreen(
                                visible = mainViewModel.showLockLoading,
                                contentDescription = stringResource(mainViewModel.loadingMessage)
                            )
                        }

                    Warning(
                        isVisible = !terminalConnected && !terminalReady && !BuildConfig.USE_EMULATED_TERMINAL,
                        text = if (BuildConfig.USE_LOCAL_TERMINAL) {
                            stringResource(R.string.terminal_connection_lost_instructions_on_device)
                        } else {
                            stringResource(R.string.terminal_connection_lost_instructions_off_device)
                        }
                    )
                    }
                }
            }
        }

    private val toneGenerator = ToneGenerator(
        AudioManager.STREAM_NOTIFICATION,
        100
    )

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        android.util.Log.d("POS_DEBUG", "KNAPP TRYCKT! Kod: $keyCode")

        return when (keyCode) {
            588 -> {
                event?.startTracking()
                true
            }
            589 -> {
                if (event?.repeatCount == 0) {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 70)
                    onPayTrigger?.invoke()
                }
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == 588) {
            onClearCartTrigger?.invoke()
            true
        } else super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 588) {
            val isLongPressCanceled = (event?.flags?.and(KeyEvent.FLAG_CANCELED_LONG_PRESS)) != 0
            if (event != null && !event.isCanceled && !isLongPressCanceled) {
                onDrawerTrigger?.invoke()
            }
            return true
        }
        return super.onKeyUp(keyCode, event)
        }

        override fun onResume() {
            super.onResume()
            jankTracker.startTracking()
        }

        override fun onPause() {
            super.onPause()
            jankTracker.stopTracking()
        }

    // TILLÄGG: Lyssnar på nya anrop om appen redan är öppen
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    // TILLÄGG: Funktion som faktiskt startar betalningen från servern
    private fun handleRemotePaymentIntent(
        intent: Intent?,
        navController: NavController,
        orderViewModel: OrderViewModel,
        mainViewModel: MainViewModel,
        scope: CoroutineScope
    ) {
        val amount = intent?.getIntExtra(PaymentServerService.EXTRA_AMOUNT, 0) ?: 0
        if (amount > 0 && !mainViewModel.isLoading) {
            // Rensa intentet så vi inte triggar om vid rotation
            intent?.removeExtra(PaymentServerService.EXTRA_AMOUNT)

            startPayment(
                navController = navController,
                cart = androidx.compose.runtime.mutableStateListOf(),
                paymentViewModel = paymentViewModel,
                orderViewModel = orderViewModel,
                discountAmount = 0,
                appliedCode = "FJÄRR_ORDER",
                total = amount,
                scope = scope,
                mainViewModel = mainViewModel
            )
        }
    }

    private fun isTabletMode(context: Context): Boolean {
        // 600dp är standardgränsen för tablets i Android
        return context.resources.configuration.screenWidthDp >= 600
    }
    }


