package net.martins.ansible.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class StageListener implements ApplicationListener<StageReadyEvent> {

    private final String applicationTitle;
    private final Resource fxml;
    private final ApplicationContext ac;
    private final Integer width;
    private final Integer height;

    public StageListener(@Value("${spring.application.ui.title}") String applicationTitle,
                         @Value("classpath:/ui.fxml") Resource fxml, ApplicationContext ac,
                         @Value("${spring.application.ui.window.width}") Integer width,
                         @Value("${spring.application.ui.window.height}") Integer height) {
        this.applicationTitle = applicationTitle;
        this.fxml = fxml;
        this.ac = ac;
        this.width = width;
        this.height = height;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent stageReadyEvent) {

        try {
            final Stage window = stageReadyEvent.getStage();
            final URL url = this.fxml.getURL();
            final FXMLLoader fxmlLoader = new FXMLLoader( url );
            fxmlLoader.setControllerFactory(ac::getBean);
            final Parent root = fxmlLoader.load();
            final Scene scene = new Scene(root, this.width, this.height);
            window.setScene(scene);
            window.setTitle(this.applicationTitle);
            window.show();
        } catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
}
