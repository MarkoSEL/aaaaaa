package proyectofinal.utp.legal.decorator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/** Singleton simple para guardar flags por documento en memoria. */
public class MetadataStore {
    public static class Meta {
        public boolean urgente;
        public boolean prioridad;
    }

    private static final MetadataStore INSTANCE = new MetadataStore();
    private final Map<String, Meta> data = new ConcurrentHashMap<>();

    private MetadataStore(){}

    public static MetadataStore getInstance(){ return INSTANCE; }

    public Meta get(String docId){
        return data.computeIfAbsent(docId, k -> new Meta());
    }

    public void setUrgente(String docId, boolean v){ get(docId).urgente = v; }
    public void setPrioridad(String docId, boolean v){ get(docId).prioridad = v; }
}
