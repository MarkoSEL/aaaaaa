package proyectofinal.utp.legal.decorator;

public class PrioridadBadge extends BadgeDecorator {
    public PrioridadBadge(Badge inner) { super(inner); }

    @Override public String render() {
        return inner.render() +
            "<span style='margin-left:6px;padding:2px 8px;border-radius:10px;"
          + "background:#f57c00;color:#fff;font-size:10px;font-weight:600;'>PRIORIDAD</span>";
    }
}
