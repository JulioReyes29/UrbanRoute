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
     * Devuelve todas las estaciones del grafo ordenadas por ID.
     */
    public Estacion[] obtenerEstaciones() {
        Estacion[] lista = estaciones.listarTodas();
        // ordenamiento por insercion usando el ID
        for (int i = 1; i < lista.length; i++) {
            Estacion actual = lista[i];
            int j = i - 1;
            while (j >= 0 && lista[j].getId().compareTo(actual.getId()) > 0) {
                lista[j + 1] = lista[j];
                j--;
            }
            lista[j + 1] = actual;
        }
        return lista;
    }

    /**
     * Devuelve los IDs de todas las estaciones ordenados.
     */
    public String[] obtenerIds() {
        Estacion[] lista = obtenerEstaciones();
        String[] ids = new String[lista.length];
        for (int i = 0; i < lista.length; i++) {
            ids[i] = lista[i].getId();
        }
        return ids;
    }

    /**
     * Devuelve los IDs de las estaciones conectadas directamente a la indicada.
     */
    public String[] obtenerVecinos(String id) {
        Estacion estacion = estaciones.buscar(id);
        if (estacion == null) {
            return new String[0];
        }
        String[] vecinos = new String[estacion.getGrado()];
        for (int i = 0; i < vecinos.length; i++) {
            vecinos[i] = estacion.getIdVecino(i);
        }
        return vecinos;
    }

    /**
     * Tiempo de la conexión entre dos estaciones, o -1 si no están conectadas.
     */
    public int obtenerTiempo(String idOrigen, String idDestino) {
        Estacion origen = estaciones.buscar(idOrigen);
        if (origen == null) {
            return -1;
        }
        for (int i = 0; i < origen.getGrado(); i++) {
            if (origen.getIdVecino(i).equals(idDestino)) {
                return origen.getTiempoConexion(i);
            }
        }
        return -1;
    }

    /**
     * Cantidad de conexiones del grafo (cada A-B cuenta una vez).
     */
    public int getCantidadConexiones() {
        Estacion[] lista = estaciones.listarTodas();
        int suma = 0;
        for (int i = 0; i < lista.length; i++) {
            suma = suma + lista[i].getGrado();
        }
        return suma / 2;
    }

    /**
     * Quita todas las conexiones pero deja las estaciones.
     * Se usa antes de cargar otro archivo de conexiones.
     */
    public void quitarConexiones() {
        Estacion[] lista = estaciones.listarTodas();
        for (int i = 0; i < lista.length; i++) {
            while (lista[i].getGrado() > 0) {
                lista[i].eliminarConexion(lista[i].getIdVecino(0));
            }
        }
        cambiosSinGuardar = true;
    }

    /**
     * Borra todas las estaciones (antes de cargar otro archivo).
     */
    public void vaciar() {
        estaciones = new TablaHashEstaciones();
        cambiosSinGuardar = false;
    }

    /**
     * Indica si hubo cambios después de la última carga o guardado.
     */
    public boolean hayCambiosSinGuardar() {
        return cambiosSinGuardar;
    }

    /**
     * Marca si hay cambios pendientes (lo usa el gestor al cargar o guardar).
     */
    public void setCambiosSinGuardar(boolean cambiosSinGuardar) {
        this.cambiosSinGuardar = cambiosSinGuardar;
    }
}
