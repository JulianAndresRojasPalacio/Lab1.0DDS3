package Logic;

import Model.Card;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Reglas del duelo: 3 rondas, gana quien llegue primero a 2 puntos.
 * No conoce la interfaz gráfica; se comunica solo mediante BattleListener.
 */
public class Duel {
    private static final int MAX_ROUNDS = 3;
    private static final int ROUNDS_TO_WIN = 2;

    private final List<Card> aiAvailable;      // cartas de la máquina aún sin usar
    private final BattleListener listener;
    private final Random random = new Random();

    private int playerScore = 0;
    private int aiScore = 0;
    private int roundsPlayed = 0;
    private boolean aiStarts;                  // turno inicial
    private Card pendingAiCard;                // carta ya elegida si la máquina juega primero
    private boolean finished = false;

    public Duel(List<Card> playerCards, List<Card> aiCards, BattleListener listener) {
        this.aiAvailable = new ArrayList<>(aiCards);
        this.listener = listener;
    }

    /** Inicia el duelo: define aleatoriamente quién empieza. */
    public void start() {
        aiStarts = random.nextBoolean();
        listener.onDuelStarted(aiStarts);
        prepareRound();
    }

    /** Si le toca a la máquina jugar primero en esta ronda, elige y anuncia su carta. */
    private void prepareRound() {
        // El que empieza juega primero en las rondas 1 y 3; el otro en la 2
        boolean aiFirst = (roundsPlayed % 2 == 0) == aiStarts;
        if (aiFirst) {
            pendingAiCard = pickAiCard();
            listener.onAiPlayed(describe(pendingAiCard));
        } else {
            pendingAiCard = null;
        }
    }

    /** Elige al azar una carta de la máquina que no haya usado, con posición aleatoria. */
    private Card pickAiCard() {
        Card card = aiAvailable.remove(random.nextInt(aiAvailable.size()));
        card.setPosition(random.nextBoolean() ? "attack" : "defense");
        return card;
    }

    /** Juega una ronda con la carta del jugador (su posición ya viene definida). */
    public void playTurn(Card playerCard) {
        if (finished) return;

        Card aiCard = (pendingAiCard != null) ? pendingAiCard : pickAiCard();
        pendingAiCard = null;

        String winner = determineWinner(playerCard, aiCard);
        if ("player".equals(winner)) playerScore++;
        else if ("ai".equals(winner)) aiScore++;
        roundsPlayed++;

        listener.onTurn(describe(playerCard), describe(aiCard), winner);
        listener.onScoreChanged(playerScore, aiScore);

        if (playerScore == ROUNDS_TO_WIN || aiScore == ROUNDS_TO_WIN || roundsPlayed == MAX_ROUNDS) {
            finished = true;
            String finalWinner;
            if (playerScore > aiScore) finalWinner = "Jugador";
            else if (aiScore > playerScore) finalWinner = "Máquina";
            else finalWinner = "Empate";
            listener.onDuelEnded(finalWinner);
        } else {
            prepareRound();
        }
    }

    /** Compara ATK vs DEF según la posición de cada carta. */
    private String determineWinner(Card player, Card ai) {
        boolean playerAttacks = "attack".equals(player.getPosition());
        boolean aiAttacks = "attack".equals(ai.getPosition());

        // Ambas en ataque: gana el mayor ATK
        if (playerAttacks && aiAttacks) {
            return compare(player.getAtk(), ai.getAtk());
        }
        // Jugador ataca, máquina defiende: ATK del jugador vs DEF de la máquina
        if (playerAttacks) {
            return compare(player.getAtk(), ai.getDef());
        }
        // Máquina ataca, jugador defiende: ATK de la máquina vs DEF del jugador
        if (aiAttacks) {
            return compare(player.getDef(), ai.getAtk());
        }
        // Ambas en defensa: nadie ataca
        return "draw";
    }

    /** Devuelve "player" si a > b, "ai" si a < b, "draw" si son iguales. */
    private String compare(int playerValue, int aiValue) {
        if (playerValue > aiValue) return "player";
        if (playerValue < aiValue) return "ai";
        return "draw";
    }

    private String describe(Card card) {
        String pos = "attack".equals(card.getPosition()) ? "ATAQUE" : "DEFENSA";
        return card.getName() + " [" + pos + "]";
    }
}