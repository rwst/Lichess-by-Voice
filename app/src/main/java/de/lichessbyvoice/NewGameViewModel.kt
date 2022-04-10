package de.lichessbyvoice

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.lichessbyvoice.service.LichessService
import kotlinx.coroutines.launch

class NewGameViewModel : ViewModel() {
    private val newGame: MutableLiveData<LichessService.GameDataEntry?> by lazy {
        MutableLiveData<LichessService.GameDataEntry?>().also {
            viewModelScope.launch {
                val channel = LichessService.aiGameParamChannel
                val params = channel.receive()
                ProgressIndicator.showProgress?.let { it() }
                it.value = LichessService.postChallengeAi(params)

                LichessService.newGameDataChannel.send(newGame.value)
                Log.i("NewGameViewModel", "sent $newGame.value")
                ProgressIndicator.hideProgress?.let { it() }
            }
        }
    }

    fun getGame(): LiveData<LichessService.GameDataEntry?> {
        return newGame
    }
}