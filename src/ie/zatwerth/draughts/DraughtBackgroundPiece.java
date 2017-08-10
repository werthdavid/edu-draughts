package ie.zatwerth.draughts;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;

public class DraughtBackgroundPiece extends Group {

    // private fields
    private int type;
    private boolean selected = false;
    private Rectangle piece;
    private Translate t;

    // default constructor for the class
    public DraughtBackgroundPiece(int type) {
        this.type = type;
        piece = new Rectangle();
        t = new Translate();
        piece.getTransforms().add(t);

        switch (type) {
            case 1: {
                piece.setFill(Color.LIGHTGREEN);
                break;
            }
            case 2: {
                piece.setFill(Color.WHITE);
                break;
            }
        }

        getChildren().add(piece);
    }

    public void toggleSelection() {
        if (selected) {
            switch (type) {
                case 1: {
                    piece.setFill(Color.LIGHTGREEN);
                    break;
                }
                case 2: {
                    piece.setFill(Color.WHITE);
                    break;
                }
            }
        } else {
            piece.setFill(Color.ORCHID);
        }
        selected = !selected;
    }

    // overridden version of the resize method to give the piece the correct
    // size
    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        piece.setWidth(width);
        piece.setHeight(height);

    }

    // overridden version of the relocate method to position the piece correctly
    @Override
    public void relocate(double x, double y) {
        super.relocate(x, y);
        t.setX(x);
        t.setY(y);
    }

    // returns the type of this piece
    public int getType() {
        return type;
    }

}
