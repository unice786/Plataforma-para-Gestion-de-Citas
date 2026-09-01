package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.modelos.CategoriaServicio;
import com.gestioncitas.plataformacitas.modelos.RolUsuario;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.repositorios.CategoriaServicioRepository;
import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
public class CategoriaController {

    private final CategoriaServicioRepository categoriaServicioRepository;
    private final ServicioRepository servicioRepository;

    public CategoriaController(CategoriaServicioRepository categoriaServicioRepository,
                               ServicioRepository servicioRepository) {
        this.categoriaServicioRepository = categoriaServicioRepository;
        this.servicioRepository = servicioRepository;
    }

    @GetMapping
    public String listar(Model model, HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";
        List<CategoriaServicio> categorias = categoriaServicioRepository.findAllByOrderByNombreAsc();
        Map<Long, Long> conteoServicios = new HashMap<>();
        for (CategoriaServicio categoria : categorias) {
            conteoServicios.put(categoria.getId(),
                    servicioRepository.countByCategoriaId(categoria.getId()));
        }
        model.addAttribute("categorias", categorias);
        model.addAttribute("conteoServicios", conteoServicios);
        return "admin-categorias";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";
        model.addAttribute("categoria", new CategoriaServicio());
        return "admin-categoria-formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("categoria") CategoriaServicio categoria,
                        BindingResult resultado,
                        RedirectAttributes atributos,
                        HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";
        if (resultado.hasErrors()) {
            return "admin-categoria-formulario";
        }
        try {
            categoriaServicioRepository.save(categoria);
        } catch (DataIntegrityViolationException ex) {
            resultado.rejectValue("nombre", "categoria.duplicada",
                    "Ya existe una categoría con ese nombre.");
            return "admin-categoria-formulario";
        }
        atributos.addFlashAttribute("exito", "Categoría creada correctamente.");
        return "redirect:/admin/categorias";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model,
                                           RedirectAttributes atributos,
                                           HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";
        CategoriaServicio categoria = categoriaServicioRepository.findById(id).orElse(null);
        if (categoria == null) {
            atributos.addFlashAttribute("error", "La categoría solicitada no existe.");
            return "redirect:/admin/categorias";
        }
        model.addAttribute("categoria", categoria);
        return "admin-categoria-formulario";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @Valid @ModelAttribute("categoria") CategoriaServicio categoria,
                         BindingResult resultado,
                         RedirectAttributes atributos,
                         HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";
        CategoriaServicio existente = categoriaServicioRepository.findById(id).orElse(null);
        if (existente == null) {
            atributos.addFlashAttribute("error", "La categoría solicitada no existe.");
            return "redirect:/admin/categorias";
        }
        if (resultado.hasErrors()) {
            categoria.setId(id);
            return "admin-categoria-formulario";
        }
        existente.setNombre(categoria.getNombre());
        existente.setDescripcion(categoria.getDescripcion());
        try {
            categoriaServicioRepository.save(existente);
        } catch (DataIntegrityViolationException ex) {
            resultado.rejectValue("nombre", "categoria.duplicada",
                    "Ya existe una categoría con ese nombre.");
            categoria.setId(id);
            return "admin-categoria-formulario";
        }
        atributos.addFlashAttribute("exito", "Categoría actualizada correctamente.");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes atributos, HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";
        CategoriaServicio categoria = categoriaServicioRepository.findById(id).orElse(null);
        if (categoria == null) {
            atributos.addFlashAttribute("error", "La categoría solicitada no existe.");
            return "redirect:/admin/categorias";
        }
        if (servicioRepository.countByCategoriaId(id) > 0) {
            atributos.addFlashAttribute("error",
                    "No se puede eliminar la categoría porque tiene servicios asociados.");
            return "redirect:/admin/categorias";
        }
        categoriaServicioRepository.delete(categoria);
        atributos.addFlashAttribute("exito", "Categoría eliminada correctamente.");
        return "redirect:/admin/categorias";
    }

    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && usuario.getRol() == RolUsuario.ADMINISTRADOR;
    }
}