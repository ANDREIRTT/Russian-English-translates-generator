import com.zaxxer.hikari.HikariDataSource
import db.dbModule
import db.initDb
import db.rout.dbRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(Koin) {
            modules(dbModule)
        }
        install(WebSockets){
            pingPeriod = 15.seconds
            timeout = 10.seconds
        }
        get<HikariDataSource>().initDb()

        dbRouting()
    }.start(wait = true)
}
