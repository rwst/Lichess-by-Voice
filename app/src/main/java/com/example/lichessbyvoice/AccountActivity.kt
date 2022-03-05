package com.example.lichessbyvoice

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AccountActivity  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_activity)
        val theButton: Button = findViewById(R.id.account_button)
        theButton.setOnClickListener { linkAccount() }
    }

    private fun linkAccount() {}
}