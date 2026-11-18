package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest

class NavIdentArgumentResolverTest {

    private val navIdentProvider: NavIdentProvider = mock()
    private val resolver = NavIdentArgumentResolver(navIdentProvider)

    @Test
    fun `resolveArgument returnerer NavIdent fra provider`() {
        whenever(navIdentProvider.getNavIdent()).thenReturn("Z123456")

        val value = resolver.resolveArgument(
            parameter = methodParameterForNavIdent(),
            mavContainer = null,
            webRequest = mock<NativeWebRequest>(),
            binderFactory = null,
        )

        assertThat(value).isEqualTo("Z123456")
    }

    @Test
    fun `resolveArgument kaster exception naar NavIdent mangler`() {
        whenever(navIdentProvider.getNavIdent()).thenReturn(null)

        assertThatThrownBy {
            resolver.resolveArgument(
                parameter = methodParameterForNavIdent(),
                mavContainer = null,
                webRequest = mock<NativeWebRequest>(),
                binderFactory = null,
            )
        }.isInstanceOf(NavIdentArgumentNotFoundException::class.java)
    }

    private fun methodParameterForNavIdent(): MethodParameter {
        val method = TestController::class.java.getDeclaredMethod("endpoint", String::class.java)
        return MethodParameter(method, 0)
    }

    private class TestController {
        @Suppress("unused")
        fun endpoint(@NavIdent navIdent: String) = navIdent
    }
}
