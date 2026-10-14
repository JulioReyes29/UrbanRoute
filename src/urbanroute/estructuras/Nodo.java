package urbanroute.estructuras;

/**
 * Nodo simple que guarda un dato y la referencia al siguiente nodo.
 * Se usa dentro de ListaEnlazada y de Cola.
 *
 */
public class Nodo<T> {

    private T dato;
    private Nodo<T> siguiente;

    /**
     * Crea un nodo con el dato indicado y sin siguiente.
     *
     */
    public Nodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    /**
     * Devuelve el dato guardado en el nodo.
     *
     */
    public T getDato() {
        return dato;
    }

    /**
     * Cambia el dato guardado en el nodo.
     *
     */
    public void setDato(T dato) {
        this.dato = dato;
    }

    /**
     * Devuelve el nodo que sigue a este.
     *
     */
    public Nodo<T> getSiguiente() {
        return siguiente;
    }

    /**
     * Cambia el nodo que sigue a este.
     *
     */
    public void setSiguiente(Nodo<T> siguiente) {
        this.siguiente = siguiente;
    }
}
