package ca.bischke.apps.filevault;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Base64;

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

public class FileEncryption
{
    private final String TAG = "FileVault";
    private final String STORAGE_ROOT = Environment.getExternalStorageDirectory().toString();
    private final String STORAGE_VAULT = STORAGE_ROOT + File.separator + "FileVault";
    private Context context;
    private IvParameterSpec iv;
    private byte[] salt;

    public FileEncryption(Context context)
    {
        this.context = context;
    }

    public String getSTORAGE_VAULT()
    {
        return STORAGE_VAULT;
    }

    private byte[] getByteArrayFromString(String string)
    {
        return Base64.decode(string, Base64.DEFAULT);
    }

    private String getStringFromByteArray(byte[] bytes)
    {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
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
            String ivPreference = "FV-IV";
            SharedPreferences sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);

            // if SharedPreference already exists
            if (sharedPreferences.contains(ivPreference))
            {
                String ivString = sharedPreferences.getString(ivPreference, null);
                byte[] ivBytes = getByteArrayFromString(ivString);

                iv = new IvParameterSpec(ivBytes);
                return iv;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            byte[] ivBytes = getRandomByteArray(16);
            String ivString = getStringFromByteArray(ivBytes);
            editor.putString(ivPreference, ivString);
            editor.apply();

            iv = new IvParameterSpec(ivBytes);
            return iv;
        }

        return iv;
    }

    private byte[] getSalt()
    {
        if (salt == null)
        {
            String saltPreference = "FV-Salt";
            SharedPreferences sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);

            // if SharedPreference already exists
            if (sharedPreferences.contains(saltPreference))
            {
                String saltString = sharedPreferences.getString(saltPreference, null);
                salt = getByteArrayFromString(saltString);

                return salt;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            salt = getRandomByteArray(16);
            String saltString = getStringFromByteArray(salt);
            editor.putString(saltPreference, saltString);
            editor.apply();

            return salt;
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

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);

        return secretKey;
    }

    private void doCrypto(int cipherMode, String password, File inputFile, File outputFile)
            throws GeneralSecurityException, IOException
    {
        SecretKey secretKey = getSecretKey(password, getSalt());
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
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

    public void encrypt(String password, File inputFile, File outputFile)
            throws GeneralSecurityException, IOException
    {
        doCrypto(Cipher.ENCRYPT_MODE, password, inputFile, outputFile);
    }

    public void decrypt(String password, File inputFile, File outputFile)
            throws GeneralSecurityException, IOException
    {
        doCrypto(Cipher.DECRYPT_MODE, password, inputFile, outputFile);
    }
}
