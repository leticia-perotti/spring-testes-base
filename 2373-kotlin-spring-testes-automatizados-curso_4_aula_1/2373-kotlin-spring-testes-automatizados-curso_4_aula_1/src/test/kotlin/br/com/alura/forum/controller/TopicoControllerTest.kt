package br.com.alura.forum.controller

import antlr.debug.ParserMatchEvent.TOKEN
import br.com.alura.forum.config.JWTUtil
import br.com.alura.forum.model.Role
import br.com.alura.forum.model.UsuarioTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.assertj.AssertableReactiveWebApplicationContext
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcBuilder
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TopicoControllerTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc : MockMvc

    @Autowired
    private lateinit var jwtUtil: JWTUtil

    companion object {
        private  const val RECURSO = "/topicos/"
        private const val RECURSO_ID = RECURSO.plus("%s")
    }

    private fun gerarToker(): String? {
        val authorities = mutableListOf(Role(1, "LEITURA_ESCRITA"))
        val usuario = UsuarioTest.buildToToken()

        return jwtUtil.generateToken(usuario.email, authorities)
    }

    private var jwt :String? = null

    @BeforeEach
    fun setup(){
        jwt = gerarToker()

        mockMvc = MockMvcBuilders.webAppContextSetup(
            webApplicationContext
        ).apply<DefaultMockMvcBuilder?>(
            SecurityMockMvcConfigurers.springSecurity()
        ).build()
    }

    @Test
    fun `deve retornar o codigo 400 quando chamar topicos sem tocken`(){
        mockMvc.get(RECURSO).andExpect { status { is4xxClientError() } }
    }

    @Test
    fun `deve retornar o codigo 200 quando chamar topicos com tocken`(){
        mockMvc.get(RECURSO) {
            headers { jwt?.let { this.setBearerAuth(it) } }
        }.andExpect { status { is2xxSuccessful() } }
    }

    @Test
    fun `deve retornar codigo 200 quando chamar topico por is com token`(){
        mockMvc.get(RECURSO_ID.format(1)){
            headers { jwt?.let { this.setBearerAuth(it) } }
        }.andExpect { status { is2xxSuccessful() } }
    }

}