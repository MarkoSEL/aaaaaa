package proyectofinal.utp.legal.composite;

import java.util.ArrayList;
import java.util.List;

public class Seccion implements DocPart {
    private final String titulo;
    private final List<DocPart> hijos = new ArrayList<>();
    public String getTitulo() { return titulo; }
    public List<DocPart> getHijos() { return hijos; }

    public Seccion(String titulo) {
        this.titulo = titulo;
    }

    public Seccion add(DocPart p) {
        hijos.add(p);
        return this;
    }

    @Override
    public String render(int level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append(titulo);
        for (DocPart h : hijos) {
            sb.append("\n").append(h.render(level + 1));
        }
        return sb.toString();
    }

    private String indent(int n) {
        return "  ".repeat(Math.max(0, n));
    }
}
