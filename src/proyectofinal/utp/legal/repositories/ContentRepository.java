package proyectofinal.utp.legal.repositories;

import java.util.List;
import proyectofinal.utp.legal.composite.DocPart;

public interface ContentRepository {
    void saveContent(String documentId, List<DocPart> parts);
    List<DocPart> loadContent(String documentId);
}

