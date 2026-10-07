package UI;

import API.YgoApiClient;
import Logic.BattleListener;
import Logic.Duel;
import Model.Card;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Stadium extends JFrame implements BattleListener {
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

    private YgoApiClient apiClient = new YgoApiClient();
    private Duel duel;
    private List<Card> playerCards = new ArrayList<>();
    private List<Card> aiCards = new ArrayList<>();
    private List<Boolean> playerCardsUsed = new ArrayList<>();

    public Stadium() {
        setContentPane(panel1);
        setTitle("Yu-Gi-Oh! A Pelear Durísimo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 620);
        setLocationRelativeTo(null);

        // Estilos generales de los paneles
        panel1.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel1.setBackground(new Color(30, 30, 30));

        panelCartas.setBorder(new LineBorder(new Color(0, 255, 0), 2, true));
        panelCartas.setBackground(new Color(40, 40, 70));

        panelRival.setBorder(new LineBorder(Color.RED, 2, true));
        panelRival.setBackground(new Color(70, 30, 30));

        panelTitulo.setBackground(new Color(25, 25, 25));
        labelTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        labelTitulo.setForeground(new Color(255, 215, 0));

        initializeUI();
    }

    private void initializeUI() {
        // Configurar textArea
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        textArea.setBackground(new Color(20, 20, 20));
        textArea.setForeground(new Color(0, 220, 0));
        textArea.setText("Bienvenido a Yu-Gi-Oh!. Presiona 'Elegir Cartas' para comenzar.\n");

        // Botón de inicio con estilo
        botonInicio.setBackground(new Color(220, 20, 60));
        botonInicio.setForeground(Color.WHITE);
        botonInicio.setFont(new Font("Segoe UI", Font.BOLD, 20));
        botonInicio.setFocusPainted(false);
        botonInicio.setBorder(new LineBorder(Color.BLACK, 2, true));

        botonInicio.addActionListener(e -> startDuel());

        // Deshabilitar botones de cartas
        carta1.setEnabled(false);
        carta2.setEnabled(false);
        carta3.setEnabled(false);
        carta1.addActionListener(e -> selectPlayerCard(0));
        carta2.addActionListener(e -> selectPlayerCard(1));
        carta3.addActionListener(e -> selectPlayerCard(2));

        cartaRival1.setEnabled(false);
        cartaRival2.setEnabled(false);
        cartaRival3.setEnabled(false);
        panelRival.setVisible(false);
    }

    private void startDuel() {
        botonInicio.setEnabled(false);
        textArea.append("Cargando cartas...\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());

        clearPlayerCards();
        clearAiCards();
        playerCardsUsed.clear();

        new Thread(() -> {
            try {
                List<Card> newPlayerCards = new ArrayList<>();
                List<Card> newAiCards = new ArrayList<>();
                List<Boolean> newUsed = new ArrayList<>();

                for (int i = 0; i < 3; i++) {
                    newPlayerCards.add(apiClient.getRandomMonsterCard());
                    newAiCards.add(apiClient.getRandomMonsterCard());
                    newUsed.add(false);
                }

                SwingUtilities.invokeLater(() -> {
                    playerCards = newPlayerCards;
                    aiCards = newAiCards;
                    playerCardsUsed = newUsed;
                    duel = new Duel(playerCards, aiCards, this);

                    textArea.append("Cartas cargadas. Selecciona una carta para jugar.\n");
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                    showPlayerCards();
                    showAiCards();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    textArea.append("Error cargando cartas: " + ex.getMessage() + "\n");
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                    botonInicio.setEnabled(true);
                });
            }
        }).start();
    }

    private void showPlayerCards() {
        if (playerCards.size() >= 3 && playerCardsUsed.size() >= 3) {
            setupCardButton(carta1, playerCards.get(0), 0);
            setupCardButton(carta2, playerCards.get(1), 1);
            setupCardButton(carta3, playerCards.get(2), 2);
        }
        panelCartas.revalidate();
        panelCartas.repaint();
    }

    private void showAiCards() {
        if (aiCards.size() >= 3) {
            setupAiCardButton(cartaRival1, aiCards.get(0));
            setupAiCardButton(cartaRival2, aiCards.get(1));
            setupAiCardButton(cartaRival3, aiCards.get(2));
        }
        panelRival.setVisible(true);
        panelRival.revalidate();
        panelRival.repaint();
    }

    private void setupCardButton(JButton button, Card card, int index) {
        boolean isUsed = playerCardsUsed.get(index);
        button.setEnabled(!isUsed);
        button.setText("<html>" + card.getName() + "<br>ATK: " + card.getAtk() + " DEF: " + card.getDef() + (isUsed ? " (Usada)" : "") + "</html>");
        try {
            ImageIcon icon = new ImageIcon(URI.create(card.getImageUrl()).toURL());
            Image img = icon.getImage().getScaledInstance(100, 145, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            button.setPreferredSize(new Dimension(120, 180));
            button.setBorder(new LineBorder(new Color(0, 255, 0), 3, true));
        } catch (Exception e) {
            System.err.println("Error cargando imagen para carta: " + card.getName());
        }
    }

    private void setupAiCardButton(JButton button, Card card) {
        button.setEnabled(false);
        button.setText("<html>" + card.getName() + "<br>ATK: " + card.getAtk() + " DEF: " + card.getDef() + "</html>");
        try {
            ImageIcon icon = new ImageIcon(URI.create(card.getImageUrl()).toURL());
            Image img = icon.getImage().getScaledInstance(100, 145, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            button.setPreferredSize(new Dimension(120, 180));
            button.setBorder(new LineBorder(Color.RED, 3, true));
        } catch (Exception e) {
            System.err.println("Error cargando imagen para carta AI: " + card.getName());
        }
    }

    private void selectPlayerCard(int index) {
        if (duel == null || index < 0 || index >= playerCards.size() || playerCardsUsed.get(index)) {
            textArea.append("Esta carta ya fue usada o no está disponible.\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
            return;
        }

        Card selectedCard = playerCards.get(index);
        playerCardsUsed.set(index, true);

        switch (index) {
            case 0 -> carta1.setEnabled(false);
            case 1 -> carta2.setEnabled(false);
            case 2 -> carta3.setEnabled(false);
        }

        showPlayerCards();
        textArea.append("Carta seleccionada: " + selectedCard.getName() + ".\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());

        // Se juega al final: si el duelo termina, onDuelEnded limpia todo
        duel.playTurn(selectedCard);
    }

    private void clearPlayerCards() {
        carta1.setText("");
        carta1.setIcon(null);
        carta1.setEnabled(false);
        carta2.setText("");
        carta2.setIcon(null);
        carta2.setEnabled(false);
        carta3.setText("");
        carta3.setIcon(null);
        carta3.setEnabled(false);
        panelCartas.revalidate();
        panelCartas.repaint();
    }

    private void clearAiCards() {
        cartaRival1.setText("");
        cartaRival1.setIcon(null);
        cartaRival2.setText("");
        cartaRival2.setIcon(null);
        cartaRival3.setText("");
        cartaRival3.setIcon(null);
        panelRival.setVisible(false);
        panelRival.revalidate();
        panelRival.repaint();
    }

    @Override
    public void onTurn(String playerCard, String aiCard, String winner) {
        String resultado;
        if ("player".equals(winner)) {
            resultado = " ¡Ganaste el turno!";
        } else if ("ai".equals(winner)) {
            resultado = " La máquina ganó el turno.";
        } else {
            resultado = " Empate en el turno.";
        }

        textArea.append(String.format("Jugador: %s | Máquina: %s -> %s%n", playerCard, aiCard, resultado));
    }

    @Override
    public void onScoreChanged(int playerScore, int aiScore) {
        textArea.append(String.format("Marcador - Jugador: %d, Máquina: %d\n", playerScore, aiScore));
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    @Override
    public void onDuelEnded(String winner) {
        textArea.append("El duelo terminó. Ganador: " + winner + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
        JOptionPane.showMessageDialog(this, "El duelo terminó. Ganador: " + winner, "Duelo Finalizado", JOptionPane.INFORMATION_MESSAGE);
        botonInicio.setEnabled(true);
        clearPlayerCards();
        clearAiCards();
        playerCardsUsed.clear();
    }
}