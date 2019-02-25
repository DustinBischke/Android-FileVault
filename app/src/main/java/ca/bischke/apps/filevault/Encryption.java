package ca.bischke.apps.filevault;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class Encryption
{
    private Context context;
    private KeyStore keyStore;
    private IvParameterSpec iv;
    private byte[] salt;

    public Encryption(Context context)
    {
        this.context = context;
        this.keyStore = new KeyStore(context);
    }

    private byte[] getRandomByteArray(int size)
    {
        SecureRandom random = new SecureRandom();
        byte[] randomByteArray = new byte[size];
        random.nextBytes(randomByteArray);

        return randomByteArray;
    }

    private IvParameterSpec getIV()
    {
        if (iv == null)
        {
            String key = context.getString(R.string.preference_iv);
            byte[] ivBytes = keyStore.getBytes(key);

            if (ivBytes == null)
            {
                ivBytes = getRandomByteArray(16);
                keyStore.setBytes(key, ivBytes);
            }

            iv = new IvParameterSpec(ivBytes);
        }

        return iv;
    }

    private byte[] getSalt()
    {
        if (salt == null)
        {
            String key = context.getString(R.string.preference_salt);
            byte[] saltBytes = keyStore.getBytes(key);

            if (saltBytes == null)
            {
                saltBytes = getRandomByteArray(16);
                keyStore.setBytes(key, saltBytes);
            }

            salt = saltBytes;
        }

        return salt;
    }

    private SecretKey getSecretKey(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        int iterations = 10000;
        int outputLength = 128;

        char[] passwordArray = password.toCharArray();
        KeySpec keySpec = new PBEKeySpec(passwordArray, salt, iterations, outputLength);

        String algorithm = "PBKDF2WithHmacSHA1";
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);

        return secretKey;
    }

    private void doCrypto(int cipherMode, String password, File inputFile, File outputFile)
            throws GeneralSecurityException, IOException
    {
        SecretKey secretKey = getSecretKey(password, getSalt());
        String algorithm = "AES/CBC/PKCS5Padding";
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(cipherMode, secretKey, getIV());

        FileInputStream inputStream = new FileInputStream(inputFile);
        byte[] inputBytes = new byte[(int) inputFile.length()];
        inputStream.read(inputBytes);

        byte[] outputBytes = cipher.doFinal(inputBytes);

        FileOutputStream outputStream = new FileOutputStream(outputFile);
        outputStream.write(outputBytes);

        inputStream.close();
        outputStream.close();
    }

    public void encryptFile(String password, File inputFile, File outputFile)
            throws GeneralSecurityException, IOException
    {
        doCrypto(Cipher.ENCRYPT_MODE, password, inputFile, outputFile);
    }

    public void decryptFile(String password, File inputFile, File outputFile)
            throws GeneralSecurityException, IOException
    {
        doCrypto(Cipher.DECRYPT_MODE, password, inputFile, outputFile);
    }

    public void encryptDirectory(String password, File directory)
            throws GeneralSecurityException, IOException
    {
        if (directory.isDirectory())
        {
            SecretKey secretKey = getSecretKey(password, getSalt());
            String algorithm = "AES/CBC/PKCS5Padding";
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, getIV());

            File[] fileArray = directory.listFiles();

            for (File file : fileArray)
            {
                FileInputStream inputStream = new FileInputStream(file);
                byte[] inputBytes = new byte[(int) file.length()];
                inputStream.read(inputBytes);

                byte[] outputBytes = cipher.doFinal(inputBytes);

                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(outputBytes);

                inputStream.close();
                outputStream.close();
            }
        }
    }

    public void decryptDirectory(String password, File directory)
            throws GeneralSecurityException, IOException
    {
        if (directory.isDirectory())
        {
            SecretKey secretKey = getSecretKey(password, getSalt());
            String algorithm = "AES/CBC/PKCS5Padding";
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, getIV());

            File[] fileArray = directory.listFiles();

            for (File file : fileArray)
            {
                FileInputStream inputStream = new FileInputStream(file);
                byte[] inputBytes = new byte[(int) file.length()];
                inputStream.read(inputBytes);

                byte[] outputBytes = cipher.doFinal(inputBytes);

                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(outputBytes);

                inputStream.close();
                outputStream.close();
            }
        }
    }
}
