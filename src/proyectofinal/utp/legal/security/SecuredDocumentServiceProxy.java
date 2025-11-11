package proyectofinal.utp.legal.security;

import proyectofinal.utp.legal.entities.Documento;
import proyectofinal.utp.legal.entities.Documento.Tipo;
import proyectofinal.utp.legal.facade.DocumentService;
import proyectofinal.utp.legal.facade.DocumentUseCases;

import java.util.List;
import java.util.Optional;

public class SecuredDocumentServiceProxy implements DocumentUseCases {

    private final DocumentUseCases target;
    private final User user;

    public SecuredDocumentServiceProxy(User user) {
        this.user = user;
        this.target = new DocumentService();
    }

    public SecuredDocumentServiceProxy(String username) {
        UserRepository repo = new MySQLUserRepository();
        this.user = repo.findByUsername(username)
                .orElseThrow(() -> new SecurityException("Usuario no encontrado: " + username));
        this.target = new DocumentService();
    }

    @Override
    public Documento crear(Tipo tipo, String titulo) {
        requireAny(Role.ADMIN, Role.LAWYER);
        return target.crear(tipo, titulo);
    }

    @Override
    public Optional<Documento> buscarPorId(String id) {
        requireAny(Role.ADMIN, Role.LAWYER, Role.USER);
        return target.buscarPorId(id);
    }

    @Override
    public List<Documento> listar() {
        requireAny(Role.ADMIN, Role.LAWYER, Role.USER);
        return target.listar();
    }

    @Override
    public Documento aprobar(String id) {
        requireAny(Role.ADMIN, Role.LAWYER);
        return target.aprobar(id);
    }

    @Override
    public Documento firmar(String id) {
        requireAny(Role.ADMIN, Role.LAWYER);
        return target.firmar(id);
    }

    @Override
    public Documento archivar(String id) {
        requireAny(Role.ADMIN); // solo admin
        return target.archivar(id);
    }

    @Override
    public void eliminar(String id) {
        requireAny(Role.ADMIN); // solo admin
        target.eliminar(id);
    }

    // >>> Nuevo método que faltaba implementar <<<
    @Override
    public void actualizarMetadatos(String docId, boolean urgente,
                                    Documento.Prioridad prioridad, String etiquetas) {
        requireAny(Role.ADMIN, Role.LAWYER);
        target.actualizarMetadatos(docId, urgente, prioridad, etiquetas);
    }

    private void requireAny(Role... allowed) {
        for (Role r : allowed) {
            if (user.getRole() == r) return;
        }
        throw new SecurityException("Acceso denegado para rol: " + user.getRole());
    }
}
