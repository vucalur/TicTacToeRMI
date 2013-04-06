package common.model;

import common.enums.Sign;
import common.exceptions.BoardFieldNotEmptyException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class Board implements Serializable {
    private final Sign[][] b = {{Sign.EMPTY, Sign.EMPTY, Sign.EMPTY}, {Sign.EMPTY, Sign.EMPTY, Sign.EMPTY}, {Sign.EMPTY, Sign.EMPTY, Sign.EMPTY}};

    public void place(Sign sign, int col, int row) throws BoardFieldNotEmptyException {
        if (col < 0 || col > 2 || row < 0 || row > 2) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (b[col][row] != Sign.EMPTY) {
            throw new BoardFieldNotEmptyException();
        }
        b[col][row] = sign;
    }

    public Sign whoWins() {
        final int[] zeroOneTwo = {0, 1, 2};
        for (Sign sign : EnumSet.of(Sign.CIRCLE, Sign.CROSS)) {
            for (int col : zeroOneTwo) {
                if (b[col][0] == b[col][1] && b[col][1] == b[col][2] && b[col][2] == sign) {
                    return sign;
                }
            }
            if (b[0][0] == b[1][1] && b[1][1] == b[2][2] && b[2][2] == sign) {
                return sign;
            }
            if (b[0][2] == b[1][1] && b[1][1] == b[2][0] && b[2][0] == sign) {
                return sign;
            }
        }

        int blanks = 0;
        for (Sign[] row : b) {
            for (Sign sign : row) {
                if (sign == Sign.EMPTY)
                    ++blanks;
            }
        }
        return (blanks == 0) ? Sign.EMPTY : null;
    }

    public String getCommunicate(Sign yourSign) {
        Sign hasWon = whoWins();
        if (hasWon == Sign.EMPTY) {
            return "There was a tie";
        } else {
            if (hasWon == yourSign) {
                return "You won !";
            } else {
                return "You lost";
            }
        }
    }

    public boolean someoneHasWonOrTie() {
        return whoWins() != null;
    }

    public String getView() {
        StringBuilder sb = new StringBuilder();
        for (Sign[] row : b) {
            sb.append('|');
            for (Sign sign : row) {
                sb.append(sign.text);
            }
            sb.append("|\n");
        }
        return String.valueOf(sb);
    }

    public void makeRandomBotMove() {
        List<Integer> shuffled1 = Arrays.asList(0, 1, 2);
        List<Integer> shuffled2 = Arrays.asList(0, 1, 2);
        Collections.shuffle(shuffled1);
        Collections.shuffle(shuffled2);
        for (int i : shuffled1) {
            for (int j : shuffled2) {
                if (b[i][j] == Sign.EMPTY) {
                    b[i][j] = Sign.BOT_SIGN;
                    return;
                }
            }
        }
    }
}
