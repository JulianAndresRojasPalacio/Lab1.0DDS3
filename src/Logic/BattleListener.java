package Logic;

/**
 * Eventos que la lógica del duelo notifica a la interfaz.
 * Así Duel no depende de Swing (lógica desacoplada de la UI).
 */
public interface BattleListener {
    /** Se define quién tiene el turno inicial. */
    void onDuelStarted(boolean aiStarts);

    /** La máquina jugó primero en esta ronda y el jugador debe responder. */
    void onAiPlayed(String aiCard);

    /** Resultado de una ronda: winner es "player", "ai" o "draw". */
    void onTurn(String playerCard, String aiCard, String winner);

    void onScoreChanged(int playerScore, int aiScore);

    void onDuelEnded(String winner);
}