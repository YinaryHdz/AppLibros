package com.example.applibros

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Convertir 200dp a píxeles (ajustable)
        val logoSizeDp = 200
        val logoSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            logoSizeDp.toFloat(),
            resources.displayMetrics
        ).toInt()

        val imageView = ImageView(this).apply {
            setImageResource(R.drawable.splash_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = FrameLayout.LayoutParams(
                logoSizePx,
                logoSizePx
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        val frameLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#E6F7FF")) // tu color azul claro de fondo
            addView(imageView)
        }

        setContentView(frameLayout)

        // Ir a MainActivity tras 1 segundo
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1000)
    }
}
