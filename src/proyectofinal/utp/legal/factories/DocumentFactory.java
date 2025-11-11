package proyectofinal.utp.legal.factories;

import proyectofinal.utp.legal.entities.Documento;
import proyectofinal.utp.legal.entities.Documento.Tipo;

public final class DocumentFactory {
    private DocumentFactory() {}

    public static Documento crear(Tipo tipo, String titulo) {
        String id = tipo.name() + "-" + java.util.UUID.randomUUID();
        Documento d = new Documento(id, titulo, tipo, java.time.LocalDateTime.now());
        // Decorator: defaults
        d.setUrgente(false);
        d.setPrioridad(Documento.Prioridad.MEDIA);
        d.setEtiquetas(null);
        return d;
    }
}
