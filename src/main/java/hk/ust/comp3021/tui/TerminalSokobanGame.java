package hk.ust.comp3021.tui;


import hk.ust.comp3021.actions.ActionResult;
import hk.ust.comp3021.actions.Exit;
import hk.ust.comp3021.game.AbstractSokobanGame;
import hk.ust.comp3021.game.GameState;
import hk.ust.comp3021.game.InputEngine;
import hk.ust.comp3021.game.RenderingEngine;
import hk.ust.comp3021.utils.NotImplementedException;
import hk.ust.comp3021.utils.StringResources;

import java.util.Optional;

/**
 * A Sokoban game running in the terminal.
 */
public class TerminalSokobanGame extends AbstractSokobanGame {

    private final InputEngine inputEngine;

    private final RenderingEngine renderingEngine;

    /**
     * Create a new instance of TerminalSokobanGame.
     * Terminal-based game only support at most two players, although the hk.ust.comp3021.game package supports up to 26 players.
     * This is only because it is hard to control too many players in a terminal-based game.
     *
     * @param gameState       The game state.
     * @param inputEngine     the terminal input engin.
     * @param renderingEngine the terminal rendering engine.
     * @throws IllegalArgumentException when there are more than two players in the map.
     */
    public TerminalSokobanGame(GameState gameState, TerminalInputEngine inputEngine, TerminalRenderingEngine renderingEngine) {
        super(gameState);
        this.inputEngine = inputEngine;
        this.renderingEngine = renderingEngine;
        // TODO
        // Check the number of players
        if (gameState.getAllPlayerPositions().size() > 2) {
            throw new IllegalArgumentException();
        }
        //throw new NotImplementedException();
    }

    @Override
    public void run() {
        // TODO
        this.renderingEngine.message(StringResources.GAME_READY_MESSAGE);
        this.printMap();
        while (!this.shouldStop()) {
            var act = this.inputEngine.fetchAction();
            if (act instanceof Exit) {
                this.requestExit = true;
            }
            var actResult = this.processAction(act);
            if (actResult instanceof ActionResult.Failed) {
                this.renderingEngine.message(((ActionResult.Failed) actResult).getReason());
            }
            this.printMap();
        }
        this.renderingEngine.message(StringResources.GAME_EXIT_MESSAGE);
        if (this.state.isWin()) {
            this.renderingEngine.message(StringResources.WIN_MESSAGE);
        }
        System.exit(0);
    }

    public void printMap() {
        this.renderingEngine.render(this.state);
        Optional<Integer> quota = this.state.getUndoQuota();
        if (quota.isEmpty()) {
            this.renderingEngine.message(StringResources.UNDO_QUOTA_UNLIMITED);
        }
        else if (quota.isPresent()) {
            this.renderingEngine.message(String.format(StringResources.UNDO_QUOTA_TEMPLATE, this.state.getUndoQuota().get()));
        }
    }
}
