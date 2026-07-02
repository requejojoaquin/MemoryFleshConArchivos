import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import red.Cliente;
import red.UsuarioDAO;

public class LoginPanel extends JFrame {

    private static final Color BG_COLOR       = new Color(0x1E1B4B);
    private static final Color CARD_COLOR     = new Color(0x2D2A7A);
    private static final Color CARD_BORDER    = new Color(0x6157E8);
    private static final Color FIELD_BG       = new Color(0xE2E8F0);
    private static final Color PLACEHOLDER_FG = new Color(0x64748B);
    private static final Color TITLE_FG       = new Color(0xF8FAFC);
    private static final Color BUTTON_COLOR   = new Color(0x6157E8);
    private static final Color ERROR_COLOR    = new Color(0xFF4D4D);

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel emailErrorLabel;

    // ─── LoginPanel ─────────────────
    public LoginPanel() {
        setTitle("Login");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel background = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(BG_COLOR);
            }
        };
        background.setBackground(BG_COLOR);

        JPanel card = createCard();
        background.add(card);

        setContentPane(background);
        setVisible(true);
    }

    // ─── createCard ─────────────────
    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_COLOR);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 24, 24));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(640, 400));
        card.setBorder(BorderFactory.createEmptyBorder(48, 64, 48, 64));

        JLabel title1 = styledLabel("¿Qué tal?", 28f, Font.BOLD);
        JLabel title2 = styledLabel("¡Te estabamos esperando!", 28f, Font.BOLD);
        title1.setAlignmentX(Component.CENTER_ALIGNMENT);
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailField = createTextField("Correo electrónico*");
        passwordField = createPasswordField("Contraseña*");

        emailErrorLabel = new JLabel(" ");
        emailErrorLabel.setForeground(ERROR_COLOR);
        emailErrorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        emailErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = createButton("Iniciar sesion");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());

        JLabel firstTime = styledLabel("¿Es la primera vez que usas Memory flesh?", 13f, Font.PLAIN);
        firstTime.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel registerLink = new JLabel("Regístrate acá");
        registerLink.setForeground(TITLE_FG);
        registerLink.setFont(new Font("SansSerif", Font.BOLD, 13));
        registerLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TITLE_FG));
        registerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegistrarPanel();
            }
        });

        card.add(title1);
        card.add(Box.createVerticalStrut(4));
        card.add(title2);
        card.add(Box.createVerticalStrut(32));
        card.add(emailField);
        card.add(Box.createVerticalStrut(4));
        card.add(emailErrorLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(24));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(20));
        card.add(firstTime);
        card.add(Box.createVerticalStrut(6));
        card.add(registerLink);

        return card;
    }

    // ─── createTextField ─────────────────
    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleField(field, placeholder);
        return field;
    }

    // ─── createPasswordField ─────────────────
    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleField(field, placeholder);
        return field;
    }

    // ─── styleField ─────────────────
    private void styleField(JTextField field, String placeholder) {
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(PLACEHOLDER_FG);
        field.setCaretColor(new Color(0x1E1B4B));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);

        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(0x1E1B4B));
                    if (field instanceof JPasswordField) {
                        ((JPasswordField) field).setEchoChar('•');
                    }
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER_FG);
                    if (field instanceof JPasswordField) {
                        ((JPasswordField) field).setEchoChar((char) 0);
                    }
                }
            }
        });

        if (field instanceof JPasswordField) {
            ((JPasswordField) field).setEchoChar((char) 0);
        }
    }

    // ─── createButton ─────────────────
    private JButton createButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? BUTTON_COLOR.darker()
                         : getModel().isRollover() ? BUTTON_COLOR.brighter()
                         : BUTTON_COLOR;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setPreferredSize(new Dimension(220, 44));
        button.setMaximumSize(new Dimension(220, 44));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // ─── styledLabel ─────────────────
    private JLabel styledLabel(String text, float size, int style) {
        JLabel label = new JLabel(text);
        label.setForeground(TITLE_FG);
        label.setFont(new Font("SansSerif", style, (int) size));
        return label;
    }

    // ─── handleLogin ─────────────────
    private void handleLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());
        
        boolean valid = true;

        if (email.equals("Correo electrónico*") || email.isEmpty()) {
            emailErrorLabel.setText("Este campo no puede estar vacío");
            valid = false;
        } else if (!email.contains("@") || !email.contains(".com")) {
            emailErrorLabel.setText("Correo con formato inválido");
            valid = false;
        } else {
            emailErrorLabel.setText(" ");
        }
        
        if (pass.equals("Contraseña*") || pass.isEmpty()) {
            valid = false;
        }

        if (valid) {
            UsuarioDAO.Usuario usuario = Cliente.login(email, pass);
            
            if (usuario != null) {
                JOptionPane.showMessageDialog(this, "¡Bienvenido " + usuario.nombre + "!");
                dispose();
                new PantallaPrincipal(usuario);
                
            } else {
                emailErrorLabel.setText("Correo o contraseña incorrectos");
            }
        }
    }
}
