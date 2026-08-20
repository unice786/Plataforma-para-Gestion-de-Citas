package com.gestioncitas.plataformacitas.servicios;

import com.gestioncitas.plataformacitas.dto.ClienteRegistroDTO;
import com.gestioncitas.plataformacitas.dto.RestablecerPasswordDTO;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import com.gestioncitas.plataformacitas.repositorios.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private static final long TOKEN_HORAS_VALIDEZ = 24;
    private static final long RECUPERACION_MINUTOS_VALIDEZ = 30;

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Usuario> autenticar(String correo, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo.trim().toLowerCase());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }

    public Optional<Cliente> registrar(ClienteRegistroDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmarPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        if (usuarioRepository.existsByCorreo(dto.getCorreo().trim().toLowerCase())) {
            return Optional.empty();
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setCorreo(dto.getCorreo().trim().toLowerCase());
        cliente.setTelefono(dto.getTelefono());
        cliente.setPassword(passwordEncoder.encode(dto.getPassword()));
        cliente.setActivo(true);
        cliente.setVerificado(false);
        cliente.setTokenVerificacion(UUID.randomUUID().toString());
        cliente.setTokenExpiracion(LocalDateTime.now().plusHours(TOKEN_HORAS_VALIDEZ));

        return Optional.of(clienteRepository.save(cliente));
    }

    public Optional<Usuario> verificar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTokenVerificacion(token);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        Usuario usuario = usuarioOpt.get();
        if (usuario.getTokenExpiracion() == null || usuario.getTokenExpiracion().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        usuario.setVerificado(true);
        usuario.setTokenVerificacion(null);
        usuario.setTokenExpiracion(null);
        usuarioRepository.save(usuario);
        return Optional.of(usuario);
    }

    public Optional<Usuario> solicitarRecuperacion(String correo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo.trim().toLowerCase());

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setTokenRecuperacion(UUID.randomUUID().toString());
        usuario.setTokenRecuperacionExpiracion(LocalDateTime.now().plusMinutes(RECUPERACION_MINUTOS_VALIDEZ));
        return Optional.of(usuarioRepository.save(usuario));
    }

    public boolean tokenRecuperacionExiste(String token) {
        return token != null && !token.isBlank() && usuarioRepository.findByTokenRecuperacion(token).isPresent();
    }

    public boolean tokenRecuperacionValido(String token) {
        return token != null && !token.isBlank()
                && usuarioRepository.findByTokenRecuperacion(token)
                .map(usuario -> usuario.getTokenRecuperacionExpiracion() != null
                        && usuario.getTokenRecuperacionExpiracion().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    public Optional<Usuario> restablecerPassword(RestablecerPasswordDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmarPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        if (dto.getToken() == null || dto.getToken().isBlank()) {
            return Optional.empty();
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByTokenRecuperacion(dto.getToken());

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        Usuario usuario = usuarioOpt.get();
        if (usuario.getTokenRecuperacionExpiracion() == null || usuario.getTokenRecuperacionExpiracion().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setTokenRecuperacion(null);
        usuario.setTokenRecuperacionExpiracion(null);
        return Optional.of(usuarioRepository.save(usuario));
    }
}