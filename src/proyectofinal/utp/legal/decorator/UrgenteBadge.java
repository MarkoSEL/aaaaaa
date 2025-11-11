package proyectofinal.utp.legal.decorator;

public class UrgenteBadge extends BadgeDecorator {
    public UrgenteBadge(Badge inner) { super(inner); }

    @Override public String render() {
        return inner.render() +
            "<span style='margin-left:6px;padding:2px 8px;border-radius:10px;"
          + "background:#d32f2f;color:#fff;font-size:10px;font-weight:600;'>URGENTE</span>";
    }
}
