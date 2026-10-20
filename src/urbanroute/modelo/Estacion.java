package urbanroute.modelo;

import urbanroute.estructuras.ListaEnlazada;

/**
 * Estación del metro con sus datos y sus conexiones directas.
 * Colabora con: Arista.
 * Es usada por: TablaHashEstaciones, GrafoTransporte, GestorArchivoCSV y AnalizadorRedUrbana.
 */
public class Estacion {

    private String id;
    private String nombre;
    private String linea;
    private String zona;
    private int capacidad;
    private ListaEnlazada<Arista> conexiones;

    /**
     * Crea una estación con todos sus datos.
     */
    public Estacion(String id, String nombre, String linea, String zona, int capacidad) {
        this.id = id;
        this.nombre = nombre;
        this.linea = linea;
        this.zona = zona;
        this.capacidad = capacidad;
        this.conexiones = new ListaEnlazada<>();
    }

    /**
     * Crea una estación solo con su ID (cuando aparece en una conexión).
     */
    public Estacion(String id) {
        this(id, "", "", "", 0);
    }

    /**
     * Agrega una conexión (arista) hacia otra estación.
     */
    public boolean agregarConexion(Arista arista) {
        // TODO: se implementa en la etapa 3
        return false;
    }

    /**
     * Quita la conexión hacia la estación indicada.
     */
    public boolean eliminarConexion(String idDestino) {
        // TODO: se implementa en la etapa 3
        return false;
    }

    /**
     * Cantidad de conexiones directas.
     */
    public int getGrado() {
        // TODO: se implementa en la etapa 3
        return 0;
    }

    /**
     * ID de la estación vecina en la posición indicada de las conexiones.
     */
    public String getIdVecino(int indice) {
        // TODO: se implementa en la etapa 3
        return null;
    }

    /**
     * Tiempo de la conexión en la posición indicada.
     */
    public int getTiempoConexion(int indice) {
        // TODO: se implementa en la etapa 3
        return 0;
    }

    public ListaEnlazada<Arista> getConexiones() {
        return conexiones;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLinea() {
        return linea;
    }

    public void setLinea(String linea) {
        this.linea = linea;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    @Override
    public String toString() {
        return id + " - " + nombre;
    }
}
