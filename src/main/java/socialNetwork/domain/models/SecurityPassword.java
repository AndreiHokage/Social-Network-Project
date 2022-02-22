package socialNetwork.domain.models;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SecurityPassword {
    private String key;
    private Cipher cipher;
    java.security.Key aesKey;

    public SecurityPassword() {
        try {
            key = "Bar12345Bar12345";
            cipher = Cipher.getInstance("AES");
            aesKey =  new SecretKeySpec(key.getBytes(),"AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public String encryptPassword(String inputPassword) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(inputPassword.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public boolean checkPassword(String inputPassword, String encryptedStoredPassword) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        String decrypted = new String(cipher.doFinal(Base64.getDecoder().decode(encryptedStoredPassword.getBytes())));
        return inputPassword.equals(decrypted);
    }

}
