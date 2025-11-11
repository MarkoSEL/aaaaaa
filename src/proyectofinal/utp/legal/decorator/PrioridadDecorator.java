package proyectofinal.utp.legal.decorator;

public class PrioridadDecorator extends DocViewDecorator {
    private final int prioridad;
    public PrioridadDecorator(DocView inner, int prioridad){
        super(inner);
        this.prioridad = prioridad;
    }

    @Override
    public String render() {
        return super.render() + " | prioridad=" + prioridad;
    }
}
