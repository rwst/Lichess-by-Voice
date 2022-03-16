package de.lichessbyvoice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ActiveGamesViewModel : ViewModel() {
    private val games: MutableLiveData<List<LichessService.GameData>> by lazy {
        MutableLiveData<List<LichessService.GameData>>().also {
            viewModelScope.launch {
                ProgressIndicator.showProgress?.let { it() }
                it.value = LichessService.getSuspendedGames()
                ProgressIndicator.hideProgress?.let { it() }
            }
        }
    }

    fun getGames(): LiveData<List<LichessService.GameData>> {
        return games
    }
}