package red;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

// Servidor simple para la aplicacion
public class Server {

    private static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("--- Servidor de Memorias Iniciado ---");
        System.out.println("Escuchando en el puerto: " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Esperamos una conexion nueva
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado");

                // Creamos un hilo para atender a este cliente
                Thread hilo = new Thread(new ClientHandler(clientSocket));
                hilo.start();
            }
        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }

    // Clase para manejar cada cliente por separado
    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.flush();

                while (true) {
                    // Leemos el comando que manda el cliente
                    String comando = (String) in.readObject();
                    System.out.println("Comando recibido: " + comando);

                    //-----------------Proceso Inicio de Sesion (recibe LOGIN, mail y pass, devuelve objeto Usuario)
                    if (comando.equals("LOGIN")) {
                        String mail = (String) in.readObject();
                        String pass = (String) in.readObject();
                        out.writeObject(UsuarioDAO.login(mail, pass));
                    } 
                    //-----------------Proceso Guardar Registro (recibe REGISTRAR, nombre, mail, pass y guarda en usuarios.txt)
                    else if (comando.equals("REGISTRAR")) {
                        String nom = (String) in.readObject();
                        String m = (String) in.readObject();
                        String p = (String) in.readObject();
                        out.writeObject(UsuarioDAO.registrar(nom, m, p));
                    }
                    //-----------------Proceso Consulta Memorias Publicas (lee memorias.txt y filtra estado==1)
                    else if (comando.equals("GET_MEMORIAS_PUBLICAS")) {
                        out.writeObject(UsuarioDAO.obtenerMemoriasPublicas());
                    }
                    //-----------------Proceso Consulta Memorias de Perfil (filtra por idUsuario; soloP=true → publicas, false → privadas)
                    else if (comando.equals("GET_MEMORIAS_USUARIO")) {
                        int idU = (int) in.readObject();
                        boolean soloP = (boolean) in.readObject();
                        out.writeObject(UsuarioDAO.obtenerMemoriasDeUsuario(idU, soloP));
                    }
                    //-----------------Proceso Alta de Memoria (guarda linea en memorias.txt con id|fecha|titulo|contenido(img)|desc|idUsuario|estado)
                    else if (comando.equals("CREAR_MEMORIA")) {
                        String tit = (String) in.readObject();
                        String cont = (String) in.readObject();
                        String desc = (String) in.readObject();
                        int idUser = (int) in.readObject();
                        boolean esPub = (boolean) in.readObject();
                        out.writeObject(UsuarioDAO.crearMemoria(tit, cont, desc, idUser, esPub));
                    }
                    //-----------------Proceso Dar de Baja Memoria (cambia ultimo campo a estado 3 en memorias.txt)
                    else if (comando.equals("ELIMINAR_MEMORIA")) {
                        int idM = (int) in.readObject();
                        int idU = (int) in.readObject();
                        out.writeObject(UsuarioDAO.darDeBajaMemoria(idM, idU));
                    }
                    //-----------------Proceso Modificacion de Usuario (reescribe la linea del usuario con nueva contrasena)
                    else if (comando.equals("CAMBIAR_PASS")) {
                        int idU = (int) in.readObject();
                        String act = (String) in.readObject();
                        String nue = (String) in.readObject();
                        out.writeObject(UsuarioDAO.cambiarContrasena(idU, act, nue));
                    }
                    //-----------------Proceso Eliminacion de Usuario (borra linea de usuarios.txt y todas sus memorias de memorias.txt)
                    else if (comando.equals("ELIMINAR_CUENTA")) {
                        int idU = (int) in.readObject();
                        String p = (String) in.readObject();
                        out.writeObject(UsuarioDAO.eliminarCuenta(idU, p));
                    }
                    else if (comando.equals("SEARCH_USERS")) {
                        String q = (String) in.readObject();
                        out.writeObject(UsuarioDAO.buscarUsuariosByName(q));
                    }
                    //-----------------Proceso Guardar Imagen en Servidor (recibe bytes y guarda en carpeta uploads/ con nombre unico)
                    else if (comando.equals("SUBIR_IMAGEN")) {
                        String fileName = (String) in.readObject();
                        long fileSize = (long) in.readObject();
                        File dir = new File("uploads");
                        if (!dir.exists()) dir.mkdirs();
                        File destino = new File(dir, fileName);
                        try (FileOutputStream fos = new FileOutputStream(destino)) {
                            byte[] buffer = new byte[4096];
                            long r = fileSize;
                            int read;
                            while (r > 0 && (read = socket.getInputStream().read(buffer, 0, (int) Math.min(buffer.length, r))) != -1) {
                                fos.write(buffer, 0, read);
                                r -= read;
                            }
                        }
                        out.writeObject(true);
                    }
                    //-----------------Proceso Descargar Imagen (lee de uploads/ y envia bytes al cliente; cliente lo guarda en uploads_cache/)
                    else if (comando.equals("DESCARGAR_IMAGEN")) {
                        String file = (String) in.readObject();
                        File f = new File("uploads", file);
                        if (f.exists()) {
                            out.writeObject(true);
                            out.writeObject(f.length());
                            out.flush();
                            try (FileInputStream fis = new FileInputStream(f)) {
                                byte[] buf = new byte[4096];
                                int r;
                                while ((r = fis.read(buf)) != -1) {
                                    socket.getOutputStream().write(buf, 0, r);
                                }
                                socket.getOutputStream().flush();
                            }
                        } else {
                            out.writeObject(false);
                        }
                    }
                    else if (comando.equals("EXIT")) {
                        break;
                    }

                    out.flush();
                    out.reset();
                }
            } catch (Exception e) {
                System.out.println("Cliente desconectado");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // ignorar
                }
            }
        }
    }
}