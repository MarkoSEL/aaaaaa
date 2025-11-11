package proyectofinal.utp.legal.facade;

import proyectofinal.utp.legal.entities.Documento;
import proyectofinal.utp.legal.entities.Documento.Tipo;
import java.util.*;

public interface DocumentUseCases {
    Documento crear(Tipo tipo, String titulo);
    Optional<Documento> buscarPorId(String id);
    List<Documento> listar();
    Documento aprobar(String id);
    Documento firmar(String id);
    Documento archivar(String id);
    void eliminar(String id);

    // nuevo
    void actualizarMetadatos(String docId, boolean urgente,
                             Documento.Prioridad prioridad, String etiquetas);
}