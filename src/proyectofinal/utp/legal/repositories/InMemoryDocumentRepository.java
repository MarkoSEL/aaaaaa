package proyectofinal.utp.legal.repositories;

import proyectofinal.utp.legal.entities.Documento;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDocumentRepository implements IDocumentRepository {

    private final Map<String, Documento> data = new ConcurrentHashMap<>();

    @Override
    public Documento save(Documento d) {
        data.put(d.getId(), d);
        return d;
    }

    @Override
    public Optional<Documento> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Documento> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void deleteById(String id) {
        data.remove(id);
    }

    @Override
    public void actualizarMetadatos(String docId, boolean urgente,
                                    Documento.Prioridad prioridad, String etiquetas) {
        Documento d = data.get(docId);
        if (d == null) {
            throw new IllegalArgumentException("Documento no encontrado: " + docId);
        }
        d.setUrgente(urgente);
        d.setPrioridad(prioridad);
        d.setEtiquetas(etiquetas);
        // opcional: regrabar
        data.put(docId, d);
    }
}
