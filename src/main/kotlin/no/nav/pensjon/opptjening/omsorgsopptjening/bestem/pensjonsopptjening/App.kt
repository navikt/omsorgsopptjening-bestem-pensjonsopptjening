package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import jakarta.persistence.EntityManagerFactory
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.support.DatabaseStartupValidator
import org.springframework.retry.annotation.EnableRetry
import javax.sql.DataSource

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

@EnableRetry
@SpringBootApplication
class App {

    @Bean
    fun databaseStartupValidator(dataSource: DataSource) = DatabaseStartupValidator().apply {
        setDataSource(dataSource)
    }

    @Bean
    fun dependsOnPostProcessor(): BeanFactoryPostProcessor? {
        return BeanFactoryPostProcessor { bf: ConfigurableListableBeanFactory ->
            val flyway = bf.getBeanNamesForType(Flyway::class.java)
            flyway.map { beanName: String? -> bf.getBeanDefinition(beanName!!) }
                .forEach { it.setDependsOn("databaseStartupValidator") }

            val jpa = bf.getBeanNamesForType(EntityManagerFactory::class.java)
            jpa.map { beanName: String? -> bf.getBeanDefinition(beanName!!) }
                .forEach { it.setDependsOn("databaseStartupValidator") }
        }
    }
}