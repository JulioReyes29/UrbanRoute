package urbanroute.control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.GrafoTransporte;

/**
 * Lee y guarda los archivos CSV de estaciones y conexiones.
 * Formato CSV simple (una línea por registro, campos separados por coma, sin comillas),
 * basado en RFC 4180 (2005) https://www.rfc-editor.org/rfc/rfc4180
 * Relación entre el archivo y el grafo:
 * - Cada línea del archivo de estaciones es un vértice (Estacion). El grafo lo
 *   guarda en la tabla hash usando su ID como clave.
 * - Cada línea del archivo de conexiones es una arista no dirigida con peso: se pone
 *   una Arista en cada extremo y el peso es el tiempo en minutos.
 * - Al guardar se hace el camino inverso: cada vértice vuelve a ser una línea de
 *   estaciones y cada arista una línea de conexiones (una sola vez, no A-B y B-A).
 * No usa nada de la interfaz: la interfaz elige los archivos y muestra los errores
 * que devuelve getErrores.
 * Colabora con: GrafoTransporte y Estacion.
 * Es usada por: InterfazUsuarioUI.
 */
public class GestorArchivoCSV {

    /**
     * Primera línea que debe tener el archivo de estaciones (formato dado en el enunciado).
     */
    public static final String ENCABEZADO_ESTACIONES = "ID_Estacion,Nombre,Linea,Zona,Capacidad_Pasajeros_Hora";
    /**
     * Primera línea que debe tener el archivo de conexiones (formato dado en el enunciado).
     */
    public static final String ENCABEZADO_CONEXIONES = "Estacion_Origen,Estacion_Destino,Tiempo_Minutos";
    // Limite para el tiempo de una conexion (valor propio, no viene de una fuente).
    // Asi la suma de tiempos en Dijkstra nunca pasa de Integer.MAX_VALUE y no se desborda.
    private static final int TIEMPO_MAXIMO = 999;

    private File archivoEstaciones;
    private File archivoConexiones;
    private String errores;

    /**
     * Crea el gestor apuntando a los archivos por defecto de la carpeta data.
     */
    public GestorArchivoCSV() {
        archivoEstaciones = new File("data/estaciones.csv");
        archivoConexiones = new File("data/conexiones.csv");
        errores = "";
    }

    /**
     * Carga los archivos por defecto al iniciar el programa.
     */
    public boolean cargarPorDefecto(GrafoTransporte grafo) {
        File estaciones = new File("data/estaciones.csv");
        File conexiones = new File("data/conexiones.csv");

        grafo.vaciar();
        boolean bien = cargarArchivo(estaciones, grafo);
        String erroresEstaciones = errores;

        if (bien) {
            bien = cargarArchivo(conexiones, grafo);
        }
        // se juntan los errores de los dos archivos
        errores = erroresEstaciones + errores;
        return bien;
    }

    /**
     * Lee un CSV y detecta por el encabezado si es de estaciones o conexiones.
     * Un archivo de estaciones reemplaza toda la red. Un archivo de conexiones
     * reemplaza las conexiones de las estaciones actuales.
     * Devuelve false si no se pudo leer o el formato no se reconoce.
     */
    public boolean cargarArchivo(File archivo, GrafoTransporte grafo) {
        errores = "";
        if (archivo == null || !archivo.exists()) {
            errores = "No se encontró el archivo " + archivo + "\n";
            return false;
        }

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {

            String encabezado = lector.readLine();
            if (encabezado == null) {
                errores = "El archivo " + archivo.getName() + " está vacío.\n";
                return false;
            }
            encabezado = limpiarEncabezado(encabezado);

            boolean esEstaciones = encabezado.equalsIgnoreCase(ENCABEZADO_ESTACIONES);
            boolean esConexiones = encabezado.equalsIgnoreCase(ENCABEZADO_CONEXIONES);
            if (!esEstaciones && !esConexiones) {
                errores = "El archivo " + archivo.getName() + " no tiene un encabezado válido.\n"
                        + "Se esperaba:\n" + ENCABEZADO_ESTACIONES + "\no\n" + ENCABEZADO_CONEXIONES + "\n";
                return false;
            }

            if (esEstaciones) {
                grafo.vaciar();
                archivoEstaciones = archivo;
                // las conexiones viejas ya no sirven para esta red
                archivoConexiones = null;
            } else {
                grafo.quitarConexiones();
                archivoConexiones = archivo;
            }

            String linea = lector.readLine();
            int numeroLinea = 2;
            while (linea != null) {
                if (!linea.trim().isEmpty()) {
                    if (esEstaciones) {
                        lineaAVertice(linea, numeroLinea, grafo);
                    } else {
                        lineaAArista(linea, numeroLinea, grafo);
                    }
                }
                linea = lector.readLine();
                numeroLinea++;
            }
        } catch (IOException e) {
            errores = errores + "Error al leer " + archivo.getName() + ": " + e.getMessage() + "\n";
            return false;
        }

        // lo que se acaba de cargar ya coincide con el archivo
        grafo.setCambiosSinGuardar(false);
        return true;
    }

    /**
     * Guarda el grafo en los dos archivos que eligió la interfaz.
     * Primero arma el texto completo de cada archivo y luego lo escribe.
     * Solo si todo se escribió bien se marca que no hay cambios pendientes.
     */
    public boolean guardar(GrafoTransporte grafo, File archivoEstaciones, File archivoConexiones) {
        errores = "";
        if (archivoEstaciones == null || archivoConexiones == null) {
            errores = "Falta indicar dónde guardar las estaciones o las conexiones.";
            return false;
        }

        Estacion[] estaciones = grafo.obtenerEstaciones();
        String textoEstaciones = armarTextoEstaciones(estaciones);
        String textoConexiones = armarTextoConexiones(estaciones);
        try {
            escribirArchivo(archivoEstaciones, textoEstaciones);
            escribirArchivo(archivoConexiones, textoConexiones);
        } catch (IOException e) {
            errores = "No se pudo guardar: " + e.getMessage();
            return false; // los cambios siguen pendientes
        }

        this.archivoEstaciones = archivoEstaciones;
        this.archivoConexiones = archivoConexiones;
        grafo.setCambiosSinGuardar(false);
        return true;
    }

    /**
     * Archivo de estaciones actual (null si todavía no hay uno).
     */
    public File getArchivoEstaciones() {
        return archivoEstaciones;
    }

    /**
     * Archivo de conexiones actual (null si todavía no hay uno).
     */
    public File getArchivoConexiones() {
        return archivoConexiones;
    }

    /**
     * Errores encontrados en la última carga (vacío si no hubo).
     */
    public String getErrores() {
        return errores;
    }

    /**
     * Nombre del archivo de estaciones actual (o "sin archivo").
     */
    public String getNombreArchivoEstaciones() {
        return nombreDe(archivoEstaciones);
    }

    /**
     * Nombre del archivo de conexiones actual (o "sin archivo").
     */
    public String getNombreArchivoConexiones() {
        return nombreDe(archivoConexiones);
    }

    // linea de estaciones -> vertice: crea la Estacion y la agrega con grafo.agregarEstacion
    private void lineaAVertice(String linea, int numeroLinea, GrafoTransporte grafo) {
        // el -1 hace que split conserve los campos vacios del final ("A,B," da 3 campos y no 2),
        // asi se detecta el dato que falta. Fuente: documentacion de String.split(String, int)
        // https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#split(java.lang.String,int)
        String[] campos = linea.split(",", -1);
        if (campos.length != 5) {
            agregarError(numeroLinea, "se esperaban 5 campos y hay " + campos.length);
            return;
        }
        String id = campos[0].trim();
        if (id.isEmpty()) {
            agregarError(numeroLinea, "falta el ID de la estación");
            return;
        }

        int capacidad;
        try {
            capacidad = Integer.parseInt(campos[4].trim());
        } catch (NumberFormatException e) {
            agregarError(numeroLinea, "la capacidad \"" + campos[4].trim() + "\" no es un número válido");
            return;
        }
        if (capacidad < 0) {
            agregarError(numeroLinea, "la capacidad no puede ser negativa");
            return;
        }

        Estacion estacion = new Estacion(id, campos[1].trim(), campos[2].trim(), campos[3].trim(), capacidad);
        if (!grafo.agregarEstacion(estacion)) {
            agregarError(numeroLinea, "la estación " + id + " está repetida");
        }
    }

    // linea de conexiones -> arista: crea las estaciones que falten (solo con ID) y llama grafo.agregarConexion
    private void lineaAArista(String linea, int numeroLinea, GrafoTransporte grafo) {
        // -1 para conservar los campos vacios (ver lineaAVertice)
        String[] campos = linea.split(",", -1);
        if (campos.length != 3) {
            agregarError(numeroLinea, "se esperaban 3 campos y hay " + campos.length);
            return;
        }
        String origen = campos[0].trim();
        String destino = campos[1].trim();
        if (origen.isEmpty() || destino.isEmpty()) {
            agregarError(numeroLinea, "falta la estación de origen o de destino");
            return;
        }
        if (origen.equals(destino)) {
            agregarError(numeroLinea, "el origen y el destino son la misma estación (" + origen + ")");
            return;
        }

        int tiempo;
        try {
            tiempo = Integer.parseInt(campos[2].trim());
        } catch (NumberFormatException e) {
            agregarError(numeroLinea, "el tiempo \"" + campos[2].trim() + "\" no es un número válido");
            return;
        }
        if (tiempo <= 0) {
            agregarError(numeroLinea, "el tiempo debe ser mayor que 0");
            return;
        }
        if (tiempo > TIEMPO_MAXIMO) {
            agregarError(numeroLinea, "el tiempo no puede ser mayor que " + TIEMPO_MAXIMO + " minutos");
            return;
        }

        crearSiNoExiste(origen, numeroLinea, grafo);
        crearSiNoExiste(destino, numeroLinea, grafo);
        if (!grafo.agregarConexion(origen, destino, tiempo)) {
            agregarError(numeroLinea, "la conexión " + origen + "-" + destino + " está repetida");
        }
    }

    // crea la estacion solo con su ID si todavia no esta en el grafo
    private void crearSiNoExiste(String id, int numeroLinea, GrafoTransporte grafo) {
        if (grafo.buscarEstacion(id) == null) {
            grafo.agregarEstacion(new Estacion(id));
            agregarError(numeroLinea, "la estación " + id + " no existía, se creó solo con su ID");
        }
    }

    // una linea por estacion, ordenadas por ID
    private String armarTextoEstaciones(Estacion[] estaciones) {
        StringBuilder texto = new StringBuilder();
        texto.append(ENCABEZADO_ESTACIONES).append("\n");
        for (int i = 0; i < estaciones.length; i++) {
            Estacion e = estaciones[i];
            texto.append(e.getId()).append(",").append(e.getNombre()).append(",")
                    .append(e.getLinea()).append(",").append(e.getZona()).append(",")
                    .append(e.getCapacidad()).append("\n");
        }
        return texto.toString();
    }

    // una linea por conexion: A-B se escribe una sola vez (y no tambien B-A)
    private String armarTextoConexiones(Estacion[] estaciones) {
        StringBuilder texto = new StringBuilder();
        texto.append(ENCABEZADO_CONEXIONES).append("\n");
        for (int i = 0; i < estaciones.length; i++) {
            Estacion e = estaciones[i];
            for (int j = 0; j < e.getGrado(); j++) {
                String vecino = e.getIdVecino(j);
                // solo se escribe cuando el ID del origen es menor
                if (e.getId().compareTo(vecino) < 0) {
                    texto.append(e.getId()).append(",").append(vecino).append(",")
                            .append(e.getTiempoConexion(j)).append("\n");
                }
            }
        }
        return texto.toString();
    }

    private void escribirArchivo(File archivo, String texto) throws IOException {
        try (BufferedWriter escritor = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            escritor.write(texto);
        }
    }

    // Quita espacios y el BOM que a veces pone Excel al inicio del archivo.
    // El BOM es el caracter invisible U+FEFF; si no se quita, el encabezado no se reconoce.
    // Fuente: Unicode, preguntas frecuentes sobre UTF y BOM https://www.unicode.org/faq/utf_bom.html
    private String limpiarEncabezado(String encabezado) {
        if (encabezado.startsWith("\uFEFF")) {
            encabezado = encabezado.substring(1);
        }
        return encabezado.replace(" ", "");
    }

    private void agregarError(int numeroLinea, String mensaje) {
        errores = errores + "Línea " + numeroLinea + ": " + mensaje + "\n";
    }

    private String nombreDe(File archivo) {
        if (archivo == null) {
            return "sin archivo";
        }
        return archivo.getName();
    }
}
