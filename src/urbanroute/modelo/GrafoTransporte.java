package urbanroute.modelo;

import urbanroute.estructuras.TablaHashEstaciones;

/**
 * Red de transporte como grafo no dirigido con lista de adyacencia.
 * Es la única clase que guarda y busca estaciones en la tabla hash.
 * Colabora con: TablaHashEstaciones, Estacion y Arista.
 * Es usada por: GestorArchivoCSV, AnalizadorRedUrbana e InterfazUsuarioUI.
 */
public class GrafoTransporte {

    private TablaHashEstaciones estaciones;
    private boolean cambiosSinGuardar;

    /**
     * Crea un grafo vacío.
     */
    public GrafoTransporte() {
        estaciones = new TablaHashEstaciones();
        cambiosSinGuardar = false;
    }

    /**
     * Agrega una estación nueva. Devuelve false si el ID ya existe.
     */
    public boolean agregarEstacion(Estacion estacion) {
        // TODO: se implementa en la etapa 3
        return false;
    }

    /**
     * Conecta dos estaciones en ambos sentidos con el tiempo dado.
     */
    public boolean agregarConexion(String idOrigen, String idDestino, int tiempo) {
        // TODO: se implementa en la etapa 3 (una Arista en cada extremo)
        return false;
    }

    /**
     * Elimina una estación (cierre temporal) y sus conexiones.
     */
    public boolean eliminarEstacion(String id) {
        // TODO: se implementa en la etapa 3
        return false;
    }

    /**
     * Elimina la conexión entre dos estaciones.
     */
    public boolean eliminarConexion(String idOrigen, String idDestino) {
        // TODO: se implementa en la etapa 3
        return false;
    }

    /**
     * Busca una estación por ID usando la tabla hash.
     */
    public Estacion buscarEstacion(String id) {
        return estaciones.buscar(id);
    }

    /**
     * Devuelve todas las estaciones del grafo.
     */
    public Estacion[] obtenerEstaciones() {
        return estaciones.listarTodas();
    }

    /**
     * Borra todas las estaciones (antes de cargar otro archivo).
     */
    public void vaciar() {
        estaciones = new TablaHashEstaciones();
        cambiosSinGuardar = false;
    }

    public boolean hayCambiosSinGuardar() {
        return cambiosSinGuardar;
    }

    public void setCambiosSinGuardar(boolean cambiosSinGuardar) {
        this.cambiosSinGuardar = cambiosSinGuardar;
    }
}
