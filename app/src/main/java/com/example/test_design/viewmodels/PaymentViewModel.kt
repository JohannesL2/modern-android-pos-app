package com.example.test_design.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import android.content.Context
import com.example.integration.api.model.PrintContentType
import android.util.Log
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import com.example.test_design.domain.models.CartItem
import com.example.test_design.R
import com.example.integration.api.model.PaymentResult
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
import android.app.Activity
import androidx.compose.runtime.mutableStateListOf
import com.example.test_design.data.dao.OrderDao
import com.example.test_design.data.dao.OrderWithRows
import com.example.test_design.data.dao.PaymentDao
import com.example.test_design.data.entity.OrderEntity
import com.example.test_design.data.entity.PaymentMethod
import com.example.test_design.domain.models.UiProduct
import com.example.test_design.domain.models.Order
import com.example.test_design.states.SaleState
import com.example.test_design.utils.ReceiptFormatter
import com.example.test_design.utils.ReceiptPrintMapper
import com.example.integration.api.model.ScanBehavior
import com.example.integration.api.ApiModule
import com.example.integration.api.model.CardType
import com.example.integration.api.model.DeviceInfo
import com.example.integration.api.model.SplitPaymentUiResult
import com.example.integration.internal.payment.SplitPaymentPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import android.graphics.BitmapFactory
import com.example.test_design.BuildConfig

/**
 * ViewModel that handles payments, scanning, receipt-printing.
 * Communicates with the Terminal API to make transactions
 */
class PaymentViewModel : ViewModel() {
    private val receiptPrintMapper = ReceiptPrintMapper()

    private val splitSlips = mutableListOf<String>()

    private val splitResults = mutableListOf<PaymentResult.Success>()
    // För state driven navigation
    private val _saleState =
        kotlinx.coroutines.flow.MutableStateFlow<com.example.test_design.states.SaleState>(com.example.test_design.states.SaleState.Idle)
    val saleState = _saleState.asStateFlow()

    private val _splitPaymentState =
        kotlinx.coroutines.flow.MutableStateFlow<SplitPaymentUiResult?>(null)

    val splitPaymentState = _splitPaymentState.asStateFlow()

    private val _splitPayments = mutableStateListOf<SplitPayment>()

    // En hjälpmetod för att uppdatera state
    private fun updateState(newState: com.example.test_design.states.SaleState) {
        _saleState.value = newState
    }

    fun resetSale() {
        updateState(SaleState.Idle)
        lastPaymentInfo = null
        lastOrder = null // VIKTIGT: Glöm inte denna!
        lastMaskedPan = null
        lastCardBrand = null
        clearSplitTracking() // Rensa även split-data för säkerhets skull
    }

    private val terminalApi = ApiModule.terminal //Getting our terminal ready
    val scannedCode: Flow<String> = terminalApi.scannedCode //Code that we scan will go here
    val deviceInfo: StateFlow<DeviceInfo?> = terminalApi.deviceInfo
    val terminalConnected: StateFlow<Boolean> = terminalApi.terminalConnected
    val terminalReady: StateFlow<Boolean> = terminalApi.terminalReady
    var lastPaidAppSpecificData: String? = null

    private var currentSplitGroupId: String? = null


    var lastPaymentInfo: String? = null
    var lastOrder: Order? = null
    var lastMaskedPan: String? = null
    var lastCardBrand: String? = null

    init {
        if (!BuildConfig.USE_EMULATED_TERMINAL) initializeScannerService()
    }

    // Ska användas en gång per livscykel:
    fun initializeScannerService() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                Log.d("PaymentViewModel", "Initierar skanner...")
                terminalApi.initializeScanner()
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Kunde inte initiera skanner: ${e.message}")
                // Här kan du sätta en state-variabel typ isScannerReady = false
            }
        }
    }

    fun startSingleScan(activity: Activity) { //Scan only one item
        terminalApi.startScanner(activity, ScanBehavior.SINGLE)
    }

    fun startContinuousScan(activity: Activity) { //Scan multiple items
        terminalApi.startScanner(activity, ScanBehavior.CONTINUOUS)
    }

    /**
     * This is the most important function for making payments
     * To make a payment we need to send the total amount to the API
     */
    suspend fun payTotal(amountInKronor: Int): PaymentResult {
        return withContext(Dispatchers.IO) {
            val result = terminalApi.pay(amountInKronor * 100)

            if (result is PaymentResult.Success) {

                lastPaymentInfo = result.paymentInfo
                lastPaidAppSpecificData = result.appSpecificData
                lastMaskedPan = result.maskedPan
                lastCardBrand = result.brand
            }

            result
        }
    }

    /**
     * Converts image to bitmap because the printer can only handle really small images & black/white
     */
    fun drawableToBase64(context: Context, drawableResId: Int): String {
        val drawable = context.getDrawable(drawableResId) ?: return ""
        val bitmap = (drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val bytes = baos.toByteArray()
        return "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * This the function that uses the order model to build a order with the items from the cart
     */
    fun buildOrderFromCart(
        cart: List<CartItem>,
        paymentMethod: PaymentMethod = PaymentMethod.CARD,
        receiptNumber: String,
    ): Order {
        val now = Date()
        return Order(
            receiptNumber = receiptNumber,
            items = cart.toList(),
            paymentMethod = paymentMethod,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now),
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now),
            seller = "John Doe"
        )
    }

    /**
     * This is the function we use to print the receipt in feedbackscreen when payment is successful
     * If we don't get the slip from the API this won't allow us to print a receipt
     */
    fun testPrintFromCart(
        context: Context,
        paymentInfo: String?,
        appliedCode: String = "",
        discountAmount: Int = 0,
        order: Order
    ) {
        if (paymentInfo.isNullOrBlank()) {
            Toast.makeText(context, "Ingen kvitto-data tillgänglig", Toast.LENGTH_SHORT).show()
            return
        }

        testPrint(context, order, paymentInfo, appliedCode, discountAmount)
    }

    /**
     * This function generates our receipt using the slipHtml and other info.
     * It also runs the Print() function from the API to make the printer start printing.
     * It runs on the Coroutines Dispatcher to use the CPU and not block threads.
     */
    fun testPrint(
        context: Context,
        order: Order,
        slipHtml: String?,
        appliedCode: String = "",
        discountAmount: Int = 0
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val finalHtml = ReceiptFormatter.generateReceiptHtml(
                context = context,
                order = order,
                slipHtml = slipHtml,
                appliedCode = appliedCode,
                discountAmount = discountAmount
            )

            val result = terminalApi.print(
                content = finalHtml,
                contentType = PrintContentType.HTML
            )
            Log.d("PRINT_TEST", "Result = $result")
        }
    }

    /**
     * Rebuilds an existing order from the database and prints the original receipt.
     * This is used when we want to reprint a receipt without depending on in-memory state.
     */
    fun printReceiptFromDB(
        context: Context,
        receiptNumber: String,
        orderDao: OrderDao,
        paymentDao: PaymentDao
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val orderWithRows = runCatching {
                orderDao.getOrderWithRows(receiptNumber)
            }.getOrNull()

            if (orderWithRows == null) {
                return@launch
            }

            val payment = paymentDao.getPaymentByOrderNumber(receiptNumber)
            val cardDetails = payment?.let { paymentDao.getCardDetailsByPaymentId(payment.id) }
            val order = orderWithRows.toPrintableOrder(payment?.method)

            val receiptContentHtml = ReceiptFormatter.generateReceiptHtml(
                context = context,
                order = order,
                slipHtml = cardDetails?.paymentInfo
            )

            val result = terminalApi.print(
                content = receiptContentHtml,
                contentType = PrintContentType.HTML
            )
            Log.d("PRINT_RECEIPT", "Result = $result")
        }
    }

    private fun resetProcessing() {
        if (_saleState.value is SaleState.ProcessingSplitPart ||
            _saleState.value is SaleState.ProcessingSplitPayment
        ) {
            updateState(SaleState.Idle)
        }
    }

    /**
     * Void order function (not in use currently)
     */

    fun voidOrder(
        order: OrderEntity,
        appSpecificData: String,
        orderDao: OrderDao
    ) {
        viewModelScope.launch {
            val result = terminalApi.voidPayment(appSpecificData)

            when (result) {
                is PaymentResult.Success -> {
                    orderDao.markOrderVoided(order.orderNumber)
                }

                is PaymentResult.Failure -> Log.e("VOID", "Error: ${result.error}")
                PaymentResult.Aborted -> Log.d("VOID", "Aborted")
            }
        }
    }


    /**
     * Print all products on a receipt for testing the scanner (not in use currently)
     * Can be used easily by putting the function inside a LaunchedEffect
     * in MainScreen or MainActivity for example
     */

    fun printEanCodes(
        context: Context,
        order: Order
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val base64Logo =
                ReceiptFormatter.drawableToBase64(context, R.drawable.receiptcoffeelogo)
            val finalHtml = ReceiptFormatter.generateEanListHtml(order, base64Logo)

            val result =
                terminalApi.print(content = finalHtml, contentType = PrintContentType.HTML)

            Log.d("PRINT_EAN", "Result = $result")
        }
    }



    data class SplitPayment(
        val amountMinorUnits: Int,
        val paymentType: CardType,
        val paymentInfo: String? = null,
        val appSpecificData: String? = null,
        val maskedPan: String? = null,
        val brand: String? = null,
        val barcode: String? = null
    )

    /**
     * amountMinorUnits: Ange belopp i minor units (öre).
     * I din UI kod där du gör amountInput.toIntOrNull()?.times(100) så skickar du redan minor units.
     */
    fun addSplitPayment(
        amountMinorUnits: Int,
        paymentType: CardType,
        barcode: String? = null
    ) {
        _splitPayments.add(
            SplitPayment(
                amountMinorUnits = amountMinorUnits,
                paymentType = paymentType,
                barcode = barcode
            )
        )
    }


    fun getSplitPaymentParts(): List<SplitPaymentPart> {
        // Matchar den SDK/DTO du använder: SplitPaymentPart(amountMinorUnits = ..., paymentType = ...)
        return _splitPayments.map { payment ->
            SplitPaymentPart(
                amountMinorUnits = payment.amountMinorUnits,
                paymentType = payment.paymentType,
                barcode = payment.barcode
            )
        }
    }


    // Returnerar redan betalt i KRONOR (Int) för att visa i UI.
    fun getPaidAmount(): Int {
        // _splitPayments stores minor units -> convert to kronor (Int)
        val totalMinor = _splitPayments.sumOf { it.amountMinorUnits }
        return (totalMinor / 100)
    }

    fun getRemainingAmount(total: Int): Int {
        val paidKronor = getPaidAmount()
        return (total - paidKronor).coerceAtLeast(0)
    }



    fun paySingleSplit(
        context: Context,
        amountKr: Int,
        paymentType: CardType,
        totalAmount: Int,
        cart: List<CartItem>,
        orderViewModel: OrderViewModel, // <--- Lägg till denna här!
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (_saleState.value is SaleState.ProcessingPayment) {
                onResult(false)
                return@launch
            }

            updateState(SaleState.ProcessingSplitPart)

            try {
                val part = SplitPaymentPart(
                    amountMinorUnits = amountKr * 100,
                    paymentType = paymentType
                )

                val result = withContext(Dispatchers.IO) {
                    terminalApi.paySplitPart(
                        part = part,
                        totalsGroupId = getOrCreateSplitGroupId()
                    )
                }

                when (result) {
                    is PaymentResult.Success -> {
                        val formattedSlip = formatPaymentInfo(result.paymentInfo ?: "", 32)

                        withContext(Dispatchers.IO) {
                            // 1. Spara delbetalningen
                            orderViewModel.addPaymentPart(
                                receiptNumber = lastOrder?.receiptNumber ?: "",
                                amountMinorUnits = amountKr * 100,
                                brand = result.brand,
                                maskedPan = result.maskedPan,
                                paymentInfo = formattedSlip,
                                appSpecificData = result.appSpecificData
                            )

                            // 2. Beräkna om allt är betalt och uppdatera status
                            val paidSoFar = _splitPayments.sumOf { it.amountMinorUnits } + (amountKr * 100)
                            val totalToPay = totalAmount * 100

                            if (paidSoFar >= totalToPay) {
                                orderViewModel.updateOrderStatus(
                                    lastOrder?.receiptNumber ?: "",
                                    com.example.test_design.data.entity.OrderStatus.PAID,
                                    PaymentMethod.CARD
                                )
                            }
                        }

                        // 3. LOGIK FÖR UI: Uppdatera lokala listor
                        splitResults.add(result)
                        splitSlips.add(formattedSlip)
                        _splitPayments.add(
                            SplitPayment(
                                amountMinorUnits = amountKr * 100,
                                paymentType = paymentType,
                                paymentInfo = formattedSlip,
                                appSpecificData = result.appSpecificData,
                                maskedPan = result.maskedPan,
                                brand = result.brand
                            )
                        )

                        // Uppdatera "senaste" (för singel-kvittoutskrift)
                        lastPaymentInfo = formattedSlip
                        lastPaidAppSpecificData = result.appSpecificData
                        lastMaskedPan = result.maskedPan
                        lastCardBrand = result.brand

                        val paidMinor = _splitPayments.sumOf { it.amountMinorUnits }
                        val totalMinor = totalAmount * 100
                        val remainingMinor = totalMinor - paidMinor

                        if (remainingMinor <= 0) {
                            updateState(SaleState.AllPaymentsDone)
                            lastOrder?.let { onAllPaymentsDone(context, it) }
                        }

                        resetProcessing()
                        onResult(true)
                    }
                    is PaymentResult.Failure -> {
                        updateState(SaleState.PaymentFailed(R.string.payment_failed))
                        resetProcessing()
                        onResult(false)
                    }
                    PaymentResult.Aborted -> {
                        updateState(SaleState.Idle)
                        resetProcessing()
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("SPLIT", "Crash in paySingleSplit", e)
                updateState(SaleState.PaymentFailed(R.string.payment_failed))
                onResult(false)
            }
        }
    }



    private fun printReceipt(context: Context, order: Order) {
        viewModelScope.launch(Dispatchers.Default) {

            val mergedSlip = buildSplitReceipt()
            val safeOrder = lastOrder ?: order

            val finalHtml = ReceiptFormatter.generateReceiptHtml(
                context = context,
                order = order,
                slipHtml = mergedSlip // 🔥 HÄR ÄR DET VIKTIGA
            )

            val result = terminalApi.print(
                content = finalHtml,
                contentType = PrintContentType.HTML
            )

            Log.d("PRINT_SPLIT", "Result = $result")
        }
    }



    private fun onAllPaymentsDone(context: Context, order: Order) {
        // Slutsteg hanteras nu via startSplitPaymentFlow() och SaleState.
    }




    fun startSplitPaymentFlow(
        totalAmountKr: Int,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            updateState(SaleState.ProcessingSplitPayment)

            val paidMinor = _splitPayments.sumOf { it.amountMinorUnits }
            val totalMinor = totalAmountKr * 100

            if (paidMinor >= totalMinor) {
                finalizeSplitPayment(lastOrder)
                onDone(true)
            } else {
                updateState(SaleState.Idle)
                onDone(false)
            }
        }
    }


    // --- (optional) clearSplitPayments (you probably already have this) ---
    fun clearSplitPayments() {
        clearSplitTracking()
    }
    private fun clearSplitTracking() {
        splitSlips.clear()
        splitResults.clear()
        _splitPayments.clear()
        _splitPaymentState.value = null
        currentSplitGroupId = null
    }



    fun startSplitFlow(cart: List<CartItem>, orderViewModel: OrderViewModel) {
        if (currentSplitGroupId == null) {
            clearSplitTracking()
            lastOrder = null
        }
        currentSplitGroupId = java.util.UUID.randomUUID().toString()

        if (lastOrder == null) {
            viewModelScope.launch(Dispatchers.IO) {
                val receiptNumber = orderViewModel.nextReceiptNumber()
                lastOrder = buildOrderFromCart(cart, PaymentMethod.CARD, receiptNumber)
                orderViewModel.createOrderAndReturnReceipt(
                    receiptNumber = receiptNumber,
                    cart = cart,
                    paymentMethod = PaymentMethod.CARD,
                    initialPaidAmount = 0
                )
            }
        } else {
            Log.d("SPLIT", "Återanvänder befintlig order: ${lastOrder?.receiptNumber}")
        }
    }


    private fun finalizeSplitPayment(order: Order?) {
        val mergedSlip = buildSplitReceipt()
        val totalAmountKr = _splitPayments.sumOf { it.amountMinorUnits } / 100

        lastPaymentInfo = mergedSlip
        lastPaidAppSpecificData = splitResults.lastOrNull()?.appSpecificData
        lastMaskedPan = splitResults.lastOrNull()?.maskedPan
        lastCardBrand = splitResults.lastOrNull()?.brand
        currentSplitGroupId = null

        updateState(
            SaleState.PaymentSuccess(
                receiptNumber = order?.receiptNumber ?: "SPLIT_${System.currentTimeMillis()}",
                totalAmount = totalAmountKr,
                appliedCode = "",
                discountAmount = 0,
                slipHtml = mergedSlip
            )
        )
    }




    private fun getOrCreateSplitGroupId(): String {
        return currentSplitGroupId ?: java.util.UUID.randomUUID().toString().also {
            currentSplitGroupId = it
        }
    }

    fun buildSplitReceipt(): String {
        return splitResults.mapIndexed { index, res ->
            // Ta bort eventuell HTML som terminalen skickat i res.paymentInfo
            val cleanSlip = stripHtml(res.paymentInfo ?: "Ingen info")

            """
        
        [DELBETALNING ${index + 1}]
        --------------------------------
        $cleanSlip
        --------------------------------
        """.trimIndent()
        }.joinToString("\n")
    }

    fun stripHtml(html: String?): String {
        if (html == null) return ""
        return html
            .replace("(?i)<br\\s*/?>".toRegex(), "\n") // Ersätt <br> med ny rad
            .replace("<[^>]*>".toRegex(), "")          // Ta bort alla andra HTML-taggar
            .replace("&nbsp;", " ")                    // Fixa mellanslag
            .trim()
    }

    private fun formatPaymentInfo(rawHtml: String, width: Int = 32): String {
        val cleanText = stripHtml(rawHtml)
        val lines = cleanText.split("\n")

        return lines.joinToString("\n") { line ->
            if (line.contains("PSN", ignoreCase = true) && line.contains(":")) {
                val parts = line.split(":", limit = 2)
                formatLine(parts[0].trim() + ":", parts[1].trim(), width)
            } else {
                line
            }
        }
    }

    private fun formatLine(left: String, right: String, width: Int = 32): String {
        val spaceNeeded = width - left.length - right.length
        return if (spaceNeeded > 0) {
            left + " ".repeat(spaceNeeded) + right
        } else {
            "$left $right"
        }
    }


    /**
     * Maps an order stored in DB to the printable Order model used by the receipt formatter.
     * This lets us reuse the existing receipt HTML for DB-backed receipt reprints.
     */
    private fun OrderWithRows.toPrintableOrder(
        paymentMethod: PaymentMethod?
    ): Order {
        val date = Date(order.orderDate)

        return Order(
            receiptNumber = order.orderNumber,
            items = rows.map { row ->
                CartItem(
                    product = UiProduct(
                        name = row.productName,
                        variantValue = null,
                        price = row.unitPrice,
                        category = emptyList(),
                        imageRes = "",
                        articleNumber = row.articleNumber,
                        ean = "",
                    ),
                    quantity = row.quantity
                )
            },
            paymentMethod = paymentMethod ?: PaymentMethod.CARD,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date),
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date),
            seller = "userId"
        )
    }

    fun printReceiptWithExternalPrinter(
        context: Context,
        order: Order,
        slipHtml: String?,
        appliedCode: String = "",
        discountAmount: Int = 0
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rawLogo = BitmapFactory.decodeResource(context.resources, R.drawable.receiptcoffeelogo)
                val logo = rawLogo?.let {
                    val maxWidth = 300
                    val scale = maxWidth.toFloat() / it.width
                    val targetHeight = (it.height * scale).toInt()
                    Bitmap.createScaledBitmap(it, maxWidth, targetHeight, true)
                        .copy(Bitmap.Config.ARGB_8888, true)
                }

                val printData = receiptPrintMapper.map(
                    order = order,
                    logo = logo,
                    bankSlip = slipHtml,
                    appliedCode = appliedCode,
                    discountAmount = discountAmount
                )
                val result = terminalApi.printExternal(printData)
                Log.d("EXTERNAL_PRINT", "Result = $result")
            } catch (e: Exception) {
                Log.e("EXTERNAL_PRINT", "Fel vid extern skrivarutskrift", e)
            }
        }
    }

    fun printReceiptFromDBWithExternalPrinter(
        context: Context,
        receiptNumber: String,
        orderDao: OrderDao,
        paymentDao: PaymentDao
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val orderWithRows = runCatching {
                    orderDao.getOrderWithRows(receiptNumber)
                }.getOrNull()

                if (orderWithRows == null) {
                    Log.e("EXTERNAL_PRINT_DB", "Order hittades inte i DB: $receiptNumber")
                    return@launch
                }

                val payment = paymentDao.getPaymentByOrderNumber(receiptNumber)
                val cardDetails = payment?.let { paymentDao.getCardDetailsByPaymentId(payment.id) }

                val order = orderWithRows.toPrintableOrder(payment?.method)

                printReceiptWithExternalPrinter(
                    context = context,
                    order = order,
                    slipHtml = cardDetails?.paymentInfo
                )

            } catch (e: Exception) {
                Log.e("EXTERNAL_PRINT_DB", "Fel vid utskrift från DB", e)
            }
        }
    }
}
