package net.martins.ansible.fx;

import javafx.animation.RotateTransition;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.GeneralSecurityException;

@Component
public class SimpleController {

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

    @FXML
    public void initialize() {
        this.alert = new Alert(Alert.AlertType.NONE);
        this.alert.getDialogPane().getStylesheets().add(getClass().getResource("ansible-vault-fx.css").toExternalForm());
        this.alert.getDialogPane().setStyle("dialog");

        final Font font = Font.loadFont(SimpleController.class.getResource("AndaleMono.ttf").toExternalForm(),14);

        this.textArea.setFont(font);
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
