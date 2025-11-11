package proyectofinal.utp.legal.observer;

import proyectofinal.utp.legal.entities.Documento;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class NotificationCenter {

    private static final NotificationCenter INSTANCE = new NotificationCenter();
    public static NotificationCenter getInstance() { return INSTANCE; }

    private final List<DocumentObserver> observers = new CopyOnWriteArrayList<>();

    private NotificationCenter() {}

    public void subscribe(DocumentObserver o) {
        if (o != null) observers.add(o);
    }

    public void unsubscribe(DocumentObserver o) {
        observers.remove(o);
    }

    /** Firma que falta y causa el error */
    public void publish(Documento doc, String event) {
        for (DocumentObserver o : observers) {
            try { o.onEvent(doc, event); } catch (Exception ignore) {}
        }
    }

    /** Overloads opcionales por compatibilidad */
    public void publish(String event) { publish(null, event); }
    public void clear() { observers.clear(); }

    public void emit(Documento doc, String archivado) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
