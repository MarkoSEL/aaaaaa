package proyectofinal.utp.legal.strategy;

import java.util.List;
import proyectofinal.utp.legal.entities.Documento;

public interface SortStrategy {
    void sort(List<Documento> docs);
}
