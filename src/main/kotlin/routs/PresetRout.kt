package routs

import db.preset.PresetRepository
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import preset.Level
import preset.PresetData
import preset.PresetParams
import java.io.File

fun Application.presetRouting() {
    val presetRepository by inject<PresetRepository>()

    routing {
        post("/preset") {
            val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 100)
            val presetParams = PresetParams.entries.associate {
                it.paramName to false
            }.toMutableMap()

            val presetValues = PresetParams.entries.associate {
                it.paramName to listOf<String>()
            }.toMutableMap()


            multipartData.receiveParts(presetParams, presetValues)

            checkPresetParams(presetParams) {
                return@post
            }

            val errors = mutableListOf<String>()

            val skippedWords = mutableListOf<String>()
                presetValues[PresetParams.JSON.paramName]!!.forEach {
                    try {
                        presetRepository.insertPreset(
                            Json.decodeFromString<PresetData>(File(it).readText())
                        ) { word ->
                            skippedWords.add(word)
                        }
                    } catch (e: Exception) {
                        errors.add("${e.message}")
                    }
                }

            call.respondText(
                text = "Skipped words: ${skippedWords.joinToString(", ")}, errors: ${errors.joinToString(", ")}",
                status = HttpStatusCode.OK
            )
        }
    }
}

private suspend fun MultiPartData.receiveParts(
    presetParams: MutableMap<String, Boolean>,
    presetValues: MutableMap<String, List<String>>
) {
    forEachPart { part ->
        when (part) {
            is PartData.FileItem -> {
                val paramName = part.name
                if (presetParams.containsKey(paramName)) {
                    presetParams[paramName!!] = true

                    part.provider().copyAndClose(File("presets/${part.originalFileName as String}").also {
                        File("presets").mkdirs()
                        presetValues[paramName] = presetValues[paramName]!! + it.absolutePath
                    }.writeChannel())
                }
            }

            else -> {}
        }
        part.dispose()
    }
}

private suspend inline fun RoutingContext.checkPresetParams(
    presetParams: MutableMap<String, Boolean>,
    onExit: () -> Unit
) {
    val missingParams = presetParams.filter { !it.value }.keys.joinToString(", ")
    if (missingParams.isNotEmpty()) {
        call.respondText(
            text = "Missing parameters: $missingParams",
            status = HttpStatusCode.BadRequest
        )
        onExit()
    }
}