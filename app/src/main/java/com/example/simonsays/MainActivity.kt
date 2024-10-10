package com.example.simonsays

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var userName: String = ""
    private lateinit var gridView: GridView
    private lateinit var playButton: Button
    private lateinit var scoreTextView: TextView
    private val sequence = mutableListOf<Int>()
    private var userInputIndex = 0
    private var score = 0
    private val imagesAdapter = ImagesAdapter(this)
    private val handler = Handler()

    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridView = findViewById(R.id.grindView)
        playButton = findViewById(R.id.button_play)
        scoreTextView = findViewById(R.id.score_text_view)
        val btnRanking: Button = findViewById(R.id.BtnRanking)

        imagesAdapter.shuffleImages()
        gridView.adapter = imagesAdapter

        checkStoragePermissions()

        btnRanking.setOnClickListener {
            startActivity(Intent(this, RankingActivity::class.java))
        }

        playButton.setOnClickListener {
            playButton.isEnabled = false
            showNameInputDialog()
        }
    }

    private fun checkStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
        }
    }

    private fun showNameInputDialog() {
        val editText = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Ingresa tu nombre")
            .setView(editText)
            .setPositiveButton("Aceptar", null)
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    positiveButton.isEnabled = s?.isNotEmpty() == true
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            positiveButton.setOnClickListener {
                val nameInput = editText.text.toString()
                if (nameInput.isNotEmpty()) {
                    userName = nameInput
                    startGame(userName)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@MainActivity, "Por favor, ingresa un nombre.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun startGame(userName: String) {
        this.userName = userName
        sequence.clear()
        userInputIndex = 0
        addNewColor()
        showSequence()
    }

    private fun addNewColor() {
        val newColor = Random.nextInt(0, 4)
        sequence.add(newColor)
    }

    private fun showSequence() {
        userInputIndex = 0
        disableUserInput()
        handler.postDelayed({ highlightNextColor(0) }, 1000)
    }

    private fun highlightNextColor(index: Int) {
        if (index < sequence.size) {
            val colorIndex = sequence[index]
            val gridViewItem = gridView.getChildAt(colorIndex) as View
            val imageView = gridViewItem.findViewById<ImageView>(R.id.img)

            imageView.setImageResource(imagesAdapter.getBrightImage(colorIndex))

            handler.postDelayed({
                imageView.setImageResource(imagesAdapter.images[imagesAdapter.getOriginalIndex(colorIndex)])
                handler.postDelayed({ highlightNextColor(index + 1) }, 300)
            }, 300)
        } else {
            handler.postDelayed({ enableUserInput() }, 50)
        }
    }

    private fun enableUserInput() {
        gridView.setOnItemClickListener { _, _, position, _ ->
            if (position == sequence[userInputIndex]) {
                userInputIndex++
                val gridViewItem = gridView.getChildAt(position) as View
                val imageView = gridViewItem.findViewById<ImageView>(R.id.img)
                imageView.setImageResource(imagesAdapter.getBrightImage(position))

                handler.postDelayed({
                    imageView.setImageResource(imagesAdapter.images[imagesAdapter.getOriginalIndex(position)])
                }, 250)

                if (userInputIndex == sequence.size) {
                    Toast.makeText(this, "Correcto!", Toast.LENGTH_SHORT).show()
                    score++
                    scoreTextView.text = "Puntaje: $score"
                    addNewColor()
                    showSequence()
                }
            } else {
                Toast.makeText(this, "Has perdido!", Toast.LENGTH_SHORT).show()
                guardarPuntuacioPartida(score)
                resetGame()
            }
        }
    }

    private fun resetGame() {
        gridView.setOnItemClickListener(null)
        handler.postDelayed({
            startActivity(Intent(this, LossActivity::class.java))
            finish()
        }, 1000)
    }

    private fun disableUserInput() {
        gridView.setOnItemClickListener(null)
    }

    private fun guardarPuntuacioPartida(puntuacion: Int) {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fechaHoraFormateada = now.format(formatter)

        val file = File(getExternalFilesDir(null), "PuntuacioPartida.json")
        val existingScores = mutableListOf<JSONObject>()
        var scoreUpdated = false

        // Leer el archivo existente
        if (file.exists()) {
            file.forEachLine { line ->
                try {
                    val json = JSONObject(line)
                    if (json.getString("nombre") == userName) {
                        if (puntuacion > json.getInt("puntuacion")) {
                            json.put("puntuacion", puntuacion)
                            json.put("fechaHora", fechaHoraFormateada)
                            scoreUpdated = true
                        }
                    }
                    existingScores.add(json)
                } catch (e: Exception) {
                    Log.e("Error", "Error al leer línea del archivo: ${e.message}")
                }
            }
        }

        if (!scoreUpdated) {
            val json = JSONObject().apply {
                put("fechaHora", fechaHoraFormateada)
                put("puntuacion", puntuacion)
                put("nombre", userName)
            }
            existingScores.add(json)
        }

        try {
            FileOutputStream(file).use { output ->
                existingScores.forEach { json ->
                    output.write((json.toString() + System.lineSeparator()).toByteArray())
                }
            }
        } catch (e: IOException) {
            Log.e("Error", "ERROR AL GUARDAR EL FICHERO: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Manejo de permisos, si es necesario
    }
}
