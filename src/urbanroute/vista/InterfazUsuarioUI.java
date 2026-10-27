package urbanroute.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import urbanroute.control.AnalizadorRedUrbana;
import urbanroute.control.GestorArchivoCSV;
import urbanroute.modelo.GrafoTransporte;

/**
 * Ventana principal del programa.
 * Colabora con: GestorArchivoCSV, AnalizadorRedUrbana y GrafoTransporte.
 */
public class InterfazUsuarioUI extends JFrame {

    // colores de la ventana
    private static final Color COLOR_BARRAS = Color.decode("#1F4E5F");   // azul petroleo
    private static final Color COLOR_COLUMNA = Color.decode("#F4EFE6");  // arena claro
    private static final Color COLOR_RESULTADOS = Color.decode("#FFFDF8");
    private static final Color COLOR_GRAFO = Color.WHITE;

    private static final int ANCHO_COLUMNA = 300;

    // estilo del dibujo del grafo (CSS de GraphStream)
    private static final String ESTILO_GRAFO =
            "graph { fill-color: white; padding: 40px; }"
            + "node { size: 14px; fill-color: #4a6fa5; stroke-mode: plain; stroke-color: #2c3e50;"
            + " text-size: 11; text-color: #2c3e50; text-alignment: under;"
            + " text-background-mode: plain; text-background-color: white; }"
            + "edge { size: 2px; fill-color: #9aa5b1; text-size: 10; text-color: #8a5a00; }";

    private GrafoTransporte grafo;
    private GestorArchivoCSV gestorArchivo;
    private AnalizadorRedUrbana analizador;

    private JButton btnCargar;
    private JButton btnGuardar;
    private JButton btnAyuda;
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
    private JLabel lblEstado;

    // visor de GraphStream que esta dentro de panelGrafo
    private SwingViewer visor;

    /**
     * Crea la ventana y todos sus componentes.
     */
    public InterfazUsuarioUI() {
        // GraphStream debe usar Swing para poder meter el grafo en la ventana
        System.setProperty("org.graphstream.ui", "swing");

        grafo = new GrafoTransporte();
        gestorArchivo = new GestorArchivoCSV();
        analizador = new AnalizadorRedUrbana(grafo);

        setTitle("UrbanRoute - Red de Metro");
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(crearBarraSuperior(), BorderLayout.NORTH);
        contenido.add(crearPanelGrafo(), BorderLayout.CENTER);
        contenido.add(crearColumnaDerecha(), BorderLayout.EAST);
        contenido.add(crearBarraEstado(), BorderLayout.SOUTH);

        setContentPane(contenido);
        agregarEventos();

        // los datos se cargan cuando la ventana ya se ve
        SwingUtilities.invokeLater(() -> cargarDatosIniciales());
    }

    // titulo a la izquierda y Cargar, Guardar, Ayuda a la derecha
    private JPanel crearBarraSuperior() {
        JLabel titulo = new JLabel("UrbanRoute - Red de Metro");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);

        btnCargar = new JButton("Cargar");
        btnGuardar = new JButton("Guardar");
        btnAyuda = new JButton("Ayuda");

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnCargar);
        botones.add(btnGuardar);
        botones.add(btnAyuda);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(COLOR_BARRAS);
        barra.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        barra.add(titulo, BorderLayout.WEST);
        barra.add(botones, BorderLayout.EAST);
        return barra;
    }

    // aqui se dibuja el grafo con GraphStream
    private JPanel crearPanelGrafo() {
        panelGrafo = new JPanel(new BorderLayout());
        panelGrafo.setBackground(COLOR_GRAFO);

        JLabel mensaje = new JLabel("Aquí se mostrará la red de transporte", SwingConstants.CENTER);
        mensaje.setFont(new Font("SansSerif", Font.ITALIC, 16));
        mensaje.setForeground(Color.GRAY);
        panelGrafo.add(mensaje, BorderLayout.CENTER);
        return panelGrafo;
    }

    // pestañas arriba y resultados abajo
    private JPanel crearColumnaDerecha() {
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setBackground(COLOR_COLUMNA);
        pestanas.addTab("Estaciones", crearPestanaEstaciones());
        pestanas.addTab("Rutas", crearPestanaRutas());
        pestanas.addTab("Análisis", crearPestanaAnalisis());

        areaResultados = new JTextArea();
        areaResultados.setEditable(false);
        areaResultados.setLineWrap(true);
        areaResultados.setWrapStyleWord(true);
        areaResultados.setBackground(COLOR_RESULTADOS);
        areaResultados.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollResultados = new JScrollPane(areaResultados);
        scrollResultados.setBorder(BorderFactory.createTitledBorder("Resultados"));
        scrollResultados.setBackground(COLOR_COLUMNA);

        JPanel columna = new JPanel(new BorderLayout(0, 10));
        columna.setBackground(COLOR_COLUMNA);
        columna.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        columna.setPreferredSize(new Dimension(ANCHO_COLUMNA, 0));
        columna.add(pestanas, BorderLayout.NORTH);
        columna.add(scrollResultados, BorderLayout.CENTER);
        return columna;
    }

    private JPanel crearPestanaEstaciones() {
        btnAgregarEstacion = new JButton("Agregar estación");
        btnEliminarEstacion = new JButton("Eliminar estación");
        btnAgregarConexion = new JButton("Agregar conexión");
        btnEliminarConexion = new JButton("Eliminar conexión");

        JPanel lista = crearListaVertical(4);
        lista.add(btnAgregarEstacion);
        lista.add(btnEliminarEstacion);
        lista.add(btnAgregarConexion);
        lista.add(btnEliminarConexion);
        return envolverPestana(lista);
    }

    private JPanel crearPestanaRutas() {
        comboOrigen = new JComboBox<>();
        comboDestino = new JComboBox<>();
        btnRutaCorta = new JButton("Ruta más corta");

        JPanel lista = crearListaVertical(5);
        lista.add(new JLabel("Origen:"));
        lista.add(comboOrigen);
        lista.add(new JLabel("Destino:"));
        lista.add(comboDestino);
        lista.add(btnRutaCorta);
        return envolverPestana(lista);
    }

    private JPanel crearPestanaAnalisis() {
        btnSubredes = new JButton("Subredes");
        btnHubs = new JButton("Hubs");

        JPanel lista = crearListaVertical(2);
        lista.add(btnSubredes);
        lista.add(btnHubs);
        return envolverPestana(lista);
    }

    // GridLayout de una columna para que todo quede del mismo ancho
    private JPanel crearListaVertical(int filas) {
        JPanel lista = new JPanel(new GridLayout(filas, 1, 0, 8));
        lista.setOpaque(false);
        return lista;
    }

    // pone la lista arriba de la pestaña para que los botones no se estiren a lo alto
    private JPanel envolverPestana(JPanel lista) {
        JPanel pestana = new JPanel(new BorderLayout());
        pestana.setBackground(COLOR_COLUMNA);
        pestana.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        pestana.add(lista, BorderLayout.NORTH);
        return pestana;
    }

    private JLabel crearBarraEstado() {
        lblEstado = new JLabel("Estaciones: 0   Conexiones: 0   Sin cambios pendientes");
        lblEstado.setOpaque(true);
        lblEstado.setBackground(COLOR_BARRAS);
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(5, 16, 5, 16));
        return lblEstado;
    }

    private void agregarEventos() {
        btnCargar.addActionListener(e -> cargarArchivo());
        btnGuardar.addActionListener(e -> guardarArchivos());
        btnAyuda.addActionListener(e -> mostrarAyuda());
    }

    private void mostrarAyuda() {
        JOptionPane.showMessageDialog(this,
                "Cómo usar UrbanRoute:\n\n"
                + "- Cargar: abre un CSV de estaciones o de conexiones.\n"
                + "- Guardar: guarda los cambios en los archivos abiertos.\n"
                + "- Pestaña Estaciones: agregar o eliminar estaciones y conexiones.\n"
                + "- Pestaña Rutas: elige Origen y Destino y pulsa Ruta más corta.\n"
                + "- Pestaña Análisis: Subredes muestra las partes aisladas y\n"
                + "  Hubs las estaciones con más conexiones.\n"
                + "- En el grafo puedes arrastrar los nodos y hacer zoom con la rueda del mouse.",
                "Ayuda", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------- archivos ----------------

    // carga data/estaciones.csv y data/conexiones.csv al abrir el programa
    private void cargarDatosIniciales() {
        boolean bien = gestorArchivo.cargarPorDefecto(grafo);
        actualizarVista();
        if (!bien) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar los datos iniciales.\n" + gestorArchivo.getErrores()
                    + "\nPuede usar el botón Cargar para abrir otro archivo.",
                    "Error al cargar", JOptionPane.ERROR_MESSAGE);
        } else {
            mostrarAvisosDeCarga();
        }
        areaResultados.setText("Red cargada: " + grafo.getCantidadEstaciones() + " estaciones y "
                + grafo.getCantidadConexiones() + " conexiones.\n");
    }

    private void cargarArchivo() {
        if (!confirmarCambiosSinGuardar()) {
            return;
        }
        File archivo = gestorArchivo.elegirArchivo(this);
        if (archivo == null) {
            return; // el usuario cancelo
        }
        boolean bien = gestorArchivo.cargarArchivo(archivo, grafo);
        if (!bien) {
            JOptionPane.showMessageDialog(this, gestorArchivo.getErrores(),
                    "No se pudo cargar el archivo", JOptionPane.ERROR_MESSAGE);
            return;
        }
        actualizarVista();
        mostrarAvisosDeCarga();
        areaResultados.setText("Archivo cargado: " + archivo.getName() + "\n"
                + "La red tiene " + grafo.getCantidadEstaciones() + " estaciones y "
                + grafo.getCantidadConexiones() + " conexiones.\n");
    }

    // devuelve true si se guardo bien
    private boolean guardarArchivos() {
        boolean bien = gestorArchivo.guardar(grafo, this);
        if (bien) {
            actualizarBarraEstado();
            JOptionPane.showMessageDialog(this, "Cambios guardados en:\n"
                    + gestorArchivo.getNombreArchivoEstaciones() + "\n"
                    + gestorArchivo.getNombreArchivoConexiones(),
                    "Guardar", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, gestorArchivo.getErrores(),
                    "No se pudo guardar", JOptionPane.ERROR_MESSAGE);
        }
        return bien;
    }

    // si hay cambios pregunta si se guardan. Devuelve false si el usuario cancela
    private boolean confirmarCambiosSinGuardar() {
        if (!grafo.hayCambiosSinGuardar()) {
            return true;
        }
        int opcion = JOptionPane.showConfirmDialog(this,
                "Hay cambios sin guardar. ¿Desea guardarlos antes de cargar otro archivo?",
                "Cambios sin guardar", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion == JOptionPane.YES_OPTION) {
            return guardarArchivos();
        }
        return opcion == JOptionPane.NO_OPTION;
    }

    // muestra los errores de las lineas que no se pudieron cargar
    private void mostrarAvisosDeCarga() {
        String errores = gestorArchivo.getErrores();
        if (errores.isEmpty()) {
            return;
        }
        JTextArea texto = new JTextArea(errores);
        texto.setEditable(false);
        JScrollPane scroll = new JScrollPane(texto);
        scroll.setPreferredSize(new Dimension(480, 220));
        JOptionPane.showMessageDialog(this, scroll, "Avisos de la carga", JOptionPane.WARNING_MESSAGE);
    }

    // ---------------- actualizar la vista ----------------

    private void actualizarVista() {
        llenarCombos();
        dibujarGrafo();
        actualizarBarraEstado();
    }

    private void llenarCombos() {
        String[] ids = grafo.obtenerIds();
        comboOrigen.removeAllItems();
        comboDestino.removeAllItems();
        for (int i = 0; i < ids.length; i++) {
            comboOrigen.addItem(ids[i]);
            comboDestino.addItem(ids[i]);
        }
    }

    private void actualizarBarraEstado() {
        String cambios;
        if (grafo.hayCambiosSinGuardar()) {
            cambios = "Hay cambios sin guardar";
        } else {
            cambios = "Sin cambios pendientes";
        }
        lblEstado.setText("Estaciones: " + grafo.getCantidadEstaciones()
                + "   Conexiones: " + grafo.getCantidadConexiones()
                + "   " + cambios);
    }

    // vuelve a crear el dibujo del grafo con GraphStream
    private void dibujarGrafo() {
        if (visor != null) {
            visor.close();
            visor = null;
        }
        panelGrafo.removeAll();

        if (grafo.getCantidadEstaciones() == 0) {
            JLabel mensaje = new JLabel("No hay estaciones cargadas", SwingConstants.CENTER);
            mensaje.setForeground(Color.GRAY);
            panelGrafo.add(mensaje, BorderLayout.CENTER);
        } else {
            Graph dibujo = crearGrafoDibujo();
            visor = new SwingViewer(dibujo, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
            visor.enableAutoLayout();
            ViewPanel vista = (ViewPanel) visor.addDefaultView(false);
            vista.enableMouseOptions(); // zoom con la rueda del mouse
            panelGrafo.add(vista, BorderLayout.CENTER);
        }
        panelGrafo.revalidate();
        panelGrafo.repaint();
    }

    // arma el grafo de GraphStream: ID en los nodos y tiempo en las aristas
    private Graph crearGrafoDibujo() {
        Graph dibujo = new SingleGraph("RedMetro");
        dibujo.setAttribute("ui.stylesheet", ESTILO_GRAFO);
        dibujo.setAttribute("ui.quality");
        dibujo.setAttribute("ui.antialias");

        String[] ids = grafo.obtenerIds();
        for (int i = 0; i < ids.length; i++) {
            Node nodo = dibujo.addNode(ids[i]);
            nodo.setAttribute("ui.label", ids[i]);
        }
        for (int i = 0; i < ids.length; i++) {
            String[] vecinos = grafo.obtenerVecinos(ids[i]);
            for (int j = 0; j < vecinos.length; j++) {
                // cada arista se agrega una sola vez
                if (ids[i].compareTo(vecinos[j]) < 0) {
                    Edge arista = dibujo.addEdge(ids[i] + "-" + vecinos[j], ids[i], vecinos[j]);
                    arista.setAttribute("ui.label", "" + grafo.obtenerTiempo(ids[i], vecinos[j]));
                }
            }
        }
        return dibujo;
    }
}
