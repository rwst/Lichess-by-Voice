package de.lichessbyvoice

import android.content.pm.PackageManager
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
import kotlinx.coroutines.runBlocking

class NewGameActivity : AppCompatActivity() {
    private var theGameParams = LichessService.AiGameParams(1, "random", "standard")
    private var newGameCode: String? = null

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
        variantSwitch.isEnabled = false
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
        runBlocking {
            LichessService.aiGameParamChannel.send(theGameParams)
            newGameCode = LichessService.newGameDataChannel.receive()?.id
        }
    }

    private fun newGame() {
        if (newGameCode != null) {
            SpeechRecognitionService.start(this@NewGameActivity, newGameCode!!)
            Log.i(TAG, "showing game $newGameCode")
        }
        else
        {
            Log.e(TAG, "create game failed")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SpeechRecognitionService.PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                newGame()
            } else {
                finish()
            }
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