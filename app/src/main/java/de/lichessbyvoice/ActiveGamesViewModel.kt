package de.lichessbyvoice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.lichessbyvoice.service.LichessService
import kotlinx.coroutines.launch

class ActiveGamesViewModel : ViewModel() {
    private val games: MutableLiveData<LichessService.GameData?> by lazy {
        MutableLiveData<LichessService.GameData?>().also {
            viewModelScope.launch {
                ProgressIndicator.showProgress?.let { it() }
                it.value = LichessService.getSuspendedGames()
                ProgressIndicator.hideProgress?.let { it() }
            }
        }
    }

    fun getGames(): LiveData<LichessService.GameData?> {
        return games
    }
}