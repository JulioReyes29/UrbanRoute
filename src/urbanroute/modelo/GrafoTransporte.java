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
        if (estacion == null || estacion.getId() == null || estacion.getId().isEmpty()) {
            return false;
        }
        boolean agregada = estaciones.insertar(estacion);
        if (agregada) {
            cambiosSinGuardar = true;
        }
        return agregada;
    }

    /**
     * Crea la estación con sus datos y la agrega. Devuelve false si el ID ya existe.
     */
    public boolean agregarEstacion(String id, String nombre, String linea, String zona, int capacidad) {
        return agregarEstacion(new Estacion(id, nombre, linea, zona, capacidad));
    }

    /**
     * Conecta dos estaciones en ambos sentidos con el tiempo dado.
     * Devuelve false si falta alguna estación, son la misma, el tiempo
     * no es positivo o la conexión ya existe.
     */
    public boolean agregarConexion(String idOrigen, String idDestino, int tiempo) {
        Estacion origen = estaciones.buscar(idOrigen);
        Estacion destino = estaciones.buscar(idDestino);
        if (origen == null || destino == null || origen == destino || tiempo <= 0) {
            return false;
        }
        // como el grafo es no dirigido se pone una arista en cada extremo
        if (!origen.agregarConexion(new Arista(idDestino, tiempo))) {
            return false;
        }
        destino.agregarConexion(new Arista(idOrigen, tiempo));
        cambiosSinGuardar = true;
        return true;
    }

    /**
     * Elimina una estación (cierre temporal). Se quita de la tabla hash
     * y de las listas de conexiones de sus vecinos.
     */
    public boolean eliminarEstacion(String id) {
        Estacion estacion = estaciones.buscar(id);
        if (estacion == null) {
            return false;
        }
        for (int i = 0; i < estacion.getGrado(); i++) {
            Estacion vecino = estaciones.buscar(estacion.getIdVecino(i));
            if (vecino != null) {
                vecino.eliminarConexion(id);
            }
        }
        estaciones.eliminar(id);
        cambiosSinGuardar = true;
        return true;
    }

    /**
     * Elimina la conexión entre dos estaciones (en los dos sentidos).
     */
    public boolean eliminarConexion(String idOrigen, String idDestino) {
        Estacion origen = estaciones.buscar(idOrigen);
        Estacion destino = estaciones.buscar(idDestino);
        if (origen == null || destino == null) {
            return false;
        }
        boolean eliminada = origen.eliminarConexion(idDestino);
        destino.eliminarConexion(idOrigen);
        if (eliminada) {
            cambiosSinGuardar = true;
        }
        return eliminada;
    }

    /**
     * Cantidad de estaciones del grafo.
     */
    public int getCantidadEstaciones() {
        return estaciones.getCantidad();
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
