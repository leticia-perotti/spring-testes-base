package br.com.alura.forum.service

import br.com.alura.forum.exception.NotFoundException
import br.com.alura.forum.mapper.TopicoFormMapper
import br.com.alura.forum.mapper.TopicoViewMapper
import br.com.alura.forum.model.Topico
import br.com.alura.forum.model.TopicoTest
import br.com.alura.forum.model.TopicoViewTest
import br.com.alura.forum.repository.TopicoRepository
import io.mockk.InternalPlatformDsl.toArray
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.*


class TopicoServiceTest {

    val topicos = PageImpl(listOf(TopicoTest.build()))

    val paginacao: Pageable = mockk()

    val topicoRepository: TopicoRepository = mockk{
        every { findByCursoNome(any(), any()) } returns topicos
        every { findAll(paginacao) } returns topicos
        every { findById(any()) } returns Optional.empty()
    }


    val topicoViewMapper: TopicoViewMapper = mockk{
        every { map(any()) } returns TopicoViewTest.build()
    }

    val topicoFormMapper: TopicoFormMapper = mockk()

    val topicoService = TopicoService(
        topicoRepository, topicoViewMapper, topicoFormMapper
    )
    val slot = slot<Topico>()

    @Test
    fun `deve listar topicos a partir do nome do curso`(){
        every { topicoViewMapper.map(capture(slot)) } returns TopicoViewTest.build()

        topicoService.listar("Kotlin avançado", paginacao)

        val topico = TopicoTest.build()
        assertThat(slot.captured.titulo).isEqualTo(topico.titulo)
        assertThat(slot.captured.mensagem).isEqualTo(topico.mensagem)
        assertThat(slot.captured.status).isEqualTo(topico.status)

        verify(exactly = 1) { topicoViewMapper.map(any()) }

        verify(exactly = 1) { topicoRepository.findByCursoNome(any(), any()) }
        verify(exactly = 0) { topicoRepository.findAll(paginacao) }
    }

    @Test
    fun `deve listar todos os topicos se o nome do curso for nulo`(){
        topicoService.listar(null, paginacao)

        verify(exactly = 1) { topicoViewMapper.map(any()) }

        verify(exactly = 0) { topicoRepository.findByCursoNome(any(), any()) }
        verify(exactly = 1) { topicoRepository.findAll(paginacao) }
    }

    @Test
    fun `deve listar not found execption quando o topico nao for achado`(){

        val  atual = assertThrows<NotFoundException> {
            topicoService.buscarPorId(1)
        }

        assertThat(atual.message).isEqualTo("Topico nao encontrado!")
    }
}