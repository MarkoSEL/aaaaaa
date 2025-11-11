package proyectofinal.utp.legal.observer;

import proyectofinal.utp.legal.entities.Documento;

public interface DocumentObserver {
    void onEvent(Documento doc, String event);
}