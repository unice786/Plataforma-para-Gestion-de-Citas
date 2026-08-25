package com.gestioncitas.plataformacitas;

import com.gestioncitas.plataformacitas.dto.ClienteRegistroDTO;
import com.gestioncitas.plataformacitas.dto.RestablecerPasswordDTO;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import com.gestioncitas.plataformacitas.repositorios.UsuarioRepository;
import com.gestioncitas.plataformacitas.servicios.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private ClienteRegistroDTO dtoValido;

    @BeforeEach
    void setUp() {
        dtoValido = new ClienteRegistroDTO();
        dtoValido.setNombre("Ana López");
        dtoValido.setCorreo("ANA@correo.com");
        dtoValido.setTelefono("0991234567");
        dtoValido.setPassword("claveSegura123");
        dtoValido.setConfirmarPassword("claveSegura123");
    }

    @Test
    void registrar_creaClienteConPasswordEncriptadaYToken() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hash");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Cliente> resultado = usuarioService.registrar(dtoValido);

        assertTrue(resultado.isPresent());
        assertEquals("ana@correo.com", resultado.get().getCorreo());
        assertEquals("$2a$10$hash", resultado.get().getPassword());
        assertFalse(resultado.get().getVerificado());
        assertNotNull(resultado.get().getTokenVerificacion());
        assertNotNull(resultado.get().getTokenExpiracion());
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    void registrar_rechazaCorreoDuplicado() {
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(true);

        Optional<Cliente> resultado = usuarioService.registrar(dtoValido);

        assertTrue(resultado.isEmpty());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void registrar_rechazaContrasenasQueNoCoinciden() {
        dtoValido.setConfirmarPassword("otraContrasena");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrar(dtoValido));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void autenticar_rechazaPasswordEnTextoPlano() {
        Usuario usuario = new Cliente();
        usuario.setPassword("$2a$10$hash");
        usuario.setVerificado(true);
        when(usuarioRepository.findByCorreo(anyString())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clavePlana", "$2a$10$hash")).thenReturn(false);

        Optional<Usuario> resultado = usuarioService.autenticar("ana@correo.com", "clavePlana");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void verificar_marcaCuentaComoVerificada() {
        Cliente cliente = new Cliente();
        cliente.setTokenVerificacion("token-123");
        cliente.setTokenExpiracion(LocalDateTime.now().plusHours(1));
        when(usuarioRepository.findByTokenVerificacion("token-123")).thenReturn(Optional.of(cliente));

        Optional<Usuario> resultado = usuarioService.verificar("token-123");

        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().getVerificado());
        assertNull(resultado.get().getTokenVerificacion());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void verificar_rechazaTokenExpirado() {
        Cliente cliente = new Cliente();
        cliente.setTokenVerificacion("token-viejo");
        cliente.setTokenExpiracion(LocalDateTime.now().minusHours(1));
        when(usuarioRepository.findByTokenVerificacion("token-viejo")).thenReturn(Optional.of(cliente));

        Optional<Usuario> resultado = usuarioService.verificar("token-viejo");

        assertTrue(resultado.isEmpty());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void solicitarRecuperacion_generaTokenConExpiracion() {
        Cliente cliente = new Cliente();
        when(usuarioRepository.findByCorreo(anyString())).thenReturn(Optional.of(cliente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Usuario> resultado = usuarioService.solicitarRecuperacion("ana@correo.com");

        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().getTokenRecuperacion());
        assertNotNull(resultado.get().getTokenRecuperacionExpiracion());
    }

    @Test
    void restablecerPassword_cambiaPasswordConHashYLimpiaToken() {
        Cliente cliente = new Cliente();
        cliente.setTokenRecuperacion("token-reset");
        cliente.setTokenRecuperacionExpiracion(LocalDateTime.now().plusHours(1));
        when(usuarioRepository.findByTokenRecuperacion("token-reset")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$nuevo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        RestablecerPasswordDTO dto = new RestablecerPasswordDTO();
        dto.setToken("token-reset");
        dto.setPassword("nuevaClave123");
        dto.setConfirmarPassword("nuevaClave123");

        Optional<Usuario> resultado = usuarioService.restablecerPassword(dto);

        assertTrue(resultado.isPresent());
        assertEquals("$2a$10$nuevo", resultado.get().getPassword());
        assertNull(resultado.get().getTokenRecuperacion());
        assertNull(resultado.get().getTokenRecuperacionExpiracion());
    }

    @Test
    void restablecerPassword_rechazaTokenExpirado() {
        Cliente cliente = new Cliente();
        cliente.setTokenRecuperacion("token-reset");
        cliente.setTokenRecuperacionExpiracion(LocalDateTime.now().minusHours(1));
        when(usuarioRepository.findByTokenRecuperacion("token-reset")).thenReturn(Optional.of(cliente));

        RestablecerPasswordDTO dto = new RestablecerPasswordDTO();
        dto.setToken("token-reset");
        dto.setPassword("nuevaClave123");
        dto.setConfirmarPassword("nuevaClave123");

        Optional<Usuario> resultado = usuarioService.restablecerPassword(dto);

        assertTrue(resultado.isEmpty());
        verify(usuarioRepository, never()).save(any());
    }
}