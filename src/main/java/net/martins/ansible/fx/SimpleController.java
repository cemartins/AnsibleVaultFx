package net.martins.ansible.fx;

import javafx.animation.RotateTransition;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Scanner;

@Component
public class SimpleController {

    private static final long MAX_FILE_SIZE = 512000;

    private final HostServices hostServices;

    private Alert alert;

    @FXML
    public TextArea textArea;

    @FXML
    public TextField passwordTextField;

    @FXML
    public Button encryptButton;

    @FXML
    public Button decryptButton;

    public SimpleController(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void decryptTextArea() {

        // Play a little animation
        RotateTransition rotateTrans = new RotateTransition();
        rotateTrans.setAxis(Rotate.X_AXIS);
        rotateTrans.setByAngle(90);
        rotateTrans.setDuration(Duration.millis(500));
        rotateTrans.setNode(textArea);
        rotateTrans.setCycleCount(2);
        rotateTrans.setAutoReverse(true);
        rotateTrans.play();

        // decrypt the text
        VaultEncryptedParser vaultEncryptedParser = new VaultEncryptedParser();
        try {
            vaultEncryptedParser.parseEncryptedText(getTextAreaContent("Decrypt"));
            textArea.setText(vaultEncryptedParser.getDecryptedVault(getPassword()));
        } catch (GeneralSecurityException e) {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setHeaderText("Decrypt");
            alert.setContentText(e.getMessage());
            alert.show();
        }

        // Show the decrypted text
        rotateTrans.play();

    }

    public void encryptTextArea() {

        // Play a little animation
        RotateTransition rotateTrans = new RotateTransition();
        rotateTrans.setAxis(Rotate.X_AXIS);
        rotateTrans.setByAngle(90);
        rotateTrans.setDuration(Duration.millis(500));
        rotateTrans.setNode(textArea);
        rotateTrans.setCycleCount(2);
        rotateTrans.setAutoReverse(true);
        rotateTrans.play();

        // Encrypt the text
        VaultDecryptedParser vaultDecryptedParser = new VaultDecryptedParser();
        try {
            vaultDecryptedParser.parseEncryptedText(getTextAreaContent("Encrypt"));
            textArea.setText(vaultDecryptedParser.getEncryptedVault(getPassword()));
        } catch (GeneralSecurityException e) {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setHeaderText("Encrypt");
            alert.setContentText(e.getMessage());
            alert.show();
        }

        // Show the encrypted text
        rotateTrans.play();

    }

    /**
     * Checks that the password field has been filled in.
     * @return The passwords that was filled in
     */
    private String getPassword() {
        String password = passwordTextField.getText();
        if(!StringUtils.hasText(password)) {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setHeaderText("Password");
            alert.setContentText("Please fill in the password field.");
            alert.show();
            passwordTextField.requestFocus();
            throw new IllegalStateException("Password not filled in");
        }
        return password;
    }

    private String getTextAreaContent(String operation) {
        String text = textArea.getText();
        if(!StringUtils.hasText(text)) {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setHeaderText(operation);
            alert.setContentText("Please fill in the text area.");
            alert.show();
            textArea.requestFocus();
            throw new IllegalStateException("Text Area not filled in");
        }
        return text;
    }

    private void dragFileOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        } else {
            event.consume();
        }
    }

    /**
     * Called when something is dropped on the text area
     * @param event
     */
    private void dropFile(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            File file = db.getFiles().get(0);
            try {
                readFileIntoTextArea(file);
            } catch (IOException e) {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setHeaderText("Drop File");
                alert.setContentText(e.getMessage());
                alert.show();
            }
        }
        event.setDropCompleted(success);
        event.consume();

    }

    private void readFileIntoTextArea(File file) throws IOException {
        if (file.length() > MAX_FILE_SIZE) {
            throw new IOException("File is too large");
        }
        if(file.isDirectory()) {
            throw new IOException("Wrong type of file");
        }
        if( ! isGoodFileType(file) ) {
            throw new IOException("Wrong type of file");
        }
        Scanner scanner = new Scanner(file);
        textArea.clear();
        while(scanner.hasNext()) {
            textArea.appendText(scanner.nextLine().concat("\n"));
        }
    }

    private boolean isGoodFileType(File file) throws IOException {

        if(file.getName().endsWith(".properties")) {
            return true;
        }
        final String contentType = Files.probeContentType(file.toPath());
        if(contentType.contains("yaml")) {
            return true;
        }
        if(contentType.contains("text")) {
            return true;
        }
        return false;
    }

    @FXML
    public void initialize() {
        this.alert = new Alert(Alert.AlertType.NONE);
        this.alert.getDialogPane().getStylesheets().add(getClass().getResource("ansible-vault-fx.css").toExternalForm());
        this.alert.getDialogPane().setStyle("dialog");

        final Font font = Font.loadFont(SimpleController.class.getResource("AndaleMono.ttf").toExternalForm(),14);

        this.textArea.setFont(font);
        this.textArea.setOnDragOver( dragEvent -> dragFileOver(dragEvent));
        this.textArea.setOnDragDropped(dragEvent -> dropFile(dragEvent));
        this.passwordTextField.setFont(font);
        final Font controlsFont = Font.font("SansSerif");
        this.encryptButton.setOnAction(actionEvent -> this.encryptTextArea());
        this.encryptButton.setFont(controlsFont);
        this.encryptButton.disableProperty().bind(Bindings.isEmpty(passwordTextField.textProperty()));
        this.decryptButton.setOnAction(actionEvent -> this.decryptTextArea());
        this.decryptButton.setFont(controlsFont);
        this.decryptButton.disableProperty().bind(Bindings.isEmpty(passwordTextField.textProperty()));
    }
}
