package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;


public class Main extends Application {
	private TabPane tabPane = new TabPane(); // Gestionnaire d'onglets

    @Override
    public void start(Stage primaryStage) {
        Button newTabButton = new Button("➕ Nouvel Onglet");
        newTabButton.setOnAction(event -> createNewTab("https://www.google.com"));

        BorderPane root = new BorderPane();
        root.setTop(newTabButton);
        root.setCenter(tabPane);

        createNewTab("https://www.google.com"); // Ouvrir un premier onglet

        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.setTitle("Navigateur avec Onglets et Historique");
        primaryStage.show();
    }

    private void createNewTab(String url) {
        Tab tab = new Tab("Nouvel Onglet");
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        TextField urlField = new TextField(url);
        Button backButton = new Button("←");
        Button forwardButton = new Button("→");
        ListView<String> historyList = new ListView<>();

        // Charger l'URL saisie
        urlField.setOnAction(event -> webEngine.load(urlField.getText()));
        webEngine.load(url);

        // Mise à jour du titre de l'onglet
        webEngine.titleProperty().addListener((obs, oldTitle, newTitle) -> {
            tab.setText(newTitle != null ? newTitle : "Nouvel Onglet");
        });

        // Gestion de l'historique
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                WebHistory history = webEngine.getHistory();
                historyList.getItems().clear();
                for (WebHistory.Entry entry : history.getEntries()) {
                    historyList.getItems().add(entry.getUrl());
                    urlField.setText(entry.getUrl());
                }
            }
        });

        // Boutons précédent / suivant
        backButton.setOnAction(event -> {
            WebHistory history = webEngine.getHistory();
            if (history.getCurrentIndex() > 0) {
                history.go(-1);
                urlField.setText(history.getCurrentIndex() == 0 ? url : history.getEntries().get(history.getCurrentIndex()).getUrl());
            }
        });

        forwardButton.setOnAction(event -> {
            WebHistory history = webEngine.getHistory();
            if (history.getCurrentIndex() < history.getEntries().size() - 1) {
                history.go(1);
                urlField.setText(history.getCurrentIndex() == 0 ? url : history.getEntries().get(history.getCurrentIndex()).getUrl());
            }
        });

        // Afficher une page en cliquant dans l'historique
        historyList.setOnMouseClicked(event -> {
            String selectedUrl = historyList.getSelectionModel().getSelectedItem();
            if (selectedUrl != null) {
                webEngine.load(selectedUrl);
                urlField.setText(selectedUrl);
            }
        });
        
        // Gestionnaire de téléchargements
        webEngine.locationProperty().addListener((obs, oldUrl, newUrl) -> {
        	System.out.println("changing page inside of the current webpage (new page): " + newUrl);
            if (newUrl.startsWith("data:")) {
                // Ne pas charger le lien 'data:', le traiter différemment
            	System.out.println("Triggered data protocol");
                saveDataProtocol(newUrl);
            } else if(newUrl.matches(".*\\.(pdf|zip|exe|mp3|mp4|jpg|png)$")){
                // Charger les fichiers classiques via HTTP(S)
            	System.out.println("Triggered file protocol");
                downloadFile(newUrl);
            }
        });
        
        webEngine.executeScript(
        	    "document.addEventListener('click', function(event) {" +
        	    "   let target = event.target.closest('a');" +
        	    "   if (target && target.href && target.href.startsWith('data:')) {" +
        	    "       jQuery.parseDataUrl(target.href);" + // Appelez votre fonction Java pour gérer le téléchargement
        	    "   }" +
        	    "}, true);"
        );



        // Interface de l'onglet
        HBox navBar = new HBox(5, backButton, forwardButton, urlField);
        BorderPane tabContent = new BorderPane();
        tabContent.setTop(navBar);
        tabContent.setCenter(webView);
        tabContent.setRight(historyList);
        tab.setContent(tabContent);
        tab.setClosable(true);

        // Ajouter et sélectionner l'onglet
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    // Fonction pour traiter le téléchargement "data:"
    private void saveDataProtocol(String dataUrl) {
        new Thread(() -> { // Exécuter en arrière-plan
            try {
                // Extraire les données du protocole "data:"
                String[] parts = dataUrl.split(",");
                String metadata = parts[0];
                String base64Data = parts[1];

                String extension = metadata.split(";")[0].split("/")[1];
                byte[] decodedBytes = Base64.getDecoder().decode(base64Data);

                // Sauvegarder le fichier décodé
                Path filePath = Paths.get(System.getProperty("user.home"), "Downloads", "file." + extension);
                Files.write(filePath, decodedBytes);
                System.out.println("Fichier enregistré sous : " + filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Fonction pour gérer le téléchargement de fichiers classiques
    @SuppressWarnings("deprecation")
    private void downloadFile(String fileUrl) {
        new Thread(() -> { // Exécuter en arrière-plan
            try (InputStream in = new URL(fileUrl).openStream()) {
                Path filePath = Paths.get(System.getProperty("user.home"), "Downloads", getFileName(fileUrl));
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Téléchargement terminé : " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String getFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }


	
	public static void main(String[] args) {
		launch(args);
	}
}
