package com.ztch.medilens_android_app.Camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel: ViewModel() {

    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    //stateflow allows to elminate the transfomation from mutable stateflow to stateflow
    val bitmaps: StateFlow<List<Bitmap>> get() = _bitmaps

    fun onTakePhoto(bitmap: Bitmap) {
        //avoided creating new lists
        _bitmaps.value += bitmap
    }
}

