package urbanroute.estructuras;

import urbanroute.modelo.Estacion;

/**
 * Tabla hash propia que guarda las estaciones usando su ID como clave.
 * Las colisiones se resuelven por encadenamiento separado (una lista por posición).
 * Colabora con: Estacion.
 * Es usada por: GrafoTransporte.
 */
public class TablaHashEstaciones {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA_MAXIMO = 0.75;

    private ListaEnlazada<Estacion>[] tabla;
    private int cantidad;

    /**
     * Crea la tabla vacía con la capacidad inicial.
     */
    public TablaHashEstaciones() {
        tabla = crearTabla(CAPACIDAD_INICIAL);
        cantidad = 0;
    }

    /**
     * Inserta una estación. Devuelve false si ya existe ese ID.
     */
    public boolean insertar(Estacion estacion) {
        // TODO: se implementa en la etapa 3
        return false;
    }

    /**
     * Busca una estación por su ID. Devuelve null si no existe.
     */
    public Estacion buscar(String id) {
        // TODO: se implementa en la etapa 3
        return null;
    }

    /**
     * Elimina la estación con ese ID.
     */
    public boolean eliminar(String id) {
        // TODO: se implementa en la etapa 3
        return false;
    }

    /**
     * Devuelve un arreglo con todas las estaciones guardadas.
     */
    public Estacion[] listarTodas() {
        // TODO: se implementa en la etapa 3
        return new Estacion[0];
    }

    /**
     * Cantidad de estaciones guardadas.
     */
    public int getCantidad() {
        return cantidad;
    }

    // calcula la posicion en la tabla a partir de los caracteres del ID
    private int funcionHash(String id) {
        // TODO: se implementa en la etapa 3
        return 0;
    }

    // duplica el tamaño de la tabla cuando se pasa del factor de carga
    private void rehash() {
        // TODO: se implementa en la etapa 3
    }

    @SuppressWarnings("unchecked")
    private ListaEnlazada<Estacion>[] crearTabla(int capacidad) {
        ListaEnlazada<Estacion>[] nueva = new ListaEnlazada[capacidad];
        for (int i = 0; i < capacidad; i++) {
            nueva[i] = new ListaEnlazada<>();
        }
        return nueva;
    }
}
