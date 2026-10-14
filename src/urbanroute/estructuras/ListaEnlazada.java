package urbanroute.estructuras;

/**
 * Lista simplemente enlazada propia, usada en lugar de las listas de Java
 *
 */
public class ListaEnlazada<T> {

    private Nodo<T> primero;
    private Nodo<T> ultimo;
    private int tamano;

    /**
     * Crea una lista vacía.
     */
    public ListaEnlazada() {
        primero = null;
        ultimo = null;
        tamano = 0;
    }

    /**
     * Inserta un dato al final de la lista.
     *
     */
    public void insertar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            primero = nuevo;
        } else {
            ultimo.setSiguiente(nuevo);
        }
        ultimo = nuevo;
        tamano++;
    }

    /**
     * Elimina la primera aparición del dato en la lista.
     */
    public boolean eliminar(T dato) {
        Nodo<T> anterior = null;
        Nodo<T> actual = primero;

        while (actual != null) {
            if (sonIguales(actual.getDato(), dato)) {
                if (anterior == null) {
                    // se elimina el primero
                    primero = actual.getSiguiente();
                } else {
                    anterior.setSiguiente(actual.getSiguiente());
                }
                if (actual == ultimo) {
                    ultimo = anterior;
                }
                tamano--;
                return true;
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        return false;
    }

    /**
     * Busca un dato en la lista.
     */
    public int buscar(T dato) {
        Nodo<T> actual = primero;
        int posicion = 0;
        while (actual != null) {
            if (sonIguales(actual.getDato(), dato)) {
                return posicion;
            }
            actual = actual.getSiguiente();
            posicion++;
        }
        return -1;
    }

    /**
     * Devuelve el dato que está en una posición de la lista.
     */
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamano) {
            return null;
        }
        Nodo<T> actual = primero;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getDato();
    }

    /**
     * Devuelve la cantidad de elementos de la lista.
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si la lista no tiene elementos.
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /**
     * Compara dos datos teniendo en cuenta que pueden ser null.
     */
    private boolean sonIguales(T a, T b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
