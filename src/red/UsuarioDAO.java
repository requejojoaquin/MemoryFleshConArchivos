package red;

import java.io.File;
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
            GestorArchivos.semUsuarios.acquire();
            try {
                ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
                for (String linea : lineas) {
                    String[] partes = linea.split("\\" + GestorArchivos.SEP);
                    if (partes.length >= 5) {
                        if (Integer.parseInt(partes[0].trim()) == idUsuario) {
                            return partes[1];
                        }
                    }
                }
            } finally {
                GestorArchivos.semUsuarios.release();
            }
        } catch (Exception e) {
            System.err.println("Error en getNombreUsuario: " + e.getMessage());
        }
        return "Desconocido";
    }

    private static int getRolUsuario(int idUsuario) {
        try {
            GestorArchivos.semUsuarios.acquire();
            try {
                ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
                for (String linea : lineas) {
                    String[] partes = linea.split("\\" + GestorArchivos.SEP);
                    if (partes.length >= 5) {
                        if (Integer.parseInt(partes[0].trim()) == idUsuario) {
                            return Integer.parseInt(partes[4].trim());
                        }
                    }
                }
            } finally {
                GestorArchivos.semUsuarios.release();
            }
        } catch (Exception e) {
            System.err.println("Error en getRolUsuario: " + e.getMessage());
        }
        return 2;
    }

    //-----------------Proceso Guardar Log de Inicio de Sesion
    // logs
    public static void registrarLog(int idUsuario, String procedimiento, String detalle) {
        try {
            GestorArchivos.semLogs.acquire();
            try {
                int nuevoId = GestorArchivos.proximoId(GestorArchivos.LOGS_FILE);
                String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String nuevaLinea = nuevoId + "|" + fecha + "|" + idUsuario + "|" + procedimiento + "|" + detalle;
                GestorArchivos.agregarLinea(GestorArchivos.LOGS_FILE, nuevaLinea);
            } finally {
                GestorArchivos.semLogs.release();
            }
        } catch (Exception e) {
            System.err.println("Error en registrarLog: " + e.getMessage());
        }
    }

    //-----------------Proceso Inicio de Sesion
    // login
    public static Usuario login(String mail, String contrasena) {
        Usuario u = null;
        try {
            GestorArchivos.semUsuarios.acquire();
            try {
                ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
                for (String linea : lineas) {
                    String[] partes = linea.split("\\" + GestorArchivos.SEP);
                    if (partes.length >= 5) {
                        if (partes[2].equalsIgnoreCase(mail.trim()) && partes[3].trim().equals(contrasena.trim())) {
                            int id = Integer.parseInt(partes[0].trim());
                            u = new Usuario(id, partes[1], partes[2], Integer.parseInt(partes[4].trim()));
                            break;
                        }
                    }
                }
            } finally {
                GestorArchivos.semUsuarios.release();
            }
            if (u != null) {
                registrarLog(u.idUsuario, "Login", u.nombre + " entro");
            }
        } catch (Exception e) { 
            System.err.println("Error en login: " + e.getMessage());
        }
        return u;
    }

    //-----------------Proceso Guardar Registro de Usuario
    // registro
    public static Resultado registrar(String nombre, String mail, String contrasena) {
        int nuevoId = -1;
        try {
            GestorArchivos.semUsuarios.acquire();
            try {
                ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
                for (String linea : lineas) {
                    String[] partes = linea.split("\\" + GestorArchivos.SEP);
                    if (partes.length >= 3) {
                        if (partes[1].equalsIgnoreCase(nombre.trim())) {
                            return new Resultado(false, "Nombre ya existe");
                        }
                        if (partes[2].equalsIgnoreCase(mail.trim())) {
                            return new Resultado(false, "Mail ya existe");
                        }
                    }
                }
                nuevoId = GestorArchivos.proximoId(GestorArchivos.USUARIOS_FILE);
                GestorArchivos.agregarLinea(GestorArchivos.USUARIOS_FILE, nuevoId + "|" + nombre.trim() + "|" + mail.trim() + "|" + contrasena.trim() + "|2");
            } finally {
                GestorArchivos.semUsuarios.release();
            }
            if (nuevoId != -1) {
                registrarLog(nuevoId, "Registro", nombre + " se anoto");
                return new Resultado(true, "Ok", nuevoId);
            }
        } catch (Exception e) { 
            System.err.println("Error en registro: " + e.getMessage());
        }
        return new Resultado(false, "Error");
    }

    //-----------------Proceso Modificacion de Usuario (Cambio de Contrasena)
    // cambio clave
    public static Resultado cambiarContrasena(int idUsuario, String actual, String nueva) {
        boolean exito = false;
        String nombre = "";
        try {
            GestorArchivos.semUsuarios.acquire();
            try {
                ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
                ArrayList<String> nuevas = new ArrayList<String>();
                for (String l : lineas) {
                    String[] p = l.split("\\" + GestorArchivos.SEP);
                    if (p.length >= 5 && Integer.parseInt(p[0].trim()) == idUsuario) {
                        if (p[3].equals(actual)) {
                            nuevas.add(p[0] + "|" + p[1] + "|" + p[2] + "|" + nueva + "|" + p[4]);
                            exito = true;
                            nombre = p[1];
                        } else {
                            return new Resultado(false, "Clave mal");
                        }
                    } else {
                        nuevas.add(l);
                    }
                }
                if (exito) {
                    GestorArchivos.escribirTodo(GestorArchivos.USUARIOS_FILE, nuevas);
                }
            } finally {
                GestorArchivos.semUsuarios.release();
            }
            if (exito) {
                registrarLog(idUsuario, "Clave", nombre + " cambio clave");
                return new Resultado(true, "Ok");
            }
        } catch (Exception e) {
            System.err.println("Error en cambiarContrasena: " + e.getMessage());
        }
        return new Resultado(false, "Error");
    }

    //-----------------Proceso Eliminacion de Usuario
    // borrar cuenta
    public static Resultado eliminarCuenta(int idUsuario, String contrasena) {
        boolean borrado = false;
        String nombre = "";
        try {
            GestorArchivos.semUsuarios.acquire();
            try {
                ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
                ArrayList<String> nuevas = new ArrayList<String>();
                for (String l : lineas) {
                    String[] p = l.split("\\" + GestorArchivos.SEP);
                    if (p.length >= 5 && Integer.parseInt(p[0].trim()) == idUsuario) {
                        if (p[3].equals(contrasena)) {
                            borrado = true;
                            nombre = p[1];
                        } else {
                            return new Resultado(false, "Clave mal");
                        }
                    } else {
                        nuevas.add(l);
                    }
                }
                if (borrado) {
                    GestorArchivos.escribirTodo(GestorArchivos.USUARIOS_FILE, nuevas);
                }
            } finally {
                GestorArchivos.semUsuarios.release();
            }

            if (borrado) {
                GestorArchivos.semMemorias.acquire();
                try {
                    ArrayList<String> mems = GestorArchivos.leerLineas(GestorArchivos.MEMORIAS_FILE);
                    ArrayList<String> nuevasMems = new ArrayList<String>();
                    for (String m : mems) {
                        String[] pm = m.split("\\" + GestorArchivos.SEP);
                        if (pm.length >= 6 && Integer.parseInt(pm[5].trim()) != idUsuario) {
                            nuevasMems.add(m);
                        }
                    }
                    GestorArchivos.escribirTodo(GestorArchivos.MEMORIAS_FILE, nuevasMems);
                } finally {
                    GestorArchivos.semMemorias.release();
                }
                registrarLog(idUsuario, "Borrar", nombre + " se fue");
                return new Resultado(true, "Ok");
            }
        } catch (Exception e) {
            System.err.println("Error en eliminarCuenta: " + e.getMessage());
        }
        return new Resultado(false, "Error");
    }

    // buscar
    public static ArrayList<Usuario> buscarUsuariosByName(String query) {
        ArrayList<Usuario> lista = new ArrayList<Usuario>();
        try {
            GestorArchivos.semUsuarios.acquire();
            try {
                ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.USUARIOS_FILE);
                for (String l : lineas) {
                    String[] p = l.split("\\" + GestorArchivos.SEP);
                    if (p.length >= 5 && p[1].toLowerCase().contains(query.toLowerCase())) {
                        lista.add(new Usuario(Integer.parseInt(p[0].trim()), p[1], p[2], Integer.parseInt(p[4].trim())));
                    }
                }
            } finally {
                GestorArchivos.semUsuarios.release();
            }
        } catch (Exception e) {
            System.err.println("Error en buscarUsuariosByName: " + e.getMessage());
        }
        return lista;
    }

    //-----------------Proceso Alta de Memoria
    // memorias
    public static Resultado crearMemoria(String titulo, String contenido, String descripcion, int idUsuario, boolean esPublica) {
        int nuevoId = -1;
        try {
            GestorArchivos.semMemorias.acquire();
            try {
                nuevoId = GestorArchivos.proximoId(GestorArchivos.MEMORIAS_FILE);
                String fecha_hr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String desc = (descripcion == null || descripcion.trim().isEmpty()) ? "Sin descripcion" : descripcion.trim();
                int idEstado = esPublica ? 1 : 2; // 1: Publico, 2: Privado
                String nuevaLinea = nuevoId + "|" + fecha_hr + "|" + titulo.trim() + "|" + contenido.trim() + "|" + desc + "|" + idUsuario + "|" + idEstado;
                GestorArchivos.agregarLinea(GestorArchivos.MEMORIAS_FILE, nuevaLinea);
            } finally {
                GestorArchivos.semMemorias.release();
            }
            if (nuevoId != -1) {
                String nombreU = getNombreUsuario(idUsuario);
                registrarLog(idUsuario, "Crear Memoria", nombreU + " creo - ID: " + nuevoId + " - Titulo: " + titulo + " - Desc: " + (descripcion == null ? "" : descripcion));
                return new Resultado(true, "Memoria creada con éxito", nuevoId);
            }
        } catch (Exception e) {
            System.err.println("Error al crear memoria: " + e.getMessage());
        }
        return new Resultado(false, "Error interno al guardar");
    }

    //-----------------Proceso Consulta de Memorias Publicas (Pantalla Principal)
    public static ArrayList<Memoria> obtenerMemoriasPublicas() {
        ArrayList<Memoria> lista = new ArrayList<Memoria>();
        try {
            ArrayList<String> lineas;
            GestorArchivos.semMemorias.acquire();
            try {
                lineas = GestorArchivos.leerLineas(GestorArchivos.MEMORIAS_FILE);
            } finally {
                GestorArchivos.semMemorias.release();
            }

            for (String l : lineas) {
                String[] p = l.split("\\" + GestorArchivos.SEP);
                if (p.length >= 7) {
                    int estado = Integer.parseInt(p[6].trim());
                    if (estado == 1) { // Solo las PUBLICAS
                        int idU = Integer.parseInt(p[5].trim());
                        lista.add(new Memoria(
                            Integer.parseInt(p[0].trim()), // id
                            p[2],                          // titulo
                            p[3],                          // contenido (img)
                            p[4],                          // descripcion
                            idU,                           // idUsuario
                            getNombreUsuario(idU),         // nombreUsuario
                            estado                         // estado
                        ));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener publicas: " + e.getMessage());
        }
        return lista;
    }

    //-----------------Proceso Consulta de Memorias de Perfil (Publicas y Privadas propias / Publicas de otros)
    public static ArrayList<Memoria> obtenerMemoriasDeUsuario(int idUsuario, boolean soloPublicas) {
        ArrayList<Memoria> lista = new ArrayList<Memoria>();
        try {
            ArrayList<String> lineas;
            GestorArchivos.semMemorias.acquire();
            try {
                lineas = GestorArchivos.leerLineas(GestorArchivos.MEMORIAS_FILE);
            } finally {
                GestorArchivos.semMemorias.release();
            }

            String nombre = getNombreUsuario(idUsuario);
            for (String l : lineas) {
                String[] p = l.split("\\" + GestorArchivos.SEP);
                if (p.length >= 7) {
                    int idU = Integer.parseInt(p[5].trim());
                    int est = Integer.parseInt(p[6].trim());
                    if (idU == idUsuario) {
                        if (soloPublicas && est == 1) {
                            lista.add(new Memoria(Integer.parseInt(p[0].trim()), p[2], p[3], p[4], idU, nombre, est));
                        } else if (!soloPublicas && est == 2) {
                            lista.add(new Memoria(Integer.parseInt(p[0].trim()), p[2], p[3], p[4], idU, nombre, est));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener memorias de usuario: " + e.getMessage());
        }
        return lista;
    }

    //-----------------Proceso Dar de Baja Memoria (Eliminacion logica - cambia estado a 3)
    public static Resultado darDeBajaMemoria(int idMemoria, int idUsuario) {
        try {
            int rol = getRolUsuario(idUsuario);
            boolean exito = false;
            GestorArchivos.semMemorias.acquire();
            try {
                ArrayList<String> lineas = GestorArchivos.leerLineas(GestorArchivos.MEMORIAS_FILE);
                ArrayList<String> nuevas = new ArrayList<String>();
                for (String l : lineas) {
                    String[] p = l.split("\\" + GestorArchivos.SEP);
                    if (p.length >= 7 && Integer.parseInt(p[0].trim()) == idMemoria) {
                        int dueno = Integer.parseInt(p[5].trim());
                        if (dueno == idUsuario || rol == 1) {
                            nuevas.add(p[0] + "|" + p[1] + "|" + p[2] + "|" + p[3] + "|" + p[4] + "|" + p[5] + "|3");
                            exito = true;
                        } else {
                            return new Resultado(false, "No permiso");
                        }
                    } else {
                        nuevas.add(l);
                    }
                }
                if (exito) {
                    GestorArchivos.escribirTodo(GestorArchivos.MEMORIAS_FILE, nuevas);
                }
            } finally {
                GestorArchivos.semMemorias.release();
            }
            if (exito) {
                registrarLog(idUsuario, "Baja", "Borro: " + idMemoria);
                return new Resultado(true, "Ok");
            }
        } catch (Exception e) {
            System.err.println("Error en darDeBajaMemoria: " + e.getMessage());
        }
        return new Resultado(false, "Error");
    }
}
