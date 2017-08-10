package ie.zatwerth.draughts;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.transform.Translate;

public class DraughtPiece extends Group {

    // private fields
    private int player; // the player that this piece belongs to
    private Ellipse piece; // ellipse representing the player's piece
    private Translate t; // translation for the player piece
    private boolean king = false;

    // default constructor for the class
    public DraughtPiece(int player) {
        this.player = player;
        piece = new Ellipse();
        t = new Translate();
        piece.getTransforms().add(t);

        setVisible(false);
        switch (player) {
            case 1: {
                piece.setFill(Color.MAROON);
                setVisible(true);
                break;
            }
            case 2: {
                piece.setFill(Color.WHITE);
                setVisible(true);
                break;
            }
        }

        getChildren().add(piece);
    }

    // overridden version of the resize method to give the piece the correct
    // size
    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        piece.setCenterX(width / 2.2);
        piece.setCenterY(height / 2.2);
        piece.setRadiusX((width / 2.2));
        piece.setRadiusY((height / 2.2));
    }

    // overridden version of the relocate method to position the piece correctly
    @Override
    public void relocate(double x, double y) {
        super.relocate(x, y);
        t.setX(x);
        t.setY(y);
    }

    // returns the type of this piece
    public int getPiece() {
        return player;
    }

    /**
     * @return the king
     */
    protected boolean isKing() {
        return king;
    }

    protected void setKing() {
        king = true;
        switch (player) {
            case 1: {
                piece.setFill(Color.RED);
                setVisible(true);
                break;
            }
            case 2: {
                piece.setFill(Color.GREY);
                setVisible(true);
                break;
            }
        }
    }

}
