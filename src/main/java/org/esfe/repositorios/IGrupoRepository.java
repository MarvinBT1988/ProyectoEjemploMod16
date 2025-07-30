package org.esfe.repositorios;

import org.esfe.modelos.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IGrupoRepository extends JpaRepository<Grupo, Integer> {
    Page<Grupo> findByNombreContainingAndDescripcionContaining(String nombre,String descripcion,Pageable pageable);   
}
