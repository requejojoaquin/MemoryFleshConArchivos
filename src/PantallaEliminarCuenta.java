import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import red.Cliente;
import red.UsuarioDAO;

public class PantallaEliminarCuenta extends JFrame {

    private static final Color BG_COLOR = new Color(0x1E1B4B);
    private static final Color TEXT_MAIN = new Color(0xF8FAFC);
    private static final Color TEXT_GRAY = new Color(0xE4E4E4);
    private static final Color DIVIDER_COLOR = new Color(0x9999FF);
    private static final Color BTN_ROJO = new Color(0x522123);
    private static final Color BTN_ROJO_DARK = new Color(0x2D1314);

    private JPasswordField passwordField;
    private JLabel errorPassword;

    private final UsuarioDAO.Usuario usuario;

    // ─── PantallaEliminarCuenta ─────────────────
    public PantallaEliminarCuenta(JFrame parent, UsuarioDAO.Usuario usuario) {
        if (usuario == null) {
            JOptionPane.showMessageDialog(null, "Error: Sesión no válida.");
            dispose();
            new LoginPanel();
            this.usuario = null;
            return;
        }
        this.usuario = usuario;
        
        setTitle("Eliminar cuenta");
        setSize(1920, 1080);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_COLOR);

        root.add(buildHeader(parent), BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
    }

    // ─── buildHeader ─────────────────
    private JPanel buildHeader(JFrame parent) {
        JPanel header = new JPanel(null);
        header.setBackground(BG_COLOR);
        header.setPreferredSize(new Dimension(0, 64));

        JLabel back = new JLabel("✕   Atrás");
        back.setForeground(TEXT_MAIN);
        back.setFont(new Font("SansSerif", Font.BOLD, 20));
        back.setBounds(32, 16, 140, 32);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        back.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
                if (parent != null) parent.setVisible(true);
            }
        });

        header.add(back);

        JPanel divider = new JPanel();
        divider.setBackground(DIVIDER_COLOR);
        divider.setBounds(0, 57, 1920, 4);
        header.add(divider);

        return header;
    }

    // ─── buildContent ─────────────────
    private JPanel buildContent() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_COLOR);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);
        content.setPreferredSize(new Dimension(600, 600));

        content.add(Box.createVerticalStrut(60));

        JLabel title = new JLabel("Eliminar cuenta");
        title.setForeground(TEXT_MAIN);
        title.setFont(new Font("SansSerif", Font.BOLD, 42));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);

        content.add(Box.createVerticalStrut(10));

        JLabel desc = new JLabel(
            "<html><div style='text-align:center; width:500px;'>"
            + "Tu perfil, fotos, comentarios y demás se eliminarán definitivamente"
            + "</div></html>"
        );
        desc.setForeground(TEXT_GRAY);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 16));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(desc);

        content.add(Box.createVerticalStrut(40));

        passwordField = createField("Contraseña*");
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(passwordField);

        errorPassword = errorLabel();
        content.add(errorPassword);

        content.add(Box.createVerticalStrut(5));

        JButton btnEliminar = redButton("Eliminar");
        btnEliminar.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnEliminar.addActionListener(e -> validar());

        JPanel panelBoton = new JPanel();
        panelBoton.setOpaque(false);
        panelBoton.add(btnEliminar);

        content.add(panelBoton);

        wrapper.add(content);
        return wrapper;
    }

    // ─── validar ─────────────────
    private void validar() {
        String pass = new String(passwordField.getPassword());
        errorPassword.setText("");

        if (pass.isEmpty() || pass.equals("Contraseña*")) {
            errorPassword.setText("Este campo no puede estar vacío");
            return;
        }

        if (pass.length() < 8) {
            errorPassword.setText("La contraseña debe tener al menos 8 caracteres");
            return;
        }

        if (!pass.matches(".*[^a-zA-Z0-9].*")) {
            errorPassword.setText("Debe contener un carácter especial. Ej: @$!%*?&/-_.");
            return;
        }

        mostrarConfirmacion();
    }

    // ─── mostrarConfirmacion ─────────────────
    private void mostrarConfirmacion() {
        JDialog dialog = new JDialog(this, true);
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(0x2E2A6E));
        panel.setBorder(BorderFactory.createLineBorder(DIVIDER_COLOR, 2));

        JLabel txt = new JLabel("¿Es seguro que quieres eliminar tu cuenta?");
        txt.setForeground(TEXT_MAIN);
        txt.setBounds(45, 40, 320, 30);
        txt.setFont(new Font("SansSerif", Font.BOLD, 15));
        panel.add(txt);

        JButton confirmarBtn = redButton("Confirmar");
        confirmarBtn.setBounds(135, 90, 120, 40);

        confirmarBtn.addActionListener(e -> {
            String pass = new String(passwordField.getPassword());
            UsuarioDAO.Resultado res = Cliente.eliminarCuenta(usuario.idUsuario, pass);
            if (res.ok) {
                JOptionPane.showMessageDialog(this, "Cuenta eliminada correctamente");
                dialog.dispose();
                dispose();
                
                Window[] windows = Window.getWindows();
                for (Window window : windows) {
                    window.dispose();
                }
                new LoginPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + res.mensaje);
                dialog.dispose();
            }
        });

        panel.add(confirmarBtn);

        JLabel cerrar = new JLabel("✕");
        cerrar.setForeground(TEXT_MAIN);
        cerrar.setBounds(380, 0, 30, 30);
        cerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        cerrar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
            }
        });

        panel.add(cerrar);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ─── createField ─────────────────
    private JPasswordField createField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0xD9D9D9));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        field.setMaximumSize(new Dimension(400, 45));
        field.setForeground(Color.GRAY);
        field.setEchoChar((char)0);
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setText(placeholder);
                    field.setEchoChar((char)0);
                    field.setForeground(Color.GRAY);
                }
            }
        });

        return field;
    }

    // ─── errorLabel ─────────────────
    private JLabel errorLabel() {
        JLabel l = new JLabel("", SwingConstants.CENTER);
        l.setForeground(new Color(0xFF4D4D));
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setMaximumSize(new Dimension(400, 20));
        l.setPreferredSize(new Dimension(400, 20));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    // ─── redButton ─────────────────
    private JButton redButton(String text) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(getModel().isRollover() ? BTN_ROJO : BTN_ROJO_DARK);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(TEXT_MAIN);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setPreferredSize(new Dimension(220, 55));
        btn.setMaximumSize(new Dimension(220, 55)); 
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}