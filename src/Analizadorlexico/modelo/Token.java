package analizadorlexico.modelo;

public class Token {
    private final String valor;
    private final String tipo;
    private final int linea;
    private int cantidad;

    public Token(String valor, String tipo, int linea) {
        this.valor = valor;
        this.tipo = tipo;
        this.linea = linea;
        this.cantidad = 1;
    }

    public String getValor() { return valor; }
    public String getTipo() { return tipo; }
    public int getLinea() { return linea; }
    public int getCantidad() { return cantidad; }
    public void incrementar() { cantidad++; }
}
