package urbanroute.control;

import urbanroute.estructuras.Cola;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.GrafoTransporte;

/**
 * Hace los análisis de la red: ruta más corta (Dijkstra), subredes (BFS) y hubs.
 * Solo lee el grafo (buscarEstacion y obtenerEstaciones), no lo modifica.
 * Colabora con: GrafoTransporte y Estacion.
 * Es usada por: InterfazUsuarioUI.
 */
public class AnalizadorRedUrbana {

    // valor que se usa como "infinito" en Dijkstra
    private static final int INFINITO = Integer.MAX_VALUE;

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
        tiempoUltimaRuta = 0;
        if (grafo.buscarEstacion(idOrigen) == null || grafo.buscarEstacion(idDestino) == null) {
            return null;
        }

        Estacion[] estaciones = grafo.obtenerEstaciones();
        int n = estaciones.length;
        int[] distancia = new int[n];
        boolean[] visitado = new boolean[n];
        int[] anterior = new int[n];

        for (int i = 0; i < n; i++) {
            distancia[i] = INFINITO;
            visitado[i] = false;
            anterior[i] = -1;
        }
        int origen = buscarIndice(estaciones, idOrigen);
        int destino = buscarIndice(estaciones, idDestino);
        distancia[origen] = 0;

        for (int paso = 0; paso < n; paso++) {
            // se elige la estacion no visitada con menor distancia
            int actual = -1;
            for (int i = 0; i < n; i++) {
                if (!visitado[i] && distancia[i] != INFINITO
                        && (actual == -1 || distancia[i] < distancia[actual])) {
                    actual = i;
                }
            }
            if (actual == -1 || actual == destino) {
                break; // no quedan alcanzables o ya se llego al destino
            }
            visitado[actual] = true;

            // se revisan los vecinos de la estacion actual
            Estacion estacion = estaciones[actual];
            for (int j = 0; j < estacion.getGrado(); j++) {
                int vecino = buscarIndice(estaciones, estacion.getIdVecino(j));
                if (vecino != -1 && !visitado[vecino]) {
                    int nuevaDistancia = distancia[actual] + estacion.getTiempoConexion(j);
                    if (nuevaDistancia < distancia[vecino]) {
                        distancia[vecino] = nuevaDistancia;
                        anterior[vecino] = actual;
                    }
                }
            }
        }

        if (distancia[destino] == INFINITO) {
            return null; // estan en subredes distintas
        }
        tiempoUltimaRuta = distancia[destino];
        return armarRuta(estaciones, anterior, destino);
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
        Estacion[] estaciones = grafo.obtenerEstaciones();
        int n = estaciones.length;
        // numeroSubred[i] dice a que subred pertenece la estacion i (-1 = sin revisar)
        int[] numeroSubred = new int[n];
        for (int i = 0; i < n; i++) {
            numeroSubred[i] = -1;
        }

        int cantidadSubredes = 0;
        for (int i = 0; i < n; i++) {
            if (numeroSubred[i] == -1) {
                recorrerBFS(estaciones, i, cantidadSubredes, numeroSubred);
                cantidadSubredes++;
            }
        }

        // se cuenta cuantas estaciones tiene cada subred para crear las filas
        int[] tamanos = new int[cantidadSubredes];
        for (int i = 0; i < n; i++) {
            tamanos[numeroSubred[i]]++;
        }
        String[][] subredes = new String[cantidadSubredes][];
        for (int s = 0; s < cantidadSubredes; s++) {
            subredes[s] = new String[tamanos[s]];
        }
        int[] llenos = new int[cantidadSubredes];
        for (int i = 0; i < n; i++) {
            int s = numeroSubred[i];
            subredes[s][llenos[s]] = estaciones[i].getId();
            llenos[s]++;
        }
        return subredes;
    }

    /**
     * Devuelve los IDs de las estaciones con mayor grado (puede haber empate).
     */
    public String[] buscarHubs() {
        Estacion[] estaciones = grafo.obtenerEstaciones();
        gradoHubs = 0;
        for (int i = 0; i < estaciones.length; i++) {
            if (estaciones[i].getGrado() > gradoHubs) {
                gradoHubs = estaciones[i].getGrado();
            }
        }
        if (gradoHubs == 0) {
            return new String[0]; // no hay conexiones
        }

        // primero se cuentan los empates y luego se llenan
        int cantidad = 0;
        for (int i = 0; i < estaciones.length; i++) {
            if (estaciones[i].getGrado() == gradoHubs) {
                cantidad++;
            }
        }
        String[] hubs = new String[cantidad];
        int k = 0;
        for (int i = 0; i < estaciones.length; i++) {
            if (estaciones[i].getGrado() == gradoHubs) {
                hubs[k] = estaciones[i].getId();
                k++;
            }
        }
        return hubs;
    }

    /**
     * Grado de los hubs encontrados en la última búsqueda.
     */
    public int getGradoHubs() {
        return gradoHubs;
    }

    // BFS desde la estacion inicio: marca con el numero de subred a todas las que alcanza
    private void recorrerBFS(Estacion[] estaciones, int inicio, int subred, int[] numeroSubred) {
        Cola<Integer> cola = new Cola<>();
        cola.encolar(inicio);
        numeroSubred[inicio] = subred;

        while (!cola.estaVacia()) {
            int actual = cola.desencolar();
            Estacion estacion = estaciones[actual];
            for (int j = 0; j < estacion.getGrado(); j++) {
                int vecino = buscarIndice(estaciones, estacion.getIdVecino(j));
                if (vecino != -1 && numeroSubred[vecino] == -1) {
                    numeroSubred[vecino] = subred;
                    cola.encolar(vecino);
                }
            }
        }
    }

    // sigue el arreglo anterior desde el destino hacia atras y lo voltea
    private String[] armarRuta(Estacion[] estaciones, int[] anterior, int destino) {
        int largo = 0;
        int actual = destino;
        while (actual != -1) {
            largo++;
            actual = anterior[actual];
        }
        String[] ruta = new String[largo];
        actual = destino;
        for (int i = largo - 1; i >= 0; i--) {
            ruta[i] = estaciones[actual].getId();
            actual = anterior[actual];
        }
        return ruta;
    }

    // devuelve la posicion de la estacion con ese ID en el arreglo, o -1
    private int buscarIndice(Estacion[] estaciones, String id) {
        for (int i = 0; i < estaciones.length; i++) {
            if (estaciones[i].getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public void setGrafo(GrafoTransporte grafo) {
        this.grafo = grafo;
    }
}
