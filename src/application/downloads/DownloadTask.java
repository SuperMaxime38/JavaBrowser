package application.downloads;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javafx.concurrent.Task;

public class DownloadTask extends Task<Void> {
    private final String fileUrl;
    private final String savePath;

    public DownloadTask(String fileUrl, String savePath) {
        this.fileUrl = fileUrl;
        this.savePath = savePath;
    }

	@SuppressWarnings("deprecation")
	@Override
    protected Void call() throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int fileSize = connection.getContentLength(); // Taille du fichier à télécharger
        try (InputStream in = connection.getInputStream();
             OutputStream out = new FileOutputStream(savePath)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                updateProgress(totalBytesRead, fileSize);
            }
        }
        return null;
    }
}