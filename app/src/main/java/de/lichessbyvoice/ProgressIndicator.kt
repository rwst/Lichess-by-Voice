package de.lichessbyvoice

object ProgressIndicator {
    var showProgress: (() -> Unit)? = null
    var hideProgress: (() -> Unit)? = null

    fun setShowFunc(func: () -> Unit) {
        showProgress = func
    }
    fun setHideFunc(func: () -> Unit) {
        hideProgress = func
    }
}