package csv

import com.univocity.parsers.common.ParsingContext
import com.univocity.parsers.common.processor.AbstractRowProcessor
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import db.word.WordType
import java.io.File
import java.io.FileReader

class CsvReader {
    fun read(
        wordType: WordType,
        onEach: (csvData: CsvData) -> Unit
    ) {
        val file = when (wordType) {
            WordType.NOUN -> File("src/main/resources/openRussian/nouns.csv")
            WordType.VERB -> File("src/main/resources/openRussian/verbs.csv")
            WordType.ADJECTIVE -> File("src/main/resources/openRussian/adjectives.csv")
            WordType.ADVERB -> File("src/main/resources/openRussian/others.csv")
            WordType.PRONOUN -> TODO("Not implemented")
            WordType.PREPOSITION -> TODO("Not implemented")
            WordType.CONJUNCTION -> TODO("Not implemented")
            WordType.INTERJECTION -> TODO("Not implemented")
        }

        val settings = CsvParserSettings().apply {
            // Разделитель — табуляция
            format.delimiter = '\t'
            // Символ кавычек — '~'
            format.quote = '~'
            // Символ экранирования — '\'
            // Отключаем автоматическое извлечение заголовков, будем обрабатывать вручную
            isHeaderExtractionEnabled = false
            // Обработчик ошибок: логируем и пропускаем некорректную строку
            setProcessorErrorHandler { error, row, _ ->
                println("Ошибка при разборе строки: ${error.message}. Строка будет пропущена: ${row.joinToString(",")}")
            }
        }

        // Собственный RowProcessor, который собирает заголовки из первой строки и далее формирует Map для каждой строки
        val rowProcessor = object : AbstractRowProcessor() {
            var headers: Array<String>? = null
            val rows = mutableListOf<Map<String, String>>()

            override fun rowProcessed(row: Array<String>, context: ParsingContext) {
                if (headers == null) {
                    // Первая строка считается заголовками
                    headers = row
                } else {
                    val headerList = headers!!.toList()
                    // Если количество значений меньше количества заголовков – дополняем пустыми строками,
                    // если больше – обрезаем лишние значения
                    val rowList = row.toList().let { list ->
                        when {
                            list.size < headerList.size -> list + List(headerList.size - list.size) { "" }
                            else -> list.take(headerList.size)
                        }
                    }
                    rows.add(headerList.zip(rowList).toMap())
                }
            }
        }

        settings.rowProcessor = rowProcessor

        // Создаем парсер и читаем файл с кодировкой UTF-8
        val parser = CsvParser(settings)
        FileReader(file, Charsets.UTF_8).use { reader ->
            parser.parse(reader)
        }

        // Обрабатываем полученные строки
        for (map in rowProcessor.rows) {
            val source = map["bare"]
            val translations = map["translations_en"]
                ?.split(",")
                ?.flatMap { it.split(";") }
                ?.map { it.trimStart() }
                ?.filter { it.isNotEmpty() }
            if (!source.isNullOrEmpty() && !translations.isNullOrEmpty()) {
                onEach(
                    CsvData(
                        sourceWord = source,
                        translates = translations,
                        wordType = wordType
                    )
                )
            } else {
                println("error parse source: $source, translation: $translations")
            }
        }
    }
}
