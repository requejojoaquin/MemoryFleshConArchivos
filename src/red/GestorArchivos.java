package red;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

// Clase para manejar los archivos
public class GestorArchivos {

    // archivos
    public static final String RUTA_BASE    = "Datos";
    public static final String USUARIOS_FILE = RUTA_BASE + "/usuarios.txt";
    public static final String MEMORIAS_FILE = RUTA_BASE + "/memorias.txt";
    public static final String LOGS_FILE     = RUTA_BASE + "/logs.txt";
    public static final String SEP = "|";

    // semaforos
    public static Semaphore semUsuarios = new Semaphore(1, true);
    public static Semaphore semMemorias = new Semaphore(1, true);
    public static Semaphore semLogs     = new Semaphore(1, true);

    static {
        File carpeta = new File(RUTA_BASE);
        if (!carpeta.exists()) carpeta.mkdirs();
        crearArchivoSiNoExiste(USUARIOS_FILE);
        crearArchivoSiNoExiste(MEMORIAS_FILE);
        crearArchivoSiNoExiste(LOGS_FILE);
    }

    private static void crearArchivoSiNoExiste(String ruta) {
        try {
            File archivo = new File(ruta);
            if (!archivo.exists()) archivo.createNewFile();
        } catch (Exception e) {
            System.err.println("Error creando archivo: " + ruta + " - " + e.getMessage());
        }
    }

    //-----------------Proceso Lectura de Archivos (usuarios.txt, memorias.txt, logs.txt)
    // leer
    public static ArrayList<String> leerLineas(String ruta) {
        ArrayList<String> lineas = new ArrayList<String>();
        try {
            File archivo = new File(ruta);
            if (archivo.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(archivo));
                String linea;
                while ((linea = br.readLine()) != null) {
                    linea = linea.trim();
                    if (!linea.isEmpty()) lineas.add(linea);
                }
                br.close();
            }
        } catch (Exception e) {
            System.err.println("Error leyendo archivo: " + ruta + " - " + e.getMessage());
        }
        return lineas;
    }

    //-----------------Proceso Escritura en Archivo (agrega linea al final - usado en registro, login log, alta memoria)
    // agregar
    public static void agregarLinea(String ruta, String nuevaLinea) {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(ruta, true)))) {
            pw.println(nuevaLinea);
        } catch (Exception e) {
            System.err.println("Error agregando linea a: " + ruta + " - " + e.getMessage());
        }
    }

    //-----------------Proceso Reescritura Total de Archivo (usado en modificacion, eliminacion de usuario y baja de memoria)
    // escribir todo
    public static void escribirTodo(String ruta, ArrayList<String> lineas) {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(ruta)))) {
            for (String l : lineas) pw.println(l);
        } catch (Exception e) {
            System.err.println("Error escribiendo archivo: " + ruta + " - " + e.getMessage());
        }
    }

    // proximo id (para usuarios y logs)
    public static int proximoId(String ruta) {
        ArrayList<String> lineas = leerLineas(ruta);
        int max = 0;
        for (String l : lineas) {
            String[] partes = l.split("\\" + SEP);
            if (partes.length > 0) {
                try {
                    int id = Integer.parseInt(partes[0].trim());
                    if (id > max) max = id;
                } catch (Exception e) {}
            }
        }
        return max + 1;
    }
}