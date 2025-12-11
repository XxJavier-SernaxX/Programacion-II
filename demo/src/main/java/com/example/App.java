package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("gui/main.fxml"));
        scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Sistema EPS - Gestión Médica");
        try {
            stage.getIcons().add(new javafx.scene.image.Image(App.class.getResourceAsStream("gui/icon.png")));
        } catch (Exception e) {
            System.err.println("Could not load icon: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
