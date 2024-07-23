package com.example.bingo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

        // Find the CardView by its ID
        val cardBusqueda = findViewById<CardView>(R.id.busquedaAvanzada)

        // Set an OnClickListener
        cardBusqueda.setOnClickListener {
            val intent = Intent(this, BusquedaAvanzada::class.java)
            startActivity(intent)
        }

        fun setCameraUiVisibility(visible: Boolean) {
            val visibility = if (visible) View.VISIBLE else View.GONE
            findViewById<CardView>(R.id.tendencias).visibility = visibility
            findViewById<CardView>(R.id.busquedaAvanzada).visibility = visibility
            findViewById<CardView>(R.id.medidas).visibility = visibility
        }

        val medidasCardView = findViewById<CardView>(R.id.medidas)
        medidasCardView.setOnClickListener {
            // Ocultar UI cuando la cámara esté activa
            setCameraUiVisibility(false)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CameraFragment())
                .addToBackStack(null)
                .commit()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            // Si el CameraFragment ya no está en el stack, mostrar la UI nuevamente
            if (supportFragmentManager.fragments.none { it is CameraFragment }) {
                setCameraUiVisibility(true)
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }
}