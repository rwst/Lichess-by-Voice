package de.lichessbyvoice

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.lichessbyvoice.service.LichessService
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