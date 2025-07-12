package analizadorlexico;

import analizadorlexico.modelo.Token;
import java.util.*;

public class AnalizadorLexico1 {
    private final String codigo;
    private final List<String> reservadas = Arrays.asList(
        "int", "float", "char", "if", "else", "main", "printf", "return", "while");

    private final List<Token> tokens = new ArrayList<>();
    private final List<Token> tokensSecuencia = new ArrayList<>();
    private final StringBuilder erroresLexicos = new StringBuilder();
    private final Map<String, String> tablaSimbolos = new HashMap<>();

    public AnalizadorLexico1(String codigo) {
        this.codigo = codigo;
    }

    public List<Token> analizarLexico() {
        tokens.clear();
        tokensSecuencia.clear();
        erroresLexicos.setLength(0);
        tablaSimbolos.clear();

        int lineaActual = 1;
        int i = 0;
        String tipoActual = null;
        boolean esperandoAsignacion = false;
        String variableAsignada = null;

        while (i < codigo.length()) {
            char c = codigo.charAt(i);

            if (c == '\n') {
                lineaActual++;
                i++;
                tipoActual = null;
                esperandoAsignacion = false;
                variableAsignada = null;
                continue;
            }

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

         
            if (c == '"') {
                StringBuilder cadena = new StringBuilder();
                cadena.append(c);
                i++;
                boolean cerrado = false;

                while (i < codigo.length()) {
                    char cc = codigo.charAt(i);

                    if (cc == '\\' && i + 1 < codigo.length()) {
                    
                        char siguiente = codigo.charAt(i + 1);
                        cadena.append(cc).append(siguiente);
                        i += 2;
                        continue;
                    }

                    cadena.append(cc);
                    if (cc == '"') {
                        cerrado = true;
                        i++;
                        break;
                    }

                    if (cc == '\n') lineaActual++;
                    i++;
                }

                if (!cerrado) {
                    erroresLexicos.append("Error léxico en línea ").append(lineaActual)
                        .append(": cadena sin cerrar\n");
                }

                agregarToken(cadena.toString(), "Cadena", lineaActual);

                if (esperandoAsignacion && variableAsignada != null) {
                    validarAsignacion(variableAsignada, "Cadena", lineaActual);
                    esperandoAsignacion = false;
                    variableAsignada = null;
                }
                continue;
            }

       
            if ("{}()=+-/*%<>;,[]".indexOf(c) >= 0) {
                String val = String.valueOf(c);
                String tipo = clasificarTipo(val);
                if (val.equals("=") && variableAsignada != null) {
                    esperandoAsignacion = true;
                }
                agregarToken(val, tipo, lineaActual);
                i++;
                continue;
            }

           
            StringBuilder tokenBuilder = new StringBuilder();
            while (i < codigo.length() &&
                   !Character.isWhitespace(codigo.charAt(i)) &&
                   "{}()=+-/*%<>;,[]\"".indexOf(codigo.charAt(i)) == -1) {
                tokenBuilder.append(codigo.charAt(i));
                i++;
            }

            String tokenStr = tokenBuilder.toString();

           
            if (tokenStr.matches(".*[^a-zA-Z0-9_\\.].*") && !tokenStr.matches("\".*\"")) {
                erroresLexicos.append("Error léxico en línea ").append(lineaActual)
                    .append(": caracter no permitido en '").append(tokenStr).append("'\n");
                continue;
            }

            String tipo = clasificarTipo(tokenStr);

            if (tipo.equals("Desconocido")) {
                erroresLexicos.append("Error léxico en línea ").append(lineaActual)
                    .append(": token desconocido '").append(tokenStr).append("'\n");
            } else {
                agregarToken(tokenStr, tipo, lineaActual);

                
                if (tipo.equals("Palabra Reservada") && Arrays.asList("int", "float", "char").contains(tokenStr)) {
                    tipoActual = tokenStr;
                } else if (tipo.equals("Variables")) {
                    if (tipoActual != null) {
                        tablaSimbolos.put(tokenStr, tipoActual);
                        tipoActual = null;
                    } else {
                        variableAsignada = tokenStr;
                    }
                } else if (tipo.equals("Número")) {
                    if (esperandoAsignacion && variableAsignada != null) {
                    
                        String tipoValor = tokenStr.contains(".") ? "float" : "int";
                        validarAsignacion(variableAsignada, tipoValor, lineaActual);
                        esperandoAsignacion = false;
                        variableAsignada = null;
                    }
                }
            }
        }

        return tokens;
    }

    private void validarAsignacion(String variable, String tipoValor, int linea) {
        String tipoDeclarado = tablaSimbolos.get(variable);
        if (tipoDeclarado == null) return;

        boolean error = false;
        if (tipoDeclarado.equals("int") && tipoValor.equals("float")) error = true;
        if (tipoDeclarado.equals("char") && !tipoValor.equals("Cadena") && !tipoValor.equals("int")) error = true;

        if (error) {
            erroresLexicos.append("Error léxico en línea ").append(linea)
                .append(": tipo incompatible, variable '").append(variable)
                .append("' es '").append(tipoDeclarado).append("' y recibe '").append(tipoValor).append("'\n");
        }
    }

    private String clasificarTipo(String p) {
        if (reservadas.contains(p)) return "Palabra Reservada";
        if (p.matches("[+\\-*/%=<>!]+")) return "Operador";
        if (p.equals("{")) return "Llave de apertura";
        if (p.equals("}")) return "Llave de cierre";
        if (p.equals("(")) return "Paréntesis abierto";
        if (p.equals(")")) return "Paréntesis cerrado";
        if (p.equals(";")) return "Punto y coma";
        if (p.equals(",")) return "Coma";
        if (p.matches("\\d+")) return "Número";
        if (p.matches("\\d+\\.\\d+")) return "Número";
        if (p.matches("\".*\"")) return "Cadena";
        if (p.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            if (reservadas.contains(p)) return "Palabra Reservada";
            else if (p.length() >= 3 && sePareceAReservada(p)) return "Desconocido";
            else return "Variables";
        }

        return "Desconocido";
    }

    private boolean sePareceAReservada(String palabra) {
        for (String r : reservadas) {
            if (palabra.length() >= r.length() - 1 && palabra.length() <= r.length() + 1 &&
                contarCoincidencias(palabra, r) >= r.length() - 2) {
                return true;
            }
        }
        return false;
    }

    private int contarCoincidencias(String a, String b) {
        int min = Math.min(a.length(), b.length());
        int count = 0;
        for (int i = 0; i < min; i++) {
            if (a.charAt(i) == b.charAt(i)) count++;
        }
        return count;
    }

    private void agregarToken(String valor, String tipo, int linea) {
        for (Token t : tokens) {
            if (t.getValor().equals(valor) && t.getTipo().equals(tipo) && t.getLinea() == linea) {
                t.incrementar();
                return;
            }
        }
        Token nuevo = new Token(valor, tipo, linea);
        tokens.add(nuevo);
        tokensSecuencia.add(nuevo);
    }

    public String getErroresLexicos() {
        return erroresLexicos.toString();
    }

    public List<Token> getTokensSecuencia() {
        return tokensSecuencia;
    }

    public String getPseudocodigo() {
        if (!getErroresLexicos().isEmpty()) {
            return "No se puede generar pseudocódigo debido a errores léxicos.\n" + getErroresLexicos();
        }

        StringBuilder pseudo = new StringBuilder("Algoritmo GENERADO\n");
        Stack<String> pilaEstructuras = new Stack<>();

        for (int i = 0; i < tokensSecuencia.size(); i++) {
            Token t = tokensSecuencia.get(i);
            String val = t.getValor();

            if (val.equals("main") || val.equals("{")) {
              
                continue;
            }

            if (val.equals("}")) {
                
                if (!pilaEstructuras.isEmpty() && pilaEstructuras.peek().equals("Si")) {
                    pseudo.append("\tFinSi\n");
                    pilaEstructuras.pop();
                }
                continue;
            }

           
            if (val.matches("[a-zA-Z_][a-zA-Z0-9_]*")
                    && i + 2 < tokensSecuencia.size()
                    && tokensSecuencia.get(i + 1).getValor().equals("=")) {
                String variable = val;
                String valor = tokensSecuencia.get(i + 2).getValor();
                pseudo.append("\t").append(variable).append(" = ").append(valor).append("\n");
                i += 2;
                continue;
            }

            
            if (val.equals("if")) {
                int inicioCond = i + 1; 
                if (inicioCond < tokensSecuencia.size() && tokensSecuencia.get(inicioCond).getValor().equals("(")) {
                    int finCond = -1;
                    int parenCount = 1;
                    for (int j = inicioCond + 1; j < tokensSecuencia.size(); j++) {
                        if (tokensSecuencia.get(j).getValor().equals("(")) parenCount++;
                        else if (tokensSecuencia.get(j).getValor().equals(")")) {
                            parenCount--;
                            if (parenCount == 0) {
                                finCond = j;
                                break;
                            }
                        }
                    }
                    if (finCond != -1) {
                        StringBuilder condicion = new StringBuilder();
                        for (int k = inicioCond + 1; k < finCond; k++) {
                            condicion.append(tokensSecuencia.get(k).getValor()).append(" ");
                        }
                        pseudo.append("\tSi ").append(condicion.toString().trim()).append(" Entonces\n");
                        pilaEstructuras.push("Si");
                        i = finCond; 
                        continue;
                    }
                }
            }

            if (val.equals("else")) {
                pseudo.append("\tSiNo\n");
                continue;
            }

           
            if (val.equals("printf")) {
                StringBuilder mostrar = new StringBuilder("\tMostrar ");

                int iPrint = i + 1;

                
                if (iPrint < tokensSecuencia.size() && tokensSecuencia.get(iPrint).getValor().equals("(")) {
                    iPrint++;

                    
                    if (iPrint < tokensSecuencia.size()) {
                        String cadenaFormato = tokensSecuencia.get(iPrint).getValor();

                        if (cadenaFormato.startsWith("\"") && cadenaFormato.endsWith("\"")) {
                            cadenaFormato = cadenaFormato.substring(1, cadenaFormato.length() - 1);

                            
                            List<String> partesTexto = new ArrayList<>();
                            List<String> formatos = new ArrayList<>();

                            java.util.regex.Pattern p = java.util.regex.Pattern.compile("%[dfcs]");
                            java.util.regex.Matcher m = p.matcher(cadenaFormato);

                            int posAnterior = 0;
                            while (m.find()) {
                                partesTexto.add(cadenaFormato.substring(posAnterior, m.start()));
                                formatos.add(m.group());
                                posAnterior = m.end();
                            }
                            partesTexto.add(cadenaFormato.substring(posAnterior));

                         
                            iPrint++; 

                            List<String> vars = new ArrayList<>();
                            while (iPrint < tokensSecuencia.size() && !tokensSecuencia.get(iPrint).getValor().equals(")")) {
                                String tok = tokensSecuencia.get(iPrint).getValor();
                                if (!tok.equals(",")) {
                                    vars.add(tok);
                                }
                                iPrint++;
                            }

                           
                            int varsIndex = 0;
                            for (int idx = 0; idx < partesTexto.size(); idx++) {
                                String texto = partesTexto.get(idx).replaceAll("\\\\[ntbrf]", " ").trim();
                                if (!texto.isEmpty()) {
                                    mostrar.append("'").append(texto).append("' ");
                                }

                                if (idx < formatos.size() && varsIndex < vars.size()) {
                                    mostrar.append(vars.get(varsIndex)).append(" ");
                                    varsIndex++;
                                }
                            }
                            mostrar.append("\n");
                            pseudo.append(mostrar.toString());
                            i = iPrint; 
                            continue;
                        }
                    }
                }
            }

            
            if (val.equals("return") && i + 1 < tokensSecuencia.size()) {
                i++;
                continue;
            }

            
            if (reservadas.contains(val)) {
                if (i + 2 < tokensSecuencia.size() &&
                    tokensSecuencia.get(i + 1).getTipo().equals("Variables") &&
                    tokensSecuencia.get(i + 2).getValor().equals(";")) {
                    i += 2;
                    continue;
                }
            }
        }

        pseudo.append("FinAlgoritmo\n");
        return pseudo.toString();
    }
}
