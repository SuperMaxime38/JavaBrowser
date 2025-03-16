package application.downloads;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class DownloadManager {
    private final List<DownloadTask> downloads = new ArrayList<>();
    private final Button downloadButton = new Button();
    private final ProgressIndicator progressIndicator = new ProgressIndicator(0);
    private final ListView<String> downloadList = new ListView<>();
    private final VBox downloadPopup = new VBox(5);

    public DownloadManager() {
        // Charger une icône de téléchargement
        ImageView downloadIcon = new ImageView(new Image(getClass().getResourceAsStream("/download.png")));
        downloadIcon.setFitWidth(24);
        downloadIcon.setFitHeight(24);

        // Configuration du bouton
        downloadButton.setGraphic(downloadIcon);
        downloadButton.setOnAction(_ -> toggleDownloadPopup());

        // Configuration de l'affichage des téléchargements
        downloadPopup.getChildren().addAll(new Label("Téléchargements"), downloadList);
        downloadPopup.setAlignment(Pos.CENTER);
        downloadPopup.setStyle("-fx-background-color: white; -fx-padding: 10;");
        downloadPopup.setVisible(false);
    }

    public Button getDownloadButton() {
        return downloadButton;
    }

    public void startDownload(String fileUrl) {
        String fileName = getFileName(fileUrl);
        String savePath = System.getProperty("user.home") + "/Downloads/" + fileName;

        DownloadTask task = new DownloadTask(fileUrl, savePath);
        downloads.add(task);

        // Lier la progression au ProgressIndicator
        progressIndicator.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(_ -> {
            Platform.runLater(() -> {
                downloadList.getItems().add(fileName + " - Terminé");
                System.out.println("Successfully downloaded to: " + savePath);
                updateDownloadIcon();
            });
        });

        task.setOnFailed(_ -> {
            Platform.runLater(() -> {
                downloadList.getItems().add(fileName + " - Échec");
                System.err.println("Failed to download: " + savePath);
                updateDownloadIcon();
            });
        });

        new Thread(task).start();
        updateDownloadIcon();
    }

    private void updateDownloadIcon() {
        Platform.runLater(() -> {
            if (downloads.isEmpty()) {
                downloadButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/download.png"))));
            } else {
                downloadButton.setGraphic(progressIndicator);
            }
        });
    }

    private void toggleDownloadPopup() {
        downloadPopup.setVisible(!downloadPopup.isVisible());
    }
    
    @SuppressWarnings("deprecation")
	private String getFileName(String url) {
        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            return path.substring(path.lastIndexOf("/") + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "fichier_inconnu";
        }
    }
}
