package com.example.calculator4android

import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    var equation: String = "0"
    var history = mutableListOf<HistoryElement>()
}