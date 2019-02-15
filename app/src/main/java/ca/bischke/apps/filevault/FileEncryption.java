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
    private Context context;
    private SharedPreferences sharedPreferences;
    private IvParameterSpec iv;
    private byte[] salt;

    public FileEncryption(Context context)
    {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
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

    private boolean sharedPreferenceExists(String key)
    {
        return sharedPreferences.contains(key);
    }

    private byte[] getSharedPreference(String key)
    {
        if (sharedPreferenceExists(key))
        {
            String valueString = sharedPreferences.getString(key, null);
            return getByteArrayFromString(valueString);
        }

        return null;
    }

    private void saveSharedPreference(String key, byte[] valueBytes)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String value = getStringFromByteArray(valueBytes);
        editor.putString(key, value);
        editor.apply();
    }

    private IvParameterSpec getIV()
    {
        if (iv == null)
        {
            String key = context.getString(R.string.preference_iv);
            byte[] ivBytes = getSharedPreference(key);

            if (ivBytes == null)
            {
                ivBytes = getRandomByteArray(16);
                saveSharedPreference(key, ivBytes);
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
            byte[] saltBytes = getSharedPreference(key);

            if (saltBytes == null)
            {
                saltBytes = getRandomByteArray(16);
                saveSharedPreference(key, saltBytes);
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
