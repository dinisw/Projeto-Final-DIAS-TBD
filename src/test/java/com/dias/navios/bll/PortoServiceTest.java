package com.dias.navios.bll;

import com.dias.navios.dal.PortoDAO;
import com.dias.navios.model.Porto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortoService — validações")
class PortoServiceTest {

    @Mock PortoDAO portoDAO;
    @InjectMocks PortoService portoService;

    private Porto portoValido;

    @BeforeEach
    void setUp() {
        portoValido = new Porto();
        portoValido.setNome("Porto de Leixões");
        portoValido.setPais("Portugal");
        portoValido.setCodigo("PTLEI");
    }

    @Test
    @DisplayName("Porto válido é registado com sucesso")
    void portoValidoERegistado() throws Exception {
        assertDoesNotThrow(() -> portoService.registarPorto(portoValido));
        verify(portoDAO).inserir(portoValido);
    }

    @Test
    @DisplayName("Nome nulo é rejeitado")
    void nomeNuloEhRejeitado() {
        portoValido.setNome(null);
        assertThrows(IllegalArgumentException.class, () -> portoService.registarPorto(portoValido));
        verifyNoInteractions(portoDAO);
    }

    @Test
    @DisplayName("Nome em branco é rejeitado")
    void nomeBrancoEhRejeitado() {
        portoValido.setNome("  ");
        assertThrows(IllegalArgumentException.class, () -> portoService.registarPorto(portoValido));
        verifyNoInteractions(portoDAO);
    }

    @Test
    @DisplayName("País nulo é rejeitado")
    void paisNuloEhRejeitado() {
        portoValido.setPais(null);
        assertThrows(IllegalArgumentException.class, () -> portoService.registarPorto(portoValido));
        verifyNoInteractions(portoDAO);
    }

    @Test
    @DisplayName("País em branco é rejeitado")
    void paisBrancoEhRejeitado() {
        portoValido.setPais("");
        assertThrows(IllegalArgumentException.class, () -> portoService.registarPorto(portoValido));
        verifyNoInteractions(portoDAO);
    }

    @Test
    @DisplayName("Código nulo é rejeitado")
    void codigoNuloEhRejeitado() {
        portoValido.setCodigo(null);
        assertThrows(IllegalArgumentException.class, () -> portoService.registarPorto(portoValido));
        verifyNoInteractions(portoDAO);
    }

    @Test
    @DisplayName("Código em branco é rejeitado")
    void codigoBrancoEhRejeitado() {
        portoValido.setCodigo("  ");
        assertThrows(IllegalArgumentException.class, () -> portoService.registarPorto(portoValido));
        verifyNoInteractions(portoDAO);
    }

    @Test
    @DisplayName("Edição com dados válidos é aceite")
    void edicaoValidaEAceite() throws Exception {
        portoValido.setId(1);
        assertDoesNotThrow(() -> portoService.editarPorto(portoValido));
        verify(portoDAO).atualizar(portoValido);
    }

    @Test
    @DisplayName("Porto pode ser apagado")
    void portoEApagado() throws Exception {
        assertDoesNotThrow(() -> portoService.apagarPorto(1));
        verify(portoDAO).apagar(1);
    }
}
