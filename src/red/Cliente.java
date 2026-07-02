package red;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

// Clase para que el programa se comunique con el servidor
public class Cliente {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    // Conectarse al servidor si no lo esta
    public static boolean conectar() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(HOST, PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
            }
            return true;
        } catch (Exception e) {
            System.out.println("No se pudo conectar: " + e.getMessage());
            return false;
        }
    }

    // Cerrar la conexion
    public static void desconectar() {
        try {
            if (socket != null) {
                out.writeObject("EXIT");
                socket.close();
            }
        } catch (Exception e) {}
        socket = null;
    }

    // --- Metodos para pedir cosas al servidor ---

    public static UsuarioDAO.Usuario login(String mail, String pass) {
        if (!conectar()) return null;
        try {
            out.writeObject("LOGIN");
            out.writeObject(mail);
            out.writeObject(pass);
            out.flush();
            return (UsuarioDAO.Usuario) in.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public static UsuarioDAO.Resultado registrar(String nom, String m, String p) {
        if (!conectar()) return new UsuarioDAO.Resultado(false, "Error de conexion");
        try {
            out.writeObject("REGISTRAR");
            out.writeObject(nom);
            out.writeObject(m);
            out.writeObject(p);
            out.flush();
            return (UsuarioDAO.Resultado) in.readObject();
        } catch (Exception e) {
            return new UsuarioDAO.Resultado(false, "Error en el registro");
        }
    }

    @SuppressWarnings("unchecked")
    public static List<UsuarioDAO.Memoria> obtenerMemoriasPublicas() {
        if (!conectar()) return new ArrayList<>();
        try {
            out.writeObject("GET_MEMORIAS_PUBLICAS");
            out.flush();
            return (List<UsuarioDAO.Memoria>) in.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<UsuarioDAO.Memoria> obtenerMemoriasDeUsuario(int idU, boolean soloP) {
        if (!conectar()) return new ArrayList<>();
        try {
            out.writeObject("GET_MEMORIAS_USUARIO");
            out.writeObject(idU);
            out.writeObject(soloP);
            out.flush();
            return (List<UsuarioDAO.Memoria>) in.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static UsuarioDAO.Resultado crearMemoria(String tit, String cont, String desc, int idUser, boolean esPub) {
        if (!conectar()) return new UsuarioDAO.Resultado(false, "Error de conexion");
        try {
            out.writeObject("CREAR_MEMORIA");
            out.writeObject(tit);
            out.writeObject(cont);
            out.writeObject(desc);
            out.writeObject(idUser);
            out.writeObject(esPub);
            out.flush();
            return (UsuarioDAO.Resultado) in.readObject();
        } catch (Exception e) {
            return new UsuarioDAO.Resultado(false, "Error al crear");
        }
    }

    public static UsuarioDAO.Resultado darDeBajaMemoria(int idM, int idU) {
        if (!conectar()) return new UsuarioDAO.Resultado(false, "Error de conexion");
        try {
            out.writeObject("ELIMINAR_MEMORIA");
            out.writeObject(idM);
            out.writeObject(idU);
            out.flush();
            return (UsuarioDAO.Resultado) in.readObject();
        } catch (Exception e) {
            return new UsuarioDAO.Resultado(false, "Error al borrar");
        }
    }

    public static UsuarioDAO.Resultado cambiarContrasena(int idU, String act, String nue) {
        if (!conectar()) return new UsuarioDAO.Resultado(false, "Error de conexion");
        try {
            out.writeObject("CAMBIAR_PASS");
            out.writeObject(idU);
            out.writeObject(act);
            out.writeObject(nue);
            out.flush();
            return (UsuarioDAO.Resultado) in.readObject();
        } catch (Exception e) {
            return new UsuarioDAO.Resultado(false, "Error al cambiar");
        }
    }

    public static UsuarioDAO.Resultado eliminarCuenta(int idU, String pass) {
        if (!conectar()) return new UsuarioDAO.Resultado(false, "Error de conexion");
        try {
            out.writeObject("ELIMINAR_CUENTA");
            out.writeObject(idU);
            out.writeObject(pass);
            out.flush();
            return (UsuarioDAO.Resultado) in.readObject();
        } catch (Exception e) {
            return new UsuarioDAO.Resultado(false, "Error al borrar cuenta");
        }
    }

    @SuppressWarnings("unchecked")
    public static List<UsuarioDAO.Usuario> buscarUsuarios(String query) {
        if (!conectar()) return new ArrayList<>();
        try {
            out.writeObject("SEARCH_USERS");
            out.writeObject(query);
            out.flush();
            return (List<UsuarioDAO.Usuario>) in.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void subirImagen(File file, String remoteName) {
        if (!conectar()) return;
        try {
            out.writeObject("SUBIR_IMAGEN");
            out.writeObject(remoteName);
            out.writeObject(file.length());
            out.flush();

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                socket.getOutputStream().write(buffer, 0, read);
            }
            socket.getOutputStream().flush();
            fis.close();
            
            in.readObject(); // esperar confirmacion
        } catch (Exception e) {}
    }

    public static File descargarImagen(String remoteName) {
        if (!conectar()) return null;
        try {
            out.writeObject("DESCARGAR_IMAGEN");
            out.writeObject(remoteName);
            out.flush();

            boolean existe = (boolean) in.readObject();
            if (!existe) return null;

            long size = (long) in.readObject();
            File cache = new File("uploads_cache");
            if (!cache.exists()) cache.mkdirs();
            File local = new File(cache, remoteName);
            
            FileOutputStream fos = new FileOutputStream(local);
            byte[] buffer = new byte[4096];
            long r = size;
            int read;
            while (r > 0 && (read = socket.getInputStream().read(buffer, 0, (int)Math.min(buffer.length, r))) != -1) {
                fos.write(buffer, 0, read);
                r -= read;
            }
            fos.close();
            return local;
        } catch (Exception e) {
            return null;
        }
    }
}
