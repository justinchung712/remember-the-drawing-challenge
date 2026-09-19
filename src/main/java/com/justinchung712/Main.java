package com.justinchung712;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private Stage stage;

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;

    private static final int CANVAS_WIDTH = 1000;
    private static final int CANVAS_HEIGHT = 500;

    private static final String DEFAULT_FONT = Font.getDefault().getFamily();

    private int numDrawings = 16;
    private int secsPerDrawing = 5;
    private int remainingSecs;

    private static final List<String> WORDS = List.of(
            "Umbrella",
            "Elephant",
            "Bottle",
            "Michael Jackson",
            "Bicycle",
            "Pizza",
            "Airplane",
            "Guitar",
            "Snowman",
            "Castle",
            "Robot",
            "Banana",
            "Camera",
            "Dinosaur",
            "Cake",
            "Sunglasses",
            "Rocket",
            "Octopus",
            "Laptop",
            "Palm Tree",
            "Hamburger",
            "Spider",
            "Toothbrush",
            "Penguin",
            "Lighthouse",
            "Crown",
            "Sailboat",
            "Backpack",
            "Dragon",
            "Traffic Light",
            "Teddy Bear",
            "Watermelon");

    private List<String> sessionWords;

    private List<DrawingRecord> drawings;
    private int curDrawingIx;
    private int curRecallIx;

    private int score;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Remember the Drawing Challenge");
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);

        showHomeScreen();

        stage.show();
    }

    private void showHomeScreen() {
        Label title = new Label("Remember the Drawing Challenge");
        title.setFont(Font.font(DEFAULT_FONT, FontWeight.BOLD, 36));

        Label numDrawingsLabel = new Label();
        Slider numDrawingsSlider = new Slider(1, WORDS.size(), WORDS.size() / 2);
        numDrawingsSlider.setMajorTickUnit(1);
        numDrawingsSlider.setMinorTickCount(0);
        numDrawingsSlider.setShowTickMarks(true);
        numDrawingsSlider.setSnapToTicks(true);
        numDrawingsSlider.setPrefWidth(WINDOW_WIDTH / 3);

        updateNumDrawings(numDrawingsLabel, numDrawingsSlider.getValue());
        numDrawingsSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateNumDrawings(numDrawingsLabel, newVal.doubleValue()));

        Label secsPerDrawingLabel = new Label();
        Slider secsPerDrawingSlider = new Slider(1, 15, 5);
        secsPerDrawingSlider.setMajorTickUnit(1);
        secsPerDrawingSlider.setMinorTickCount(0);
        secsPerDrawingSlider.setShowTickMarks(true);
        secsPerDrawingSlider.setSnapToTicks(true);
        secsPerDrawingSlider.setPrefWidth(WINDOW_WIDTH / 3);

        updateSecsPerDrawing(secsPerDrawingLabel, secsPerDrawingSlider.getValue());
        secsPerDrawingSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateSecsPerDrawing(secsPerDrawingLabel, newVal.doubleValue()));

        VBox numDrawingsBox = new VBox(10, numDrawingsLabel, numDrawingsSlider);
        VBox secsPerDrawingBox = new VBox(10, secsPerDrawingLabel, secsPerDrawingSlider);
        HBox gameOptsBox = new HBox(100, numDrawingsBox, secsPerDrawingBox);
        gameOptsBox.setAlignment(Pos.CENTER);

        Button startButton = new Button("Start");
        startButton.setFont(Font.font(DEFAULT_FONT, FontWeight.BOLD, 24));
        startButton.setOnAction(event -> {
            numDrawings = (int) numDrawingsSlider.getValue();
            secsPerDrawing = (int) secsPerDrawingSlider.getValue();

            prepGame();
        });

        VBox root = new VBox(50, title, gameOptsBox, startButton);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    private void updateNumDrawings(Label label, double value) {
        int intVal = (int) Math.round(value);
        label.setText("Number of Drawings: " + intVal);
    }

    private void updateSecsPerDrawing(Label label, double value) {
        int intVal = (int) Math.round(value);
        label.setText("Seconds per Drawing: " + intVal);
    }

    private void prepGame() {
        List<String> shuffledWords = new ArrayList<>(WORDS);
        Collections.shuffle(shuffledWords);

        sessionWords = new ArrayList<>(shuffledWords.subList(0, numDrawings));
        drawings = new ArrayList<>(numDrawings);
        curDrawingIx = 0;
        curRecallIx = 0;
        score = 0;
        
        Label countdownLabel = new Label("3");
        countdownLabel.setFont(Font.font(DEFAULT_FONT, FontWeight.BOLD, 100));

        StackPane root = new StackPane(countdownLabel);
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));

        Timeline countdown = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        event -> countdownLabel.setText("2")
                ),
                new KeyFrame(
                        Duration.seconds(2),
                        event -> countdownLabel.setText("1")
                ),
                new KeyFrame(
                        Duration.seconds(3),
                        event -> {
                            countdownLabel.setText("GO!");
                            countdownLabel.setTextFill(Color.FORESTGREEN);
                        }
                ),
                new KeyFrame(
                        Duration.seconds(4),
                        event -> showDrawingScreen()
                )
        );

        countdown.play();
    }

    private void showDrawingScreen() {
        if (curDrawingIx >= numDrawings) {
            showRecallIntro();
            return;
        }

        String word = sessionWords.get(curDrawingIx);

        Label progressLabel = new Label((curDrawingIx + 1) + "/" + numDrawings);
        progressLabel.setStyle("-fx-font-size: 60px;");
        progressLabel.setFont(Font.font(DEFAULT_FONT, 60));

        Label wordLabel = new Label(word);
        wordLabel.setFont(Font.font(DEFAULT_FONT, FontWeight.BOLD, 80));

        Label timerLabel = new Label(secsPerDrawing + "");
        timerLabel.setFont(Font.font(DEFAULT_FONT, FontWeight.BOLD, 60));

        Canvas canvas = newCanvas();

        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setMaxWidth(CANVAS_WIDTH);
        canvasContainer.setMaxHeight(CANVAS_HEIGHT);
        canvasContainer.setStyle("-fx-border-color: black; -fx-border-width: 2;");

        HBox header = new HBox(200, progressLabel, wordLabel, timerLabel);
        header.setAlignment(Pos.CENTER);

        VBox root = new VBox(40, header, canvasContainer);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        
        startDrawingTimer(word, canvas, timerLabel);
    }

    private void startDrawingTimer(String word, Canvas canvas, Label timerLabel) {
        remainingSecs = secsPerDrawing;

        Timeline drawingTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSecs--;
            timerLabel.setText(remainingSecs + "");
        }));

        drawingTimer.setCycleCount(secsPerDrawing);
        drawingTimer.setOnFinished(event -> saveDrawing(word, canvas));
        drawingTimer.play();
    }

    private void saveDrawing(String word, Canvas canvas) {
        WritableImage snapshot = new WritableImage(CANVAS_WIDTH, CANVAS_HEIGHT);

        canvas.snapshot(new SnapshotParameters(), snapshot);

        drawings.add(new DrawingRecord(word, snapshot));

        curDrawingIx++;

        PauseTransition pause = new PauseTransition(Duration.millis(500));
        pause.setOnFinished(event -> showDrawingScreen());
        pause.play();
    }

    private void showRecallIntro() {
        Label timeUp = new Label("Time's up!");
        timeUp.setFont(Font.font(DEFAULT_FONT, FontWeight.BOLD, 80));
        
        StackPane root = new StackPane(timeUp);
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> {
            Label label = new Label("Can you remember what you drew?");
            label.setFont(Font.font(DEFAULT_FONT, FontWeight.BOLD, 60));
            root.getChildren().setAll(label);

            Collections.shuffle(drawings);

            PauseTransition pause2 = new PauseTransition(Duration.seconds(4));
            pause2.setOnFinished(event2 -> showRecallScreen());
            pause2.play();
        });
        pause.play();
    }

    private void showRecallScreen() {
        if (curRecallIx >= numDrawings) {
            showFinalScore();
            return;
        }

        DrawingRecord curRecord = drawings.get(curRecallIx);

        Label progressLabel = new Label((curRecallIx + 1) + "/" + numDrawings);
        progressLabel.setFont(Font.font(DEFAULT_FONT, 30));

        Label text = new Label("What did you draw?");
        text.setFont(Font.font(DEFAULT_FONT, 40));

        ImageView imageView = new ImageView(curRecord.drawing());

        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setMaxWidth(CANVAS_WIDTH);
        imageContainer.setMaxHeight(CANVAS_HEIGHT);
        imageContainer.setStyle("-fx-border-color: black; -fx-border-width: 2;");

        TextField answerField = new TextField();
        answerField.setPromptText("Type your answer here...");
        answerField.setMaxWidth(400);
        answerField.setFont(Font.font(24));

        Button submitButton = new Button("Submit");
        submitButton.setFont(Font.font(24));

        Label resultLabel = new Label();
        resultLabel.setFont(Font.font(DEFAULT_FONT, FontWeight.BOLD, 24));

        Runnable submitAnswer = () -> {
            String answer = normalize(answerField.getText());
            String curWord = normalize(curRecord.word());

            answerField.setDisable(true);
            submitButton.setDisable(true);

            if (answer.equals(curWord)) {
                score++;
                resultLabel.setText("Correct!");
                resultLabel.setTextFill(Color.FORESTGREEN);
            } else {
                resultLabel.setText("Incorrect, this was supposed to be: " + curRecord.word());
                resultLabel.setTextFill(Color.FIREBRICK);
            }

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                curRecallIx++;
                showRecallScreen();
            });
            pause.play();
        };

        submitButton.setOnAction(event -> submitAnswer.run());
        answerField.setOnAction(event -> submitAnswer.run());

        HBox header = new HBox(60, text, progressLabel);
        header.setAlignment(Pos.CENTER);
        HBox submitRow = new HBox(20, answerField, submitButton);
        submitRow.setAlignment(Pos.CENTER);

        VBox root = new VBox(10, header, imageContainer, submitRow, resultLabel);
        root.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));

        answerField.requestFocus();
    }

    public void showFinalScore() {
        Label gameFinishLabel = new Label("Game Finish!");
        gameFinishLabel.setFont(Font.font(DEFAULT_FONT, FontWeight.BOLD, 40));

        int percentage = (int) Math.round(100.0 * score / numDrawings);
        Label scoreLabel = new Label("Score: " + score + "/" + numDrawings + " = " + percentage + "%");
        scoreLabel.setFont(Font.font(DEFAULT_FONT, FontWeight.BOLD, 32));

        Button newGameButton = new Button("New Game");
        newGameButton.setFont(Font.font(DEFAULT_FONT, FontWeight.BOLD, 20));

        newGameButton.setOnAction(event -> showHomeScreen());

        VBox root = new VBox(25, gameFinishLabel, scoreLabel, newGameButton);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    private String normalize(String str) {
        return str.toLowerCase().replace(" ", "");
    }

    public Canvas newCanvas() {
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        canvas.setOnMousePressed(event -> {
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
        });
        canvas.setOnMouseDragged(event -> {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
        });

        return canvas;
    }

    private record DrawingRecord(String word, WritableImage drawing) {}

    public static void main(String[] args) {
        launch();
    }
}
