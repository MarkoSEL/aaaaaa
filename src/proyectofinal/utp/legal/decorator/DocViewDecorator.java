package proyectofinal.utp.legal.decorator;

public abstract class DocViewDecorator implements DocView {
    protected final DocView inner;
    protected DocViewDecorator(DocView inner){ this.inner = inner; }

    @Override
    public String render() { return inner.render(); }
}
