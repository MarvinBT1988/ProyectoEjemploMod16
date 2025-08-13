package org.esfe.controladores;

import org.esfe.modelos.Producto;
import org.esfe.servicios.interfaces.IProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/producto")
public class ProductoController {

    @Autowired
    private IProductoService productoService;

     @GetMapping
    public String index(Model model, @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size){
        int currentPage = page.orElse(1) - 1; // si no está seteado se asigna 0
        int pageSize = size.orElse(5); // tamaño de la página, se asigna 5
        Pageable pageable = PageRequest.of(currentPage, pageSize);

        Page<Producto> productos = productoService.buscarTodosPaginados(pageable);
        model.addAttribute("productos", productos);

        int totalPages = productos.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "producto/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("producto", new Producto());
        return "producto/create";
    }
     @PostMapping("/save")
    public String save(Producto producto, @RequestParam("fileImagen") MultipartFile fileImagen, BindingResult result, Model model, RedirectAttributes attributes){
        if(result.hasErrors()){
            model.addAttribute(producto);
            attributes.addFlashAttribute("error", "No se pudo guardar debido a un error.");
            return "producto/create";
        }
         if (fileImagen != null && !fileImagen.isEmpty()) {
        try {
            producto.setImagen(fileImagen.getBytes());
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Error al procesar la imagen: " + e.getMessage());
            return "redirect:/producto/create";
        }
        } else {
            Producto productoExiste = productoService.buscarPorId(producto.getId()).get();
            producto.setImagen(productoExiste.getImagen());
        // En caso de que no se suba una nueva imagen,
        // puedes decidir qué hacer. Por ejemplo, si es una edición,
        // podrías buscar el producto existente y mantener la imagen anterior.
        // Si es un nuevo producto, puedes dejar la imagen como null o asignar una por defecto.
        }
        productoService.crearOEditar(producto);
        attributes.addFlashAttribute("msg", "producto creado correctamente");
        return "redirect:/producto";
    }
   @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model){
        Producto producto = productoService.buscarPorId(id).get();
        model.addAttribute("producto", producto);
        return "producto/edit";
    }

   @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Integer id, Model model){
        Producto producto = productoService.buscarPorId(id).get();
        model.addAttribute("producto", producto);
       return "producto/details";
    }

   @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Integer id, Model model){
         Producto producto = productoService.buscarPorId(id).get();
        model.addAttribute("producto", producto);
        return "producto/delete";
    }

    @PostMapping("/delete")
    public String delete(Producto producto, RedirectAttributes attributes){
        productoService.eliminarPorId(producto.getId());
        attributes.addFlashAttribute("msg", "Producto eliminado correctamente");
        return "redirect:/producto";
    }   

    @GetMapping("/imagen/{id}")
    @ResponseBody
    public byte[] mostrarImagen(@PathVariable Integer id) {
        return productoService.buscarPorId(id)
                .map(Producto::getImagen)
                .orElse(null);
    }
}
