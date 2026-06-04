import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
 
import red.UsuarioDAO;

public class Pantallaajustes extends JFrame {
 
    private static final Color BG_COLOR      = new Color(0x1E1B4B);
    private static final Color ACCENT_COLOR  = new Color(0x9999FF);
    private static final Color TEXT_MAIN     = new Color(0xF8FAFC);
    private static final Color TEXT_DIM      = new Color(0xD9D9D9);
    private static final Color TEXT_GRAY     = new Color(0xE4E4E4);
    private static final Color DIVIDER_COLOR = new Color(0x9999FF);
    private static final Color BTN_ROJO      = new Color(0x522123);
    private static final Color BTN_ROJO_DARK = new Color(0x2D1314);
    private static final Color CARD_COLOR    = new Color(0x27226E);
 
    private final UsuarioDAO.Usuario usuario;
 
    // ─── Pantallaajustes ─────────────────
    public Pantallaajustes(JFrame parent, UsuarioDAO.Usuario usuario) {
        if (usuario == null) {
            JOptionPane.showMessageDialog(null, "Error: Sesión no válida.");
            dispose();
            new LoginPanel();
            this.usuario = null;
            return;
        }
        this.usuario = usuario;
 
        setTitle("Ajustes y seguridad - Memory Flesh");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
 
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_COLOR);
        root.add(buildHeader(parent), BorderLayout.NORTH);
        root.add(buildContent(),      BorderLayout.CENTER);
 
        setContentPane(root);
        setVisible(true);
    }
 
    // ─── buildHeader ─────────────────
    private JPanel buildHeader(JFrame parent) {
        JPanel header = new JPanel(null);
        header.setBackground(BG_COLOR);
        header.setPreferredSize(new Dimension(0, 64));
 
        JLabel btnAtras = new JLabel("✕   Atrás");
        btnAtras.setForeground(TEXT_MAIN);
        btnAtras.setFont(new Font("SansSerif", Font.BOLD, 20));
        btnAtras.setBounds(32, 16, 140, 32);
        btnAtras.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAtras.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                if (parent != null) parent.setVisible(true);
            }
            @Override public void mouseEntered(MouseEvent e) { btnAtras.setForeground(ACCENT_COLOR); }
            @Override public void mouseExited(MouseEvent e)  { btnAtras.setForeground(TEXT_MAIN); }
        });
        header.add(btnAtras);
 
        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(DIVIDER_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        divider.setBounds(0, 57, 1920, 6);
        header.add(divider);
 
        return header;
    }
 
    // ─── buildContent ─────────────────
    private JPanel buildContent() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_COLOR);
 
        JPanel content = new JPanel();
        content.setBackground(BG_COLOR);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setPreferredSize(new Dimension(860, 800));
        content.setBorder(BorderFactory.createEmptyBorder(48, 0, 48, 0));
 
        JLabel lblTitulo = new JLabel("Ajustes y seguridad");
        lblTitulo.setForeground(TEXT_MAIN);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 42));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblTitulo);
        content.add(Box.createVerticalStrut(10));
 
        JLabel lblSub = new JLabel("Gestionar datos de cuenta.");
        lblSub.setForeground(TEXT_MAIN);
        lblSub.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblSub);
        content.add(Box.createVerticalStrut(20));
 
        JPanel datosPanel = new JPanel(new GridLayout(2, 2, 60, 6));
        datosPanel.setBackground(BG_COLOR);
        datosPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        datosPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
        JLabel lblNomLabel   = smallLabel("Nombre de usuario");
        JLabel lblEmailLabel = smallLabel("Correo electrónico");
        JLabel lblNomVal     = valueLabel(usuario.nombre);
        JLabel lblEmailVal   = valueLabel(usuario.mail);
 
        datosPanel.add(lblNomLabel);
        datosPanel.add(lblEmailLabel);
        datosPanel.add(lblNomVal);
        datosPanel.add(lblEmailVal);
        content.add(datosPanel);
        content.add(Box.createVerticalStrut(28));
 
        content.add(buildDivider());
        content.add(Box.createVerticalStrut(24));
 
        JLabel lblCambiarT = sectionTitle("Cambiar contraseña");
        content.add(lblCambiarT);
        content.add(Box.createVerticalStrut(6));
 
        JLabel lblCambiarD = descLabel("En caso de no recordar tu contraseña o mejorar tu seguridad, te recomendamos que no utilices la misma contraseña que uses en otras cuentas.");
        content.add(lblCambiarD);
        content.add(Box.createVerticalStrut(14));
 
        JButton btnCambiar = redButton("Cambia contraseña");
        btnCambiar.addActionListener(e -> {
            setVisible(false);
            new PantallaCambiarContrasena(Pantallaajustes.this, usuario);
        });
        content.add(btnCambiar);
        content.add(Box.createVerticalStrut(28));
 
        content.add(buildDivider());
        content.add(Box.createVerticalStrut(24));
 
        JLabel lblCerrarT = sectionTitle("Cerrar sesión");
        content.add(lblCerrarT);
        content.add(Box.createVerticalStrut(6));
 
        JLabel lblCerrarD = descLabel("Recomendamos cerrar la sesión una vez hecho el cambio de contraseña para verificar que hayas puesto de manera correcta el nuevo cambio.");
        content.add(lblCerrarD);
        content.add(Box.createVerticalStrut(14));
 
        JButton btnCerrar = redButton("Cerrar sesión");
        btnCerrar.addActionListener(e -> {
            dispose();
            new LoginPanel();
        });
        content.add(btnCerrar);
        content.add(Box.createVerticalStrut(28));
 
        content.add(buildDivider());
        content.add(Box.createVerticalStrut(24));
 
        JLabel lblEliminarT = sectionTitle("Eliminar cuenta");
        content.add(lblEliminarT);
        content.add(Box.createVerticalStrut(6));
 
        JLabel lblEliminarD = descLabel("Tu perfil, fotos, comentarios y demás se eliminarán definitivamente.");
        content.add(lblEliminarD);
        content.add(Box.createVerticalStrut(14));
 
        JButton btnEliminar = redButton("Eliminar cuenta");
        btnEliminar.addActionListener(e -> {
            setVisible(false);
            new PantallaEliminarCuenta(Pantallaajustes.this, usuario);
        });
        content.add(btnEliminar);
 
        wrapper.add(content);
        return wrapper;
    }
 
    // ─── buildDivider ─────────────────
    private JPanel buildDivider() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(DIVIDER_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        d.setPreferredSize(new Dimension(0, 2));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        return d;
    }
 
    // ─── smallLabel ─────────────────
    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_DIM);
        l.setFont(new Font("SansSerif", Font.PLAIN, 15));
        return l;
    }
 
    // ─── valueLabel ─────────────────
    private JLabel valueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MAIN);
        l.setFont(new Font("SansSerif", Font.BOLD, 15));
        return l;
    }
 
    // ─── sectionTitle ─────────────────
    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MAIN);
        l.setFont(new Font("SansSerif", Font.BOLD, 26));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
 
    // ─── descLabel ─────────────────
    private JLabel descLabel(String text) {
        JLabel l = new JLabel("<html><body style='width:700px'>" + text + "</body></html>");
        l.setForeground(TEXT_GRAY);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
 
    // ─── redButton ─────────────────
    private JButton redButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BTN_ROJO : BTN_ROJO_DARK);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(TEXT_MAIN);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setPreferredSize(new Dimension(220, 44));
        btn.setMaximumSize(new Dimension(220, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
 
    // ─── main ─────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Pantallaajustes(null, null));
    }
}