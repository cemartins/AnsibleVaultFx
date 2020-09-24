# JavaFX Ansible Vault Application

This application allows you to handle Ansible encrypted vaults.

### Easy peasy

Paste an encrypted vault variable into the text area   
![encrypted](site/images/decrypted_vault.png)
... And press `Decrypt`   
![decrypted](site/images/encrypted_vault.png)
Or press `Encrypt` on a clear text vault variable to encrypt it   
![encrypted_again](site/images/decrypted_vault.png)

### Building and running

Maven build (requires Java 11) `mvn clean install`

To start the application, just double-click on the generated `ansible-vault-fx-<version>.jar` file.
    
### Notices and Limitations
Requires Java 11 or higher
The application handles versions 1.1 and 1.2 format of the vaults. It will cry and crash and burn with any previous versions of the vaults.
