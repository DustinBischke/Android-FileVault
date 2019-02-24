package ca.bischke.apps.filevault;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA
{
    private final static String algorithm = "SHA-512";

    public static byte[] getHashBytes(String string) throws NoSuchAlgorithmException
    {
        byte[] bytes = Convert.getByteArrayFromString(string);

        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(bytes);
        return messageDigest.digest();
    }
}
