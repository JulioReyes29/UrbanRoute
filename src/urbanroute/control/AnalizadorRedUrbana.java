package urbanroute.control;

import urbanroute.modelo.Estacion;
import urbanroute.modelo.GrafoTransporte;

/**
 * Hace los análisis de la red: ruta más corta, subredes aisladas y hubs.
 * Busca las estaciones con grafo.buscarEstacion.
 * Colabora con: GrafoTransporte y Estacion.
 * Es usada por: InterfazUsuarioUI.
 */
public class AnalizadorRedUrbana {

    private GrafoTransporte grafo;
    private int tiempoUltimaRuta;
    private int gradoHubs;

    /**
     * Crea el analizador para el grafo dado.
     */
    public AnalizadorRedUrbana(GrafoTransporte grafo) {
        this.grafo = grafo;
        this.tiempoUltimaRuta = 0;
        this.gradoHubs = 0;
    }

    /**
     * Ruta de menor tiempo con Dijkstra. Devuelve los IDs en orden,
     * o null si no hay camino.
     */
    public String[] rutaMasCorta(String idOrigen, String idDestino) {
        // TODO: se implementa en la etapa 6
        return null;
    }

    /**
     * Tiempo total en minutos de la última ruta calculada.
     */
    public int getTiempoUltimaRuta() {
        return tiempoUltimaRuta;
    }

    /**
     * Busca las subredes aisladas con BFS. Cada fila tiene los IDs de una subred.
     */
    public String[][] buscarSubredes() {
        // TODO: se implementa en la etapa 6
        return new String[0][0];
    }

    /**
     * Devuelve los IDs de las estaciones con mayor grado (puede haber empate).
     */
    public String[] buscarHubs() {
        // TODO: se implementa en la etapa 6
        return new String[0];
    }

    /**
     * Grado de los hubs encontrados en la última búsqueda.
     */
    public int getGradoHubs() {
        return gradoHubs;
    }

    // devuelve la posicion de la estacion con ese ID en el arreglo, o -1
    private int buscarIndice(Estacion[] estaciones, String id) {
        // TODO: se implementa en la etapa 6
        return -1;
    }

    public void setGrafo(GrafoTransporte grafo) {
        this.grafo = grafo;
    }
}
