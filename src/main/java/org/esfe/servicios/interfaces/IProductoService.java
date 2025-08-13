package org.esfe.servicios.interfaces;

import org.esfe.modelos.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductoService {
    Page<Producto> buscarTodosPaginados(Pageable pageable);
    List<Producto> obtenerTodos();
    Optional<Producto> buscarPorId(Integer id);
    Producto crearOEditar(Producto producto);
    void eliminarPorId(Integer id);
    boolean existePorId(Integer id);
}
