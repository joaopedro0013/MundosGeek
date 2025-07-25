module marvel2 {
     requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires com.google.gson;
    requires java.desktop;
    requires javafx.media;

    opens com.marvel to javafx.fxml, javafx.graphics;
    opens com.pokemon to javafx.fxml, javafx.graphics;
    exports com.marvel;
    exports com.principal;
    exports com.pokemon;
}
