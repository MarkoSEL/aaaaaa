package proyectofinal.utp.legal.strategy;

import proyectofinal.utp.legal.entities.Documento;
import java.util.Comparator;
import java.util.List;

public class SortByTipoThenTitulo implements SortStrategy {
    @Override
    public void sort(List<Documento> docs) {
        docs.sort(
            Comparator.comparing((Documento d) -> d.getTipo().name())
                      .thenComparing(Documento::getTitulo)
        );
    }
}
