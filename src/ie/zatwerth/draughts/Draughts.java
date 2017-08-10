package ie.zatwerth.draughts;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Draughts extends Application {

    private VBox vb_mainlayout;
    private StackPane sp_mainlayout;
    private HBox hb_control;
    private HBox hb_names;
    private DraughtControl control;
    private MenuBar menubar;
    private Button btn_draw;
    private Label lbl_p1name;
    private Label lbl_p2name;
    private Label lbl_scores;
    private Label lbl_next;
    private TextField txt_p1name;
    private TextField txt_p2name;

    // entry point into our program for launching our javafx applicaton
    public static void main(String[] args) {
        launch(args);
    }

    // overridden init method
    @Override
    public void init() {
        vb_mainlayout = new VBox();
        sp_mainlayout = new StackPane();
        hb_control = new HBox();
        hb_names = new HBox();

        menubar = new MenuBar();
        fillMenu(menubar);

        lbl_p1name = new Label("Player 1 Name: ");
        lbl_p2name = new Label(" Player 2 Name: ");
        lbl_scores = new Label("");
        lbl_next = new Label("");
        txt_p1name = new TextField();
        txt_p2name = new TextField();
        btn_draw = new Button("Draw");
        btn_draw.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                btnDrawPressed();
            }
        });

        hb_control.getChildren().addAll(lbl_p1name, txt_p1name, lbl_p2name,
                txt_p2name, lbl_scores, lbl_next);

        vb_mainlayout.getChildren().addAll(menubar, btn_draw, hb_control,
                hb_names, lbl_scores, lbl_next, sp_mainlayout);
        sp_mainlayout.setPrefSize(800, 800);

        control = new DraughtControl(this);
        sp_mainlayout.getChildren().add(control);

        lbl_next.setText("Enter names first");

        // HBox.setHgrow(btn_draw, Priority.ALWAYS);
        btn_draw.setPrefWidth(Double.MAX_VALUE);

    }

    private void fillMenu(MenuBar menubar) {
        Menu menuFile = new Menu("File");
        menubar.getMenus().add(menuFile);

        Draughts instance = this;
        MenuItem mi_new = new MenuItem("New Game");
        mi_new.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                control.newGame(instance);
            }
        });

        MenuItem mi_save = new MenuItem("Save");
        mi_save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                control.save();
            }
        });

        MenuItem mi_load = new MenuItem("Load");
        mi_load.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                control.load();
            }
        });

        MenuItem mi_quit = new MenuItem("Quit");
        mi_quit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Platform.exit();
            }
        });

        menuFile.getItems().addAll(mi_new, mi_save, mi_load, mi_quit);
    }

    // overridden start method
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Draughts");
        primaryStage.setScene(new Scene(vb_mainlayout, 800, 800));
        primaryStage.show();
    }

    // overridden stop method
    @Override
    public void stop() {

    }

    private void btnDrawPressed() {
        Stage dialogStage = new Stage();
        Button btnYes = new Button("Yes");
        btnYes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                control.draw();
                dialogStage.close();
            }
        });
        Button btnNo = new Button("No");
        btnNo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialogStage.close();
            }
        });
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setScene(new Scene(VBoxBuilder
                .create()
                .children(new Text("Do both Players agree to a Draw?"), btnYes,
                        btnNo).alignment(Pos.CENTER).padding(new Insets(5))
                .build()));
        dialogStage.show();
    }

    public String getP1Name() {
        return txt_p1name.getText();
    }

    public void setP1Name(String name) {
        txt_p1name.setText(name);
    }

    public String getP2Name() {
        return txt_p2name.getText();
    }

    public void setP2Name(String name) {
        txt_p2name.setText(name);
    }

    public void setWinner(int player) {

    }

    public void printProgress(int nextPlayer, int p1Score, int p2Score,
                              boolean inProgress, boolean draw) {
        StringBuilder sbNext = new StringBuilder();
        StringBuilder sbScores = new StringBuilder();
        if (inProgress) {
            sbNext.append("Player " + nextPlayer + " "
                    + (nextPlayer == 1 ? getP1Name() : getP2Name()) + " ("
                    + (nextPlayer == 1 ? "red" : "white") + ") is next");

            sbScores.append("Player 1 " + getP1Name() + " (red): " + p1Score
                    + " pieces left.");
            sbScores.append(" \t ");
            sbScores.append("Player 2 " + getP2Name() + " (white): " + p2Score
                    + " pieces left.");
        } else {
            sbNext.append("Game is over");
            if (draw) {
                sbScores.append("Draw");
            } else {
                if (p1Score > p2Score) {
                    sbScores.append("Player 1 " + getP1Name() + " (red) won");
                } else if (p2Score > p1Score) {
                    sbScores.append("Player 2 " + getP2Name() + " (white) won");
                } else {
                    sbScores.append("Draw");
                }
            }
        }
        lbl_next.setText(sbNext.toString());
        lbl_scores.setText(sbScores.toString());
    }

}
