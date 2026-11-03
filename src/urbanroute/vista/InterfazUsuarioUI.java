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
import javax.swing.JTextField;
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
            + "edge { size: 2px; fill-color: #9aa5b1; text-size: 10; text-color: #8a5a00; }"
            // resaltado de la ruta optima
            + "node.ruta { size: 20px; stroke-color: #C0392B; stroke-width: 3px; text-style: bold; }"
            + "edge.ruta { size: 5px; fill-color: #C0392B; text-color: #C0392B; text-style: bold; }"
            // resaltado de los hubs
            + "node.hub { size: 26px; stroke-color: #1F4E5F; stroke-width: 4px; text-style: bold; text-size: 13; }";

    // un color por subred (si hay mas subredes que colores se repiten)
    private static final String[] COLORES_SUBREDES = {
        "#4A6FA5", "#6A994E", "#D08C2D", "#8E5572", "#2A9D8F", "#9C6644", "#7B6D8D", "#B5838D"
    };
    // nombre de cada color para mostrarlo en los resultados
    private static final String[] NOMBRES_COLORES = {
        "azul", "verde", "naranja", "morado", "turquesa", "marrón", "lila", "rosado"
    };

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

    // grafo de GraphStream que se dibuja y su visor (se crean una sola vez)
    private Graph dibujo;
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
        btnAgregarEstacion.addActionListener(e -> agregarEstacion());
        btnEliminarEstacion.addActionListener(e -> eliminarEstacion());
        btnAgregarConexion.addActionListener(e -> agregarConexion());
        btnEliminarConexion.addActionListener(e -> eliminarConexion());
        btnRutaCorta.addActionListener(e -> mostrarRutaMasCorta());
        btnSubredes.addActionListener(e -> mostrarSubredes());
        btnHubs.addActionListener(e -> mostrarHubs());
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

    // ---------------- editar la red ----------------

    private void agregarEstacion() {
        JTextField txtId = new JTextField();
        JTextField txtNombre = new JTextField();
        JTextField txtLinea = new JTextField();
        JTextField txtZona = new JTextField();
        JTextField txtCapacidad = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(5, 2, 8, 6));
        formulario.add(new JLabel("ID:"));
        formulario.add(txtId);
        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);
        formulario.add(new JLabel("Línea:"));
        formulario.add(txtLinea);
        formulario.add(new JLabel("Zona:"));
        formulario.add(txtZona);
        formulario.add(new JLabel("Capacidad (pasajeros/hora):"));
        formulario.add(txtCapacidad);

        // se repite hasta que los datos sean validos o el usuario cancele
        while (true) {
            int opcion = JOptionPane.showConfirmDialog(this, formulario, "Agregar estación",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }

            String id = txtId.getText().trim().toUpperCase();
            String nombre = txtNombre.getText().trim();
            String linea = txtLinea.getText().trim();
            String zona = txtZona.getText().trim();
            String textoCapacidad = txtCapacidad.getText().trim();

            String error = validarEstacion(id, nombre, linea, zona, textoCapacidad);
            if (error != null) {
                mostrarError(error);
                continue;
            }

            int capacidad = Integer.parseInt(textoCapacidad);
            if (!grafo.agregarEstacion(id, nombre, linea, zona, capacidad)) {
                mostrarError("No se pudo agregar la estación " + id + ".");
                continue;
            }
            actualizarVista();
            areaResultados.setText("Estación agregada: " + id + " - " + nombre + "\n"
                    + "Ahora puede conectarla con \"Agregar conexión\".\n");
            return;
        }
    }

    // devuelve el mensaje de error o null si los datos estan bien
    private String validarEstacion(String id, String nombre, String linea, String zona, String capacidad) {
        if (id.isEmpty() || nombre.isEmpty() || linea.isEmpty() || zona.isEmpty() || capacidad.isEmpty()) {
            return "Todos los campos son obligatorios.";
        }
        if (id.contains(" ")) {
            return "El ID no puede tener espacios (puede usar _ por ejemplo LA_P).";
        }
        if (tieneComa(id) || tieneComa(nombre) || tieneComa(linea) || tieneComa(zona)) {
            return "Los datos no pueden tener comas porque se guardan en un archivo CSV.";
        }
        if (grafo.buscarEstacion(id) != null) {
            return "Ya existe una estación con el ID " + id + ".";
        }
        int numero;
        try {
            numero = Integer.parseInt(capacidad);
        } catch (NumberFormatException e) {
            return "La capacidad debe ser un número entero.";
        }
        if (numero <= 0) {
            return "La capacidad debe ser mayor que 0.";
        }
        return null;
    }

    private void eliminarEstacion() {
        if (grafo.getCantidadEstaciones() == 0) {
            mostrarError("No hay estaciones para eliminar.");
            return;
        }
        JComboBox<String> combo = new JComboBox<>(grafo.obtenerIds());
        int opcion = JOptionPane.showConfirmDialog(this, combo, "Elija la estación a eliminar",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION || combo.getSelectedItem() == null) {
            return;
        }

        String id = (String) combo.getSelectedItem();
        int cantidadConexiones = grafo.obtenerVecinos(id).length;
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea eliminar la estación " + id + "?\n"
                + "También se eliminarán sus " + cantidadConexiones + " conexiones.",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        if (grafo.eliminarEstacion(id)) {
            actualizarVista();
            areaResultados.setText("Estación eliminada (cierre temporal): " + id + "\n"
                    + "Se quitaron " + cantidadConexiones + " conexiones.\n");
        } else {
            mostrarError("No se pudo eliminar la estación " + id + ".");
        }
    }

    private void agregarConexion() {
        if (grafo.getCantidadEstaciones() < 2) {
            mostrarError("Se necesitan al menos dos estaciones para crear una conexión.");
            return;
        }
        String[] ids = grafo.obtenerIds();
        JComboBox<String> comboA = new JComboBox<>(ids);
        JComboBox<String> comboB = new JComboBox<>(ids);
        JTextField txtTiempo = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(3, 2, 8, 6));
        formulario.add(new JLabel("Estación origen:"));
        formulario.add(comboA);
        formulario.add(new JLabel("Estación destino:"));
        formulario.add(comboB);
        formulario.add(new JLabel("Tiempo (minutos):"));
        formulario.add(txtTiempo);

        while (true) {
            int opcion = JOptionPane.showConfirmDialog(this, formulario, "Agregar conexión",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }

            String origen = (String) comboA.getSelectedItem();
            String destino = (String) comboB.getSelectedItem();
            String error = validarConexion(origen, destino, txtTiempo.getText().trim());
            if (error != null) {
                mostrarError(error);
                continue;
            }

            int tiempo = Integer.parseInt(txtTiempo.getText().trim());
            if (!grafo.agregarConexion(origen, destino, tiempo)) {
                mostrarError("No se pudo agregar la conexión " + origen + " - " + destino + ".");
                continue;
            }
            actualizarVista();
            areaResultados.setText("Conexión agregada: " + origen + " - " + destino
                    + " (" + tiempo + " min)\n");
            return;
        }
    }

    // devuelve el mensaje de error o null si los datos estan bien
    private String validarConexion(String origen, String destino, String textoTiempo) {
        if (origen == null || destino == null) {
            return "Debe elegir las dos estaciones.";
        }
        if (origen.equals(destino)) {
            return "El origen y el destino no pueden ser la misma estación.";
        }
        if (grafo.obtenerTiempo(origen, destino) != -1) {
            return "Ya existe una conexión entre " + origen + " y " + destino + ".";
        }
        if (textoTiempo.isEmpty()) {
            return "Debe escribir el tiempo en minutos.";
        }
        int tiempo;
        try {
            tiempo = Integer.parseInt(textoTiempo);
        } catch (NumberFormatException e) {
            return "El tiempo debe ser un número entero de minutos.";
        }
        if (tiempo <= 0) {
            return "El tiempo debe ser mayor que 0.";
        }
        return null;
    }

    private void eliminarConexion() {
        // se arma la lista de conexiones existentes, cada una una sola vez
        int total = grafo.getCantidadConexiones();
        if (total == 0) {
            mostrarError("No hay conexiones para eliminar.");
            return;
        }
        String[] origenes = new String[total];
        String[] destinos = new String[total];
        String[] textos = new String[total];
        int k = 0;
        String[] ids = grafo.obtenerIds();
        for (int i = 0; i < ids.length; i++) {
            String[] vecinos = grafo.obtenerVecinos(ids[i]);
            for (int j = 0; j < vecinos.length; j++) {
                if (ids[i].compareTo(vecinos[j]) < 0) {
                    origenes[k] = ids[i];
                    destinos[k] = vecinos[j];
                    textos[k] = ids[i] + " - " + vecinos[j] + " (" + grafo.obtenerTiempo(ids[i], vecinos[j]) + " min)";
                    k++;
                }
            }
        }

        JComboBox<String> combo = new JComboBox<>(textos);
        int opcion = JOptionPane.showConfirmDialog(this, combo, "Elija la conexión a eliminar",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION || combo.getSelectedIndex() < 0) {
            return;
        }

        int elegida = combo.getSelectedIndex();
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea eliminar la conexión " + textos[elegida] + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        if (grafo.eliminarConexion(origenes[elegida], destinos[elegida])) {
            actualizarVista();
            areaResultados.setText("Conexión eliminada: " + textos[elegida] + "\n");
        } else {
            mostrarError("No se pudo eliminar la conexión.");
        }
    }

    private boolean tieneComa(String texto) {
        return texto.contains(",");
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Dato no válido", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.WARNING_MESSAGE);
    }

    // ---------------- analisis de la red ----------------

    private void mostrarRutaMasCorta() {
        String origen = (String) comboOrigen.getSelectedItem();
        String destino = (String) comboDestino.getSelectedItem();
        if (origen == null || destino == null) {
            mostrarAdvertencia("Ruta más corta", "No hay estaciones cargadas.");
            return;
        }
        if (origen.equals(destino)) {
            mostrarAdvertencia("Ruta más corta", "El origen y el destino son la misma estación (" + origen + ").\n"
                    + "Elija dos estaciones distintas.");
            return;
        }

        quitarResaltados();
        String[] ruta = analizador.rutaMasCorta(origen, destino);
        if (ruta == null) {
            areaResultados.setText("No hay camino entre " + origen + " y " + destino + ".\n"
                    + "Las dos estaciones están en subredes distintas.\n");
            mostrarAdvertencia("Ruta más corta", "No existe una ruta entre " + origen + " y " + destino + ".\n"
                    + "Las dos estaciones no están conectadas (subredes distintas).");
            return;
        }

        // primera parte: la ruta completa en una linea
        String texto = "Ruta más corta de " + origen + " a " + destino + ": ";
        for (int i = 0; i < ruta.length; i++) {
            texto = texto + ruta[i];
            if (i < ruta.length - 1) {
                texto = texto + " -> ";
            }
        }
        texto = texto + "\nTiempo total: " + analizador.getTiempoUltimaRuta() + " min ("
                + ruta.length + " estaciones)\n\nDetalle por tramo:\n";
        // segunda parte: el tiempo de cada tramo
        for (int i = 0; i < ruta.length - 1; i++) {
            texto = texto + "  " + ruta[i] + " -> " + ruta[i + 1] + ": "
                    + grafo.obtenerTiempo(ruta[i], ruta[i + 1]) + " min\n";
        }
        areaResultados.setText(texto);
        areaResultados.setCaretPosition(0);
        resaltarRuta(ruta);
    }

    private void mostrarSubredes() {
        if (grafo.getCantidadEstaciones() == 0) {
            mostrarAdvertencia("Subredes", "No hay estaciones cargadas.");
            return;
        }
        quitarResaltados();
        pintarSubredes();
        String[][] subredes = analizador.buscarSubredes();

        String texto;
        if (subredes.length == 1) {
            texto = "La red tiene 1 subred: toda la red está conectada.\n";
        } else {
            texto = "La red tiene " + subredes.length + " subredes:\n";
        }
        for (int s = 0; s < subredes.length; s++) {
            texto = texto + "\nSubred " + (s + 1);
            if (subredes[s].length == 1) {
                texto = texto + " (1 estación, aislada";
            } else {
                texto = texto + " (" + subredes[s].length + " estaciones";
            }
            texto = texto + ", color " + NOMBRES_COLORES[s % NOMBRES_COLORES.length] + "):\n";
            for (int i = 0; i < subredes[s].length; i++) {
                texto = texto + subredes[s][i];
                if (i < subredes[s].length - 1) {
                    texto = texto + ", ";
                }
            }
            texto = texto + "\n";
        }
        areaResultados.setText(texto);
        areaResultados.setCaretPosition(0);
    }

    private void mostrarHubs() {
        if (grafo.getCantidadEstaciones() == 0) {
            mostrarAdvertencia("Hubs", "No hay estaciones cargadas.");
            return;
        }
        quitarResaltados();
        String[] hubs = analizador.buscarHubs();
        if (hubs.length == 0) {
            areaResultados.setText("Hubs: ninguna estación tiene conexiones todavía.\n");
            return;
        }

        String texto = "Hubs (grado " + analizador.getGradoHubs() + "): ";
        for (int i = 0; i < hubs.length; i++) {
            texto = texto + hubs[i];
            if (i < hubs.length - 1) {
                texto = texto + ", ";
            }
        }
        texto = texto + "\n";
        if (hubs.length > 1) {
            texto = texto + "Hay un empate entre " + hubs.length + " estaciones.\n";
        }
        texto = texto + "\nConexiones de cada hub:\n";
        for (int i = 0; i < hubs.length; i++) {
            texto = texto + "- " + hubs[i] + ": conecta con ";
            String[] vecinos = grafo.obtenerVecinos(hubs[i]);
            for (int j = 0; j < vecinos.length; j++) {
                texto = texto + vecinos[j];
                if (j < vecinos.length - 1) {
                    texto = texto + ", ";
                }
            }
            texto = texto + "\n";
            if (dibujo != null && dibujo.getNode(hubs[i]) != null) {
                dibujo.getNode(hubs[i]).setAttribute("ui.class", "hub");
            }
        }
        areaResultados.setText(texto);
        areaResultados.setCaretPosition(0);
    }

    // marca los nodos y aristas de la ruta con la clase "ruta"
    private void resaltarRuta(String[] ruta) {
        if (dibujo == null) {
            return;
        }
        for (int i = 0; i < ruta.length; i++) {
            Node nodo = dibujo.getNode(ruta[i]);
            if (nodo != null) {
                nodo.setAttribute("ui.class", "ruta");
            }
            if (i < ruta.length - 1) {
                Edge arista = buscarArista(ruta[i], ruta[i + 1]);
                if (arista != null) {
                    arista.setAttribute("ui.class", "ruta");
                }
            }
        }
    }

    // las aristas se guardan con el ID menor primero
    private Edge buscarArista(String a, String b) {
        if (a.compareTo(b) < 0) {
            return dibujo.getEdge(a + "-" + b);
        }
        return dibujo.getEdge(b + "-" + a);
    }

    // quita el resaltado de ruta y de hubs (los colores de subred se quedan)
    private void quitarResaltados() {
        if (dibujo == null) {
            return;
        }
        for (int i = 0; i < dibujo.getNodeCount(); i++) {
            dibujo.getNode(i).removeAttribute("ui.class");
        }
        for (int i = 0; i < dibujo.getEdgeCount(); i++) {
            dibujo.getEdge(i).removeAttribute("ui.class");
        }
    }

    // pinta cada subred de un color distinto
    private void pintarSubredes() {
        if (dibujo == null) {
            return;
        }
        String[][] subredes = analizador.buscarSubredes();
        for (int s = 0; s < subredes.length; s++) {
            String color = COLORES_SUBREDES[s % COLORES_SUBREDES.length];
            for (int i = 0; i < subredes[s].length; i++) {
                Node nodo = dibujo.getNode(subredes[s][i]);
                if (nodo != null) {
                    nodo.setAttribute("ui.style", "fill-color: " + color + ";");
                }
            }
        }
    }

    // ---------------- actualizar la vista ----------------

    private void actualizarVista() {
        llenarCombos();
        dibujarGrafo();
        actualizarBarraEstado();
    }

    private void llenarCombos() {
        // se recuerda lo que estaba elegido para dejarlo igual si la estacion sigue existiendo
        Object origenAnterior = comboOrigen.getSelectedItem();
        Object destinoAnterior = comboDestino.getSelectedItem();

        String[] ids = grafo.obtenerIds();
        comboOrigen.removeAllItems();
        comboDestino.removeAllItems();
        for (int i = 0; i < ids.length; i++) {
            comboOrigen.addItem(ids[i]);
            comboDestino.addItem(ids[i]);
        }
        if (origenAnterior != null) {
            comboOrigen.setSelectedItem(origenAnterior);
        }
        if (destinoAnterior != null) {
            comboDestino.setSelectedItem(destinoAnterior);
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

    // actualiza el dibujo para que tenga las mismas estaciones y conexiones que el grafo
    private void dibujarGrafo() {
        if (dibujo == null) {
            if (grafo.getCantidadEstaciones() == 0) {
                return; // se deja el mensaje del panel
            }
            crearVisor();
        }
        actualizarNodos();
        actualizarAristas();
        // si la red cambio, los resaltados viejos ya no sirven
        quitarResaltados();
        pintarSubredes();
    }

    // crea el grafo de GraphStream y lo mete en el panel del centro
    private void crearVisor() {
        dibujo = new SingleGraph("RedMetro");
        dibujo.setAttribute("ui.stylesheet", ESTILO_GRAFO);
        dibujo.setAttribute("ui.quality");
        dibujo.setAttribute("ui.antialias");

        visor = new SwingViewer(dibujo, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        visor.enableAutoLayout();
        ViewPanel vista = (ViewPanel) visor.addDefaultView(false);
        vista.enableMouseOptions(); // zoom con la rueda del mouse

        panelGrafo.removeAll();
        panelGrafo.add(vista, BorderLayout.CENTER);
        panelGrafo.revalidate();
        panelGrafo.repaint();
    }

    // quita los nodos de estaciones eliminadas y agrega los de estaciones nuevas (con su ID)
    private void actualizarNodos() {
        // se recorre al reves porque al borrar cambian las posiciones
        for (int i = dibujo.getNodeCount() - 1; i >= 0; i--) {
            Node nodo = dibujo.getNode(i);
            if (grafo.buscarEstacion(nodo.getId()) == null) {
                dibujo.removeNode(nodo);
            }
        }
        String[] ids = grafo.obtenerIds();
        for (int i = 0; i < ids.length; i++) {
            if (dibujo.getNode(ids[i]) == null) {
                Node nodo = dibujo.addNode(ids[i]);
                nodo.setAttribute("ui.label", ids[i]);
            }
        }
    }

    // igual que con los nodos pero con las conexiones (tiempo en la etiqueta)
    private void actualizarAristas() {
        for (int i = dibujo.getEdgeCount() - 1; i >= 0; i--) {
            Edge arista = dibujo.getEdge(i);
            int tiempo = grafo.obtenerTiempo(arista.getNode0().getId(), arista.getNode1().getId());
            if (tiempo == -1) {
                dibujo.removeEdge(arista);
            } else {
                arista.setAttribute("ui.label", "" + tiempo);
            }
        }
        String[] ids = grafo.obtenerIds();
        for (int i = 0; i < ids.length; i++) {
            String[] vecinos = grafo.obtenerVecinos(ids[i]);
            for (int j = 0; j < vecinos.length; j++) {
                // cada arista se agrega una sola vez
                String idArista = ids[i] + "-" + vecinos[j];
                if (ids[i].compareTo(vecinos[j]) < 0 && dibujo.getEdge(idArista) == null) {
                    Edge arista = dibujo.addEdge(idArista, ids[i], vecinos[j]);
                    arista.setAttribute("ui.label", "" + grafo.obtenerTiempo(ids[i], vecinos[j]));
                }
            }
        }
    }
}
