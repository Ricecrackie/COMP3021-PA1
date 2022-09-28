package hk.ust.comp3021.game;

import hk.ust.comp3021.actions.*;
import hk.ust.comp3021.entities.*;
import hk.ust.comp3021.utils.NotImplementedException;
import hk.ust.comp3021.utils.StringResources;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A base implementation of Sokoban Game.
 */
public abstract class AbstractSokobanGame implements SokobanGame {
    @NotNull
    protected final GameState state;
    protected boolean requestExit;

    protected AbstractSokobanGame(@NotNull GameState gameState) {
        this.state = gameState;
    }

    /**
     * @return True is the game should stop running.
     * For example when the user specified to exit the game or the user won the game.
     */
    protected boolean shouldStop() {
        // TODO
        if (this.state.isWin() || this.requestExit) {
            return true;
        }
        return false;
    }

    /**
     * @param action The action received from the user.
     * @return The result of the action.
     */
    protected ActionResult processAction(@NotNull Action action) {
        // TODO
        return switch (action) {
            case Exit e-> new ActionResult.Success(e);
            case Undo u-> {
                if (this.state.getUndoQuota().isEmpty() || this.state.getUndoQuota().get() > 0) {
                    this.state.undo();
                    yield new ActionResult.Success(u);
                }
                else {
                    yield new ActionResult.Failed(u, StringResources.UNDO_QUOTA_RUN_OUT);
                }
            }
            case Move m-> {
                if (m.getInitiator() > 1 || this.state.getPlayerPositionById(m.getInitiator()) == null) {
                    yield new ActionResult.Failed(m, StringResources.PLAYER_NOT_FOUND);
                }
                Position currentpos = this.state.getPlayerPositionById(m.getInitiator());
                Position nextpos = m.nextPosition(currentpos);
                yield switch (this.state.getEntity(nextpos)) {
                    case Player ignored-> new ActionResult.Failed(m, "You hit another player.");
                    case Wall ignored-> new ActionResult.Failed(m, "You hit a wall");
                    case Empty ignored-> {
                        this.state.move(currentpos, nextpos);
                        yield new ActionResult.Success(m);
                    }
                    case Box ignored-> {
                        Position boxNextpos = switch(m) {
                            case Move.Up u -> new Position(nextpos.x(), nextpos.y()-1);
                            case Move.Down d -> new Position(nextpos.x(), nextpos.y()+1);
                            case Move.Left l -> new Position(nextpos.x()-1, nextpos.y());
                            case Move.Right r -> new Position(nextpos.x()+1, nextpos.y());
                        };
                        if (this.state.getEntity(boxNextpos) instanceof Empty) {
                            this.state.checkpoint();
                            this.state.move(nextpos, boxNextpos);
                            this.state.move(currentpos, nextpos);
                            yield new ActionResult.Success(m);
                        }
                        else {
                            yield new ActionResult.Failed(m, "Failed to push the box.");
                        }
                    }
                };
            }
            case InvalidInput i-> new ActionResult.Failed(i, StringResources.INVALID_INPUT_MESSAGE);
        };
    }
}
