package csv

import db.word.WordType

data class CsvData(
    val sourceWord: String,
    val translates: List<String>,
    val wordType: WordType
)