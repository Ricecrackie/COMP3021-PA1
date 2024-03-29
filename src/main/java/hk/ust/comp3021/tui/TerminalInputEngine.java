package hk.ust.comp3021.tui;

import hk.ust.comp3021.actions.*;
import hk.ust.comp3021.game.InputEngine;
import hk.ust.comp3021.utils.StringResources;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Scanner;

/**
 * An input engine that fetches actions from terminal input.
 */
public class TerminalInputEngine implements InputEngine {

    /**
     * The {@link Scanner} for reading input from the terminal.
     */
    private final Scanner terminalScanner;

    /**
     * @param terminalStream The stream to read terminal inputs.
     */
    public TerminalInputEngine(InputStream terminalStream) {
        this.terminalScanner = new Scanner(terminalStream);
    }

    /**
     * Fetch an action from user in terminal to process.
     *
     * @return the user action.
     */
    @Override
    public @NotNull Action fetchAction() {
        // This is an example showing how to read a line from the Scanner class.
        // Feel free to change it if you do not like it.
        final var inputLine = terminalScanner.nextLine();

        // TODO
        var inputUpper = inputLine.toUpperCase();
        switch (inputUpper) {
            case "A":
                return new Move.Left(0);
            case "S":
                return new Move.Down(0);
            case "W":
                return new Move.Up(0);
            case "D":
                return new Move.Right(0);
            case "H":
                return new Move.Left(1);
            case "J":
                return new Move.Down(1);
            case "K":
                return new Move.Up(1);
            case "L":
                return new Move.Right(1);
            case "U":
                return new Undo(-1);
            case "EXIT":
                return new Exit(-1);
            default:
                return new InvalidInput(-1, StringResources.INVALID_INPUT_MESSAGE);
        }

        //throw new NotImplementedException();
    }
}
