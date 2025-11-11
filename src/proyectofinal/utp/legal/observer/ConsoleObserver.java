package proyectofinal.utp.legal.observer;

import proyectofinal.utp.legal.entities.Documento;

public class ConsoleObserver implements DocumentObserver {
    @Override
    public void onEvent(Documento doc, String event) {
        System.out.println("[OBS] " + event + " -> " + doc.getId() + " | " + doc.getTitulo());
    }
}
