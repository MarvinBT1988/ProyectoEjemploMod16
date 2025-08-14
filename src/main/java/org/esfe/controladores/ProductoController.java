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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/producto")
public class ProductoController {

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";
    @Autowired
    private IProductoService productoService;

    @GetMapping
    public String index(Model model, @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size) {
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
    public String save(Producto producto, @RequestParam("fileImagen") MultipartFile fileImagen, BindingResult result,
            Model model, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute(producto);
            attributes.addFlashAttribute("error", "No se pudo guardar debido a un error.");
            return "producto/create";
        }
        if (fileImagen != null && !fileImagen.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String fileName = fileImagen.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(fileImagen.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                producto.setImagen(fileName);
                if (producto.getId() !=null && producto.getId() > 0) {
                    Producto productoData = productoService.buscarPorId(producto.getId()).get();
                    if (productoData != null && productoData.getImagen() != null) {

                        Path filePathDelete = uploadPath.resolve(productoData.getImagen());
                        Files.deleteIfExists(filePathDelete);
                    }
                }

            } catch (Exception e) {
                attributes.addFlashAttribute("error", "Error al procesar la imagen: " + e.getMessage());
                return "redirect:/producto";
            }
        }
        productoService.crearOEditar(producto);
        attributes.addFlashAttribute("msg", "producto creado correctamente");
        return "redirect:/producto";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        Producto producto = productoService.buscarPorId(id).get();
        model.addAttribute("producto", producto);
        return "producto/edit";
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Integer id, Model model) {
        Producto producto = productoService.buscarPorId(id).get();
        model.addAttribute("producto", producto);
        return "producto/details";
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Integer id, Model model) {
        Producto producto = productoService.buscarPorId(id).get();
        model.addAttribute("producto", producto);
        return "producto/delete";
    }

    @PostMapping("/delete")
    public String delete(Producto producto, RedirectAttributes attributes) {
        Producto productoData = productoService.buscarPorId(producto.getId()).get();
        if (productoData != null && productoData.getImagen() != null) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePathDelete = uploadPath.resolve(productoData.getImagen());
                Files.deleteIfExists(filePathDelete);
            } catch (Exception e) {
                attributes.addFlashAttribute("error", "Error al procesar la imagen: " + e.getMessage());
                return "redirect:/producto/create";
            }
        }
        productoService.eliminarPorId(producto.getId());
        attributes.addFlashAttribute("msg", "Producto eliminado correctamente");
        return "redirect:/producto";
    }

    @GetMapping("/imagen/{id}")
    @ResponseBody
    public byte[] mostrarImagen(@PathVariable Integer id) {
        try {
            Producto producto = productoService.buscarPorId(id).get();
            String fileName = producto.getImagen();
            // Construye la ruta completa del archivo
            Path filePath = Paths.get(UPLOAD_DIR, fileName);

            // Verifica si el archivo existe
            if (!Files.exists(filePath)) {
                // Puedes lanzar una excepción o devolver null
                throw new IOException("Archivo no encontrado: " + fileName);
            }

            // Lee todos los bytes del archivo y los devuelve
            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            return new byte[0]; // Devuelve un arreglo vacío si hay un error
        }
    }
}
