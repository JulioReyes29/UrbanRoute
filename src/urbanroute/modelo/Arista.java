package urbanroute.modelo;

/**
 * Representa una conexión directa desde una estación hacia otra
 */
public class Arista {

    private String idDestino;
    private int tiempo;

    /**
     * Crea una arista hacia la estación destino.
     */
    public Arista(String idDestino, int tiempo) {
        if (tiempo <= 0) {
            throw new IllegalArgumentException("El tiempo debe ser mayor que 0.");
        }
        this.idDestino = idDestino;
        this.tiempo = tiempo;
    }

    /**
     * Devuelve el ID de la estación destino.
     */
    public String getIdDestino() {
        return idDestino;
    }

    /**
     * Devuelve el tiempo del viaje en minutos.
     */
    public int getTiempo() {
        return tiempo;
    }

    /**
     * Devuelve la arista como texto
     */
    @Override
    public String toString() {
        return idDestino + " (" + tiempo + " min)";
    }
}
