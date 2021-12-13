package hello

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger

@SpringBootApplication
class KotlinApplication {

    var countWithoutHits = AtomicInteger(0)
    val hitCount = AtomicInteger(0)
    val tlr = ThreadLocalRandom.current()

    @Bean
    fun routes() = router {
        GET {
            ServerResponse.ok().body(Mono.just("Let the battle begin!"))
        }

        POST("/**", accept(APPLICATION_JSON)) { request ->
            request.bodyToMono(ArenaUpdate::class.java).flatMap { arenaUpdate ->
                val selfLink: String = arenaUpdate._links.self.href
                val self: PlayerState = arenaUpdate.arena.state[selfLink]!!

                println(self)

                var hitCountValue = 0

                if (self.wasHit) {
                    hitCountValue = hitCount.incrementAndGet()
                }

                return@flatMap when (self.direction) {
                    "N" -> {
                        arenaUpdate.arena.state.values
                            .firstOrNull { player -> player.x == self.x && player.y - self.y >= -3 && player.y - self.y < 0 }
                            ?.let {
                                if (hitCountValue == 3) {
                                    hitCount.set(0)
                                    println("moving forward north after being hit too many times")
                                    ServerResponse.ok().body(Mono.just("F"))
                                } else {
                                    println("thrown north")
                                    ServerResponse.ok().body(Mono.just("T"))
                                }
                            }
                            ?: if (countWithoutHits.getAndIncrement() + + tlr.nextInt(0, 2) % 3 == 0) {
                                println("moving forward north")
                                ServerResponse.ok().body(Mono.just("F"))
                            } else if (hitCountValue == 3) {
                                hitCount.set(0)
                                println("moving forward north after being hit too many times")
                                ServerResponse.ok().body(Mono.just("F"))
                            } else {
                                ServerResponse.ok().body(Mono.just("R"))
                            }

                    }
                    "S" -> {
                        arenaUpdate.arena.state.values
                            .firstOrNull { player -> player.x == self.x && player.y - self.y <= 3 && player.y - self.y > 0 }
                            ?.let {
                                if (hitCountValue == 3) {
                                    hitCount.set(0)
                                    println("moving south after being hit too many times")
                                    ServerResponse.ok().body(Mono.just("F"))
                                } else {
                                    println("thrown south")
                                    ServerResponse.ok().body(Mono.just("T"))
                                }
                            }
                            ?: if (countWithoutHits.getAndIncrement() + + tlr.nextInt(0, 2) % 3 == 0) {
                                println("moving forward south")
                                ServerResponse.ok().body(Mono.just("F"))
                            } else if (hitCountValue == 3) {
                                hitCount.set(0)
                                println("moving forward south after being hit too many times")
                                ServerResponse.ok().body(Mono.just("F"))
                            } else {
                                ServerResponse.ok().body(Mono.just("R"))
                            }

                    }
                    "W" -> {
                        arenaUpdate.arena.state.values
                            .firstOrNull { player -> player.y == self.y && player.x - self.x <= -3 && player.y - self.y < 0 }
                            ?.let {
                                if (hitCountValue == 3) {
                                    hitCount.set(0)
                                    println("moving west after being hit too many times")
                                    ServerResponse.ok().body(Mono.just("F"))
                                } else {
                                    println("thrown west")
                                    ServerResponse.ok().body(Mono.just("T"))
                                }
                            }
                            ?: if (countWithoutHits.getAndIncrement() + + tlr.nextInt(0, 2) % 3 == 0) {
                                println("moving forward west")
                                ServerResponse.ok().body(Mono.just("F"))
                            } else if (hitCountValue == 3) {
                                hitCount.set(0)
                                println("moving forward West after being hit too many times")
                                ServerResponse.ok().body(Mono.just("F"))
                            } else {
                                ServerResponse.ok().body(Mono.just("R"))
                            }
                    }
                    else -> {
                        arenaUpdate.arena.state.values
                            .firstOrNull { player -> player.y == self.y && player.x - self.x <= 3 && player.x - self.x > 0 }
                            ?.let {
                                if (hitCountValue == 3) {
                                    hitCount.set(0)
                                    println("moving west after being hit too many times")
                                    ServerResponse.ok().body(Mono.just("F"))
                                } else {
                                    println("thrown west")
                                    ServerResponse.ok().body(Mono.just("T"))
                                }
                            }
                            ?: if (countWithoutHits.getAndIncrement() + + tlr.nextInt(0, 2) % 3 == 0) {
                                println("moving forward north")
                                ServerResponse.ok().body(Mono.just("F"))
                            } else if (hitCountValue == 3) {
                                hitCount.set(0)
                                println("moving forward east after being hit too many times")
                                ServerResponse.ok().body(Mono.just("F"))
                            } else {
                                ServerResponse.ok().body(Mono.just("R"))
                            }
                    }
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<KotlinApplication>(*args)
}

data class ArenaUpdate(val _links: Links, val arena: Arena)
data class PlayerState(val x: Int, val y: Int, val direction: String, val score: Int, val wasHit: Boolean)
data class Links(val self: Self)
data class Self(val href: String)
data class Arena(val dims: List<Int>, val state: Map<String, PlayerState>)
