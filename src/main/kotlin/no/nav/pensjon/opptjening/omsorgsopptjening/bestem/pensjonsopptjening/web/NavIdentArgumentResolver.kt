package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class NavIdent

@Component
class NavIdentArgumentResolver(
    private val navIdentProvider: NavIdentProvider,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(NavIdent::class.java) &&
            parameter.parameterType == String::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        return navIdentProvider.getNavIdent() ?: throw NavIdentArgumentNotFoundException()
    }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class NavIdentArgumentNotFoundException : RuntimeException("Fant ikke NAVident i token")
