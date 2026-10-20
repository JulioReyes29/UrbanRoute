package urbanroute.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import urbanroute.control.AnalizadorRedUrbana;
import urbanroute.control.GestorArchivoCSV;
import urbanroute.modelo.GrafoTransporte;

/**
 * Ventana principal del programa.
 * Colabora con: GestorArchivoCSV, AnalizadorRedUrbana y GrafoTransporte.
 */
public class InterfazUsuarioUI extends JFrame {

    // colores de la ventana
    private static final Color COLOR_FONDO = new Color(236, 239, 242);
    private static final Color COLOR_TITULO = new Color(40, 55, 71);

    private GrafoTransporte grafo;
    private GestorArchivoCSV gestorArchivo;
    private AnalizadorRedUrbana analizador;

    private JButton btnCargar;
    private JButton btnGuardar;
    private JButton btnAgregarEstacion;
    private JButton btnAgregarConexion;
    private JButton btnEliminarEstacion;
    private JButton btnEliminarConexion;
    private JButton btnRutaCorta;
    private JButton btnSubredes;
    private JButton btnHubs;

    private JComboBox<String> comboOrigen;
    private JComboBox<String> comboDestino;

    private JPanel panelGrafo;
    private JTextArea areaResultados;

    /**
     * Crea la ventana y todos sus componentes.
     */
    public InterfazUsuarioUI() {
        grafo = new GrafoTransporte();
        gestorArchivo = new GestorArchivoCSV();
        analizador = new AnalizadorRedUrbana(grafo);

        setTitle("UrbanRoute - Metro de Caracas");
        setSize(1150, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contenido = new JPanel(new BorderLayout(10, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contenido.setBackground(COLOR_FONDO);

        contenido.add(crearTitulo(), BorderLayout.NORTH);
        contenido.add(crearPanelIzquierdo(), BorderLayout.WEST);
        contenido.add(crearPanelGrafo(), BorderLayout.CENTER);
        contenido.add(crearPanelDerecho(), BorderLayout.EAST);

        setContentPane(contenido);
    }

    private JLabel crearTitulo() {
        JLabel titulo = new JLabel("UrbanRoute - Red del Metro de Caracas");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(COLOR_TITULO);
        return titulo;
    }

    // botones y combos de la izquierda
    private JPanel crearPanelIzquierdo() {
        btnCargar = new JButton("Cargar");
        btnGuardar = new JButton("Guardar");
        btnAgregarEstacion = new JButton("Agregar estación");
        btnAgregarConexion = new JButton("Agregar conexión");
        btnEliminarEstacion = new JButton("Eliminar estación");
        btnEliminarConexion = new JButton("Eliminar conexión");
        btnRutaCorta = new JButton("Ruta más corta");
        btnSubredes = new JButton("Subredes");
        btnHubs = new JButton("Hubs");

        comboOrigen = new JComboBox<>();
        comboDestino = new JComboBox<>();

        JPanel panelArchivo = crearSeccion("Archivo", 2);
        panelArchivo.add(btnCargar);
        panelArchivo.add(btnGuardar);

        JPanel panelEstaciones = crearSeccion("Estaciones", 4);
        panelEstaciones.add(new JLabel("Origen:"));
        panelEstaciones.add(comboOrigen);
        panelEstaciones.add(new JLabel("Destino:"));
        panelEstaciones.add(comboDestino);

        JPanel panelEditar = crearSeccion("Editar red", 4);
        panelEditar.add(btnAgregarEstacion);
        panelEditar.add(btnAgregarConexion);
        panelEditar.add(btnEliminarEstacion);
        panelEditar.add(btnEliminarConexion);

        JPanel panelAnalisis = crearSeccion("Análisis", 3);
        panelAnalisis.add(btnRutaCorta);
        panelAnalisis.add(btnSubredes);
        panelAnalisis.add(btnHubs);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONDO);
        panel.setPreferredSize(new Dimension(220, 0));
        panel.add(panelArchivo);
        panel.add(panelEstaciones);
        panel.add(panelEditar);
        panel.add(panelAnalisis);
        return panel;
    }

    // panel con borde y titulo donde los componentes van uno debajo del otro
    private JPanel crearSeccion(String titulo, int filas) {
        JPanel seccion = new JPanel(new GridLayout(filas, 1, 5, 5));
        seccion.setBackground(COLOR_FONDO);
        seccion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titulo),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        seccion.setMaximumSize(new Dimension(220, filas * 38 + 30));
        return seccion;
    }

    // aqui despues se va a dibujar el grafo con GraphStream
    private JPanel crearPanelGrafo() {
        panelGrafo = new JPanel(new BorderLayout());
        panelGrafo.setBackground(Color.WHITE);
        panelGrafo.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel mensaje = new JLabel("Aquí se mostrará la red de transporte", SwingConstants.CENTER);
        mensaje.setFont(new Font("SansSerif", Font.ITALIC, 16));
        mensaje.setForeground(Color.GRAY);
        panelGrafo.add(mensaje, BorderLayout.CENTER);
        return panelGrafo;
    }

    // panel de resultados a la derecha
    private JPanel crearPanelDerecho() {
        areaResultados = new JTextArea();
        areaResultados.setEditable(false);
        areaResultados.setLineWrap(true);
        areaResultados.setWrapStyleWord(true);
        areaResultados.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollResultados = new JScrollPane(areaResultados);
        scrollResultados.setBorder(BorderFactory.createTitledBorder("Resultados"));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(COLOR_FONDO);
        panel.setPreferredSize(new Dimension(280, 0));
        panel.add(scrollResultados, BorderLayout.CENTER);
        return panel;
    }
}
