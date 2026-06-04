import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import red.Cliente;
import red.UsuarioDAO;

public class PantallaPerfil extends JFrame {

    private static final Color BG_COLOR      = new Color(0x1E1B4B);
    private static final Color ACCENT_COLOR  = new Color(0x9999FF);
    private static final Color BUTTON_COLOR  = new Color(0x6157E8);
    private static final Color TEXT_MAIN     = new Color(0xF8FAFC);
    private static final Color TEXT_DIM      = new Color(0xD9D9D9);
    private static final Color DIVIDER_COLOR = new Color(0x9999FF);
    private static final Color CARD_COLOR    = new Color(0x27226E);
    private static final Color CARD_BORDER   = new Color(0x9999FF);
    private static final Color MENU_BG       = new Color(0x2E2A6E);
    private static final Color MENU_ELIMINAR = new Color(0x2D1314);
    private static final Color MENU_BTN      = new Color(0x6157E8);

    private final UsuarioDAO.Usuario usuario;
    private int cantPublicaciones = 0;
    private boolean viendoPublicas = true;
    private final boolean esPerfilPropio;
    private UsuarioDAO.Usuario usuarioLogueado;

    private JPanel menuDesplegable;
    private JPanel gridPanel;
    private JLabel lblCantidad;
    private JFrame parent;

    private boolean modoEliminar = false;
    private final java.util.Set<Integer> seleccionadas = new java.util.HashSet<>();

    // ─── PantallaPerfil ─────────────────
    public PantallaPerfil(JFrame parent, UsuarioDAO.Usuario usuarioVisualizado, UsuarioDAO.Usuario usuarioLogueado) {
        if (usuarioVisualizado == null) {
            JOptionPane.showMessageDialog(null, "Error: Sesión no válida.");
            dispose();
            new LoginPanel();
            this.usuario = null;
            this.esPerfilPropio = false;
            return;
        }
        this.usuario = usuarioVisualizado;
        this.usuarioLogueado = usuarioLogueado;
        this.esPerfilPropio = (usuarioLogueado != null && usuarioLogueado.idUsuario == usuarioVisualizado.idUsuario);
        this.parent = parent;
        
        setTitle("Perfil de " + usuario.nombre + " - Memory Flesh");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_COLOR);
        root.add(buildTop(parent), BorderLayout.NORTH);
        root.add(buildGrid(), BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);

        refreshGrid(true);
    }

    // ─── PantallaPerfil ─────────────────
    public PantallaPerfil(JFrame parent, UsuarioDAO.Usuario usuario) {
        this(parent, usuario, usuario);
    }

    // ─── buildTop ─────────────────
    private JPanel buildTop(JFrame parent) {
        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(1920, 420));
        layered.setBackground(BG_COLOR);
        layered.setOpaque(true);

        JPanel top = new JPanel(null);
        top.setBackground(BG_COLOR);
        top.setBounds(0, 0, 1920, 420);

        JLabel btnAtras = new JLabel("✕   Atrás");
        btnAtras.setForeground(TEXT_MAIN);
        btnAtras.setFont(new Font("SansSerif", Font.BOLD, 20));
        btnAtras.setBounds(32, 32, 140, 36);
        btnAtras.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAtras.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                if (parent instanceof PantallaPrincipal) {
                    ((PantallaPrincipal) parent).actualizarFeed();
                }
                if (parent != null) parent.setVisible(true);
            }
            @Override public void mouseEntered(MouseEvent e) { btnAtras.setForeground(ACCENT_COLOR); }
            @Override public void mouseExited(MouseEvent e)  { btnAtras.setForeground(TEXT_MAIN); }
        });
        top.add(btnAtras);

        int avatarSize = 160;
        int avatarX = (1920 - avatarSize) / 2;
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_COLOR);
                g2.setStroke(new BasicStroke(6f));
                g2.drawOval(4, 4, getWidth() - 8, getHeight() - 8);
                g2.setStroke(new BasicStroke(5f));
                int cx = getWidth() / 2;
                g2.drawOval(cx - 28, 26, 56, 56);
                g2.drawRoundRect(cx - 40, 94, 80, 46, 36, 36);
                g2.dispose();
            }
        };
        avatar.setBounds(avatarX, 50, avatarSize, avatarSize);
        avatar.setOpaque(false);
        top.add(avatar);

        JLabel lblNombre = new JLabel(usuario.nombre, SwingConstants.CENTER);
        lblNombre.setForeground(TEXT_MAIN);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 30));
        lblNombre.setBounds(760, 222, 400, 40);
        top.add(lblNombre);

        lblCantidad = new JLabel("0", SwingConstants.CENTER);
        lblCantidad.setForeground(TEXT_MAIN);
        lblCantidad.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblCantidad.setBounds(760, 268, 400, 30);
        top.add(lblCantidad);

        JLabel lblPubs = new JLabel("Publicaciones", SwingConstants.CENTER);
        lblPubs.setForeground(TEXT_DIM);
        lblPubs.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblPubs.setBounds(760, 298, 400, 24);
        top.add(lblPubs);

        int eyeCX = 1920 / 2;
        int eyeY  = 338;

        JPanel btnPublicas = eyeButton(false);
        JPanel btnPrivadas = eyeButton(true);

        btnPublicas.setBounds(eyeCX - 90, eyeY, 72, 54);
        btnPrivadas.setBounds(eyeCX + 18,  eyeY, 72, 54);

        updateEyeStyle(btnPublicas, true);
        updateEyeStyle(btnPrivadas, false);

        btnPublicas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPrivadas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnPublicas.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                viendoPublicas = true;
                updateEyeStyle(btnPublicas, true);
                updateEyeStyle(btnPrivadas, false);
                refreshGrid(true);
            }
        });

        if (esPerfilPropio) {
            btnPrivadas.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    viendoPublicas = false;
                    updateEyeStyle(btnPrivadas, true);
                    updateEyeStyle(btnPublicas, false);
                    refreshGrid(false);
                }
            });
            top.add(btnPrivadas);
        } else {
            btnPublicas.setBounds(eyeCX - 36, eyeY, 72, 54);
        }
        top.add(btnPublicas);

        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(DIVIDER_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        divider.setBounds(0, 408, 1920, 5);
        top.add(divider);

        JLabel tresPuntos = new JLabel("• • •");
        tresPuntos.setForeground(TEXT_MAIN);
        tresPuntos.setFont(new Font("SansSerif", Font.BOLD, 22));
        tresPuntos.setBounds(1840, 32, 70, 30);
        tresPuntos.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tresPuntos.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                menuDesplegable.setVisible(!menuDesplegable.isVisible());
                layered.repaint();
            }
        });
        top.add(tresPuntos);

        menuDesplegable = buildMenuDesplegable();
        menuDesplegable.setBounds(1680, 68, 220, 120);
        menuDesplegable.setVisible(false);

        layered.add(top, JLayeredPane.DEFAULT_LAYER);
        layered.add(menuDesplegable, JLayeredPane.POPUP_LAYER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_COLOR);
        wrapper.add(layered, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── eyeButton ─────────────────
    private JPanel eyeButton(boolean tachado) {
        JPanel btn = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = Boolean.TRUE.equals(getClientProperty("selected"));
                if (sel) {
                    g2.setColor(new Color(0x2E2A6E));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.setColor(ACCENT_COLOR);
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Double(1, 1, getWidth()-2, getHeight()-2, 12, 12));
                }
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setColor(ACCENT_COLOR);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawArc(cx - 18, cy - 10, 36, 20, 0,  180);
                g2.drawArc(cx - 18, cy - 10, 36, 20, 0, -180);
                g2.fillOval(cx - 6, cy - 6, 12, 12);
                if (tachado) {
                    g2.setColor(BG_COLOR);
                    g2.setStroke(new BasicStroke(5f));
                    g2.drawLine(cx - 16, cy + 14, cx + 16, cy - 14);
                    g2.setColor(ACCENT_COLOR);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawLine(cx - 16, cy + 14, cx + 16, cy - 14);
                }
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        return btn;
    }

    // ─── updateEyeStyle ─────────────────
    private void updateEyeStyle(JPanel btn, boolean selected) {
        btn.putClientProperty("selected", selected);
        btn.repaint();
    }

    // ─── buildMenuDesplegable ─────────────────
    private JPanel buildMenuDesplegable() {
        JPanel menu = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MENU_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JButton btnEliminar = menuButton("Eliminar publicación", MENU_ELIMINAR);
        JButton btnAjustes  = menuButton("Ajustes cuenta",       MENU_BTN);

        btnEliminar.addActionListener(e -> {
            menuDesplegable.setVisible(false);
            if (!modoEliminar) {
                modoEliminar = true;
                seleccionadas.clear();
                btnEliminar.setText("Confirmar eliminación");
                refreshGrid(viendoPublicas);
            } else {
                if (seleccionadas.isEmpty()) {
                    modoEliminar = false;
                    btnEliminar.setText("Eliminar publicación");
                    refreshGrid(viendoPublicas);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(
                    PantallaPerfil.this,
                    "¿Eliminar " + seleccionadas.size() + " publicación(es)?",
                    "Confirmar", JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    for (int idMem : seleccionadas) {
                        Cliente.darDeBajaMemoria(idMem, usuario.idUsuario);
                    }
                    if (parent instanceof PantallaPrincipal) {
                        ((PantallaPrincipal) parent).agregarNotificacion("Memoria eliminada con exito");
                    }
                    seleccionadas.clear();
                    modoEliminar = false;
                    btnEliminar.setText("Eliminar publicación");
                    refreshGrid(viendoPublicas);
                }
            }
        });
        btnAjustes.addActionListener(e -> {
            setVisible(false);
            new Pantallaajustes(PantallaPerfil.this, usuario);
        });

        menu.add(btnEliminar);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnAjustes);

        return menu;
    }

    // ─── menuButton ─────────────────
    private JButton menuButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(TEXT_MAIN);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setMaximumSize(new Dimension(196, 44));
        btn.setPreferredSize(new Dimension(196, 44));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── buildGrid ─────────────────
    private JPanel buildGrid() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_COLOR);

        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setBackground(BG_COLOR);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(28, 60, 28, 60));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(1800, 0));

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── refreshGrid ─────────────────
    private void refreshGrid(boolean soloPublicas) {
        gridPanel.removeAll();
        boolean cargarSoloPub = !esPerfilPropio || soloPublicas;
        java.util.List<UsuarioDAO.Memoria> memorias = Cliente.obtenerMemoriasDeUsuario(usuario.idUsuario, cargarSoloPub);
        
        cantPublicaciones = memorias.size();
        if (lblCantidad != null) lblCantidad.setText(String.valueOf(cantPublicaciones));

        for (UsuarioDAO.Memoria mem : memorias) {
            gridPanel.add(buildMemoryCardSmall(mem));
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // ─── buildMemoryCardSmall ─────────────────
    private JPanel buildMemoryCardSmall(UsuarioDAO.Memoria mem) {
        final boolean[] seleccionada = { seleccionadas.contains(mem.idMemoria) };

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_COLOR);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(seleccionada[0] ? new Color(0xFF4444) : CARD_BORDER);
                g2.setStroke(new BasicStroke(seleccionada[0] ? 3f : 2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth()-2, getHeight()-2, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel filaTop = new JPanel(new BorderLayout());
        filaTop.setOpaque(false);
        filaTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel lblAutor = new JLabel("@" + mem.nombreUsuario);
        lblAutor.setForeground(ACCENT_COLOR);
        lblAutor.setFont(new Font("SansSerif", Font.PLAIN, 12));
        filaTop.add(lblAutor, BorderLayout.WEST);

        if (modoEliminar) {
            JPanel checkbox = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int s = 18;
                    int x = (getWidth() - s) / 2;
                    int y = (getHeight() - s) / 2;
                    g2.setColor(seleccionada[0] ? new Color(0xFF4444) : new Color(0x3D3580));
                    g2.fillRoundRect(x, y, s, s, 5, 5);
                    g2.setColor(seleccionada[0] ? new Color(0xFF8888) : ACCENT_COLOR);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(x, y, s, s, 5, 5);
                    if (seleccionada[0]) {
                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(x+4, y+9, x+7, y+13);
                        g2.drawLine(x+7, y+13, x+14, y+5);
                    }
                    g2.dispose();
                }
            };
            checkbox.setOpaque(false);
            checkbox.setPreferredSize(new Dimension(26, 20));
            checkbox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            filaTop.add(checkbox, BorderLayout.EAST);

            MouseAdapter toggleSelect = new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    seleccionada[0] = !seleccionada[0];
                    if (seleccionada[0]) seleccionadas.add(mem.idMemoria);
                    else                 seleccionadas.remove(mem.idMemoria);
                    card.repaint();
                    checkbox.repaint();
                }
            };
            card.addMouseListener(toggleSelect);
            checkbox.addMouseListener(toggleSelect);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        card.add(filaTop);
        card.add(Box.createVerticalStrut(4));

        JLabel lblTitulo = new JLabel(mem.titulo);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitulo.setForeground(TEXT_MAIN);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(10));

        BufferedImage imgCargada = null;
        if (mem.contenido != null && !mem.contenido.isEmpty()) {
            try {
                File f = new File("uploads_cache", mem.contenido);
                if (!f.exists()) {
                    f = Cliente.descargarImagen(mem.contenido);
                }
                if (f != null && f.exists()) imgCargada = ImageIO.read(f);
            } catch (Exception ignored) {}
        }
        final BufferedImage imgFinal = imgCargada;
        final int IMG_H = 280;

        JPanel imgPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (imgFinal != null) {
                    int iw = imgFinal.getWidth();
                    int ih = imgFinal.getHeight();
                    double scale = Math.min((double) getWidth() / iw, (double) getHeight() / ih);
                    int nw = (int)(iw * scale);
                    int nh = (int)(ih * scale);
                    int ox = (getWidth()  - nw) / 2;
                    int oy = (getHeight() - nh) / 2;
                    g2.setColor(new Color(0x1A1740));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.drawImage(imgFinal, ox, oy, nw, nh, null);
                } else {
                    g2.setColor(new Color(0x3D3580));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(CARD_BORDER);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 42));
                    FontMetrics fm = g2.getFontMetrics();
                    String plus = "+";
                    g2.drawString(plus, (getWidth() - fm.stringWidth(plus)) / 2, getHeight() / 2 + 16);
                }
                g2.dispose();
            }
        };
        imgPanel.setOpaque(false);
        imgPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        imgPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, IMG_H));
        imgPanel.setMinimumSize(new Dimension(100, IMG_H));
        imgPanel.setPreferredSize(new Dimension(400, IMG_H));
        card.add(imgPanel);
        card.add(Box.createVerticalStrut(10));

        String descText = (mem.descripcion != null && !mem.descripcion.isEmpty()) ? mem.descripcion : "";
        if (!descText.isEmpty()) {
            JTextArea lblDesc = new JTextArea(descText);
            lblDesc.setForeground(TEXT_DIM);
            lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblDesc.setOpaque(false);
            lblDesc.setEditable(false);
            lblDesc.setLineWrap(true);
            lblDesc.setWrapStyleWord(true);
            lblDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(lblDesc);
        }
        card.setMaximumSize(new Dimension(440, Integer.MAX_VALUE));
        card.setMinimumSize(new Dimension(300, 200));
        card.setPreferredSize(new Dimension(440, 500));
        return card;
    }

    // ─── main ─────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PantallaPerfil(null, null));
    }
}