package proyectofinal.utp.legal.command;

import proyectofinal.utp.legal.facade.DocumentUseCases;

public class ApproveCommand implements Command {
    private final DocumentUseCases svc;
    private final String id;

    public ApproveCommand(DocumentUseCases svc, String id) {
        this.svc = svc; this.id = id;
    }
    @Override public void execute() { svc.aprobar(id); }
    @Override public String name() { return "APROBAR"; }
}
