package proyectofinal.utp.legal.decorator;

import proyectofinal.utp.legal.entities.Documento;

public class BasicDocView implements DocView {
    private final Documento doc;
    public BasicDocView(Documento doc){ this.doc = doc; }

    @Override
    public String render() {
        return doc.getId() + " | " + doc.getTitulo() + " | " + doc.getTipo()
               + " | estado=" + doc.getEstado();
    }
}
