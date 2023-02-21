package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}