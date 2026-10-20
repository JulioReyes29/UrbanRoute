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
     * No se permite conectar la estación consigo misma ni repetir el destino.
     */
    public boolean agregarConexion(Arista arista) {
        if (arista == null || arista.getIdDestino().equals(id)) {
            return false;
        }
        if (buscarPosicion(arista.getIdDestino()) != -1) {
            return false; // ya existe una conexion con ese destino
        }
        conexiones.insertar(arista);
        return true;
    }

    /**
     * Quita la conexión hacia la estación indicada.
     */
    public boolean eliminarConexion(String idDestino) {
        int posicion = buscarPosicion(idDestino);
        if (posicion == -1) {
            return false;
        }
        Arista arista = conexiones.obtener(posicion);
        return conexiones.eliminar(arista);
    }

    /**
     * Cantidad de conexiones directas.
     */
    public int getGrado() {
        return conexiones.tamano();
    }

    /**
     * ID de la estación vecina en la posición indicada de las conexiones.
     */
    public String getIdVecino(int indice) {
        Arista arista = conexiones.obtener(indice);
        if (arista == null) {
            return null;
        }
        return arista.getIdDestino();
    }

    /**
     * Tiempo de la conexión en la posición indicada.
     */
    public int getTiempoConexion(int indice) {
        Arista arista = conexiones.obtener(indice);
        if (arista == null) {
            return 0;
        }
        return arista.getTiempo();
    }

    // posicion de la conexion hacia ese destino, o -1 si no existe
    private int buscarPosicion(String idDestino) {
        for (int i = 0; i < conexiones.tamano(); i++) {
            if (conexiones.obtener(i).getIdDestino().equals(idDestino)) {
                return i;
            }
        }
        return -1;
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
