import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import red.Cliente;
import red.UsuarioDAO;

public class PantallaCambiarContrasena extends JFrame {

    private static final Color BG_COLOR      = new Color(0x1E1B4B);
    private static final Color TEXT_MAIN     = new Color(0xF8FAFC);
    private static final Color TEXT_GRAY     = new Color(0xE4E4E4);
    private static final Color ACCENT_COLOR  = new Color(0x9999FF);
    private static final Color DIVIDER_COLOR = new Color(0x9999FF);
    private static final Color BTN_ROJO      = new Color(0x522123);
    private static final Color BTN_ROJO_DARK = new Color(0x2D1314);

    private JPasswordField actualField;
    private JPasswordField nuevaField;
    private JPasswordField confirmarField;

    private JLabel errorActual;
    private JLabel errorNueva;
    private JLabel errorConfirmar;

    private final UsuarioDAO.Usuario usuario;

    // ─── PantallaCambiarContrasena ─────────────────
    public PantallaCambiarContrasena(JFrame parent, UsuarioDAO.Usuario usuario) {
        if (usuario == null) {
            JOptionPane.showMessageDialog(null, "Error: Sesión no válida.");
            dispose();
            new LoginPanel();
            this.usuario = null;
            return;
        }
        this.usuario = usuario;
        
        setTitle("Cambiar contraseña");
        setSize(1920, 1080);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                if (parent != null) parent.setVisible(true);
            }
            @Override public void mouseEntered(MouseEvent e) { back.setForeground(ACCENT_COLOR); }
            @Override public void mouseExited(MouseEvent e)  { back.setForeground(TEXT_MAIN); }
        });

        header.add(back);

        JPanel divider = new JPanel();
        divider.setBackground(DIVIDER_COLOR);
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
        content.setPreferredSize(new Dimension(700, 600));

        JLabel titulo = new JLabel("Cambiar contraseña");
        titulo.setForeground(TEXT_MAIN);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 42));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel(
            "<html><div style='text-align:center; width:520px;'>"
            + "En caso de no recordar tu contraseña o mejorar tu seguridad.<br>"
            + "Te recomendamos que no utilices la misma contraseña que uses en otras cuentas."
            + "</div></html>"
        );
        desc.setForeground(TEXT_GRAY);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 15));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(Box.createVerticalStrut(40));
        content.add(titulo);
        content.add(Box.createVerticalStrut(10));
        content.add(desc);
        content.add(Box.createVerticalStrut(40));

        actualField = createField("Contraseña*");
        errorActual = errorLabel();

        nuevaField = createField("Nueva contraseña*");
        errorNueva = errorLabel();

        confirmarField = createField("Confirmar nueva contraseña*");
        errorConfirmar = errorLabel();

        content.add(actualField);
        content.add(errorActual);
        content.add(Box.createVerticalStrut(10));

        content.add(nuevaField);
        content.add(errorNueva);
        content.add(Box.createVerticalStrut(10));

        content.add(confirmarField);
        content.add(errorConfirmar);
        content.add(Box.createVerticalStrut(20));

        JButton btn = redButton("Cambiar");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.addActionListener(e -> validar());

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.setOpaque(false);
        panelBoton.setMaximumSize(new Dimension(420, 60));

        btn.setPreferredSize(new Dimension(180, 50));

        panelBoton.add(btn);
        content.add(panelBoton);

        wrapper.add(content);
        return wrapper;
    }

    // ─── validar ─────────────────
    private void validar() {
        boolean ok = true;

        String actual = new String(actualField.getPassword());
        String nueva = new String(nuevaField.getPassword());
        String confirmar = new String(confirmarField.getPassword());

        limpiarErrores();

        if (actual.isEmpty() || actual.equals("Contraseña*")) {
            errorActual.setText("Este campo no puede estar vacío");
            ok = false;
        }

        if (nueva.isEmpty() || nueva.equals("Nueva contraseña*")) {
            errorNueva.setText("Este campo no puede estar vacío");
            ok = false;
        } else if (nueva.length() < 8) {
            errorNueva.setText("La nueva contraseña debe tener al menos 8 caracteres");
            ok = false;
        }

        if (confirmar.isEmpty() || confirmar.equals("Confirmar nueva contraseña*")) {
            errorConfirmar.setText("Este campo no puede estar vacío");
            ok = false;
        }

        if (!nueva.matches(".*[^a-zA-Z0-9].*")) {
            errorNueva.setText("Debe contener un carácter especial. Ej: @$!%*?&/-_.");
            return;
        }

        if (!nueva.equals(confirmar)) {
            errorConfirmar.setText("Las contraseñas no coinciden");
            return;
        }

        UsuarioDAO.Resultado res = Cliente.cambiarContrasena(usuario.idUsuario, actual, nueva);

        if (res.ok) {
            JOptionPane.showMessageDialog(this, "Contraseña cambiada con éxito. Por favor, iniciá sesión nuevamente.");
            Window[] windows = Window.getWindows();
            for (Window w : windows) {
                w.dispose();
            }
            new LoginPanel();
        } else {
            if (res.mensaje.contains("Contraseña actual incorrecta")) {
                errorActual.setText("Contraseña actual incorrecta");
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + res.mensaje);
            }
        }
    }

    // ─── limpiarErrores ─────────────────
    private void limpiarErrores() {
        errorActual.setText("");
        errorNueva.setText("");
        errorConfirmar.setText("");
    }

    // ─── createField ─────────────────
    private JPasswordField createField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0xD9D9D9));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        field.setMaximumSize(new Dimension(500, 45));
        field.setForeground(Color.GRAY);
        field.setEchoChar((char) 0);
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
                    field.setEchoChar((char) 0);
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
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(500, 20));
        return l;
    }

    // ─── redButton ─────────────────
    private JButton redButton(String text) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(getModel().isRollover() ? BTN_ROJO : BTN_ROJO_DARK);
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),10,10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(TEXT_MAIN);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}