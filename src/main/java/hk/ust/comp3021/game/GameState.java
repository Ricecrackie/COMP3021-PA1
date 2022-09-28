package hk.ust.comp3021.game;

import hk.ust.comp3021.entities.Box;
import hk.ust.comp3021.entities.Empty;
import hk.ust.comp3021.entities.Entity;
import hk.ust.comp3021.entities.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * The state of the Sokoban Game.
 * Each game state represents an ongoing game.
 * As the game goes, the game state changes while players are moving while the original game map stays the unmodified.
 * <b>The game state should not modify the original game map.</b>
 * <p>
 * GameState consists of things changing as the game goes, such as:
 * <li>Current locations of all crates.</li>
 * <li>A move history.</li>
 * <li>Current location of player.</li>
 * <li>Undo quota left.</li>
 */
public class GameState {
    private GameMap map;
    private Optional<Integer> undoQuota;
    private GameState checkpoint;
    private GameMap state;
    private boolean init = true;
    /**
     * Create a running game state from a game map.
     *
     * @param map the game map from which to create this game state.
     */
    public GameState(@NotNull GameMap map) {
        // TODO
        this.map = map;
        this.undoQuota = map.getUndoLimit();
        this.state = new GameMap(map);
    }

    /**
     * Get the current position of the player with the given id.
     *
     * @param id player id.
     * @return the current position of the player.
     */
    public @Nullable Position getPlayerPositionById(int id) {
        // TODO
        return this.state.getPlayerPosition().get((char)('A'+id));
    }

    /**
     * Get current positions of all players in the game map.
     *
     * @return a set of positions of all players.
     */
    public @NotNull Set<Position> getAllPlayerPositions() {
        // TODO
        return new HashSet<Position>(this.state.getPlayerPosition().values());
    }

    /**
     * Get the entity that is currently at the given position.
     *
     * @param position the position of the entity.
     * @return the entity object.
     */
    public @Nullable Entity getEntity(@NotNull Position position) {
        // TODO
        return this.state.getEntity(position);
    }

    /**
     * Get all box destination positions as a set in the game map.
     * This should be the same as that in {@link GameMap} class.
     *
     * @return a set of positions.
     */
    public @NotNull @Unmodifiable Set<Position> getDestinations() {
        // TODO
        return this.map.getDestinations();
    }

    /**
     * Get the undo quota currently left, i.e., the maximum number of undo actions that can be performed from now on.
     * If undo is unlimited,
     *
     * @return the undo quota left (using {@link Optional#of(Object)}) if the game has an undo limit;
     * {@link Optional#empty()} if the game has unlimited undo.
     */
    public Optional<Integer> getUndoQuota() {
        // TODO
        return this.undoQuota;
    }

    /**
     * Check whether the game wins or not.
     * The game wins only when all box destinations have been occupied by boxes.
     *
     * @return true is the game wins.
     */
    public boolean isWin() {
// TODO
        for (var position : this.map.getDestinations()) {
            if (!(this.state.getEntity(position) instanceof Box)) {
                return false;
            }
        }
        return true;
        //throw new NotImplementedException();
    }

    /**
     * Move the entity from one position to another.
     * This method assumes the validity of this move is ensured.
     * <b>The validity of the move of the entity in one position to another need not to check.</b>
     *
     * @param from The current position of the entity to move.
     * @param to   The position to move the entity to.
     */
    public void move(Position from, Position to) {
        // TODO
        if (this.state.getEntity(from) instanceof Player p) {
            this.state.getPlayerPosition().put((char)(p.getId() + 'A'), to);
        }
        this.state.putEntity(to, this.state.getEntity(from));
        this.state.putEntity(from, new Empty());
    }

    /**
     * Record a checkpoint of the game state, including:
     * <li>All current positions of entities in the game map.</li>
     * <li>Current undo quota</li>
     * <p>
     * Checkpoint is used in {@link GameState#undo()}.
     * Every undo actions reverts the game state to the last checkpoint.
     */
    public void checkpoint() {
        // TODO
        this.checkpoint = new GameState(new GameMap(this.state));
        this.checkpoint.undoQuota = this.undoQuota;
        this.checkpoint.init = false;
    }

    /**
     * Revert the game state to the last checkpoint in history.
     * This method assumes there is still undo quota left, and decreases the undo quota by one.
     * <p>
     * If there is no checkpoint recorded, i.e., before moving any box when the game starts,
     * revert to the initial game state.
     */
    public void undo() {
        // TODO
        if (this.checkpoint == null) {
            this.state = new GameMap(this.map);
            if (!init) {
                this.undoQuota = Optional.of(this.undoQuota.get()-1);
            }
        } else {
            var currentBoxPositions = this.state.getBoxPositions();
            //var checkpointBoxPositions = this.checkpoint.state.getBoxPositions();
            for (var entry : currentBoxPositions) {
                if (!(this.checkpoint.state.getEntity(entry) instanceof Box)) {
                    this.state = new GameMap(this.checkpoint.state);
                    if (this.undoQuota.isPresent() && this.undoQuota.get() > 1) {
                        this.undoQuota = Optional.of(this.undoQuota.get()-1);
                    }
                    this.checkpoint = this.checkpoint.checkpoint;
                    return;
                }
            }
            this.init = false;
            this.checkpoint = this.checkpoint.checkpoint;
            undo();
        }
        //throw new NotImplementedException();
    }

    /**
     * Get the maximum width of the game map.
     * This should be the same as that in {@link GameMap} class.
     *
     * @return maximum width.
     */
    public int getMapMaxWidth() {
        // TODO
        return this.map.getMaxWidth();
    }

    /**
     * Get the maximum height of the game map.
     * This should be the same as that in {@link GameMap} class.
     *
     * @return maximum height.
     */
    public int getMapMaxHeight() {
        // TODO
        return this.map.getMaxHeight();
    }
}
