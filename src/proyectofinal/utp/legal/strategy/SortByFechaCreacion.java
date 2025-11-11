package proyectofinal.utp.legal.strategy;

import proyectofinal.utp.legal.entities.Documento;
import java.util.Comparator;
import java.util.List;

public class SortByFechaCreacion implements SortStrategy {
    @Override
    public void sort(List<Documento> docs) {
        docs.sort(Comparator.comparing(Documento::getCreado));
    }
}
