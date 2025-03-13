package application;
	
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;


public class Main extends Application {
	private WebEngine webEngine;
    private ListView<String> historyView;

    @Override
    public void start(Stage primaryStage) {
        WebView webView = new WebView();
        webEngine = webView.getEngine();
        historyView = new ListView<>();

        // Barre d’adresse
        TextField urlField = new TextField("https://www.google.com");
        urlField.setOnAction(event -> webEngine.load(urlField.getText()));

        // Gestion de l'historique
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                updateHistory();
            }
        });

        // Sélectionner une page de l’historique
        historyView.setOnMouseClicked(event -> {
            int index = historyView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                webEngine.getHistory().go(index - webEngine.getHistory().getCurrentIndex());
            }
        });

        // Layout
        BorderPane root = new BorderPane();
        root.setTop(urlField);
        root.setCenter(webView);
        root.setRight(historyView);
        
        // ----- BOUTONS ------
        
        Button backButton = new Button("⬅️");
        Button forwardButton = new Button("➡️");

        // Navigation arrière
        backButton.setOnAction(event -> {
            WebHistory history = webEngine.getHistory();
            if (history.getCurrentIndex() > 0) {
                history.go(-1);
            }
        });

        // Navigation avant
        forwardButton.setOnAction(event -> {
            WebHistory history = webEngine.getHistory();
            if (history.getCurrentIndex() < history.getEntries().size() - 1) {
                history.go(1);
            }
        });

        // Ajout des boutons à la barre d’adresse
        HBox navBar = new HBox(backButton, forwardButton, urlField);
        root.setTop(navBar);
        
        // ##### BOUTONS ######

        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.setTitle("Navigateur avec Historique");
        primaryStage.show();

        webEngine.load(urlField.getText()); // Charger la page initiale
    }

    private void updateHistory() {
        WebHistory history = webEngine.getHistory();
        historyView.getItems().clear();
        for (WebHistory.Entry entry : history.getEntries()) {
            historyView.getItems().add(entry.getUrl());
        }
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}
