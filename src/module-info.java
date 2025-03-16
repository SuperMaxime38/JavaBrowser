module JavaBrowser {
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.web;
	requires jdk.jsobject;
	
	opens application to javafx.graphics, javafx.fxml;
}
