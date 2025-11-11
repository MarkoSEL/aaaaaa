package proyectofinal.utp.legal.composite;

public class Clausula implements DocPart {
    private final String texto;
    public String getTexto() { return texto; }

    public Clausula(String texto) {
        this.texto = texto;
    }

    @Override
    public String render(int level) {
        return indent(level) + "- " + texto;
    }

    private String indent(int n) {
        return "  ".repeat(Math.max(0, n));
    }
}
