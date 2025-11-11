package proyectofinal.utp.legal.repositories;

import proyectofinal.utp.legal.entities.Documento;
import java.util.*;

public interface IDocumentRepository {
    Documento save(Documento d);
    Optional<Documento> findById(String id);
    List<Documento> findAll();
    void deleteById(String id);

    // nuevo para metadatos
    void actualizarMetadatos(String docId, boolean urgente,
                             Documento.Prioridad prioridad, String etiquetas);
}
