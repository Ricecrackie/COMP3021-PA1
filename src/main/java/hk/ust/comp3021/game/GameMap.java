package hk.ust.comp3021.game;

import hk.ust.comp3021.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * A Sokoban game board.
 * GameBoard consists of information loaded from map data, such as
 * <li>Width and height of the game map</li>
 * <li>Walls in the map</li>
 * <li>Box destinations</li>
 * <li>Initial locations of boxes and player</li>
 * <p/>
 * GameBoard is capable to create many GameState instances, each representing an ongoing game.
 */
public class GameMap {
    private int width;
    private int height;
    private Optional<Integer> undoLimit;
    private Set<Position> destinations;
    private Entity[][] map;
    private Map<Character, Position> playerPosition;
    /**
     * Create a new GameMap with width, height, set of box destinations and undo limit.
     *
     * @param maxWidth     Width of the game map.
     * @param maxHeight    Height of the game map.
     * @param destinations Set of box destination positions.
     * @param undoLimit    Undo limit.
     *                     Positive numbers specify the maximum number of undo actions.
     *                     0 means undo is not allowed.
     *                     -1 means unlimited. Other negative numbers are not allowed.
     */
    public GameMap(int maxWidth, int maxHeight, Set<Position> destinations, int undoLimit) {
        // TODO
        if (undoLimit < -1) {
            throw new IllegalArgumentException();
        }
        this.width = maxWidth;
        this.height = maxHeight;
        this.destinations = destinations;
        if (undoLimit == -1) {
            this.undoLimit = Optional.empty();
        } else if (undoLimit >= 0) {
            this.undoLimit = Optional.of(undoLimit);
        } else {
            throw new IllegalArgumentException();
        }
        this.map = new Entity[maxHeight][maxWidth];
        //throw new NotImplementedException();
    }

    public GameMap(GameMap gm) {
        this.width = gm.width;
        this.height = gm.height;
        this.undoLimit = gm.undoLimit;
        this.destinations = new HashSet<Position>(gm.destinations);
        this.playerPosition = new HashMap<Character, Position>();
        //this.playerPosition = new HashMap<Character, Position>(gm.playerPosition);
        for (var entry : gm.playerPosition.keySet()) {
            this.playerPosition.put(entry, new Position(gm.playerPosition.get(entry).x(), gm.playerPosition.get(entry).y()));
        }
        this.map = new Entity[this.height][this.width];
        for (int i = 0; i < this.height; ++i) {
            for (int j = 0; j < this.width; ++j) {
                this.putEntity(new Position(j, i), gm.getEntity(new Position(j, i)));
            }
        }
    }

    /**
     * Parses the map from a string representation.
     * The first line is undo limit.
     * Starting from the second line, the game map is represented as follows,
     * <li># represents a {@link Wall}</li>
     * <li>@ represents a box destination.</li>
     * <li>Any upper-case letter represents a {@link Player}.</li>
     * <li>
     * Any lower-case letter represents a {@link Box} that is only movable by the player with the corresponding upper-case letter.
     * For instance, box "a" can only be moved by player "A" and not movable by player "B".
     * </li>
     * <li>. represents an {@link Empty} position in the map, meaning there is no player or box currently at this position.</li>
     * <p>
     * Notes:
     * <li>
     * There can be at most 26 players.
     * All implementations of classes in the hk.ust.comp3021.game package should support up to 26 players.
     * </li>
     * <li>
     * For simplicity, we assume the given map is bounded with a closed boundary.
     * There is no need to check this point.
     * </li>
     * <li>
     * Example maps can be found in "src/main/resources".
     * </li>
     *
     * @param mapText The string representation.
     * @return The parsed GameMap object.
     * @throws IllegalArgumentException if undo limit is negative but not -1.
     * @throws IllegalArgumentException if there are multiple same upper-case letters, i.e., one player can only exist at one position.
     * @throws IllegalArgumentException if there are no players in the map.
     * @throws IllegalArgumentException if the number of boxes is not equal to the number of box destinations.
     * @throws IllegalArgumentException if there are boxes whose {@link Box#getPlayerId()} do not match any player on the game board,
     *                                  or if there are players that have no corresponding boxes.
     */
    public static GameMap parse(String mapText) {
        // TODO
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(mapText.split("\\r?\\n")));
        int undoLimit = Integer.parseInt(list.get(0));
        if (undoLimit < -1) {
            throw new IllegalArgumentException();
        }
        boolean illegal = false;

        list.remove(0);
        int height = list.size();
        int width = list.get(0).length();
        for (var entry : list) {
            if (entry.length() > width) {
                width = entry.length();
            }
        }
        ArrayList<Position> destinationList = new ArrayList<Position>();

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < list.get(i).length(); ++j) {
                char temp = list.get(i).charAt(j);
                if (temp == '@') {
                    destinationList.add(new Position(j, i));
                }
            }
        }
        var result = new GameMap(width, height, new HashSet<Position>(destinationList), undoLimit);
        Map<Character, Position> playerPosition = new HashMap<Character, Position>();
        Set<Character> boxType = new HashSet<Character>();
        int boxCount = 0;

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < list.get(i).length(); ++j) {
                char temp = list.get(i).charAt(j);
                if (Character.isAlphabetic(temp)) {
                    if (Character.isUpperCase(temp)) {
                        if (playerPosition.containsKey(temp)) {
                            illegal = true;
                            break;
                        }
                        playerPosition.put(temp, new Position(j, i));
                        result.putEntity(new Position(j, i), new Player(temp - 'A'));
                    } else {
                        boxCount++;
                        boxType.add(temp);
                        result.putEntity(new Position(j, i), new Box(temp - 'a'));
                    }
                } else {
                    switch (temp) {
                        case '#':
                            result.putEntity(new Position(j, i), new Wall());
                            break;
                        case '@':
                        case '.':
                            result.putEntity(new Position(j, i), new Empty());
                            break;
                        default:
                            break;
                    }
                }
            }
        }



        if (playerPosition.isEmpty() || boxCount != destinationList.size() || boxType.size() != playerPosition.size()) {
            illegal = true;
        }

        if (boxType.size() == playerPosition.size()) {
            for (var entry : boxType) {
                if (!playerPosition.containsKey(Character.toUpperCase(entry))) {
                    illegal = true;
                    break;
                }
            }
        }

        if (illegal) {
            throw new IllegalArgumentException();
        }

        result.playerPosition = playerPosition;
        return result;
    }

    /**
     * Get the entity object at the given position.
     *
     * @param position the position of the entity in the game map.
     * @return Entity object.
     */
    @Nullable
    public Entity getEntity(Position position) {
        // TODO
        return this.map[position.y()][position.x()];
    }

    /**
     * Put one entity at the given position in the game map.
     *
     * @param position the position in the game map to put the entity.
     * @param entity   the entity to put into game map.
     */
    public void putEntity(Position position, Entity entity) {
        // TODO
        if (entity == null) {
            return;
        }
        int x = position.x();
        int y = position.y();
        switch (entity) {
            case Box b -> {
                this.map[y][x] = new Box(b.getPlayerId());
            }
            case Empty ignored -> {
                this.map[y][x] = new Empty();
            }
            case Player p -> {
                this.map[y][x] = new Player(p.getId());
            }
            case Wall ignored -> {
                this.map[y][x] = new Wall();
            }
        }
    }

    /**
     * Get all box destination positions as a set in the game map.
     *
     * @return a set of positions.
     */
    public @NotNull @Unmodifiable Set<Position> getDestinations() {
        // TODO
        return this.destinations;
    }

    public @NotNull Set<Position> getBoxPositions() {
        Set<Position> result = new HashSet<Position>();
        for (int y = 0; y < getMaxHeight(); ++y) {
            for (int x = 0; x < map[y].length; ++x) {
                if (getEntity(new Position(x, y)) instanceof Box) {
                    result.add(new Position(x, y));
                }
            }
        }
        return result;
    }

    /**
     * Get the undo limit of the game map.
     *
     * @return undo limit.
     */
    public Optional<Integer> getUndoLimit() {
        // TODO
        return this.undoLimit;
    }

    /**
     * Get all players' id as a set.
     *
     * @return a set of player id.
     */
    public Set<Integer> getPlayerIds() {
        // TODO
        var result = new HashSet<Integer>();
        for (var entry : this.playerPosition.keySet()) {
            result.add(entry - 'A');
        }
        return result;
    }

    /**
     * Get the maximum width of the game map.
     *
     * @return maximum width.
     */
    public int getMaxWidth() {
        // TODO
        return this.width;
    }

    /**
     * Get the maximum height of the game map.
     *
     * @return maximum height.
     */
    public int getMaxHeight() {
        // TODO
        return this.height;
    }

    public Map<Character, Position> getPlayerPosition() {
        return this.playerPosition;
    }
}
