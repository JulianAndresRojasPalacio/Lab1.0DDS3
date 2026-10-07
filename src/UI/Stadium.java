package UI;

import API.YgoApiClient;
import Logic.BattleListener;
import Logic.Duel;
import Model.Card;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ventana principal. Muestra las cartas y el log de batalla.
 * Implementa BattleListener para reaccionar a los eventos de Duel.
 */
public class Stadium extends JFrame implements BattleListener {
    // Componentes enlazados con Stadium.form
    private JPanel panel1;
    private JTextArea textArea;
    private JButton botonInicio;
    private JButton carta1;
    private JButton carta2;
    private JButton carta3;
    private JPanel panelInicial;
    private JPanel panelCartas;
    private JScrollPane panelText;
    private JButton cartaRival1;
    private JButton cartaRival2;
    private JButton cartaRival3;
    private JPanel panelRival;
    private JPanel panelTitulo;
    private JLabel labelTitulo;

    private JButton[] playerButtons;
    private JButton[] aiButtons;

    private final YgoApiClient apiClient = new YgoApiClient();
    private Duel duel;
    private List<Card> playerCards = new ArrayList<>();
    private List<Card> aiCards = new ArrayList<>();
    private List<Boolean> playerCardsUsed = new ArrayList<>();
    // Imágenes ya descargadas y escaladas (se cargan fuera del hilo de la UI)
    private Map<Card, ImageIcon> images = new HashMap<>();

    // Colores del tema
    private static final Color GREEN = new Color(0, 255, 0);
    private static final Color GOLD = new Color(255, 215, 0);

    public Stadium() {
        setContentPane(panel1);
        setTitle("Yu-Gi-Oh! A Pelear Durísimo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 620);
        setLocationRelativeTo(null);

        // Estilos generales de los paneles
        panel1.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel1.setBackground(new Color(30, 30, 30));

        panelCartas.setBorder(new LineBorder(GREEN, 2, true));
        panelCartas.setBackground(new Color(40, 40, 70));

        panelRival.setBorder(new LineBorder(Color.RED, 2, true));
        panelRival.setBackground(new Color(70, 30, 30));

        panelTitulo.setBackground(new Color(25, 25, 25));
        labelTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        labelTitulo.setForeground(GOLD);

        initializeUI();
    }

    /** Configura componentes y listeners. */
    private void initializeUI() {
        // Los arrays se crean aquí porque los botones del .form ya existen en este punto
        playerButtons = new JButton[]{carta1, carta2, carta3};
        aiButtons = new JButton[]{cartaRival1, cartaRival2, cartaRival3};

        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        textArea.setBackground(new Color(20, 20, 20));
        textArea.setForeground(new Color(0, 220, 0));
        textArea.setText("Bienvenido a Yu-Gi-Oh!. Presiona 'Iniciar duelo' para comenzar.\n");

        botonInicio.setText("Iniciar duelo");
        botonInicio.setBackground(new Color(220, 20, 60));
        botonInicio.setForeground(Color.WHITE);
        botonInicio.setFont(new Font("Segoe UI", Font.BOLD, 20));
        botonInicio.setFocusPainted(false);
        botonInicio.setBorder(new LineBorder(Color.BLACK, 2, true));
        botonInicio.addActionListener(e -> startDuel());

        for (int i = 0; i < 3; i++) {
            final int index = i;
            final JButton b = playerButtons[i];
            b.setEnabled(false);
            b.addActionListener(e -> selectPlayerCard(index));

            // Borde dorado al pasar el mouse sobre una carta disponible
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (b.isEnabled()) b.setBorder(new LineBorder(GOLD, 3, true));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    b.setBorder(new LineBorder(GREEN, 3, true));
                }
            });

            aiButtons[i].setEnabled(false);
        }
        panelRival.setVisible(false);
    }

    /** Descarga las 6 cartas y sus imágenes en un hilo aparte para no bloquear la UI. */
    private void startDuel() {
        botonInicio.setEnabled(false);
        log("Cargando cartas...");

        clearPlayerCards();
        clearAiCards();
        playerCardsUsed.clear();

        new Thread(() -> {
            try {
                List<Card> newPlayerCards = new ArrayList<>();
                List<Card> newAiCards = new ArrayList<>();
                List<Boolean> newUsed = new ArrayList<>();
                Map<Card, ImageIcon> newImages = new HashMap<>();

                for (int i = 0; i < 3; i++) {
                    Card p = apiClient.getRandomMonsterCard();
                    Card a = apiClient.getRandomMonsterCard();
                    newPlayerCards.add(p);
                    newAiCards.add(a);
                    newUsed.add(false);
                    newImages.put(p, loadImage(p));
                    newImages.put(a, loadImage(a));
                }

                // Solo cuando están las 6 cartas se actualiza la UI y se inicia el duelo
                SwingUtilities.invokeLater(() -> {
                    playerCards = newPlayerCards;
                    aiCards = newAiCards;
                    playerCardsUsed = newUsed;
                    images = newImages;
                    duel = new Duel(playerCards, aiCards, this);

                    log("Cartas cargadas.");
                    showPlayerCards();
                    showAiCards();
                    duel.start();
                });
            } catch (IOException ex) {
                showLoadError("Error de red: " + ex.getMessage());
            } catch (Exception ex) {
                showLoadError("No se pudo cargar la carta: " + ex.getMessage());
            }
        }).start();
    }

    private void showLoadError(String message) {
        SwingUtilities.invokeLater(() -> {
            log(message);
            botonInicio.setEnabled(true);
        });
    }

    /** Descarga y escala la imagen de una carta (se llama fuera del hilo de la UI). */
    private ImageIcon loadImage(Card card) {
        try {
            BufferedImage img = ImageIO.read(URI.create(card.getImageUrl()).toURL());
            if (img == null) return null;
            return new ImageIcon(img.getScaledInstance(100, 145, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de: " + card.getName());
            return null;
        }
    }

    private void showPlayerCards() {
        if (playerCards.size() >= 3 && playerCardsUsed.size() >= 3) {
            for (int i = 0; i < 3; i++) {
                boolean used = playerCardsUsed.get(i);
                setupCardButton(playerButtons[i], playerCards.get(i), !used, used, GREEN);
            }
        }
        panelCartas.revalidate();
        panelCartas.repaint();
    }

    private void showAiCards() {
        if (aiCards.size() >= 3) {
            for (int i = 0; i < 3; i++) {
                setupCardButton(aiButtons[i], aiCards.get(i), false, false, Color.RED);
            }
        }
        panelRival.setVisible(true);
        panelRival.revalidate();
        panelRival.repaint();
    }

    /** Pinta una carta en un botón con estilo oscuro, usando la imagen ya cargada. */
    private void setupCardButton(JButton button, Card card, boolean enabled, boolean used, Color border) {
        button.setEnabled(enabled);
        button.setText("<html><div style='text-align:center; color:white'><b>" + card.getName()
                + "</b><br>ATK: " + card.getAtk() + " | DEF: " + card.getDef()
                + (used ? "<br><i>(Usada)</i>" : "") + "</div></html>");

        ImageIcon icon = images.get(card);
        if (icon != null) {
            button.setIcon(icon);
            // Usada: en gris. Rival o disponible: conserva el color
            button.setDisabledIcon(used
                    ? new ImageIcon(GrayFilter.createDisabledImage(icon.getImage()))
                    : icon);
        }

        // Fondo sólido oscuro en lugar del degradado por defecto
        boolean isAi = border.equals(Color.RED);
        button.setOpaque(true);
        button.setContentAreaFilled(false);
        button.setBackground(isAi ? new Color(60, 25, 25) : new Color(25, 35, 70));
        button.setFocusPainted(false);

        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setPreferredSize(new Dimension(120, 180));
        button.setBorder(new LineBorder(border, 3, true));
    }

    /** El jugador elige carta y posición; luego se juega la ronda. */
    private void selectPlayerCard(int index) {
        if (duel == null || index < 0 || index >= playerCards.size() || playerCardsUsed.get(index)) {
            log("Esta carta ya fue usada o no está disponible.");
            return;
        }

        Card selected = playerCards.get(index);

        Object[] options = {"Ataque", "Defensa"};
        int choice = JOptionPane.showOptionDialog(this,
                "¿En qué posición juegas a " + selected.getName() + "?",
                "Elegir posición", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == JOptionPane.CLOSED_OPTION) return; // canceló: no se gasta la carta
        selected.setPosition(choice == 0 ? "attack" : "defense");

        playerCardsUsed.set(index, true);
        showPlayerCards();
        log("Carta seleccionada: " + selected.getName() + ".");

        // Se juega al final: si el duelo termina, onDuelEnded limpia todo
        duel.playTurn(selected);
    }

    private void clearPlayerCards() {
        for (JButton b : playerButtons) {
            b.setText("");
            b.setIcon(null);
            b.setDisabledIcon(null);
            b.setEnabled(false);
        }
        panelCartas.revalidate();
        panelCartas.repaint();
    }

    private void clearAiCards() {
        for (JButton b : aiButtons) {
            b.setText("");
            b.setIcon(null);
            b.setDisabledIcon(null);
        }
        panelRival.setVisible(false);
        panelRival.revalidate();
        panelRival.repaint();
    }

    private void log(String message) {
        textArea.append(message + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    // ---------- Eventos de BattleListener ----------

    @Override
    public void onDuelStarted(boolean aiStarts) {
        log("Turno inicial (sorteo): " + (aiStarts ? "la Máquina" : "el Jugador") + ".");
    }

    @Override
    public void onAiPlayed(String aiCard) {
        log("La máquina juega primero: " + aiCard + ". Elige tu carta.");
    }

    @Override
    public void onTurn(String playerCard, String aiCard, String winner) {
        String resultado;
        if ("player".equals(winner)) resultado = "¡Ganaste el turno!";
        else if ("ai".equals(winner)) resultado = "La máquina ganó el turno.";
        else resultado = "Empate en el turno.";

        log(String.format("Jugador: %s | Máquina: %s -> %s", playerCard, aiCard, resultado));
    }

    @Override
    public void onScoreChanged(int playerScore, int aiScore) {
        log(String.format("Marcador - Jugador: %d, Máquina: %d", playerScore, aiScore));
    }

    @Override
    public void onDuelEnded(String winner) {
        log("El duelo terminó. Ganador: " + winner);
        JOptionPane.showMessageDialog(this, "El duelo terminó. Ganador: " + winner,
                "Duelo Finalizado", JOptionPane.INFORMATION_MESSAGE);
        botonInicio.setEnabled(true);
        clearPlayerCards();
        clearAiCards();
        playerCardsUsed.clear();
        duel = null;
    }
}