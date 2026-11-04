UrbanRoute - Red de Metro

Programa en Java que carga la red del Metro de Caracas desde archivos CSV y la
representa como un grafo no dirigido con peso. Permite agregar y cerrar
estaciones y conexiones, buscar la ruta más corta (Dijkstra), las subredes
aisladas (BFS) y los hubs, y ver el grafo dibujado con GraphStream.

Autor: Julio Reyes
Repositorio: https://github.com/JulioReyes29/UrbanRoute.git


Relación entre el archivo y el grafo
------------------------------------
- Cada línea del archivo de estaciones es un vértice (Estacion). El grafo lo
  guarda en la tabla hash usando su ID como clave.
- Cada línea del archivo de conexiones es una arista no dirigida con peso: se
  pone una Arista en cada extremo y el peso es el tiempo en minutos.
- Al guardar se hace el camino inverso: cada vértice vuelve a ser una línea del
  archivo de estaciones y cada arista una línea del archivo de conexiones (se
  escribe una sola vez, no A-B y también B-A).

Ejemplo: la línea "PRO,PER,2" del archivo de conexiones significa que las
estaciones PRO (Propatria) y PER (Perez Bonalde) están conectadas en los dos
sentidos con un tiempo de 2 minutos. En el grafo queda:
  PRO -> Arista(PER, 2)
  PER -> Arista(PRO, 2)
