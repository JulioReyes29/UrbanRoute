package urbanroute.estructuras;

/**
 * Cola propia.
 *
 */
public class Cola<T> {

    private Nodo<T> frente;
    private Nodo<T> fin;

    /**
     * Crea una cola vacía.
     */
    public Cola() {
        frente = null;
        fin = null;
    }

    /**
     * Agrega un dato al final de la cola.
     *
     */
    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
    }

    /**
     * Saca y devuelve el dato que está al frente de la cola.
     *
     */
    public T desencolar() {
        if (estaVacia()) {
            return null;
        }
        T dato = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) {
            // la cola quedó vacía
            fin = null;
        }
        return dato;
    }

    /**
     * Indica si la cola no tiene elementos.
     *
     */
    public boolean estaVacia() {
        return frente == null;
    }
}
