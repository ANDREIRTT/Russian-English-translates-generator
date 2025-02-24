# RussianEnglish translates generator
## Описание
Приложение создает SQLite базу данных (.db файл) из CSV-таблицы, содержащей переводы слов

## Окружение
Для работы с проектом требуется:

- IntelliJ IDEA (рекомендуется последняя версия)
- JDK 17

## Используемые зависимости
Проект использует следующие библиотеки:

- Kotlin Coroutines – для асинхронной работы (kotlinx-coroutines-core:1.6.4)
- univocity-parsers – для работы с CSV (univocity-parsers:2.9.1)
- HikariCP – для управления подключением к базе данных (HikariCP:5.0.1)
- SQLite JDBC – драйвер для работы с SQLite (sqlite-jdbc:3.34.0)

## Пример SQL-запроса для поиска переводов
```sql
WITH vars AS (
    SELECT 'hi' AS word
)
SELECT
    source.word AS source_word,
    target.word AS translation,
    t.wordType
FROM translations t
JOIN words source ON t.sourceWordId = source.wordId
JOIN words target ON t.targetWordId = target.wordId,
vars
WHERE source.word LIKE vars.word || ' %' OR source.word = vars.word;
```
## Источники данных
Проект использует открытые словари:

- [Badestrand Russian Dictionary](https://github.com/Badestrand/russian-dictionary "Badestrand Russian Dictionary")
- [R Online Course Data](https://github.com/agricolamz/r_on_line_course_data "R Online Course Data")
