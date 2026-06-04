package red;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// Clase para las operaciones de datos
public class UsuarioDAO {

    // clases
    public static class Usuario implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public int    idUsuario;
        public String nombre;
        public String mail;
        public int    idRol;
        public Usuario(int idUsuario, String nombre, String mail, int idRol) {
            this.idUsuario = idUsuario;
            this.nombre    = nombre;
            this.mail      = mail;
            this.idRol     = idRol;
        }
    }

    public static class Resultado implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public boolean ok;
        public String  mensaje;
        public int     id;
        public Resultado(boolean ok, String mensaje) {
            this.ok = ok; this.mensaje = mensaje; this.id = 0;
        }
        public Resultado(boolean ok, String mensaje, int id) {
            this.ok = ok; this.mensaje = mensaje; this.id = id;
        }
    }

    public static class Memoria implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public int    idMemoria;
        public String titulo;
        public String contenido;
        public String descripcion;
        public int    idUsuario;
        public String nombreUsuario;
        public int    idEstado;
        public Memoria(int idMemoria, String titulo, String contenido, String descripcion, int idUsuario, String nombreUsuario, int idEstado) {
            this.idMemoria = idMemoria; this.titulo = titulo; this.contenido = contenido; this.descripcion = descripcion;
            this.idUsuario = idUsuario; this.nombreUsuario = nombreUsuario; this.idEstado = idEstado;
        }
    }

    // metodos de ayuda
    private static String getNombreUsuario(int idUsuario) {
        try {
            // semaforos
            GestorArchivos.semUsuarios.acquire();
            ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
            for (String linea : lineas) {
                String[] partes = linea.split("\\" + GestorArchivos.SEP);
                if (partes.length >= 5) {
                    if (Integer.parseInt(partes[0].trim()) == idUsuario) {
                        GestorArchivos.semUsuarios.release();
                        return partes[1];
                    }
                }
            }
            GestorArchivos.semUsuarios.release();
        } catch (Exception e) { GestorArchivos.semUsuarios.release(); }
        return "Desconocido";
    }

    private static int getRolUsuario(int idUsuario) {
        try {
            // semaforos
            GestorArchivos.semUsuarios.acquire();
            ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
            for (String linea : lineas) {
                String[] partes = linea.split("\\" + GestorArchivos.SEP);
                if (partes.length >= 5) {
                    if (Integer.parseInt(partes[0].trim()) == idUsuario) {
                        GestorArchivos.semUsuarios.release();
                        return Integer.parseInt(partes[4].trim());
                    }
                }
            }
            GestorArchivos.semUsuarios.release();
        } catch (Exception e) { GestorArchivos.semUsuarios.release(); }
        return 2;
    }

    // logs
    public static void registrarLog(int idUsuario, String procedimiento, String detalle) {
        try {
            // semaforos
            GestorArchivos.semLogs.acquire();
            int nuevoId = GestorArchivos.proximoId(GestorArchivos.LOGS_FILE);
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String nuevaLinea = nuevoId + "|" + fecha + "|" + idUsuario + "|" + procedimiento + "|" + detalle;
            GestorArchivos.agregarLinea(GestorArchivos.LOGS_FILE, nuevaLinea);
            GestorArchivos.semLogs.release();
        } catch (Exception e) { GestorArchivos.semLogs.release(); }
    }

    // login
    public static Usuario login(String mail, String contrasena) {
        try {
            // semaforos
            GestorArchivos.semUsuarios.acquire();
            ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
            for (String linea : lineas) {
                String[] partes = linea.split("\\" + GestorArchivos.SEP);
                if (partes.length >= 5) {
                    if (partes[2].equalsIgnoreCase(mail.trim()) && partes[3].equals(contrasena)) {
                        int id = Integer.parseInt(partes[0].trim());
                        Usuario u = new Usuario(id, partes[1], partes[2], Integer.parseInt(partes[4].trim()));
                        GestorArchivos.semUsuarios.release();
                        registrarLog(id, "Login", partes[1] + " entro");
                        return u;
                    }
                }
            }
            GestorArchivos.semUsuarios.release();
        } catch (Exception e) { GestorArchivos.semUsuarios.release(); }
        return null;
    }

    // registro
    public static Resultado registrar(String nombre, String mail, String contrasena) {
        try {
            // semaforos
            GestorArchivos.semUsuarios.acquire();
            ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
            for (String linea : lineas) {
                String[] partes = linea.split("\\" + GestorArchivos.SEP);
                if (partes.length >= 3) {
                    if (partes[1].equalsIgnoreCase(nombre.trim())) { GestorArchivos.semUsuarios.release(); return new Resultado(false, "Nombre ya existe"); }
                    if (partes[2].equalsIgnoreCase(mail.trim())) { GestorArchivos.semUsuarios.release(); return new Resultado(false, "Mail ya existe"); }
                }
            }
            int nuevoId = GestorArchivos.proximoId(GestorArchivos.USUARIOS_FILE);
            GestorArchivos.agregarLinea(GestorArchivos.USUARIOS_FILE, nuevoId + "|" + nombre.trim() + "|" + mail.trim() + "|" + contrasena + "|2");
            GestorArchivos.semUsuarios.release();
            registrarLog(nuevoId, "Registro", nombre + " se anoto");
            return new Resultado(true, "Ok", nuevoId);
        } catch (Exception e) { GestorArchivos.semUsuarios.release(); }
        return new Resultado(false, "Error");
    }

    // cambio clave
    public static Resultado cambiarContrasena(int idUsuario, String actual, String nueva) {
        try {
            // semaforos
            GestorArchivos.semUsuarios.acquire();
            ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
            ArrayList<String> nuevas = new ArrayList<String>();
            boolean exito = false; String nombre = "";
            for (String l : lineas) {
                String[] p = l.split("\\" + GestorArchivos.SEP);
                if (p.length >= 5 && Integer.parseInt(p[0].trim()) == idUsuario) {
                    if (p[3].equals(actual)) { nuevas.add(p[0] + "|" + p[1] + "|" + p[2] + "|" + nueva + "|" + p[4]); exito = true; nombre = p[1]; }
                    else { GestorArchivos.semUsuarios.release(); return new Resultado(false, "Clave mal"); }
                } else nuevas.add(l);
            }
            if (exito) GestorArchivos.escribirTodo(GestorArchivos.USUARIOS_FILE, nuevas);
            GestorArchivos.semUsuarios.release();
            if (exito) { registrarLog(idUsuario, "Clave", nombre + " cambio clave"); return new Resultado(true, "Ok"); }
        } catch (Exception e) { GestorArchivos.semUsuarios.release(); }
        return new Resultado(false, "Error");
    }

    // borrar cuenta
    public static Resultado eliminarCuenta(int idUsuario, String contrasena) {
        try {
            // semaforos
            GestorArchivos.semUsuarios.acquire();
            ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
            ArrayList<String> nuevas = new ArrayList<String>();
            boolean borrado = false; String nombre = "";
            for (String l : lineas) {
                String[] p = l.split("\\" + GestorArchivos.SEP);
                if (p.length >= 5 && Integer.parseInt(p[0].trim()) == idUsuario) {
                    if (p[3].equals(contrasena)) { borrado = true; nombre = p[1]; }
                    else { GestorArchivos.semUsuarios.release(); return new Resultado(false, "Clave mal"); }
                } else nuevas.add(l);
            }
            if (borrado) GestorArchivos.escribirTodo(GestorArchivos.USUARIOS_FILE, nuevas);
            GestorArchivos.semUsuarios.release();
            if (borrado) {
                GestorArchivos.semMemorias.acquire();
                File dir = new File(GestorArchivos.RUTA_MEMORIAS);
                File[] files = dir.listFiles((d, name) -> name.endsWith(".mem"));
                if (files != null) {
                    for (File f : files) {
                        Memoria m = (Memoria) GestorArchivos.cargarObjeto(f);
                        if (m != null && m.idUsuario == idUsuario) {
                            f.delete();
                        }
                    }
                }
                GestorArchivos.semMemorias.release();
                registrarLog(idUsuario, "Borrar", nombre + " se fue");
                return new Resultado(true, "Ok");
            }
        } catch (Exception e) { GestorArchivos.semUsuarios.release(); GestorArchivos.semMemorias.release(); }
        return new Resultado(false, "Error");
    }

    // buscar
    public static ArrayList<Usuario> buscarUsuariosByName(String query) {
        ArrayList<Usuario> lista = new ArrayList<Usuario>();
        try {
            // semaforos
            GestorArchivos.semUsuarios.acquire();
            ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
            for (String l : lineas) {
                String[] p = l.split("\\" + GestorArchivos.SEP);
                if (p.length >= 5 && p[1].toLowerCase().contains(query.toLowerCase())) {
                    lista.add(new Usuario(Integer.parseInt(p[0].trim()), p[1], p[2], Integer.parseInt(p[4].trim())));
                }
            }
            GestorArchivos.semUsuarios.release();
        } catch (Exception e) { GestorArchivos.semUsuarios.release(); }
        return lista;
    }

    // memorias
    public static Resultado crearMemoria(String titulo, String contenido, String descripcion, int idUsuario, boolean esPublica) {
        try {
            GestorArchivos.semMemorias.acquire();
            int nuevoId = GestorArchivos.proximoIdMemoria();
            Memoria mem = new Memoria(nuevoId, titulo, contenido, (descripcion == null) ? "" : descripcion, idUsuario, getNombreUsuario(idUsuario), (esPublica ? 1 : 2));
            GestorArchivos.guardarObjeto(GestorArchivos.RUTA_MEMORIAS, nuevoId + ".mem", mem);
            GestorArchivos.semMemorias.release();
            registrarLog(idUsuario, "Memoria", "Nueva: " + titulo);
            return new Resultado(true, "Ok", nuevoId);
        } catch (Exception e) { GestorArchivos.semMemorias.release(); }
        return new Resultado(false, "Error");
    }

    public static ArrayList<Memoria> obtenerMemoriasPublicas() {
        ArrayList<Memoria> lista = new ArrayList<Memoria>();
        try {
            GestorArchivos.semMemorias.acquire();
            File dir = new File(GestorArchivos.RUTA_MEMORIAS);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".mem"));
            if (files != null) {
                for (File f : files) {
                    Memoria m = (Memoria) GestorArchivos.cargarObjeto(f);
                    if (m != null && m.idEstado == 1) {
                        lista.add(m);
                    }
                }
            }
            GestorArchivos.semMemorias.release();
        } catch (Exception e) { GestorArchivos.semMemorias.release(); }
        return lista;
    }

    public static ArrayList<Memoria> obtenerMemoriasDeUsuario(int idUsuario, boolean soloPublicas) {
        ArrayList<Memoria> lista = new ArrayList<Memoria>();
        try {
            GestorArchivos.semMemorias.acquire();
            File dir = new File(GestorArchivos.RUTA_MEMORIAS);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".mem"));
            int estBusq = soloPublicas ? 1 : 2;
            if (files != null) {
                for (File f : files) {
                    Memoria m = (Memoria) GestorArchivos.cargarObjeto(f);
                    if (m != null && m.idUsuario == idUsuario && m.idEstado == estBusq) {
                        lista.add(m);
                    }
                }
            }
            GestorArchivos.semMemorias.release();
        } catch (Exception e) { GestorArchivos.semMemorias.release(); }
        return lista;
    }

    public static Resultado darDeBajaMemoria(int idMemoria, int idUsuario) {
        try {
            int rol = getRolUsuario(idUsuario);
            GestorArchivos.semMemorias.acquire();
            File f = new File(GestorArchivos.RUTA_MEMORIAS, idMemoria + ".mem");
            if (f.exists()) {
                Memoria m = (Memoria) GestorArchivos.cargarObjeto(f);
                if (m != null && (m.idUsuario == idUsuario || rol == 1)) {
                    m.idEstado = 3; // Baja
                    GestorArchivos.guardarObjeto(GestorArchivos.RUTA_MEMORIAS, idMemoria + ".mem", m);
                    GestorArchivos.semMemorias.release();
                    registrarLog(idUsuario, "Baja", "Borro: " + idMemoria);
                    return new Resultado(true, "Ok");
                }
            }
            GestorArchivos.semMemorias.release();
        } catch (Exception e) { GestorArchivos.semMemorias.release(); }
        return new Resultado(false, "Error");
    }
}