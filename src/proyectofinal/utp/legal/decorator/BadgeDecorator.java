package proyectofinal.utp.legal.decorator;

public abstract class BadgeDecorator implements Badge {
    protected final Badge inner;
    protected BadgeDecorator(Badge inner) { this.inner = inner; }
    @Override public String render() { return inner.render(); }
}