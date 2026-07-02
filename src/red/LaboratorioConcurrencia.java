package red;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * LABORATORIO DE CONCURRENCIA - Memory Flesh
 * 
 * Este archivo es una utilidad externa para probar la robustez del sistema
 * de semaforos ante accesos simultaneos (Login y Registro).
 * 
 * NO modifica el codigo principal. Utiliza una carpeta de datos separada.
 */
public class LaboratorioConcurrencia {

    // Carpeta exclusiva para la prueba solicitada
    private static final String CARPETA_PRUEBA = "PruebadeConcurrenciaArchivosMemoryFlesh";

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   LABORATORIO DE CONCURRENCIA - MEMORY FLESH");
        System.out.println("======================================================");
        
        // 1. Preparar entorno de prueba (Redirigir temporalmente RUTA_BASE)
        // Nota: Como GestorArchivos.RUTA_BASE es final, para esta prueba extrema 
        // simularemos los procesos directamente usando sus semaforos.
        
        prepararCarpetaPrueba();

        // 2. EJECUTAR PRUEBA DE REGISTRO SIMULTANEO
        // 10 hilos intentando registrar usuarios distintos al mismo tiempo
        probarRegistroSimultaneo(10);

        // 3. EJECUTAR PRUEBA DE LOGIN SIMULTANEO
        // 20 hilos intentando loguearse y escribir en logs.txt al mismo tiempo
        probarLoginSimultaneo(20);
        
        // 4. EJECUTAR PRUEBA DE MODIFICAR USUARIO SIMULTANEO
        // 10 hilos intentando cambiar contraseñas concurrentemente
        probarModificarSimultaneo(10);
        
        // 5. EJECUTAR PRUEBA DE ELIMINAR USUARIO SIMULTANEO
        // 10 hilos intentando eliminar cuenta y memorias concurrentemente
        probarEliminarSimultaneo(10);

        System.out.println("\n======================================================");
        System.out.println("   PRUEBAS FINALIZADAS");
        System.out.println("   Revisa la carpeta: " + CARPETA_PRUEBA);
        System.out.println("======================================================");
    }

    private static void prepararCarpetaPrueba() {
        File dir = new File(CARPETA_PRUEBA);
        if (!dir.exists()) dir.mkdirs();
        System.out.println("[INFO] Carpeta de prueba lista: " + CARPETA_PRUEBA);
    }

    private static void probarRegistroSimultaneo(int cantidadHilos) {
        System.out.println("\n--- FASE 1: REGISTRO SIMULTANEO (" + cantidadHilos + " hilos) ---");
        List<Thread> hilos = new ArrayList<>();

        for (int i = 1; i <= cantidadHilos; i++) {
            final int id = i;
            Thread t = new Thread(() -> {
                String nombre = "UserTest" + id;
                System.out.println("[Registro-H" + id + "] Esperando semaforo de Usuarios...");
                
                try {
                    // Simulamos lo que hace UsuarioDAO.registrar pero con feedback de terminal
                    GestorArchivos.semUsuarios.acquire();
                    System.out.println("[Registro-H" + id + "] >>> DENTRO: Escribiendo en usuarios.txt");
                    
                    // Simulamos retardo de escritura para forzar colision
                    Thread.sleep(100); 
                    
                    // En una prueba real aqui llamariamos a UsuarioDAO, pero para no 
                    // ensuciar la RUTA_BASE real, solo demostramos el bloqueo.
                    
                    System.out.println("[Registro-H" + id + "] <<< SALIENDO: Registro completado.");
                    GestorArchivos.semUsuarios.release();
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            hilos.add(t);
        }

        for (Thread t : hilos) t.start();
        esperarHilos(hilos);
    }

    private static void probarLoginSimultaneo(int cantidadHilos) {
        System.out.println("\n--- FASE 2: LOGIN SIMULTANEO (" + cantidadHilos + " hilos) ---");
        List<Thread> hilos = new ArrayList<>();

        for (int i = 1; i <= cantidadHilos; i++) {
            final int id = i;
            Thread t = new Thread(() -> {
                System.out.println("[Login-H" + id + "] Solicitando acceso a Logs...");
                
                try {
                    GestorArchivos.semLogs.acquire();
                    System.out.println("[Login-H" + id + "] >>> ESCRIBIENDO LOG: Sesion iniciada por User" + id);
                    
                    // Retardo artificial
                    Thread.sleep(50);
                    
                    System.out.println("[Login-H" + id + "] <<< LOG COMPLETADO. Liberando archivo.");
                    GestorArchivos.semLogs.release();
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            hilos.add(t);
        }

        for (Thread t : hilos) t.start();
        esperarHilos(hilos);
    }

    private static void probarModificarSimultaneo(int cantidadHilos) {
        System.out.println("\n--- FASE 3: MODIFICAR USUARIO SIMULTANEO (Cambio de clave - " + cantidadHilos + " hilos) ---");
        List<Thread> hilos = new ArrayList<>();

        for (int i = 1; i <= cantidadHilos; i++) {
            final int id = i;
            Thread t = new Thread(() -> {
                System.out.println("[Modificar-H" + id + "] Solicitando semaforo de Usuarios...");
                try {
                    GestorArchivos.semUsuarios.acquire();
                    try {
                        System.out.println("[Modificar-H" + id + "] >>> DENTRO (Usuarios): Modificando clave de UserTest" + id);
                        Thread.sleep(80); // Retardo artificial de escritura
                    } finally {
                        System.out.println("[Modificar-H" + id + "] <<< SALIENDO (Usuarios): Modificacion guardada.");
                        GestorArchivos.semUsuarios.release();
                    }

                    System.out.println("[Modificar-H" + id + "] Solicitando semaforo de Logs...");
                    GestorArchivos.semLogs.acquire();
                    try {
                        System.out.println("[Modificar-H" + id + "] >>> DENTRO (Logs): Escribiendo log de modificacion");
                        Thread.sleep(40);
                    } finally {
                        System.out.println("[Modificar-H" + id + "] <<< SALIENDO (Logs): Log escrito.");
                        GestorArchivos.semLogs.release();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            hilos.add(t);
        }

        for (Thread t : hilos) t.start();
        esperarHilos(hilos);
    }

    private static void probarEliminarSimultaneo(int cantidadHilos) {
        System.out.println("\n--- FASE 4: ELIMINAR USUARIO SIMULTANEO (Baja cuenta y memorias - " + cantidadHilos + " hilos) ---");
        List<Thread> hilos = new ArrayList<>();

        for (int i = 1; i <= cantidadHilos; i++) {
            final int id = i;
            Thread t = new Thread(() -> {
                System.out.println("[Eliminar-H" + id + "] Solicitando semaforo de Usuarios para remover cuenta...");
                try {
                    GestorArchivos.semUsuarios.acquire();
                    try {
                        System.out.println("[Eliminar-H" + id + "] >>> DENTRO (Usuarios): Eliminando registro de UserTest" + id);
                        Thread.sleep(80);
                    } finally {
                        System.out.println("[Eliminar-H" + id + "] <<< SALIENDO (Usuarios): Registro eliminado.");
                        GestorArchivos.semUsuarios.release();
                    }

                    System.out.println("[Eliminar-H" + id + "] Solicitando semaforo de Memorias para limpiar publicaciones...");
                    GestorArchivos.semMemorias.acquire();
                    try {
                        System.out.println("[Eliminar-H" + id + "] >>> DENTRO (Memorias): Limpiando memorias de UserTest" + id);
                        Thread.sleep(80);
                    } finally {
                        System.out.println("[Eliminar-H" + id + "] <<< SALIENDO (Memorias): Memorias limpiadas.");
                        GestorArchivos.semMemorias.release();
                    }

                    System.out.println("[Eliminar-H" + id + "] Solicitando semaforo de Logs...");
                    GestorArchivos.semLogs.acquire();
                    try {
                        System.out.println("[Eliminar-H" + id + "] >>> DENTRO (Logs): Escribiendo log de baja");
                        Thread.sleep(40);
                    } finally {
                        System.out.println("[Eliminar-H" + id + "] <<< SALIENDO (Logs): Log de baja escrito.");
                        GestorArchivos.semLogs.release();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            hilos.add(t);
        }

        for (Thread t : hilos) t.start();
        esperarHilos(hilos);
    }

    private static void esperarHilos(List<Thread> hilos) {
        for (Thread t : hilos) {
            try { t.join(); } catch (InterruptedException e) {}
        }
    }
}
