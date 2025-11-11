package proyectofinal.utp.legal.state;

import proyectofinal.utp.legal.entities.Documento;
import proyectofinal.utp.legal.observer.NotificationCenter;

public class PendingState implements DocumentState {
    private final Documento doc;
    public PendingState(Documento doc){ this.doc = doc; }

    @Override public String name(){ return "PENDIENTE"; }

    @Override public void aprobar(){
        doc.setState(new ApprovedState(doc));
        NotificationCenter.getInstance().emit(doc, "APROBADO");
    }

    @Override public void firmar(){
        throw new IllegalStateException("No se puede firmar desde PENDIENTE");
    }

    @Override public void archivar(){
        throw new IllegalStateException("No se puede archivar desde PENDIENTE");
    }
}
