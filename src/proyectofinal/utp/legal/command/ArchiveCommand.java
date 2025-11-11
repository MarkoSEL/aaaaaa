package proyectofinal.utp.legal.command;

import proyectofinal.utp.legal.facade.DocumentUseCases;

public class ArchiveCommand implements Command {
    private final DocumentUseCases svc;
    private final String id;

    public ArchiveCommand(DocumentUseCases svc, String id) {
        this.svc = svc; this.id = id;
    }
    @Override public void execute() { svc.archivar(id); }
    @Override public String name() { return "ARCHIVAR"; }
}
