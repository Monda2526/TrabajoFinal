package analizadorsintactico;

import analizadorlexico.modelo.Token;
import java.util.List;
import java.util.Stack;

public class AnalizadorSintactico1 {

    private final List<Token> tokens;
    private final StringBuilder errores = new StringBuilder();

    private static class SimboloLinea {
        String simbolo;
        int linea;
        SimboloLinea(String simbolo, int linea) {
            this.simbolo = simbolo;
            this.linea = linea;
        }
    }

    public AnalizadorSintactico1(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void analizar() {
        errores.setLength(0);
        Stack<SimboloLinea> pila = new Stack<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token tk = tokens.get(i);
            String tipo = tk.getTipo();
            String valor = tk.getValor();
            int linea = tk.getLinea();

            if (valor.equals("main") && i + 2 < tokens.size()) {
                if (tokens.get(i + 1).getTipo().equals("Paréntesis abierto") &&
                    tokens.get(i + 2).getTipo().equals("Paréntesis cerrado") &&
                   (i + 3 >= tokens.size() || !tokens.get(i + 3).getTipo().equals("Llave de apertura"))) {
                    errores.append("Error sintáctico en línea ").append(linea)
                           .append(": falta la llave de apertura '{' en la función main\n");
                }
            }

            switch (tipo) {
                case "Llave de apertura" -> pila.push(new SimboloLinea("{", linea));

                case "Llave de cierre" -> {
                    if (pila.isEmpty() || !pila.peek().simbolo.equals("{")) {
                        errores.append("Error sintáctico en línea ").append(linea)
                               .append(": falta la llave de apertura correspondiente\n");
                    } else {
                        pila.pop();
                    }
                }

                case "Paréntesis abierto" -> pila.push(new SimboloLinea("(", linea));

                case "Paréntesis cerrado" -> {
                    if (pila.isEmpty() || !pila.peek().simbolo.equals("(")) {
                        errores.append("Error sintáctico en línea ").append(linea)
                               .append(": falta el paréntesis de apertura correspondiente\n");
                    } else {
                        pila.pop();
                    }
                }

                case "Palabra Reservada" -> {
                    if (valor.equals("if") || valor.equals("else")) {
                        boolean tieneBloque = false;
                        for (int j = i + 1; j < tokens.size(); j++) {
                            String tipoSig = tokens.get(j).getTipo();
                            if (tipoSig.equals("Llave de apertura")) {
                                tieneBloque = true;
                                break;
                            }
                            
                            if (!tipoSig.equals("Paréntesis abierto") &&
                                !tipoSig.equals("Paréntesis cerrado") &&
                                !tipoSig.equals("Variables") &&
                                !tipoSig.equals("Número") &&
                                !tipoSig.equals("Operador") &&
                               
                                !tipoSig.equals("Coma")) {
                                break;
                            }
                        }
                        if (!tieneBloque) {
                            errores.append("Error sintáctico en línea ").append(linea)
                                   .append(": falta la llave de apertura o cierre en '").append(valor).append("'\n");
                        }
                    }

                    if (valor.equals("return")) {
                        boolean tienePuntoComa = false;
                        for (int j = i + 1; j < tokens.size(); j++) {
                            String tipoSig = tokens.get(j).getTipo();
                            if (tipoSig.equals("Punto y coma")) {
                                tienePuntoComa = true;
                                break;
                            }
                            if (tipoSig.equals("Llave de cierre")) {
                                break;
                            }
                        }
                        if (!tienePuntoComa) {
                            errores.append("Error sintáctico en línea ").append(linea)
                                   .append(": falta punto y coma ';' después de 'return'\n");
                        }
                    }
                }

                case "Variables", "Número" -> {
                    if (i + 1 < tokens.size()) {
                        String sigTipo = tokens.get(i + 1).getTipo();
                        //String sigValor = tokens.get(i + 1).getValor();

                        if (!sigTipo.equals("Punto y coma") &&
                            !sigTipo.equals("Operador") &&
                            !sigTipo.equals("Llave de cierre") &&
                            !sigTipo.equals("Paréntesis cerrado") &&
                                 !sigTipo.equals("Corchete de apertura") && 
                                !sigTipo.equals("Corchete de cierre") && 
                            !sigTipo.equals("Coma")) {

                            
                            boolean esParteDeFuncion = false;
                            for (int j = i - 1; j >= 0; j--) {
                                String valAnt = tokens.get(j).getValor();
                                if (valAnt.equals("printf") || valAnt.equals("scanf")) {
                                    esParteDeFuncion = true;
                                    break;
                                }
                                if (tokens.get(j).getTipo().equals("Punto y coma") ||
                                    tokens.get(j).getTipo().equals("Llave de apertura") ||
                                    tokens.get(j).getTipo().equals("Llave de cierre")) {
                                    break;
                                }
                            }

                            if (!esParteDeFuncion) {
                                errores.append("Error sintáctico en línea ").append(linea)
                                       .append(":  falta de punto y coma ';'\n");
                            }
                        }
                    }
                }
            }
        }
        int ultimaLinea = tokens.isEmpty() ? 0: tokens.get(tokens.size() - 1).getLinea();
        while (!pila.isEmpty()) {
    SimboloLinea s = pila.pop();
    if (s.simbolo.equals("{")) {
        errores.append("Error sintáctico: falta la llave de cierre '}' que corresponde a la apertura en la línea ")
               .append(s.linea)
               .append(". Se esperaba posiblemente en la línea ")
               .append(ultimaLinea + 1)
               .append(".\n");
    } else if (s.simbolo.equals("(")) {
        errores.append("Error sintáctico: falta el paréntesis de cierre ')' en la línea: ")
               .append(s.linea)
              // .append(". Se esperaba posiblemente en la línea ")
               //.append(ultimaLinea + 1)
               .append(".\n");
            }
        }
    }

    public String getErrores() {
        return errores.toString();
        
    }
}
