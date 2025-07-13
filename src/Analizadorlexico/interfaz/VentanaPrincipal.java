package analizadorlexico.interfaz;

import analizadorlexico.modelo.Token;
import analizadorlexico.AnalizadorLexico1;
import analizadorsintactico.AnalizadorSintactico1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

    public class VentanaPrincipal extends JFrame {
    private JTextArea areaCodigo;
    private JTextArea areaNumeracionLineas; 
    private JTextPane areaTokens;
    private JTable tablaSimbolos;
    private JLabel lblReservadas, lblOperadores, lblVariables, lblAnalisisSintactico, lblAnalisisLexico;
    private JTextArea consolaErroresS;
    private JTextArea consolaErroresL;
    private AnalizadorLexico1 analizadorLexico;
    private AnalizadorSintactico1 analizadorSintactico;

    public VentanaPrincipal() {
        setTitle("ANÁLISIS LÉXICO Y SINTÁCTICO");
        setSize(1200, 1000);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(197, 206, 214));

        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/analizadorlexico/interfaz/Imagen1.png"));
        Image imagenEscalada = originalIcon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
        JLabel etiquetaImagen = new JLabel(new ImageIcon(imagenEscalada));
        etiquetaImagen.setBounds(1, 1, 45, 45);
        add(etiquetaImagen);

        ImageIcon icono2 = new ImageIcon(getClass().getResource("/analizadorlexico/interfaz/Imagen2.png"));
        Image imagen2 = icono2.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
        JLabel etiquetaImagen2 = new JLabel(new ImageIcon(imagen2));
        etiquetaImagen2.setBounds(1100, 1, 45, 45);
        add(etiquetaImagen2);

        JLabel lblCodigo = new JLabel("Código:");
        lblCodigo.setBounds(130, 20, 150, 20);
        add(lblCodigo);

       
        areaCodigo = new JTextArea();
        areaNumeracionLineas = crearNumeracionLineas(areaCodigo);

        JScrollPane scroll1 = new JScrollPane(areaCodigo);
        scroll1.setBounds(20, 48, 300, 295);
        scroll1.setRowHeaderView(areaNumeracionLineas);  
        add(scroll1);

        JButton btnAnalizarL = new JButton("ANALIZADOR LÉXICO");
        btnAnalizarL.setBounds(350, 540, 180, 30);
        btnAnalizarL.addActionListener(e -> analizarLexico());
        add(btnAnalizarL);

        JButton btnAnalizarS = new JButton("ANALIZADOR SINTÁCTICO");
        btnAnalizarS.setBounds(700, 540, 190, 30);
        btnAnalizarS.addActionListener(e -> analizarSintactico());
        add(btnAnalizarS);

        JButton btnLimpiar = new JButton("LIMPIAR");
        btnLimpiar.setBounds(350, 100, 100, 30);
        btnLimpiar.addActionListener(e -> limpiar());
        add(btnLimpiar);

        JButton btnPC = new JButton("PSEUDOCÓDIGO");
        btnPC.setBounds(325, 250, 140, 30);
        btnPC.addActionListener(e -> {
    if (analizadorLexico != null && analizadorSintactico != null &&
        consolaErroresL.getText().trim().isEmpty() &&
        consolaErroresS.getText().trim().isEmpty()) {

        String pseudocodigo = analizadorLexico.getPseudocodigo();  // Generar pseudocódigo
        areaTokens.setText(pseudocodigo);  // Mostrarlo en el JTextPane

        JOptionPane.showMessageDialog(this,
                "Pseudocódigo generado correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
    } else {
        areaTokens.setText("No se puede generar pseudocódigo debido a errores.\n\n" +
            consolaErroresL.getText() + consolaErroresS.getText());

        JOptionPane.showMessageDialog(this,
                "No se puede generar pseudocódigo. ¡Corrige los errores léxicos y sintácticos primero!",
                "Error", JOptionPane.ERROR_MESSAGE);
    }
});

        add(btnPC);

        JButton btnSalir = new JButton("SALIR");
        btnSalir.setBounds(1000, 750, 100, 30);
        btnSalir.addActionListener(e -> System.exit(0));
        add(btnSalir);

        JLabel lblPseudo = new JLabel("Pseudocódigo generado:");
        lblPseudo.setBounds(470, 20, 200, 20);
        add(lblPseudo);

        areaTokens = new JTextPane();
        JScrollPane scroll2 = new JScrollPane(areaTokens);
        scroll2.setBounds(470, 40, 300, 300);
        add(scroll2);

        tablaSimbolos = new JTable(new DefaultTableModel(
                new Object[]{"Token", "Cantidad", "Tipo"}, 0));
        JScrollPane scrollTabla = new JScrollPane(tablaSimbolos);
        scrollTabla.setBounds(800, 40, 300, 300);
        add(scrollTabla);

        lblReservadas = new JLabel("Palabras Reservadas: 0");
        lblReservadas.setBounds(20, 350, 150, 20);
        add(lblReservadas);

        lblOperadores = new JLabel("Operadores: 0");
        lblOperadores.setBounds(220, 350, 150, 20);
        add(lblOperadores);

        lblVariables = new JLabel("Variables: 0");
        lblVariables.setBounds(370, 350, 150, 20);
        add(lblVariables);

        consolaErroresS = new JTextArea();
        consolaErroresS.setEditable(false);
        JScrollPane scrollErroresS = new JScrollPane(consolaErroresS);
        scrollErroresS.setBounds(150, 380, 950, 150);
        add(scrollErroresS);

        consolaErroresL = new JTextArea();
        consolaErroresL.setEditable(false);
        JScrollPane scrollErroresL = new JScrollPane(consolaErroresL);
        scrollErroresL.setBounds(150, 580, 950, 150);
        add(scrollErroresL);

        lblAnalisisSintactico = new JLabel("Análisis Sintáctico:");
        lblAnalisisSintactico.setBounds(30, 430, 250, 20);
        add(lblAnalisisSintactico);

        lblAnalisisLexico = new JLabel("Análisis Léxico:");
        lblAnalisisLexico.setBounds(30, 630, 280, 20);
        add(lblAnalisisLexico);
    }

    private JTextArea crearNumeracionLineas(JTextArea areaCodigo) {
        JTextArea lineNumbers = new JTextArea("1");
        lineNumbers.setBackground(Color.LIGHT_GRAY);
        lineNumbers.setEditable(false);
        lineNumbers.setFont(areaCodigo.getFont());
        lineNumbers.setFocusable(false);
        lineNumbers.setBorder(null);

        areaCodigo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void actualizarLineas() {
                int totalLineas = areaCodigo.getLineCount();
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= totalLineas; i++) {
                    sb.append(i).append("\n");
                }
                lineNumbers.setText(sb.toString());
            }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarLineas(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarLineas(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarLineas(); }
        });

        return lineNumbers;
    }
private void analizarLexico() {
        String codigoOriginal = areaCodigo.getText();
        analizadorLexico = new AnalizadorLexico1(codigoOriginal);
        List<Token> tokens = analizadorLexico.analizarLexico();

        DefaultTableModel modelo = (DefaultTableModel) tablaSimbolos.getModel();
        modelo.setRowCount(0);
        for (Token token : tokens) {
            modelo.addRow(new Object[]{token.getValor(), token.getCantidad(), token.getTipo()});
        }

        lblReservadas.setText("Palabras Reservadas: " + contar(tokens, "Palabra Reservada"));
        lblOperadores.setText("Operadores: " + contar(tokens, "Operador"));
        lblVariables.setText("Variables: " + contar(tokens, "Variables"));

        consolaErroresL.setText(analizadorLexico.getErroresLexicos());
        consolaErroresS.setText("");
        areaTokens.setText("");
    }

    private void analizarSintactico() {
        if (analizadorLexico == null) {
            JOptionPane.showMessageDialog(this, "Primero ejecuta el análisis léxico.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        analizadorSintactico = new AnalizadorSintactico1(analizadorLexico.getTokensSecuencia());
        analizadorSintactico.analizar();
        consolaErroresS.setText(analizadorSintactico.getErrores());
        areaTokens.setText("");
    }

    private int contar(List<Token> tokens, String tipo) {
        return tokens.stream()
                .filter(t -> t.getTipo().equals(tipo))
                .mapToInt(Token::getCantidad)
                .sum();
    }

    private void limpiar() {
        areaCodigo.setText("");
        areaTokens.setText("");
        ((DefaultTableModel) tablaSimbolos.getModel()).setRowCount(0);
        lblReservadas.setText("Palabras Reservadas: 0");
        lblOperadores.setText("Operadores: 0");
        lblVariables.setText("Variables: 0");
        consolaErroresS.setText("");
        consolaErroresL.setText("");
        analizadorLexico = null;
        analizadorSintactico = null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
//

