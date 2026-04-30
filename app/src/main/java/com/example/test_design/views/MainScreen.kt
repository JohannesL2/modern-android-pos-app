package com.example.test_design.views

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.example.test_design.viewmodels.PaymentViewModel
import com.example.test_design.R
import com.example.test_design.domain.models.UiProduct
import com.example.test_design.components.cart.CartBar
import com.example.test_design.components.common.inputs.CategorySelector
import com.example.test_design.components.product.ProductList
import com.example.test_design.scancontrol.ScanSoundPlayer
import com.example.test_design.scancontrol.ScanCooldownGate
import com.example.test_design.components.common.inputs.SearchBar
import com.example.test_design.data.dao.ProductDao
import com.example.test_design.data.dao.PaymentDao
import com.example.test_design.data.entity.ProductEntity
import kotlinx.coroutines.launch
import com.example.test_design.data.dao.OrderDao
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.*
import com.example.test_design.components.menu.MenuDrawer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import com.example.test_design.components.common.feedback.SwipeMenuHint
import com.example.test_design.events.CartUiEvent
import com.example.test_design.viewmodels.CartViewModel
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxHeight
import com.example.test_design.features.scanner.ScannerResultActivity
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.res.stringResource
import com.example.test_design.views.ui.theme.AppMotion
import com.example.test_design.components.common.inputs.CATEGORY_ALL
import com.example.test_design.components.common.inputs.CATEGORY_FAVORITES
import com.example.test_design.viewmodels.MainViewModel
import androidx.compose.ui.zIndex
import com.example.test_design.performance.JankTracker
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import com.example.test_design.components.cart.CartSummary
import com.example.test_design.components.menu.MenuShortcutButton
import com.example.test_design.components.product.ProductGrid
import com.example.test_design.components.overlays.ProductVarietyModal
import androidx.compose.foundation.layout.heightIn
import com.example.test_design.components.ui.LanguageSelector


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    mainViewModel: MainViewModel,
    products: List<ProductEntity>,
    dao: ProductDao,
    orderDao: OrderDao,
    paymentDao: PaymentDao,
    paymentViewModel: PaymentViewModel,
    onPay: (Int, String, Int) -> Unit,
    onDrawerTrigger: (DrawerState) -> Unit,
    jankTracker: JankTracker? = null
) {

    var showCartModal by remember { mutableStateOf(false) }

    var selectedProductForModal by remember { mutableStateOf<UiProduct?>(null) }

    var isCategoriesExpanded by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf(setOf(CATEGORY_ALL)) }
    var isSearchFocused by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    val activity = context as? ComponentActivity
    val window = activity?.window
    val cart = cartViewModel.cart
    val scanCooldownGate = remember { ScanCooldownGate(1_500L) }

    val horizontalPadding = if (isTablet) 8.dp else 0.dp

    var drawerReady by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val variantsByProduct = remember(products) {
        products
            .filter { it.isVariant }
            .groupBy { it.baseProductCode }
    }
    val allProducts = remember(products, variantsByProduct) {
        products
            .groupBy { it.baseProductCode }
            .values
            .mapNotNull { productGroup ->
                val base = productGroup.firstOrNull { !it.isVariant && it.variantValue == null }
                    ?: productGroup.firstOrNull { !it.isVariant }
                    ?: productGroup.firstOrNull()
                    ?: return@mapNotNull null

                val purchasableProducts = productGroup.filter {
                    it.isVariant || !it.variantValue.isNullOrBlank()
                }

                val displayProduct = purchasableProducts
                    .firstOrNull { it.variantValue.equals("S", ignoreCase = true) }
                    ?: purchasableProducts.firstOrNull { it.variantValue.equals("STANDARD", ignoreCase = true) }
                    ?: purchasableProducts.firstOrNull()
                    ?: base

                UiProduct(
                    name = base.productName,
                    price = displayProduct.unitPrice,
                    category = base.category,
                    imageRes = base.imageResName,
                    articleNumber = displayProduct.articleNumber,
                    ean = displayProduct.ean,
                    variantValue = displayProduct.variantValue
                )
            }
    }

    val categories = remember(products) {
        listOf(CATEGORY_FAVORITES, CATEGORY_ALL) +
                products.asSequence()
                    .flatMap { (it.category ?: emptyList()).asSequence() }
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()
                    .toList()
    }

    val favoriteNames = remember { setOf("Kaffe", "Latte", "Smörgås") }

    val filteredProducts by remember(debouncedQuery, selectedCategories, allProducts) {
        derivedStateOf {
            allProducts.filter { product ->
                val categoryMatch = when {
                    selectedCategories.contains(CATEGORY_FAVORITES) -> {
                        favoriteNames.contains(product.name)
                    }

                    selectedCategories.contains(CATEGORY_ALL) -> true
                    else -> product.category.any { it in selectedCategories }
                }

                val searchMatch = debouncedQuery.isBlank() ||
                        product.name.contains(debouncedQuery, ignoreCase = true) ||
                        product.ean.contains(debouncedQuery, ignoreCase = true)

                categoryMatch && searchMatch
            }
        }
    }

    LaunchedEffect(isSearchFocused, isCategoriesExpanded, showCartModal) {
        when {
            showCartModal -> jankTracker?.setContext("UI_State", "Cart_Open")
            isSearchFocused -> jankTracker?.setContext("UI_State", "Searching")
            isCategoriesExpanded -> jankTracker?.setContext("UI_State", "Category_Menu")
            else -> jankTracker?.removeContext("UI_State")
        }
    }

    LaunchedEffect(drawerState) {
        onDrawerTrigger(drawerState)
    }

    LaunchedEffect(Unit) {
        drawerReady = true
    }

    val insetsController = remember(window) {
        window?.let { WindowInsetsControllerCompat(it, it.decorView) }
    }

    SideEffect {
        window?.navigationBarColor = Color.White.toArgb()
        insetsController?.isAppearanceLightNavigationBars = true
    }

    LaunchedEffect(Unit) {
        paymentViewModel.scannedCode.collect { scannedRaw ->
            if (scannedRaw.isNotBlank() && scanCooldownGate.shouldHandle(scannedRaw)) {
                cartViewModel.addProductByScannedCode(scannedRaw)
            }
        }
    }

    // Ska flyttas och bytas ut, är en temporär toaster för att visa skannade produkter
    LaunchedEffect(Unit) {
        cartViewModel.uiEvents.collect { event: CartUiEvent ->
            when (event) {
                is CartUiEvent.ProductAdded -> {
                    ScanSoundPlayer.playScanSuccess()

                    val intent = Intent(context, ScannerResultActivity::class.java).apply {
                        // Viktigt: Eftersom vi startar från en Composable (Unit-effekt)
                        // behöver vi ibland NEW_TASK om vi inte är direkt i en Activity-kontext
                        putExtra("SCANNED_CODE", event.productName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    }
                    context.startActivity(intent)
                }

                CartUiEvent.InvalidScan -> {
                    ScanSoundPlayer.playScanDenied()
                }
            }
        }
    }

    LaunchedEffect(categories) {
        val allowed = categories.toSet()
        val cleanedSelection = selectedCategories.filter { it in allowed }.toSet()
        if (cleanedSelection.isEmpty()) {
            selectedCategories = setOf(CATEGORY_ALL)
        } else if (cleanedSelection != selectedCategories) {
            selectedCategories = cleanedSelection
        }
    }

    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(300)
        debouncedQuery = searchQuery
    }

    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                // source == Drag betyder att användaren faktiskt har fingret på skärmen
                if (available.y < -15 && isCategoriesExpanded && source == androidx.compose.ui.input.nestedscroll.NestedScrollSource.Drag) {
                    isCategoriesExpanded = false
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    if (drawerReady) {
        DismissibleNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                MenuDrawer(
                    cart = cart,
                    navController = navController,
                    onLock = { mainViewModel.lockApp() },
                    drawerState = drawerState,
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        if (route != "home") {
                            navController.navigate(route)
                        }
                    }
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                        .background(Color.White),
                ) {
                    // RAD 1: Sökfält och Snabbknappar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onClear = { searchQuery = "" },
                            onScanClick = {
                                activity?.let { paymentViewModel.startContinuousScan(it) }
                            },
                            placeholderText = stringResource(R.string.main_search_placeholder),
                            modifier = Modifier
                                .weight(1f) // Tar upp allt tillgängligt utrymme
                                .heightIn(max = 56.dp)
                                .padding(horizontal = horizontalPadding)
                                .onFocusChanged { focusState ->
                                    isSearchFocused = focusState.isFocused
                                    if (focusState.isFocused) {
                                        isCategoriesExpanded = false
                                    }
                                }
                        )

                        if (isTablet) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 12.dp)
                                    .padding(top = 12.dp)
                            ) {
                                LanguageSelector()

                                Spacer(modifier = Modifier.width(12.dp))

                                MenuShortcutButton(
                                    icon = R.drawable.ic_restore,
                                    onClick = { navController.navigate("refund") }
                                )
                                MenuShortcutButton(
                                    icon = Icons.Default.Info,
                                    onClick = { navController.navigate("about") }
                                )
                                MenuShortcutButton(
                                    icon = Icons.Default.Lock,
                                    containerColor = Color(0xFFFFEBEE),
                                    onClick = {
                                        cart.clear()
                                        mainViewModel.lockApp()
                                    }
                                )
                            }
                        }
                    }

                    // RAD 2: Kategoriväljare (Snyggt placerad under sökfältet)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        CategorySelector(
                            categories = categories,
                            selected = selectedCategories,
                            isExpanded = isCategoriesExpanded,
                            onExpandChange = { isCategoriesExpanded = it },
                            onSelect = { newSelection ->
                                selectedCategories = when {
                                    newSelection.contains(CATEGORY_FAVORITES) && !selectedCategories.contains(CATEGORY_FAVORITES) -> setOf(CATEGORY_FAVORITES)
                                    newSelection.size > 1 && newSelection.contains(CATEGORY_FAVORITES) -> newSelection - CATEGORY_FAVORITES
                                    newSelection.isEmpty() -> setOf(CATEGORY_ALL)
                                    else -> newSelection
                                }
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // INNEHÅLL (GRID ELLER LISTA)
                    if (filteredProducts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // ... (Här behåller du din befintliga "inga resultat"-kod) ...
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(painterResource(R.drawable.ic_search_off), null, Modifier.size(64.dp), Color.LightGray)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(stringResource(R.string.main_search_no_results, searchQuery), style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { searchQuery = ""; selectedCategories = setOf(CATEGORY_ALL); focusManager.clearFocus() },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                                ) { Text(stringResource(R.string.main_btn_show_all)) }
                            }
                        }
                    } else {
                        if (isTablet) {
                            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                ProductGrid(
                                    products = filteredProducts,
                                    cart = cart,
                                    onProductClick = { selectedProductForModal = it },
                                    allProductEntities = products,
                                    modifier = Modifier.weight(0.65f).fillMaxHeight().background(Color(0xFFFAFAFA))
                                )
                                Box(modifier = Modifier.weight(0.35f).fillMaxHeight().background(Color(0xFFFAFAFA)).padding(top = 18.dp, end = 16.dp)) {
                                    CartSummary(cart = cart, appliedAmount = 0, appliedCode = "", navController = navController,onPay = onPay)
                                }
                            }
                        } else {
                            ProductList(
                                products = filteredProducts,
                                cart = cart,
                                onProductClick = { selectedProductForModal = it },
                                allProductEntities = products,
                                modifier = Modifier.weight(1f).nestedScroll(nestedScrollConnection)
                            )
                        }
                    }
                }

                if (drawerState.isClosed && !showCartModal) {
                    SwipeMenuHint()
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showCartModal,
                    modifier = Modifier.fillMaxSize(),
                    enter = AppMotion.PopEnter,
                    exit = AppMotion.PopExit
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFAFAFA) //bakgrundsfärg i cart
                        )
                    ) {
                        SecondScreen(
                            navController = navController,
                            cart = cart,
                            onClose = { showCartModal = false },
                            appliedAmount = mainViewModel.discountAmount,
                            appliedCode = mainViewModel.appliedCode,

                            onDiscountApplied = { amount, code ->
                                mainViewModel.applyDiscount(amount, code)
                            },
                            onPay = onPay
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = cart.isNotEmpty() && drawerState.isClosed && !isTablet,
                    enter = AppMotion.PopEnter,
                    exit = AppMotion.PopExit,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(2f)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(12.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        CartBar(
                            cart = cart,
                            onClick = { showCartModal = !showCartModal },
                            onPay = onPay,
                            discountAmount = mainViewModel.discountAmount,
                            appliedCode = mainViewModel.appliedCode,
                            isExpanded = showCartModal,
                            isMenuOpen = drawerState.isOpen
                        )
                    }
                }
                selectedProductForModal?.let { product ->
                    val baseProductCode = product.articleNumber.substringBefore("-")
                    val variants: List<ProductEntity> =
                        variantsByProduct[baseProductCode] ?: emptyList()

                    ProductVarietyModal(
                        product = product,
                        variants = variants, // 👈 NY
                        cart = cartViewModel.cart,
                        onDismiss = { selectedProductForModal = null }
                    )
                }
            }
        }
    }
}
