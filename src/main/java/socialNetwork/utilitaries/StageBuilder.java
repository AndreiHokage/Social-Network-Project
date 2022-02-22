package socialNetwork.utilitaries;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;

import java.io.IOException;
import java.util.Optional;

public class StageBuilder {
    public static UnorderedPair<Stage, FXMLLoader> buildStage(Class sourceClass , String path, Optional<String> pathCSSFile, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(sourceClass.getResource(path));
        Parent root = loader.load();
        Stage stage = new Stage();
        Image icon = new Image("images/applicationLogo.png");
        stage.getIcons().add(icon);
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        Scene newScene = new Scene(root);
        if(pathCSSFile.isPresent())
            newScene.getStylesheets().add(sourceClass.getResource(pathCSSFile.get()).toExternalForm());
        stage.setScene(newScene);
        return new UnorderedPair<>(stage,loader);
    }
}
