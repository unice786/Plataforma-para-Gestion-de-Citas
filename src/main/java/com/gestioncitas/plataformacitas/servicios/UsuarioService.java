package com.gestioncitas.plataformacitas.servicios;

import com.gestioncitas.plataformacitas.dto.ClienteRegistroDTO;
import com.gestioncitas.plataformacitas.dto.RestablecerPasswordDTO;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Usuario;

import java.util.Optional;

public interface UsuarioService {

    Optional<Usuario> autenticar(String correo, String password);

    Optional<Cliente> registrar(ClienteRegistroDTO dto);

    Optional<Usuario> verificar(String token);

    Optional<Usuario> solicitarRecuperacion(String correo);

    boolean tokenRecuperacionExiste(String token);

    boolean tokenRecuperacionValido(String token);

    Optional<Usuario> restablecerPassword(RestablecerPasswordDTO dto);
}
