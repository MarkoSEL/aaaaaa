package proyectofinal.utp.legal.state;

import proyectofinal.utp.legal.entities.Documento;

public class ArchivedState implements DocumentState {
    public ArchivedState(Documento doc){}

    @Override public String name(){ return "ARCHIVADO"; }

    @Override public void aprobar(){ throw new IllegalStateException("Archivado"); }

    @Override public void firmar(){ throw new IllegalStateException("Archivado"); }

    @Override public void archivar(){ /* no hace nada */ }
}
