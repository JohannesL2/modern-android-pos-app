package com.example.integration.internal.terminal

/*
PSEUDO-CODE ONLY

TerminalConnectionManager observes connection and transaction-manager state from
the payment terminal SDK and exposes app-level readiness flags.

state connectionConfig
state isConnected = false
state terminalReady = false

setConnectionConfig(config):
    connectionConfig = config

startObserving():
    collect terminalSdk.statusEvents:
        when event means connected:
            isConnected = true
        when event means disconnected:
            isConnected = false
            terminalReady = false

    collect terminalSdk.transactionManagerState:
        terminalReady = state can accept a new transaction

setConnected(value):
    isConnected = value
    if value is false:
        terminalReady = false
*/
