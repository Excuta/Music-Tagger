package com.excuta.musictagger.song

data class Song(
    val id: Long,
    val title: String,
    val fileName: String,
    val artist: String,
    val album: String,
    val data: String
)