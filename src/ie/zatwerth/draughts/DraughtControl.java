package ie.zatwerth.draughts;

import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.input.MouseEvent;

public class DraughtControl extends Control {

    // private fields of a reversi board
    DraughtBoard board;

    // constructor for the class
    public DraughtControl(Draughts instance) {
        setSkin(new DraughtControlSkin(this));

        newGame(instance);
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                board.mouseClicked(event.getX(), event.getY());
            }
        });
    }

    // overridden version of the resize method
    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        board.resize(width, height);
    }

    public void save() {
        board.save();
    }

    public void load() {
        board.load();
    }

    public void newGame(Draughts instance) {
        this.getChildren().clear();

        board = new DraughtBoard(instance);
        this.getChildren().add(board);
    }

    public void draw() {
        board.draw();
    }

}
