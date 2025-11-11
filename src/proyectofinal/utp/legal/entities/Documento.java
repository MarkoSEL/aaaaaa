package proyectofinal.utp.legal.entities;

import java.time.LocalDateTime;
import proyectofinal.utp.legal.state.*;
import java.util.ArrayList;
import java.util.List;
import proyectofinal.utp.legal.composite.DocPart;

public class Documento {

    public enum Tipo { CONTRATO, ACTA, ESCRITURA }
    public enum Prioridad { BAJA, MEDIA, ALTA }

    private final List<DocPart> partes = new ArrayList<>();
    
    public void addParte(DocPart p) {
        partes.add(p);
    }

    public String renderContenido() {
        StringBuilder sb = new StringBuilder();
        for (DocPart p : partes) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(p.render(0));
        }
        return sb.toString();
    }

    private final String id;
    private String titulo;
    private Tipo tipo;
    private final LocalDateTime creado;
    private boolean urgente;
    private Prioridad prioridad;
    private String etiquetas;
    
    // patron State
    private DocumentState state;

    public Documento(String id, String titulo, Tipo tipo, java.time.LocalDateTime creado) {
        this.id = id;
        this.titulo = titulo;
        this.tipo = tipo;
        this.creado = creado;
        this.state = new proyectofinal.utp.legal.state.PendingState(this);
    }

    // acciones de negocio delegadas al estado
    public void aprobar(){ state.aprobar(); }
    public void firmar(){ state.firmar(); }
    public void archivar(){ state.archivar(); }

    // acceso al nombre del estado actual
    public String getEstado(){ return state.name(); }

    // usado por los estados para cambiar de estado
    public void setState(DocumentState s){ this.state = s; }

    // getters/setters basicos
    public String getId(){ return id; }
    public String getTitulo(){ return titulo; }
    public Tipo getTipo(){ return tipo; }
    public LocalDateTime getCreado(){ return creado; }
    public void setTitulo(String t){ this.titulo = t; }
    public void setTipo(Tipo t){ this.tipo = t; }
    public boolean isUrgente() { return urgente; }
    public void setUrgente(boolean v) { this.urgente = v; }

    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad p) { this.prioridad = (p==null? Prioridad.MEDIA : p); }

    public String getEtiquetas() { return etiquetas; }
    public void setEtiquetas(String e) { this.etiquetas = (e==null || e.isBlank()) ? null : e; }

    
    @Override
    public String toString() {
        return "Documento{id=" + id + ", titulo=" + titulo + ", tipo=" + tipo +
               ", estado=" + getEstado() + ", creado=" + creado + "}";
    }
}
