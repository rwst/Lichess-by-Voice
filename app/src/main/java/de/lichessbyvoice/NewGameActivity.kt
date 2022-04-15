package de.lichessbyvoice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.forEach
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import de.lichessbyvoice.service.LichessService
import de.lichessbyvoice.service.SpeechRecognitionService
import kotlinx.coroutines.launch

// Copyright 2022 Ralf Stephan
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

class NewGameActivity : AppCompatActivity() {
    private var theGameParams = LichessService.AiGameParams(1, "random", "standard")
    private var newGameCode: String? = null
    private var newGameColor: String? = null

    private val buttons: IntArray = intArrayOf(
        R.id.button1,
        R.id.button2,
        R.id.button3,
        R.id.button4,
        R.id.button5,
        R.id.button6,
        R.id.button7,
        R.id.button8,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.newgame_activity)
        val variantSwitch: SwitchCompat = findViewById(R.id.newgame_variant)
        variantSwitch.isEnabled = true
        variantSwitch.setOnCheckedChangeListener { _, isChecked ->
            when(isChecked) {
                true -> theGameParams.variant = "chess960"
                false -> theGameParams.variant = "standard"
            }
        }
        val strengthRadio: MaterialButtonToggleGroup = findViewById(R.id.newgame_radio_strength)
        strengthRadio.check(buttons[theGameParams.level - 1])
        strengthRadio.forEach { button ->
            button.setOnClickListener {
                val mbutton = (button as MaterialButton)
                mbutton.isChecked = true
                theGameParams.level = buttons.indexOf(mbutton.id) + 1
            }
        }
        val startWhite: Button = findViewById(R.id.newgame_color_white)
        startWhite.setOnClickListener { go("white") }
        val startRandom: Button = findViewById(R.id.newgame_color_random)
        startRandom.setOnClickListener { go("random") }
        val startBlack: Button = findViewById(R.id.newgame_color_black)
        startBlack.setOnClickListener { go("black") }
        ProgressIndicator.setShowFunc { showProgress() }
        ProgressIndicator.setHideFunc { hideProgress() }
        hideProgress()
    }

    private fun go(color: String) {
        theGameParams.color = color
        Log.i(TAG, "Start game, variant: ${theGameParams.variant}, level: ${theGameParams.level}, color: ${theGameParams.color}")
        val model: NewGameViewModel by viewModels()
        model.getGame().observe(this) { }  // TODO
        SelectGameActivity.mainScope.launch {
            LichessService.aiGameParamChannel.send(theGameParams)
            val newGame = LichessService.newGameDataChannel.receive()
            if (newGame != null) {
                newGameCode = newGame.id
                newGameColor = newGame.color
                newGame()
            }
        }
    }

    private fun newGame() {
        if (newGameCode != null) {
            val intentFlags = Intent.FLAG_ACTIVITY_NEW_TASK
            SpeechRecognitionService.start(this@NewGameActivity,
                intentFlags,
                newGameCode!!,
                newGameColor!!)
            Log.i(TAG, "showing game $newGameCode / $newGameColor")
            finish()
        }
        else
        {
            Log.e(TAG, "create game failed")
        }
    }

    private fun showProgress() {
        val spinner: ProgressBar = findViewById(R.id.newGame_progressBar)
        spinner.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        val spinner: ProgressBar = findViewById(R.id.newGame_progressBar)
        spinner.visibility = View.GONE
    }

    companion object {
        private const val TAG = "NewGameActivity"
    }
}