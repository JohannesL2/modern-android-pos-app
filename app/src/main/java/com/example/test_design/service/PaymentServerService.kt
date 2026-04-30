package com.example.test_design.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.test_design.MainActivity
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.response.respond

class PaymentServerService : Service() {

    private var server: NettyApplicationEngine? = null
    private val CHANNEL_ID = "payment_server_channel"
    private val NOTIFICATION_ID = 1

    companion object {
        const val ACTION_STOP_SERVER = "ACTION_STOP_SERVER"
        const val EXTRA_AMOUNT = "remote_amount"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == ACTION_STOP_SERVER) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        if (server == null) {
            startKtor()
        }

        return START_STICKY
    }

    private fun startKtor() {
        server = embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
            routing {
                get("/pay") {
                    val amount = call.parameters["amount"]?.toIntOrNull() ?: 0
                    if (amount > 0) {
                        val intent = Intent(this@PaymentServerService, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            putExtra(EXTRA_AMOUNT, amount)
                        }
                        startActivity(intent)
                        call.respondText("Betalning på $amount kr startad!")
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Ange ett giltigt belopp, ex: /pay?amount=100")
                    }
                }
            }
        }.start(wait = false)
        Log.d("PaymentServer", "Ktor körs nu på port 8080")
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPending = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, PaymentServerService::class.java).apply {
            action = ACTION_STOP_SERVER
        }
        val stopPending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Betalserver Aktiv")
            .setContentText("Lyssnar på nätverksanrop...")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setOngoing(true)
            .setContentIntent(openAppPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stäng av", stopPending)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Betalserver Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        server?.stop(500, 1000)
        server = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}