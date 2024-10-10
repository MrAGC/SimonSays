package com.example.simonsays

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import kotlin.random.Random

class ImagesAdapter(val context: Context) : BaseAdapter() {
    val images = intArrayOf(R.drawable.azul, R.drawable.rojo, R.drawable.verde, R.drawable.amarillo)
    private val images_b = intArrayOf(R.drawable.azul_b, R.drawable.rojo_b, R.drawable.verde_b, R.drawable.amarillo_b)

    private var shuffledIndices = images.indices.toList() // Lista de índices originales

    // Función para mezclar los colores
    fun shuffleImages() {
        shuffledIndices = shuffledIndices.shuffled() // Mezclar solo los índices
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var gridView: View? = convertView
        if (gridView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            gridView = inflater.inflate(R.layout.custom_image, parent, false)
        }
        val imageView = gridView?.findViewById<ImageView>(R.id.img) as ImageView
        imageView.setImageResource(images[shuffledIndices[position]]) // Usar el índice mezclado
        return gridView
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun getItem(position: Int): Any {
        return images[shuffledIndices[position]]
    }

    override fun getItemId(position: Int): Long {
        return shuffledIndices[position].toLong()
    }

    // Método para obtener el índice original de un color mezclado
    fun getOriginalIndex(shuffledIndex: Int): Int {
        return shuffledIndices[shuffledIndex]
    }

    // Método para obtener el recurso brillante correspondiente
    fun getBrightImage(shuffledIndex: Int): Int {
        return images_b[shuffledIndices[shuffledIndex]]
    }
}
