package com.example.kilitwidget

import android.app.Activity
import android.app.PendingIntent
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView

class LockAdminReceiver : DeviceAdminReceiver()

class LockActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, LockAdminReceiver::class.java)
        if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}

class LockWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pi = PendingIntent.getActivity(
            context, 0,
            Intent(context, LockActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_lock)
            views.setOnClickPendingIntent(R.id.widget_root, pi)
            manager.updateAppWidget(id, views)
        }
    }
}

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var button: Button

    private val dpm get() = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val admin get() = ComponentName(this, LockAdminReceiver::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(pad, pad, pad, pad)
            setBackgroundColor(Color.WHITE)
        }
        val title = TextView(this).apply {
            text = "Tek Tık Kilit"
            textSize = 26f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        status = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, pad, 0, pad)
        }
        button = Button(this)
        button.setOnClickListener {
            if (dpm.isAdminActive(admin)) {
                dpm.removeActiveAdmin(admin)
                refresh()
            } else {
                startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                    putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Widget'a dokununca ekranı kilitleyebilmek için gerekli."
                    )
                })
            }
        }
        root.addView(title)
        root.addView(status)
        root.addView(button)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        if (dpm.isAdminActive(admin)) {
            status.text = "Yetki verildi.\nAna ekrana uzun basıp Widget'lar > Tek Tık Kilit'i ekleyin."
            button.text = "Yetkiyi kaldır"
        } else {
            status.text = "Önce kilitleme yetkisini etkinleştirin."
            button.text = "Yetkiyi etkinleştir"
        }
    }
}
