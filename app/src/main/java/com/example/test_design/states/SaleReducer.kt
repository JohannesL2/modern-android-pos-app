package com.example.test_design.states

/* ANVÄNDS INTE ÄNNU  OBS   KORRIGERA FÖR NYA SALE STATES

fun reduceSaleState(
    state: SaleState,
    event: SaleEvent
): SaleState {

    return when (state) {

        // -------------------------
        // BUILDING SALE
        // -------------------------

        is SaleState.BuildingSale -> when (event) {

            is SaleEvent.AddItem ->
                state.copy(items = state.items + event.item)

            is SaleEvent.RemoveItem ->
                state.copy(items = state.items - event.item)

            SaleEvent.StartCheckout ->
                if (state.items.isNotEmpty())
                    SaleState.ReadyForPayment
                else
                    state

            else -> state
        }

        // -------------------------
        // PAYMENT FAILED
        // -------------------------

        is SaleState.PaymentFailed -> when (event) {

            SaleEvent.StartCheckout ->
                SaleState.ReadyForPayment

            else -> state
        }

        // -------------------------
        // SUCCESS
        // -------------------------

        SaleState.PaymentSuccess -> when (event) {

            SaleEvent.ReceiptHandled ->
                SaleState.BuildingSale(
                    orderId = java.util.UUID.randomUUID().toString()
                )

            else -> state
        }

        // -------------------------
        // PROCESSING PAYMENT
        // (placeholder — används senare)
        // -------------------------

        SaleState.ProcessingPayment ->
            state
    }
}
*/