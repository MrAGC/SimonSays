package com.example.simonsays

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File

class RankingActivity : AppCompatActivity() {

    private lateinit var rankingListView: ListView
    private lateinit var exitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        rankingListView = findViewById(R.id.ranking_list_view)
        exitButton = findViewById(R.id.exit_ranking_button)

        loadRanking()

        exitButton.setOnClickListener {
            finish()
        }
    }

    private fun loadRanking() {
        val file = File(getExternalFilesDir(null), "PuntuacioPartida.json")
        val rankings = mutableListOf<String>()

        if (file.exists()) {
            try {
                file.forEachLine { line ->
                    val jsonObject = JSONObject(line)
                    // Verificar si el campo "nombre" existe
                    if (jsonObject.has("nombre")) {
                        val nombre = jsonObject.getString("nombre")
                        val puntuacion = jsonObject.getInt("puntuacion")
                        rankings.add("$nombre: $puntuacion")
                    } else {
                        Log.e("RankingActivity", "Entrada sin nombre: $line")
                    }
                }
                if (rankings.isNotEmpty()) {
                    rankings.sortByDescending { it.split(": ")[1].toInt() }
                    val adapter = ArrayAdapter(this, R.layout.list_item_ranking, rankings)
                    rankingListView.adapter = adapter
                } else {
                    Toast.makeText(this, "No hay datos de ranking disponibles.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al leer el archivo de ranking: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No hay datos de ranking disponibles.", Toast.LENGTH_SHORT).show()
        }
    }
}
