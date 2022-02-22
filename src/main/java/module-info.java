module socialNetwork.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.jensd.fx.glyphs.fontawesome;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    requires org.apache.pdfbox;

    opens socialNetwork.ui to javafx.fxml;
    exports socialNetwork.ui;
    exports socialNetwork.gui;
    opens socialNetwork.gui to javafx.fxml;
    exports socialNetwork.guiControllers;
    opens socialNetwork.guiControllers to javafx.fxml;
    exports socialNetwork.utilitaries;
    opens socialNetwork.utilitaries to javafx.fxml;

    opens socialNetwork.domain.models to javafx.graphics, javafx.fxml, javafx.base;

}