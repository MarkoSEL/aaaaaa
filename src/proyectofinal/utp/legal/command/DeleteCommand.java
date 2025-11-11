package proyectofinal.utp.legal.command;

import proyectofinal.utp.legal.facade.DocumentUseCases;

public class DeleteCommand implements Command {
    private final DocumentUseCases svc;
    private final String id;

    public DeleteCommand(DocumentUseCases svc, String id) {
        this.svc = svc; this.id = id;
    }
    @Override public void execute() { svc.eliminar(id); }
    @Override public String name() { return "ELIMINAR"; }
}
