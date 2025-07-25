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
                /*esperandoAsignacion = false;
                variableAsignada = null;*/
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
            if (c == '\'') {
            StringBuilder caracter = new StringBuilder();
            caracter.append(c); 
            i++;

            if (i < codigo.length()) {
            char contenido = codigo.charAt(i);
            caracter.append(contenido); 
            i++;

            if (i < codigo.length() && codigo.charAt(i) == '\'') {
            caracter.append('\''); 
            i++;
            agregarToken(caracter.toString(), "Caracter", lineaActual);

            if (esperandoAsignacion && variableAsignada != null) {
                validarAsignacion(variableAsignada, "char", lineaActual);
                esperandoAsignacion = false;
                variableAsignada = null;
                                                                  }
            } else {
            erroresLexicos.append("Error léxico en línea ").append(lineaActual)
                .append(": carácter sin cerrar correctamente\n");
            }
            } else {
             erroresLexicos.append("Error léxico en línea ").append(lineaActual)
            .append(": carácter sin cerrar\n");
             }
             continue;
             }

            if ("{}()=+-/*%<>;,[]".indexOf(c) >= 0) {
    String val = String.valueOf(c);
    String tipo = clasificarTipo(val);



    agregarToken(val, tipo, lineaActual);
    i++;
    continue;
}        
           
            StringBuilder tokenBuilder = new StringBuilder();
            String separadores = "{}()=+-/*%<>;,[]\"'";  // incluye comillas simples y todos los separadores
            while (i < codigo.length() &&
            !Character.isWhitespace(codigo.charAt(i)) &&
            separadores.indexOf(codigo.charAt(i)) == -1) {
            tokenBuilder.append(codigo.charAt(i));
            i++;
            }

            String tokenStr = tokenBuilder.toString();


            String tipo = clasificarTipo(tokenStr);

            if (tipo.equals("Desconocido") || tipo.equals("CasiReservada")) {
    erroresLexicos.append("Error léxico en línea ").append(lineaActual)
        .append(": posible palabra reservada mal escrita '").append(tokenStr).append("'\n");
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
                } 
            }
        }
for (int j = 0; j < tokensSecuencia.size() - 2; j++) {
    Token t1 = tokensSecuencia.get(j);
    Token t2 = tokensSecuencia.get(j + 1);
    Token t3 = tokensSecuencia.get(j + 2);

    if (t1.getTipo().equals("Variables") &&
        t2.getValor().equals("=") &&
        (t3.getTipo().equals("Número") || t3.getTipo().equals("Número decimal"))) {

        String tipoValor = t3.getTipo().equals("Número decimal") ? "float" : "int";
        validarAsignacion(t1.getValor(), tipoValor, t3.getLinea());
    }

 
    if (t1.getTipo().equals("Variables") &&
        t2.getValor().equals("=") &&
        (t3.getTipo().equals("Caracter") || t3.getTipo().equals("Cadena"))) {

        String tipoValor = t3.getTipo();
        validarAsignacion(t1.getValor(), tipoValor, t3.getLinea());
    }
}

        return tokens;
    }

  
    private void validarAsignacion(String variable, String tipoValor, int linea) {
    String tipoDeclarado = tablaSimbolos.get(variable);
    if (tipoDeclarado == null) return;

    boolean error = false;

    if (tipoDeclarado.equals("int")) {
        if (tipoValor.equals("float")) error = true;
    } else if (tipoDeclarado.equals("float")) {
        if (tipoValor.equals("int")) error = true; 
    } else if (tipoDeclarado.equals("char")) {
        if (!tipoValor.equals("Caracter") && !tipoValor.equals("int")) error = true;
    }

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
        if (p.equals("[")) return "Corchete de apertura";
        if (p.equals("]")) return "Corchete de cierre";
        if (p.equals("(")) return "Paréntesis abierto";
        if (p.equals(")")) return "Paréntesis cerrado";
        if (p.equals(";")) return "Punto y coma";
        if (p.equals(",")) return "Coma";
        if (p.matches("\\d+")) return "Número";
        if (p.matches("\\d+\\.\\d+")) return "Número decimal";
        if (p.matches("\".*\"")) return "Cadena";
        if (p.matches("'[^']'")) return "Caracter";
       if (p.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
    if (reservadas.contains(p)) return "Palabra Reservada";
        else if (sePareceAReservada(p)) return "CasiReservada";
        return "Variables";
    }
        return "Desconocido";
        
    }
    private boolean sePareceAReservada(String palabra) {
    for (String r : reservadas) {
        int diferencia = distanciaLevenshtein(palabra, r);
        if (diferencia == 1) {
            return true;
        }
    }
    return false;
}

private int distanciaLevenshtein(String a, String b) {
    int[][] dp = new int[a.length() + 1][b.length() + 1];

    for (int i = 0; i <= a.length(); i++) {
        for (int j = 0; j <= b.length(); j++) {
            if (i == 0) dp[i][j] = j;
            else if (j == 0) dp[i][j] = i;
            else if (a.charAt(i - 1) == b.charAt(j - 1))
                dp[i][j] = dp[i - 1][j - 1];
            else
                dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                                 Math.min(dp[i - 1][j], dp[i][j - 1]));
        }
    }
    return dp[a.length()][b.length()];
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


    for (Map.Entry<String, String> entry : tablaSimbolos.entrySet()) {
        String var = entry.getKey();
        String tipo = entry.getValue();
        String tipoPseudo = switch (tipo) {
            case "int" -> "Entero";
            case "float" -> "Real";
            case "char" -> "Caracter";
            default -> "Cadena";
        };
        pseudo.append("\tDefinir ").append(var).append(" Como ").append(tipoPseudo).append("\n");
    }

    pseudo.append("\n");
    Stack<String> pilaEstructuras = new Stack<>();

    for (int i = 0; i < tokensSecuencia.size(); i++) {
        Token t = tokensSecuencia.get(i);
        String val = t.getValor();

        if (val.equals("main") || val.equals("{")) continue;
        if (val.equals("}")) {
    if (!pilaEstructuras.isEmpty()) {
        String top = pilaEstructuras.peek();

        boolean hayElse = false;
        if (i + 1 < tokensSecuencia.size()) {
            String siguiente = tokensSecuencia.get(i + 1).getValor();
            if (siguiente.equals("else")) {
                hayElse = true;
            }
        }

        if (top.equals("Si") && !hayElse) {
            pseudo.append("\tFinSi\n");
            pilaEstructuras.pop();
        } else if (top.equals("SiElse")) {
            pseudo.append("\tFinSi\n");
            pilaEstructuras.pop();
        }
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
                int finCond = -1, parenCount = 1;
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
    if (!pilaEstructuras.isEmpty() && pilaEstructuras.peek().equals("Si")) {
        pilaEstructuras.pop();
    }
    pseudo.append("\tSiNo\n");
    pilaEstructuras.push("SiElse"); 
    continue;
}
        if (val.equals("printf")) {
            StringBuilder escribir = new StringBuilder("\tEscribir ");
            int iPrint = i + 1;
            if (iPrint < tokensSecuencia.size() && tokensSecuencia.get(iPrint).getValor().equals("(")) {
                iPrint++;
                if (iPrint < tokensSecuencia.size()) {
                    String formato = tokensSecuencia.get(iPrint).getValor();
                    if (formato.startsWith("\"") && formato.endsWith("\"")) {
                        formato = formato.substring(1, formato.length() - 1);

                        List<String> partes = new ArrayList<>();
                        List<String> formatos = new ArrayList<>();
                        java.util.regex.Pattern pat = java.util.regex.Pattern.compile("%[dfcs]");
                        java.util.regex.Matcher mat = pat.matcher(formato);

                        int posAnterior = 0;
                        while (mat.find()) {
                            partes.add(formato.substring(posAnterior, mat.start()));
                            formatos.add(mat.group());
                            posAnterior = mat.end();
                        }
                        partes.add(formato.substring(posAnterior));

                        iPrint++;
                        List<String> variables = new ArrayList<>();
                        while (iPrint < tokensSecuencia.size() &&
                               !tokensSecuencia.get(iPrint).getValor().equals(")")) {
                            String tok = tokensSecuencia.get(iPrint).getValor();
                            if (!tok.equals(",")) variables.add(tok);
                            iPrint++;
                        }

                        int varIdx = 0;
                        for (int idx = 0; idx < partes.size(); idx++) {
                            String texto = partes.get(idx).replaceAll("\\\\[ntbrf]", " ").trim();
                            if (!texto.isEmpty()) {
                                escribir.append("\"").append(texto).append("\"").append(", ");
                            }
                            if (idx < formatos.size() && varIdx < variables.size()) {
                                escribir.append(variables.get(varIdx)).append(", ");
                                varIdx++;
                            }
                        }

                        if (escribir.toString().endsWith(", ")) {
                            escribir.setLength(escribir.length() - 2);
                        }
                        escribir.append("\n");
                        pseudo.append(escribir);
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
    }
    pseudo.append("FinAlgoritmo\n");
    return pseudo.toString();
     
    }
}
