package net.martins.ansible.fx;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Font;
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
        VaultEncryptedParser vaultEncryptedParser = new VaultEncryptedParser();
        try {
            vaultEncryptedParser.parseEncryptedText(getTextAreaContent("Decrypt"));
            textArea.setText(vaultEncryptedParser.getDecryptedVault(getPassword()));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setHeaderText("Decrypt");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    public void encryptTextArea() {
        VaultDecryptedParser vaultDecryptedParser = new VaultDecryptedParser();
        try {
            vaultDecryptedParser.parseEncryptedText(getTextAreaContent("Encrypt"));
            textArea.setText(vaultDecryptedParser.getEncryptedVault(getPassword()));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setHeaderText("Encrypt");
            alert.setContentText(e.getMessage());
            alert.show();
        }
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
        this.decryptButton.setOnAction(actionEvent -> this.decryptTextArea());
        this.decryptButton.setFont(controlsFont);
    }
}
