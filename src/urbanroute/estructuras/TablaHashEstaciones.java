package urbanroute.estructuras;

import urbanroute.modelo.Estacion;

/**
 * Tabla hash propia que guarda las estaciones usando su ID como clave.
 * Las colisiones se resuelven por encadenamiento separado (una lista por posición).
 * <p>
 * Valores usados y su fuente:
 * <ul>
 * <li>Función hash (hash * 31 + c): es la fórmula de String.hashCode() de Java,
 * s[0]*31^(n-1) + ... + s[n-1], calculada con el método de Horner. Se usa 31 porque
 * es primo e impar y reparte bien los IDs (con los datos del metro, las 44 estaciones
 * quedan en 33 posiciones distintas). Fuente: Oracle, documentación de String (Java SE 17)
 * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#hashCode()
 * y J. Bloch, Effective Java, 3.ª ed., Item 11.</li>
 * <li>Capacidad inicial 16: es el valor por defecto de HashMap. Con 44 estaciones la
 * tabla crece 16, 32, 64. Fuente: Oracle, documentación de HashMap (Java SE 17)
 * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/HashMap.html</li>
 * <li>Factor de carga máximo 0.75: si cantidad / tamaño pasa de 0.75 se hace rehash
 * (se duplica la tabla). Con encadenamiento buscar cuesta en promedio O(1 + factor de carga),
 * así que las listas quedan cortas sin desperdiciar espacio. Fuente: misma documentación
 * de HashMap ("a good tradeoff between time and space costs") y Cormen et al.,
 * Introduction to Algorithms, 3.ª ed., cap. 11 "Hash Tables".</li>
 * </ul>
 * Colabora con: Estacion.
 * Es usada por: GrafoTransporte.
 */
public class TablaHashEstaciones {

    // capacidad inicial 16, igual que HashMap (fuente en el Javadoc de la clase)
    private static final int CAPACIDAD_INICIAL = 16;

    // si cantidad / tamaño pasa de 0.75 se hace rehash (fuente en el Javadoc de la clase)
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
        if (estacion == null || estacion.getId() == null) {
            return false;
        }
        if (buscar(estacion.getId()) != null) {
            return false;
        }
        int posicion = funcionHash(estacion.getId());
        tabla[posicion].insertar(estacion);
        cantidad++;

        double factorCarga = (double) cantidad / tabla.length;
        if (factorCarga > FACTOR_CARGA_MAXIMO) {
            rehash();
        }
        return true;
    }

    /**
     * Busca una estación por su ID. Devuelve null si no existe.
     */
    public Estacion buscar(String id) {
        if (id == null) {
            return null;
        }
        ListaEnlazada<Estacion> lista = tabla[funcionHash(id)];
        for (int i = 0; i < lista.tamano(); i++) {
            Estacion estacion = lista.obtener(i);
            if (estacion.getId().equals(id)) {
                return estacion;
            }
        }
        return null;
    }

    /**
     * Elimina la estación con ese ID.
     */
    public boolean eliminar(String id) {
        Estacion estacion = buscar(id);
        if (estacion == null) {
            return false;
        }
        tabla[funcionHash(id)].eliminar(estacion);
        cantidad--;
        return true;
    }

    /**
     * Devuelve un arreglo con todas las estaciones guardadas.
     */
    public Estacion[] listarTodas() {
        Estacion[] todas = new Estacion[cantidad];
        int k = 0;
        for (int i = 0; i < tabla.length; i++) {
            for (int j = 0; j < tabla[i].tamano(); j++) {
                todas[k] = tabla[i].obtener(j);
                k++;
            }
        }
        return todas;
    }

    /**
     * Cantidad de estaciones guardadas.
     */
    public int getCantidad() {
        return cantidad;
    }

    /**
     * Tamaño actual del arreglo de la tabla.
     */
    public int getCapacidad() {
        return tabla.length;
    }

    // Calcula la posición en la tabla a partir de los caracteres del ID (hash polinomial
    // con 31, igual que String.hashCode(); fuente en el Javadoc de la clase).
    private int funcionHash(String id) {
        int hash = 0;
        for (int i = 0; i < id.length(); i++) {
            // se multiplica por 31 y se saca el modulo en cada paso para no desbordar
            hash = (hash * 31 + id.charAt(i)) % tabla.length;
        }
        return hash;
    }

    // duplica el tamaño de la tabla y vuelve a ubicar todas las estaciones
    private void rehash() {
        Estacion[] todas = listarTodas();
        tabla = crearTabla(tabla.length * 2);
        for (int i = 0; i < todas.length; i++) {
            int posicion = funcionHash(todas[i].getId());
            tabla[posicion].insertar(todas[i]);
        }
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
