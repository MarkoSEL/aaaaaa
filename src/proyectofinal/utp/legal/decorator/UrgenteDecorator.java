package proyectofinal.utp.legal.decorator;

public class UrgenteDecorator extends DocViewDecorator {
    public UrgenteDecorator(DocView inner){ super(inner); }

    @Override
    public String render() {
        return "[URGENTE] " + super.render();
    }
}
