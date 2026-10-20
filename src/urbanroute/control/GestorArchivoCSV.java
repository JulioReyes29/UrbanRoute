package urbanroute.control;

import java.awt.Component;
import java.io.File;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.GrafoTransporte;

/**
 * Lee y guarda los archivos CSV de estaciones y conexiones.
 * Todo lo que lee lo agrega a través del grafo.
 * Colabora con: GrafoTransporte y Estacion.
 * Es usada por: InterfazUsuarioUI.
 */
public class GestorArchivoCSV {

    public static final String ENCABEZADO_ESTACIONES = "ID_Estacion,Nombre,Linea,Zona,Capacidad_Pasajeros_Hora";
    public static final String ENCABEZADO_CONEXIONES = "Estacion_Origen,Estacion_Destino,Tiempo_Minutos";

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
     * Abre un JFileChooser para elegir un CSV. Devuelve null si se cancela.
     */
    public File elegirArchivo(Component padre) {
        // TODO: se implementa en la etapa 4
        return null;
    }

    /**
     * Carga los archivos por defecto al iniciar el programa.
     */
    public boolean cargarPorDefecto(GrafoTransporte grafo) {
        // TODO: se implementa en la etapa 4
        return false;
    }

    /**
     * Lee un CSV y detecta por el encabezado si es de estaciones o conexiones.
     */
    public boolean cargarArchivo(File archivo, GrafoTransporte grafo) {
        // TODO: se implementa en la etapa 4
        return false;
    }

    /**
     * Sobrescribe los archivos de origen con los datos actuales del grafo.
     * Cada conexión se escribe una sola vez (A-B y no también B-A).
     */
    public boolean guardar(GrafoTransporte grafo) {
        // TODO: se implementa en la etapa 4 usando grafo.obtenerEstaciones()
        return false;
    }

    // linea de estaciones: crea la Estacion y la agrega con grafo.agregarEstacion
    private void procesarLineaEstacion(String linea, int numeroLinea, GrafoTransporte grafo) {
        // TODO: se implementa en la etapa 4
    }

    // linea de conexiones: crea las estaciones que falten (solo con ID) y llama grafo.agregarConexion
    private void procesarLineaConexion(String linea, int numeroLinea, GrafoTransporte grafo) {
        // TODO: se implementa en la etapa 4
    }

    // crea la estacion solo con su ID si todavia no esta en el grafo
    private void crearSiNoExiste(String id, GrafoTransporte grafo) {
        if (grafo.buscarEstacion(id) == null) {
            grafo.agregarEstacion(new Estacion(id));
        }
    }

    /**
     * Errores encontrados en la última carga (vacío si no hubo).
     */
    public String getErrores() {
        return errores;
    }
}
