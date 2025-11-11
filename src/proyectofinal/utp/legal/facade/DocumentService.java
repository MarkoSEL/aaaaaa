package proyectofinal.utp.legal.facade;

import proyectofinal.utp.legal.app.AppContext;
import proyectofinal.utp.legal.entities.Documento;
import proyectofinal.utp.legal.entities.Documento.Tipo;
import proyectofinal.utp.legal.factories.DocumentFactory;
import proyectofinal.utp.legal.repositories.IDocumentRepository;
import proyectofinal.utp.legal.observer.NotificationCenter;
import proyectofinal.utp.legal.security.Role;
import proyectofinal.utp.legal.security.SessionManager;

import java.util.List;
import java.util.Optional;

public class DocumentService implements DocumentUseCases {

    private final IDocumentRepository repo;

    public DocumentService() {
        this.repo = AppContext.getInstance().documents();
    }

    @Override
    public void actualizarMetadatos(String docId, boolean urgente,
                                    Documento.Prioridad prioridad, String etiquetas) {
        Role r = SessionManager.get().currentUser().getRole();
        if (r != Role.ADMIN && r != Role.LAWYER) {
            throw new SecurityException("No autorizado");
        }
        repo.actualizarMetadatos(docId, urgente, prioridad, etiquetas);
        Documento d = repo.findById(docId).orElseThrow(() -> new RuntimeException("No existe"));
        NotificationCenter.getInstance().publish(d, "META_ACTUALIZADA");
    }

    @Override
    public Documento crear(Tipo tipo, String titulo) {
        Documento d = DocumentFactory.crear(tipo, titulo);
        return repo.save(d);
    }

    @Override
    public Optional<Documento> buscarPorId(String id) { return repo.findById(id); }

    @Override
    public List<Documento> listar() { return repo.findAll(); }

    @Override
    public Documento aprobar(String id) {
        Documento d = getOrThrow(id);
        d.aprobar();
        return repo.save(d);
    }

    @Override
    public Documento firmar(String id) {
        Documento d = getOrThrow(id);
        d.firmar();
        return repo.save(d);
    }

    @Override
    public Documento archivar(String id) {
        Documento d = getOrThrow(id);
        d.archivar();
        return repo.save(d);
    }

    @Override
    public void eliminar(String id) { repo.deleteById(id); }

    private Documento getOrThrow(String id) {
        return repo.findById(id).orElseThrow(
            () -> new IllegalArgumentException("Documento no encontrado: " + id)
        );
    }
}