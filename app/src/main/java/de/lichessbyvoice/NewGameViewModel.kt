package de.lichessbyvoice

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NewGameViewModel : ViewModel() {
    private val newGame: MutableLiveData<LichessService.GameDataEntry?> by lazy {
        MutableLiveData<LichessService.GameDataEntry?>().also {
            viewModelScope.launch {
                val params = LichessService.aiGameParamChannel.receive()
                Log.i("NewGameViewModel", "received $params")
                ProgressIndicator.showProgress?.let { it() }
                it.value = LichessService.postChallengeAi(params)
                ProgressIndicator.hideProgress?.let { it() }
            }
        }
    }

    fun getGame(): LiveData<LichessService.GameDataEntry?> {
        return newGame
    }
}