package proyectofinal.utp.legal.state;

import proyectofinal.utp.legal.entities.Documento;
import proyectofinal.utp.legal.observer.NotificationCenter;

public class SignedState implements DocumentState {
    private final Documento doc;
    public SignedState(Documento doc){ this.doc = doc; }

    @Override public String name(){ return "FIRMADO"; }

    @Override public void aprobar(){ /* no aplica */ }

    @Override public void firmar(){ /* no hace nada */ }

    @Override public void archivar(){
        doc.setState(new ArchivedState(doc));
        NotificationCenter.getInstance().emit(doc, "ARCHIVADO");
    }
}
