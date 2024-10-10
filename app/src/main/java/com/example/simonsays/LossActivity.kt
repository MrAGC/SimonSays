package com.example.simonsays

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LossActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loss)

        val retryButton: Button = findViewById(R.id.retry_button)
        val exitButton: Button = findViewById(R.id.exit_button)
        val rankingButton: Button = findViewById(R.id.ranking_button)

        retryButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        exitButton.setOnClickListener {
            finish()
        }

        rankingButton.setOnClickListener {
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }
    }
}
