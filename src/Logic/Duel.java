package Logic;

import Model.Card;
import java.util.List;
import java.util.Random;

public class Duel {
    private List<Card> playerCards;
    private List<Card> aiCards;
    private int playerScore = 0;
    private int aiScore = 0;
    private int roundsPlayed = 0;
    private BattleListener listener;
    private Random random = new Random();

    public Duel(List<Card> playerCards, List<Card> aiCards, BattleListener listener) {
        this.playerCards = playerCards;
        this.aiCards = aiCards;
        this.listener = listener;
    }

    public void playTurn(Card playerCard) {
        if (roundsPlayed >= 3) return;

        Card aiCard = aiCards.get(random.nextInt(aiCards.size()));

        String winner = determineWinner(playerCard, aiCard);

        if ("player".equals(winner)) playerScore++;
        else if ("ai".equals(winner)) aiScore++;

        roundsPlayed++;

        listener.onTurn(playerCard.getName(), aiCard.getName(), winner);
        listener.onScoreChanged(playerScore, aiScore);

        if (playerScore == 2 || aiScore == 2 || roundsPlayed == 3) {
            String finalWinner;
            if (playerScore > aiScore) finalWinner = "Jugador";
            else if (aiScore > playerScore) finalWinner = "Máquina";
            else finalWinner = "Empate";
            listener.onDuelEnded(finalWinner);
        }
    }

    private String determineWinner(Card playerCard, Card aiCard) {
        int playerAtk = playerCard.getAtk();
        int playerDef = playerCard.getDef();
        int aiAtk = aiCard.getAtk();
        int aiDef = aiCard.getDef();

        boolean playerAttacks = playerCard.getPosition().equals("attack");
        boolean aiAttacks = aiCard.getPosition().equals("attack");

        if (playerAttacks && aiAttacks) {
            if (playerAtk > aiAtk) return "player";
            else if (playerAtk < aiAtk) return "ai";
            else return "draw";
        }

        if (playerAttacks && !aiAttacks) {
            if (playerAtk > aiDef) return "player";
            else if (playerAtk < aiDef) return "ai";
            else return "draw";
        }

        if (!playerAttacks && aiAttacks) {
            if (aiAtk > playerDef) return "ai";
            else if (aiAtk < playerDef) return "player";
            else return "draw";
        }

        return "draw";
    }
}