package proyectofinal.utp.legal.strategy;

import proyectofinal.utp.legal.entities.Documento;
import java.util.ArrayList;
import java.util.List;

public class DocumentSorter {
    private SortStrategy strategy;

    public DocumentSorter(SortStrategy strategy) { this.strategy = strategy; }
    public void setStrategy(SortStrategy strategy) { this.strategy = strategy; }

    public List<Documento> sort(List<Documento> input) {
        List<Documento> copy = new ArrayList<>(input);
        strategy.sort(copy);
        return copy;
    }
}
