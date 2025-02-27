package db.rout

import db.DatabaseCreator
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

fun Application.dbRouting() {
    val databaseCreator by inject<DatabaseCreator>()

    routing {
        webSocket("/create") {
            databaseCreator.create {
                launch {
                    send(Frame.Text(it))
                }
            }
        }
    }
}