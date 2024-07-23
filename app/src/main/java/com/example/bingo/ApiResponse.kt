package com.example.bingo

data class ApiResponse(
    val predictions: List<Prediction>
)
data class Prediction(
    val label: String
)
