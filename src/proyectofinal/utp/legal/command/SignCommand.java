package proyectofinal.utp.legal.command;

import proyectofinal.utp.legal.facade.DocumentUseCases;

public class SignCommand implements Command {
    private final DocumentUseCases svc;
    private final String id;

    public SignCommand(DocumentUseCases svc, String id) {
        this.svc = svc; this.id = id;
    }
    @Override public void execute() { svc.firmar(id); }
    @Override public String name() { return "FIRMAR"; }
}
