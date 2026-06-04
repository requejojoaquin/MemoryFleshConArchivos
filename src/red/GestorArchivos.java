package red;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

// Clase para manejar los archivos
public class GestorArchivos {

    // archivos
    public static final String RUTA_BASE      = "Datos";
    public static final String USUARIOS_FILE  = RUTA_BASE + "/usuarios.txt";
    public static final String LOGS_FILE      = RUTA_BASE + "/logs.txt";
    public static final String RUTA_MEMORIAS  = RUTA_BASE + "/Memorias";
    public static final String SEP = "|";

    // semaforos
    public static Semaphore semUsuarios = new Semaphore(1, true);
    public static Semaphore semLogs     = new Semaphore(1, true);
    public static Semaphore semMemorias = new Semaphore(1, true); // Mantener para sincronizar acceso a carpeta

    static {
        File carpeta = new File(RUTA_BASE);
        if (!carpeta.exists()) carpeta.mkdirs();
        File carpetaMem = new File(RUTA_MEMORIAS);
        if (!carpetaMem.exists()) carpetaMem.mkdirs();
        
        crearArchivoSiNoExiste(USUARIOS_FILE);
        crearArchivoSiNoExiste(LOGS_FILE);
    }

    private static void crearArchivoSiNoExiste(String ruta) {
        try {
            File archivo = new File(ruta);
            if (!archivo.exists()) archivo.createNewFile();
        } catch (Exception e) {}
    }

    // leer lineas (para usuarios y logs)
    public static ArrayList<String> leerLineas(String ruta) {
        ArrayList<String> lineas = new ArrayList<String>();
        try {
            File archivo = new File(ruta);
            if (archivo.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(archivo));
                String linea;
                while ((linea = br.readLine()) != null) {
                    if (!linea.trim().isEmpty()) lineas.add(linea);
                }
                br.close();
            }
        } catch (Exception e) {}
        return lineas;
    }

    // agregar linea
    public static void agregarLinea(String ruta, String nuevaLinea) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(ruta, true)));
            pw.println(nuevaLinea);
            pw.close();
        } catch (Exception e) {}
    }

    // escribir todo
    public static void escribirTodo(String ruta, ArrayList<String> lineas) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(ruta)));
            for (String l : lineas) pw.println(l);
            pw.close();
        } catch (Exception e) {}
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

    // --- Metodos para Memorias (Objetos Serializados) ---

    public static void guardarObjeto(String folder, String filename, Object obj) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(folder, filename)));
            oos.writeObject(obj);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object cargarObjeto(File file) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (Exception e) {
            return null;
        }
    }
    
    public static int proximoIdMemoria() {
        File dir = new File(RUTA_MEMORIAS);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".mem"));
        int max = 0;
        if (files != null) {
            for (File f : files) {
                try {
                    int id = Integer.parseInt(f.getName().replace(".mem", ""));
                    if (id > max) max = id;
                } catch (Exception e) {}
            }
        }
        return max + 1;
    }
}