package urbanroute.control;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.GrafoTransporte;

/**
 * Lee y guarda los archivos CSV de estaciones y conexiones.
 * Todo lo que lee lo agrega a través del grafo.
 * Colabora con: GrafoTransporte y Estacion.
 * Es usada por: InterfazUsuarioUI.
 */
public class GestorArchivoCSV {

    public static final String ENCABEZADO_ESTACIONES = "ID_Estacion,Nombre,Linea,Zona,Capacidad_Pasajeros_Hora";
    public static final String ENCABEZADO_CONEXIONES = "Estacion_Origen,Estacion_Destino,Tiempo_Minutos";

    private File archivoEstaciones;
    private File archivoConexiones;
    private String errores;

    /**
     * Crea el gestor apuntando a los archivos por defecto de la carpeta data.
     */
    public GestorArchivoCSV() {
        archivoEstaciones = new File("data/estaciones.csv");
        archivoConexiones = new File("data/conexiones.csv");
        errores = "";
    }

    /**
     * Abre un JFileChooser para elegir un CSV. Devuelve null si se cancela.
     */
    public File elegirArchivo(Component padre) {
        JFileChooser selector = new JFileChooser(new File("data"));
        selector.setDialogTitle("Cargar archivo CSV");
        selector.setFileFilter(new FileNameExtensionFilter("Archivos CSV", "csv"));
        if (selector.showOpenDialog(padre) == JFileChooser.APPROVE_OPTION) {
            return selector.getSelectedFile();
        }
        return null;
    }

    /**
     * Carga los archivos por defecto al iniciar el programa.
     */
    public boolean cargarPorDefecto(GrafoTransporte grafo) {
        File estaciones = new File("data/estaciones.csv");
        File conexiones = new File("data/conexiones.csv");

        grafo.vaciar();
        boolean bien = cargarArchivo(estaciones, grafo);
        String erroresEstaciones = errores;

        if (bien) {
            bien = cargarArchivo(conexiones, grafo);
        }
        // se juntan los errores de los dos archivos
        errores = erroresEstaciones + errores;
        return bien;
    }

    /**
     * Lee un CSV y detecta por el encabezado si es de estaciones o conexiones.
     * Un archivo de estaciones reemplaza toda la red. Un archivo de conexiones
     * reemplaza las conexiones de las estaciones actuales.
     * Devuelve false si no se pudo leer o el formato no se reconoce.
     */
    public boolean cargarArchivo(File archivo, GrafoTransporte grafo) {
        errores = "";
        if (archivo == null || !archivo.exists()) {
            errores = "No se encontró el archivo " + archivo + "\n";
            return false;
        }

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {

            String encabezado = lector.readLine();
            if (encabezado == null) {
                errores = "El archivo " + archivo.getName() + " está vacío.\n";
                return false;
            }
            encabezado = limpiarEncabezado(encabezado);

            boolean esEstaciones = encabezado.equalsIgnoreCase(ENCABEZADO_ESTACIONES);
            boolean esConexiones = encabezado.equalsIgnoreCase(ENCABEZADO_CONEXIONES);
            if (!esEstaciones && !esConexiones) {
                errores = "El archivo " + archivo.getName() + " no tiene un encabezado válido.\n"
                        + "Se esperaba:\n" + ENCABEZADO_ESTACIONES + "\no\n" + ENCABEZADO_CONEXIONES + "\n";
                return false;
            }

            if (esEstaciones) {
                grafo.vaciar();
                archivoEstaciones = archivo;
                // las conexiones viejas ya no sirven para esta red
                archivoConexiones = null;
            } else {
                grafo.quitarConexiones();
                archivoConexiones = archivo;
            }

            String linea = lector.readLine();
            int numeroLinea = 2;
            while (linea != null) {
                if (!linea.trim().isEmpty()) {
                    if (esEstaciones) {
                        procesarLineaEstacion(linea, numeroLinea, grafo);
                    } else {
                        procesarLineaConexion(linea, numeroLinea, grafo);
                    }
                }
                linea = lector.readLine();
                numeroLinea++;
            }
        } catch (IOException e) {
            errores = errores + "Error al leer " + archivo.getName() + ": " + e.getMessage() + "\n";
            return false;
        }

        // lo que se acaba de cargar ya coincide con el archivo
        grafo.setCambiosSinGuardar(false);
        return true;
    }

    /**
     * Sobrescribe los archivos de origen con los datos actuales del grafo.
     * Cada conexión se escribe una sola vez (A-B y no también B-A).
     * Si falta alguno de los archivos se pregunta dónde guardarlo.
     */
    public boolean guardar(GrafoTransporte grafo, Component padre) {
        errores = "";
        if (archivoEstaciones == null) {
            archivoEstaciones = elegirDondeGuardar(padre, "Guardar estaciones como");
        }
        if (archivoConexiones == null) {
            archivoConexiones = elegirDondeGuardar(padre, "Guardar conexiones como");
        }
        if (archivoEstaciones == null || archivoConexiones == null) {
            errores = "No se eligió dónde guardar los archivos.";
            return false;
        }

        Estacion[] estaciones = grafo.obtenerEstaciones();
        try {
            escribirEstaciones(estaciones);
            escribirConexiones(estaciones);
        } catch (IOException e) {
            errores = "No se pudo guardar: " + e.getMessage();
            return false;
        }
        grafo.setCambiosSinGuardar(false);
        return true;
    }

    /**
     * Errores encontrados en la última carga (vacío si no hubo).
     */
    public String getErrores() {
        return errores;
    }

    /**
     * Nombre del archivo de estaciones actual (o "sin archivo").
     */
    public String getNombreArchivoEstaciones() {
        return nombreDe(archivoEstaciones);
    }

    /**
     * Nombre del archivo de conexiones actual (o "sin archivo").
     */
    public String getNombreArchivoConexiones() {
        return nombreDe(archivoConexiones);
    }

    // linea de estaciones: crea la Estacion y la agrega con grafo.agregarEstacion
    private void procesarLineaEstacion(String linea, int numeroLinea, GrafoTransporte grafo) {
        String[] campos = linea.split(",", -1);
        if (campos.length != 5) {
            agregarError(numeroLinea, "se esperaban 5 campos y hay " + campos.length);
            return;
        }
        String id = campos[0].trim();
        if (id.isEmpty()) {
            agregarError(numeroLinea, "falta el ID de la estación");
            return;
        }

        int capacidad;
        try {
            capacidad = Integer.parseInt(campos[4].trim());
        } catch (NumberFormatException e) {
            agregarError(numeroLinea, "la capacidad \"" + campos[4].trim() + "\" no es un número válido");
            return;
        }
        if (capacidad < 0) {
            agregarError(numeroLinea, "la capacidad no puede ser negativa");
            return;
        }

        Estacion estacion = new Estacion(id, campos[1].trim(), campos[2].trim(), campos[3].trim(), capacidad);
        if (!grafo.agregarEstacion(estacion)) {
            agregarError(numeroLinea, "la estación " + id + " está repetida");
        }
    }

    // linea de conexiones: crea las estaciones que falten (solo con ID) y llama grafo.agregarConexion
    private void procesarLineaConexion(String linea, int numeroLinea, GrafoTransporte grafo) {
        String[] campos = linea.split(",", -1);
        if (campos.length != 3) {
            agregarError(numeroLinea, "se esperaban 3 campos y hay " + campos.length);
            return;
        }
        String origen = campos[0].trim();
        String destino = campos[1].trim();
        if (origen.isEmpty() || destino.isEmpty()) {
            agregarError(numeroLinea, "falta la estación de origen o de destino");
            return;
        }
        if (origen.equals(destino)) {
            agregarError(numeroLinea, "el origen y el destino son la misma estación (" + origen + ")");
            return;
        }

        int tiempo;
        try {
            tiempo = Integer.parseInt(campos[2].trim());
        } catch (NumberFormatException e) {
            agregarError(numeroLinea, "el tiempo \"" + campos[2].trim() + "\" no es un número válido");
            return;
        }
        if (tiempo <= 0) {
            agregarError(numeroLinea, "el tiempo debe ser mayor que 0");
            return;
        }

        crearSiNoExiste(origen, numeroLinea, grafo);
        crearSiNoExiste(destino, numeroLinea, grafo);
        if (!grafo.agregarConexion(origen, destino, tiempo)) {
            agregarError(numeroLinea, "la conexión " + origen + "-" + destino + " está repetida");
        }
    }

    // crea la estacion solo con su ID si todavia no esta en el grafo
    private void crearSiNoExiste(String id, int numeroLinea, GrafoTransporte grafo) {
        if (grafo.buscarEstacion(id) == null) {
            grafo.agregarEstacion(new Estacion(id));
            agregarError(numeroLinea, "la estación " + id + " no existía, se creó solo con su ID");
        }
    }

    private void escribirEstaciones(Estacion[] estaciones) throws IOException {
        try (BufferedWriter escritor = crearEscritor(archivoEstaciones)) {
            escritor.write(ENCABEZADO_ESTACIONES);
            escritor.newLine();
            for (int i = 0; i < estaciones.length; i++) {
                Estacion e = estaciones[i];
                escritor.write(e.getId() + "," + e.getNombre() + "," + e.getLinea() + ","
                        + e.getZona() + "," + e.getCapacidad());
                escritor.newLine();
            }
        }
    }

    private void escribirConexiones(Estacion[] estaciones) throws IOException {
        try (BufferedWriter escritor = crearEscritor(archivoConexiones)) {
            escritor.write(ENCABEZADO_CONEXIONES);
            escritor.newLine();
            for (int i = 0; i < estaciones.length; i++) {
                Estacion e = estaciones[i];
                for (int j = 0; j < e.getGrado(); j++) {
                    String vecino = e.getIdVecino(j);
                    // solo se escribe cuando el ID del origen es menor, asi A-B no sale dos veces
                    if (e.getId().compareTo(vecino) < 0) {
                        escritor.write(e.getId() + "," + vecino + "," + e.getTiempoConexion(j));
                        escritor.newLine();
                    }
                }
            }
        }
    }

    private BufferedWriter crearEscritor(File archivo) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8));
    }

    private File elegirDondeGuardar(Component padre, String titulo) {
        JFileChooser selector = new JFileChooser(new File("data"));
        selector.setDialogTitle(titulo);
        selector.setFileFilter(new FileNameExtensionFilter("Archivos CSV", "csv"));
        if (selector.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getPath() + ".csv");
        }
        return archivo;
    }

    // quita espacios y el BOM que a veces pone Excel al inicio del archivo
    private String limpiarEncabezado(String encabezado) {
        if (encabezado.startsWith("﻿")) {
            encabezado = encabezado.substring(1);
        }
        return encabezado.replace(" ", "");
    }

    private void agregarError(int numeroLinea, String mensaje) {
        errores = errores + "Línea " + numeroLinea + ": " + mensaje + "\n";
    }

    private String nombreDe(File archivo) {
        if (archivo == null) {
            return "sin archivo";
        }
        return archivo.getName();
    }
}
