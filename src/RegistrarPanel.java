import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import red.Cliente;
import red.UsuarioDAO;

public class RegistrarPanel extends JFrame {

    private static final Color BG_COLOR       = new Color(0x1E1B4B);
    private static final Color CARD_COLOR     = new Color(0x2D2A7A);
    private static final Color CARD_BORDER    = new Color(0x6157E8);
    private static final Color FIELD_BG       = new Color(0xE2E8F0);
    private static final Color PLACEHOLDER_FG = new Color(0x64748B);
    private static final Color TITLE_FG       = new Color(0xF8FAFC);
    private static final Color BUTTON_COLOR   = new Color(0x6157E8);
    private static final Color ERROR_COLOR    = new Color(0xFF4D4D);

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private JLabel usernameErrorLabel;
    private JLabel emailErrorLabel;
    private JLabel passwordErrorLabel;

    // ─── RegistrarPanel ─────────────────
    public RegistrarPanel() {
        setTitle("Register - Memory Flesh");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(BG_COLOR);

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JLabel mainTitle = new JLabel("¡Hola! bienvenido a Memory Flesh");
        mainTitle.setForeground(TITLE_FG);
        mainTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel card = createCard();

        wrapper.add(mainTitle);
        wrapper.add(Box.createVerticalStrut(24));
        wrapper.add(card);

        background.add(wrapper);
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
        card.setPreferredSize(new Dimension(640, 580));
        card.setBorder(BorderFactory.createEmptyBorder(30, 64, 30, 64));

        JLabel cardTitle = new JLabel("Registrarse");
        cardTitle.setForeground(TITLE_FG);
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        cardTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField        = createTextField("Nombre de usuario*");
        emailField           = createTextField("Correo electrónico*");
        passwordField        = createPasswordField("Contraseña*");
        confirmPasswordField = createPasswordField("Confirmar contraseña*");

        usernameErrorLabel = new JLabel(" ");
        usernameErrorLabel.setForeground(ERROR_COLOR);
        usernameErrorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        usernameErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailErrorLabel = new JLabel(" ");
        emailErrorLabel.setForeground(ERROR_COLOR);
        emailErrorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        emailErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordErrorLabel = new JLabel(" ");
        passwordErrorLabel.setForeground(ERROR_COLOR);
        passwordErrorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        passwordErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton createButton = createButton("Crear cuenta");
        createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createButton.addActionListener(e -> handleRegister());

        card.add(cardTitle);
        card.add(Box.createVerticalStrut(20));

        card.add(usernameField);
        card.add(Box.createVerticalStrut(4));
        card.add(usernameErrorLabel);
        card.add(Box.createVerticalStrut(4));

        card.add(emailField);
        card.add(Box.createVerticalStrut(4));
        card.add(emailErrorLabel);
        card.add(Box.createVerticalStrut(4));

        card.add(passwordField);
        card.add(Box.createVerticalStrut(12));
        card.add(confirmPasswordField);
        card.add(Box.createVerticalStrut(4));
        card.add(passwordErrorLabel);
        card.add(Box.createVerticalStrut(20));

        card.add(createButton);

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
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(0x1E1B4B));
                    if (field instanceof JPasswordField)
                        ((JPasswordField) field).setEchoChar('•');
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER_FG);
                    if (field instanceof JPasswordField)
                        ((JPasswordField) field).setEchoChar((char) 0);
                }
            }
        });

        if (field instanceof JPasswordField)
            ((JPasswordField) field).setEchoChar((char) 0);
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

    // ─── handleRegister ─────────────────
    private void handleRegister() {
        usernameErrorLabel.setText(" ");
        emailErrorLabel.setText(" ");
        passwordErrorLabel.setText(" ");

        boolean valid = true;

        String username = usernameField.getText().trim();
        if (username.equals("Nombre de usuario*") || username.isEmpty()) {
            usernameErrorLabel.setText("Este campo no puede estar vacío");
            valid = false;
        }

        String email = emailField.getText().trim();
        if (email.equals("Correo electrónico*") || email.isEmpty()) {
            emailErrorLabel.setText("Este campo no puede estar vacío");
            valid = false;
        } else if (!email.contains("@") || !email.contains(".com")) {
            emailErrorLabel.setText("Correo con formato inválido");
            valid = false;
        }

        String pass    = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        boolean passEmpty    = pass.equals("Contraseña*") || pass.isEmpty();
        boolean confirmEmpty = confirm.equals("Confirmar contraseña*") || confirm.isEmpty();

        if (passEmpty || confirmEmpty) {
            passwordErrorLabel.setText("Este campo no puede estar vacío");
            valid = false;
        } else if (pass.length() < 8) {
            passwordErrorLabel.setText("La contraseña debe tener al menos 8 caracteres");
            valid = false;
        } else if (!pass.equals(confirm)) {
            passwordErrorLabel.setText("Las contraseñas no coinciden");
            valid = false;
        } else if (!pass.matches(".*[^a-zA-Z0-9].*")) {
            passwordErrorLabel.setText("Debe contener un carácter especial. Ejemplo: @$!%*?&./-_");
            valid = false;
        }

        if (!valid) return;

        UsuarioDAO.Resultado resultado = Cliente.registrar(username, email, pass);

        if (resultado.ok) {
            JOptionPane.showMessageDialog(
                this,
                "¡Cuenta creada con éxito! Ya podés iniciar sesión.",
                "Registro exitoso",
                JOptionPane.INFORMATION_MESSAGE
            );
            dispose(); 
        } else {
            String mensaje = resultado.mensaje;

            if (mensaje.contains("usuario")) {
                usernameErrorLabel.setText(mensaje);
            } else if (mensaje.contains("correo") || mensaje.contains("mail")) {
                emailErrorLabel.setText(mensaje);
            } else {
                passwordErrorLabel.setText(mensaje);
            }
        }
    }

    // ─── main ─────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegistrarPanel::new);
    }
}