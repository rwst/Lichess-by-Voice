package de.lichessbyvoice

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