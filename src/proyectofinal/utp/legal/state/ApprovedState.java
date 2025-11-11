package proyectofinal.utp.legal.state;

import proyectofinal.utp.legal.entities.Documento;
import proyectofinal.utp.legal.observer.NotificationCenter;

public class ApprovedState implements DocumentState {
    private final Documento doc;
    public ApprovedState(Documento doc){ this.doc = doc; }

    @Override public String name(){ return "APROBADO"; }

    @Override public void aprobar(){ /* no hace nada */ }

    @Override public void firmar(){
        doc.setState(new SignedState(doc));
        NotificationCenter.getInstance().emit(doc, "FIRMADO");
    }

    @Override public void archivar(){
        doc.setState(new ArchivedState(doc));
        NotificationCenter.getInstance().emit(doc, "ARCHIVADO");
    }
}
