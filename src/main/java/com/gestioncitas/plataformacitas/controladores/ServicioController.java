package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.modelos.CategoriaServicio;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import com.gestioncitas.plataformacitas.repositorios.CategoriaServicioRepository;
import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import jakarta.validation.Valid;
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
@RequestMapping("/admin/servicios")
public class ServicioController {

    private final ServicioRepository servicioRepository;
    private final CategoriaServicioRepository categoriaServicioRepository;

    public ServicioController(ServicioRepository servicioRepository,
                              CategoriaServicioRepository categoriaServicioRepository) {
        this.servicioRepository = servicioRepository;
        this.categoriaServicioRepository = categoriaServicioRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicios", servicioRepository.findAllConCategoriaOrderByNombreAsc());
        return "admin-servicios";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        prepararFormulario(model, new Servicio());
        return "admin-servicio-formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("servicio") Servicio servicio,
                        BindingResult resultado,
                        Model model,
                        RedirectAttributes atributos) {
        validarCategoria(servicio, resultado);
        if (resultado.hasErrors()) {
            prepararFormulario(model, servicio);
            return "admin-servicio-formulario";
        }

        servicio.setActivo(true);
        servicioRepository.save(servicio);
        atributos.addFlashAttribute("exito", "Servicio creado correctamente.");
        return "redirect:/admin/servicios";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model,
                                           RedirectAttributes atributos) {
        Servicio servicio = servicioRepository.findById(id).orElse(null);
        if (servicio == null) {
            atributos.addFlashAttribute("error", "El servicio solicitado no existe.");
            return "redirect:/admin/servicios";
        }
        prepararFormulario(model, servicio);
        return "admin-servicio-formulario";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @Valid @ModelAttribute("servicio") Servicio servicio,
                         BindingResult resultado,
                         Model model,
                         RedirectAttributes atributos) {
        Servicio existente = servicioRepository.findById(id).orElse(null);
        if (existente == null) {
            atributos.addFlashAttribute("error", "El servicio solicitado no existe.");
            return "redirect:/admin/servicios";
        }

        validarCategoria(servicio, resultado);
        if (resultado.hasErrors()) {
            servicio.setId(id);
            prepararFormulario(model, servicio);
            return "admin-servicio-formulario";
        }

        existente.setNombre(servicio.getNombre());
        existente.setDescripcion(servicio.getDescripcion());
        existente.setPrecio(servicio.getPrecio());
        existente.setDuracionMinutos(servicio.getDuracionMinutos());
        existente.setCategoria(servicio.getCategoria());
        servicioRepository.save(existente);
        atributos.addFlashAttribute("exito", "Servicio actualizado correctamente.");
        return "redirect:/admin/servicios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes atributos) {
        Servicio servicio = servicioRepository.findById(id).orElse(null);
        if (servicio == null) {
            atributos.addFlashAttribute("error", "El servicio solicitado no existe.");
        } else {
            servicio.setActivo(false);
            servicioRepository.save(servicio);
            atributos.addFlashAttribute("exito", "Servicio retirado del catálogo correctamente.");
        }
        return "redirect:/admin/servicios";
    }

    private void prepararFormulario(Model model, Servicio servicio) {
        if (servicio.getCategoria() == null) {
            servicio.setCategoria(new CategoriaServicio());
        }
        model.addAttribute("servicio", servicio);
        model.addAttribute("categorias", categoriaServicioRepository.findAllByOrderByNombreAsc());
    }

    private void validarCategoria(Servicio servicio, BindingResult resultado) {
        if (servicio.getCategoria() == null || servicio.getCategoria().getId() == null) {
            resultado.rejectValue("categoria", "categoria.requerida", "Debes seleccionar una categoría.");
            return;
        }

        CategoriaServicio categoria = categoriaServicioRepository
                .findById(servicio.getCategoria().getId())
                .orElse(null);
        if (categoria == null) {
            resultado.rejectValue("categoria", "categoria.invalida", "La categoría seleccionada no existe.");
        } else {
            servicio.setCategoria(categoria);
        }
    }
}