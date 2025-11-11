package proyectofinal.utp.legal.app;

import proyectofinal.utp.legal.repositories.IDocumentRepository;
import proyectofinal.utp.legal.repositories.MySQLDocumentRepository;
import proyectofinal.utp.legal.repositories.ContentRepository;
import proyectofinal.utp.legal.repositories.MySQLContentRepository;

public final class AppContext {

    private static final AppContext INSTANCE = new AppContext();

    private final IDocumentRepository documentRepository = new MySQLDocumentRepository();

    private AppContext() {}

    public static AppContext getInstance() { return INSTANCE; }

    public IDocumentRepository documents() { return documentRepository; }

    private final ContentRepository contentRepository = new MySQLContentRepository();

    public ContentRepository content() { return contentRepository; }
}