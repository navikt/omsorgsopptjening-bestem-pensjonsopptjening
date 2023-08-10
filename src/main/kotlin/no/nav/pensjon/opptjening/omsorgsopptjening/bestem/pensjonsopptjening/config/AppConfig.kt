package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.support.DatabaseStartupValidator
import org.springframework.retry.annotation.EnableRetry
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@EnableRetry
@EnableTransactionManagement
class AppConfig {

    @Bean
    fun databaseStartupValidator(dataSource: DataSource) = DatabaseStartupValidator().apply {
        setDataSource(dataSource)
    }

    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }

    @Bean
    fun dependsOnPostProcessor(): BeanFactoryPostProcessor? {
        return BeanFactoryPostProcessor { bf: ConfigurableListableBeanFactory ->
            val flyway = bf.getBeanNamesForType(Flyway::class.java)
            flyway.map { beanName: String? -> bf.getBeanDefinition(beanName!!) }
                .forEach { it.setDependsOn("databaseStartupValidator") }
        }
    }
}