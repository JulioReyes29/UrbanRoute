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


Referencias de la tabla hash
----------------------------
- Función hash (hash * 31 + c): es la fórmula de String.hashCode() de Java.
  Se usa 31 por ser primo e impar, lo que reparte mejor los IDs en la tabla.
  Oracle. (2021). String (Java SE 17 & JDK 17). https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#hashCode()
  Bloch, J. (2018). Effective Java (3.ª ed.), Item 11. Addison-Wesley.
- Capacidad inicial 16 y factor de carga 0.75: son los valores por defecto de
  HashMap en Java. El 0.75 es un equilibrio entre tiempo de búsqueda y espacio.
  Oracle. (2021). HashMap (Java SE 17 & JDK 17). https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/HashMap.html
  Cormen, T. H., Leiserson, C. E., Rivest, R. L. y Stein, C. (2009).
  Introduction to Algorithms (3.ª ed.), cap. 11 "Hash Tables". MIT Press.


Referencias del analizador
--------------------------
- Ruta más corta (Dijkstra, versión con arreglos):
  Dijkstra, E. W. (1959). A note on two problems in connexion with graphs.
  Numerische Mathematik, 1, 269-271. https://doi.org/10.1007/BF01386390
- Subredes (BFS / búsqueda en anchura):
  Cormen, T. H., Leiserson, C. E., Rivest, R. L. y Stein, C. (2009).
  Introduction to Algorithms (3.ª ed.), cap. 22 "Elementary Graph Algorithms". MIT Press.
- INFINITO = Integer.MAX_VALUE (el mayor int de Java):
  Oracle. (2021). Integer (Java SE 17 & JDK 17). https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Integer.html#MAX_VALUE


Referencias del gestor de archivos
----------------------------------
- Formato CSV: Shafranovich, Y. (2005). RFC 4180: Common Format and MIME Type
  for Comma-Separated Values (CSV) Files. https://www.rfc-editor.org/rfc/rfc4180
- split(",", -1) para conservar los campos vacíos:
  Oracle. (2021). String (Java SE 17 & JDK 17), método split(String, int).
  https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#split(java.lang.String,int)
- BOM (U+FEFF) al inicio de los CSV guardados con Excel:
  Unicode Consortium. UTF-8, UTF-16, UTF-32 & BOM (FAQ). https://www.unicode.org/faq/utf_bom.html
- Encabezados de los archivos: formato dado en el enunciado del proyecto.
- TIEMPO_MAXIMO = 999: valor propio, para que la suma de tiempos en Dijkstra no se desborde.
