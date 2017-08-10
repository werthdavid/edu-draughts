package ie.zatwerth.draughts;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class DraughtBoard extends Pane {

    private final static String SAVE_FILENAME = "save.xml";
    private final static String CURRENT_PLAYER = "currentPlayer";
    private final static String PLAYER_1_NAME = "player_1_name";
    private final static String PLAYER_2_NAME = "player_2_name";

    // rectangle that makes the background of the board
    private Rectangle background;
    // arrays for the lines that makeup the horizontal and vertical grid lines
    private Line[] horizontal;
    private Line[] vertical;
    // arrays holding translate objects for the horizontal and vertical grid
    // lines
    private Translate[] horizontal_t;
    private Translate[] vertical_t;
    // arrays for the internal representation of the board and the pieces that
    // are in place
    private DraughtPiece[][] render;
    private DraughtBackgroundPiece[][] backgroundArr;
    // the current player who is playing and who is his opposition
    private int current_player;
    private int opposing;
    // is the game currently in play
    private boolean in_play;
    // current scores of player 1 and player 2
    private int player1_score;
    private int player2_score;
    // the width and height of a cell in the board
    private double cell_width;
    private double cell_height;
    private Rectangle selected = null;
    private List<Rectangle> validMoves = new ArrayList<Rectangle>();
    private List<Rectangle> validJumps = new ArrayList<Rectangle>();
    private List<Rectangle> validGlobalJumps = new ArrayList<Rectangle>();
    private Draughts draughtsInstance;

    // default constructor for the class
    public DraughtBoard(Draughts instance) {
        horizontal = new Line[8];
        vertical = new Line[8];
        horizontal_t = new Translate[8];
        vertical_t = new Translate[8];

        this.draughtsInstance = instance;

        resetBoard();

        initialiseLinesBackground();
        initialiseBackgroundArr();
        initialiseRender();
        determineGlobalJumps();
    }

    public synchronized void mouseClicked(final double x, final double y) {
        int cx = (int) (x / cell_width);
        int cy = (int) (y / cell_height);

        if (!in_play) {
            return;
        }

        if (backgroundArr[cx][cy].getType() == 2) {
            return;
        }

        if ("".equals(draughtsInstance.getP1Name())
                || "".equals(draughtsInstance.getP2Name())) {
            return;
        }
        if (render[cx][cy] != null) {
            if (render[cx][cy].getPiece() == current_player) {
                if (selected != null) {
                    backgroundArr[(int) selected.getX()][(int) selected.getY()]
                            .toggleSelection();
                }
                selected = new Rectangle();
                selected.setX(cx);
                selected.setY(cy);
                backgroundArr[cx][cy].toggleSelection();
                determineValidMoves(cx, cy);
            }
        } else {
            if (selected != null) {
                if (canPlace(cx, cy)) {
                    placePiece(cx, cy);
                }
            }
        }
    }

    private void determineGlobalJumps() {
        validGlobalJumps = new ArrayList<Rectangle>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int type = getType(i, j);
                if (type == current_player) {
                    determineValidJumps(i, j, validGlobalJumps);
                }
            }
        }
    }

    private void determineValidJumps(int x, int y, List<Rectangle> validJumps) {
        // Only King can go backwards
        DraughtPiece piece = render[x][y];
        int type = piece.getPiece();
        boolean king = piece.isKing();
        int dy = 1 * (type == 1 ? 1 : -1);
        determineJump(x, y, 1, dy, validJumps);
        determineJump(x, y, -1, dy, validJumps);
        if (king) {
            determineJump(x, y, 1, dy * -1, validJumps);
            determineJump(x, y, -1, dy * -1, validJumps);
        }
    }

    private void determineValidMoves(int x, int y) {
        validMoves = new ArrayList<Rectangle>();
        validJumps = new ArrayList<Rectangle>();
        // Only King can go backwards
        DraughtPiece piece = render[x][y];
        int type = piece.getPiece();
        boolean king = piece.isKing();
        int dy = 1 * (type == 1 ? 1 : -1);

        determineJump(x, y, 1, dy, validJumps);
        determineJump(x, y, -1, dy, validJumps);
        if (king) {
            determineJump(x, y, 1, dy * -1, validJumps);
            determineJump(x, y, -1, dy * -1, validJumps);
        }

        // No Jump possible
        if (validJumps.isEmpty()) {
            determineValidMove(x, y, 1, dy);
            determineValidMove(x, y, -1, dy);
            if (king) {
                determineValidMove(x, y, 1, dy * -1);
                determineValidMove(x, y, -1, dy * -1);
            }
        }
    }

    private boolean determineJump(int x, int y, int dx, int dy,
                                  List<Rectangle> validJumps) {
        int type = getType(x + dx, y + dy);
        if (type != opposing) {
            return false;
        }
        type = getType(x + dx + dx, y + dy + dy);
        if (type != 0) {
            return false;
        }
        Rectangle rect = new Rectangle();
        rect.setX(x + dx + dx);
        rect.setY(y + dy + dy);
        validJumps.add(rect);
        return true;
    }

    private boolean determineValidMove(int x, int y, int dx, int dy) {
        int type = getType(x + dx, y + dy);
        if (type != 0) {
            return false;
        }
        Rectangle rect = new Rectangle();
        rect.setX(x + dx);
        rect.setY(y + dy);
        validMoves.add(rect);
        return true;
    }

    private boolean canPlace(int x, int y) {
        if (validJumps.isEmpty() && validMoves.isEmpty()) {
            return false;
        }

        if (!validGlobalJumps.isEmpty() && validJumps.isEmpty()) {
            return false;
        }

        for (Rectangle rect : validJumps) {
            if (rect.getX() == x && rect.getY() == y) {
                return true;
            }
        }

        for (Rectangle rect : validMoves) {
            if (rect.getX() == x && rect.getY() == y) {
                return true;
            }
        }

        return false;
    }

    private void placePiece(final int x, final int y) {
        // Unselect Background piece
        backgroundArr[(int) selected.getX()][(int) selected.getY()]
                .toggleSelection();
        // Move piece
        DraughtPiece piece = render[(int) selected.getX()][(int) selected
                .getY()];
        piece.relocate(x * cell_width, y * cell_height);
        render[(int) selected.getX()][(int) selected.getY()] = null;
        render[x][y] = piece;
        boolean jumped = jump(x, y);
        selected = null;
        boolean crowned = toggleKing(piece, y);

        updateScores();

        if (crowned) {
            swapPlayers();
            determineGlobalJumps();
        } else {
            determineGlobalJumps();
            if (!jumped) {
                swapPlayers();
                determineGlobalJumps();
            } else {
                if (validGlobalJumps.isEmpty()) {
                    swapPlayers();
                    determineGlobalJumps();
                }
            }
        }
        determineEndGame();

        printProgress();
    }

    private void printProgress() {
        draughtsInstance.printProgress(current_player, player1_score,
                player2_score, in_play, false);
    }

    public void draw() {
        in_play = false;
        draughtsInstance.printProgress(0, player1_score, player2_score, false,
                true);
    }

    private boolean jump(final int x, final int y) {
        if (selected == null) {
            return false;
        }
        int dx = (int) selected.getX();
        int dy = (int) selected.getY();

        int cx = -1;
        int cy = -1;
        int distanceX = Math.max(x, dx) - Math.min(x, dx);
        int distanceY = Math.max(y, dy) - Math.min(y, dy);

        if (distanceX == 2 && distanceY == 2) {
            if (x > dx) {
                cx = dx + 1;
            } else {
                cx = dx - 1;
            }
            if (y > dy) {
                cy = dy + 1;
            } else {
                cy = dy - 1;
            }
            DraughtPiece piece = render[cx][cy];
            render[cx][cy] = null;
            this.getChildren().remove(piece);
            return true;
        }
        return false;
    }

    private boolean toggleKing(DraughtPiece piece, final int y) {
        if (piece.isKing()) {
            return false;
        }
        switch (piece.getPiece()) {
            case 1: {
                if (y == 7) {
                    piece.setKing();
                    return true;
                }
                break;
            }
            case 2: {
                if (y == 0) {
                    piece.setKing();
                    return true;
                }
                break;
            }
        }
        return false;
    }

    // overridden version of the resize method to give the board the correct
    // size
    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        cell_height = height / 8;
        cell_width = width / 8;

        background.setWidth(width);
        background.setHeight(height);

        horizontalResizeRelocate(width);
        verticalResizeRelocate(height);

        compsResizeRelocate();
    }

    // public method for resetting the board
    public final void resetBoard() {
        this.getChildren().clear();
        in_play = true;
        current_player = 1;
        opposing = 2;
        player1_score = 12;
        player2_score = 12;
        printProgress();
    }

    // public method for resetting the game
    public final void resetGame() {
        resetBoard();
        initialiseLinesBackground();
        initialiseBackgroundArr();
        initialiseRender();
        determineGlobalJumps();
    }

    // private method that will initialise the background and the lines
    private void initialiseLinesBackground() {
        background = new Rectangle();
        background.setFill(Color.CYAN);
        this.getChildren().add(background);
        for (int i = 0; i < 8; i++) {
            Line h1 = new Line();
            h1.setStroke(Color.DARKGRAY);
            h1.setStartX(0);
            h1.setStartY(0);
            h1.setEndY(0);
            Translate t1h = new Translate(0, 0);
            horizontal_t[i] = t1h;
            h1.getTransforms().add(t1h);
            horizontal[i] = h1;
            this.getChildren().add(h1);

            Line v1 = new Line();
            v1.setStroke(Color.DARKGRAY);
            v1.setStartX(0);
            v1.setStartY(0);
            v1.setEndX(0);
            Translate t1v = new Translate(0, 0);
            vertical_t[i] = t1v;
            v1.getTransforms().add(t1v);
            vertical[i] = v1;
            this.getChildren().add(v1);
        }
    }

    // private method for resizing and relocating the horizontal lines
    private void horizontalResizeRelocate(final double width) {
        for (int i = 0; i < 8; i++) {
            horizontal[i].setEndX(width);
            horizontal_t[i].setY(cell_height * i);
        }
    }

    // private method for resizing and relocating the vertical lines
    private void verticalResizeRelocate(final double height) {
        for (int i = 0; i < 8; i++) {
            vertical[i].setEndY(height);
            vertical_t[i].setX(cell_width * i);
        }
    }

    // private method for swapping the players
    private void swapPlayers() {
        int sw = opposing;
        opposing = current_player;
        current_player = sw;
    }

    // private method for updating the player scores
    private void updateScores() {
        player1_score = 0;
        player2_score = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int type = getType(i, j);
                switch (type) {
                    case 1: {
                        player1_score++;
                        break;
                    }
                    case 2: {
                        player2_score++;
                        break;
                    }
                }
            }
        }
    }

    // private method for resizing and relocating all the pieces
    private void compsResizeRelocate() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (render[i][j] != null) {
                    render[i][j].relocate(i * cell_width, j * cell_height);
                    render[i][j].resize(cell_width, cell_height);
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                backgroundArr[i][j].relocate(i * cell_width, j * cell_height);
                backgroundArr[i][j].resize(cell_width, cell_height);
            }
        }
    }

    private int getType(int x, int y) {
        int retVal = -1;
        if (x < 8 && x >= 0 && y < 8 && y >= 0) {
            retVal = 0;
            if (render[x][y] != null) {
                retVal = render[x][y].getPiece();
            }
        }
        return retVal;
    }

    private void determineEndGame() {
        boolean noPieces = false;
        if (player1_score == 0 || player2_score == 0) {
            noPieces = true;
        }

        boolean canMove = false;
        if (canMove()) {
            canMove = true;
        }

        if (noPieces || !canMove) {
            in_play = false;
            determineWinner();
        }
    }

    private boolean canMove() {

        return true;
    }

    private void determineWinner() {
        int winner = 0;
        if (player1_score > player2_score) {
            winner = 1;
        } else if (player2_score > player1_score) {
            winner = 2;
        }
        draughtsInstance.setWinner(winner);
    }

    private void initialiseRender() {
        render = new DraughtPiece[8][8];
        int type = 1;
        boolean odd = true;

        for (int i = 0; i < 8; i++) {
            if (odd) {
                render[i][0] = new DraughtPiece(type);
                this.getChildren().add(render[i][0]);
                render[i][2] = new DraughtPiece(type);
                this.getChildren().add(render[i][2]);
            } else {
                render[i][1] = new DraughtPiece(type);
                this.getChildren().add(render[i][1]);
            }
            odd = !odd;
        }
        type++;
        odd = !odd;
        for (int i = 0; i < 8; i++) {
            if (odd) {
                render[i][5] = new DraughtPiece(type);
                this.getChildren().add(render[i][5]);
                render[i][7] = new DraughtPiece(type);
                this.getChildren().add(render[i][7]);
            } else {
                render[i][6] = new DraughtPiece(type);
                this.getChildren().add(render[i][6]);
            }
            odd = !odd;
        }
    }

    private void initialiseBackgroundArr() {
        backgroundArr = new DraughtBackgroundPiece[8][8];
        int type = 1;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                backgroundArr[i][j] = new DraughtBackgroundPiece(type);
                this.getChildren().add(backgroundArr[i][j]);
                type = (type == 1 ? 2 : 1);
            }
            type = (type == 1 ? 2 : 1);
        }
    }

    public void save() {
        Properties props = new Properties();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                DraughtPiece piece = render[i][j];
                if (piece != null) {
                    props.put(
                            i + "," + j,
                            (piece.isKing() ? "k" : "")
                                    + String.valueOf(piece.getPiece()));
                }
            }
        }
        props.put(CURRENT_PLAYER, String.valueOf(current_player));
        props.put(PLAYER_1_NAME, draughtsInstance.getP1Name());
        props.put(PLAYER_2_NAME, draughtsInstance.getP2Name());
        try {
            props.storeToXML(new FileOutputStream(new File(SAVE_FILENAME)),
                    String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        Properties props = new Properties();
        try {
            props.loadFromXML(new FileInputStream(new File(SAVE_FILENAME)));
        } catch (Exception e) {
            e.printStackTrace();
            props = null;
        }

        if (props != null) {
            resetBoard();
            initialiseLinesBackground();
            initialiseBackgroundArr();

            render = new DraughtPiece[8][8];
            StringTokenizer st = null;
            for (Object key : props.keySet()) {
                String keyString = key.toString();
                if (keyString.equals(CURRENT_PLAYER)) {
                    current_player = Integer.valueOf(props.getProperty(key
                            .toString()));
                    if (current_player == 1) {
                        opposing = 2;
                    } else {
                        opposing = 1;
                    }
                } else if (keyString.equals(PLAYER_1_NAME)) {
                    draughtsInstance
                            .setP1Name(props.getProperty(key.toString()));
                } else if (keyString.equals(PLAYER_2_NAME)) {
                    draughtsInstance
                            .setP2Name(props.getProperty(key.toString()));
                } else {
                    st = new StringTokenizer(keyString, ",");
                    int x = Integer.valueOf(st.nextToken());
                    int y = Integer.valueOf(st.nextToken());
                    String val = props.getProperty(key.toString());
                    int intval = 0;
                    boolean king = false;
                    if (val.startsWith("k")) {
                        king = true;
                        intval = Integer.valueOf(val.substring(1));
                    } else {
                        intval = Integer.valueOf(val);
                    }
                    DraughtPiece piece = new DraughtPiece(intval);
                    if (king) {
                        piece.setKing();
                    }
                    render[x][y] = piece;
                    this.getChildren().add(piece);
                }
            }
            updateScores();

            determineGlobalJumps();

            printProgress();
        }
    }

    /**
     * @return the in_play
     */
    protected boolean isIn_play() {
        return in_play;
    }

    /**
     * @param in_play the in_play to set
     */
    protected void setIn_play(boolean in_play) {
        this.in_play = in_play;
    }

}
