package com.pentasecurity.edge.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.pentasecurity.cryptowallet.key.HDKeyPair;
import com.pentasecurity.edge.model.AMOWallet;

public class KeyStoreUtil {
    public static KeyStore createKeyStore(File path, char[] password, List<String> mnemonic) throws IOException, GeneralSecurityException {
        AMOWallet amoWallet = new AMOWallet(null);
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null, password);
        HDKeyPair hdKeyPair = amoWallet.deriveMasterHDKeyPair(mnemonic);
        updateKey(keyStore, hdKeyPair);
        saveKeyStore(keyStore, path, password);

        return keyStore;
    }

    public static KeyStore loadKeyStore(File path, char[] password) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(path);
            keyStore.load(fileInputStream, password);
        } finally {
            if (null != fileInputStream) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {}
            }
        }
        return keyStore;
    }

    public static void saveKeyStore(KeyStore keyStore, File path, char[] password) throws GeneralSecurityException, IOException {
        OutputStream outputStream = null;
        if (null == path) {
            return;
        }
        try {
            if (false == path.getParentFile().exists()) {
                path.getParentFile().mkdirs();
            }
            outputStream = new FileOutputStream(path);
            keyStore.store(outputStream, password);
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void updateKey(KeyStore keyStore, HDKeyPair masterKey) throws GeneralSecurityException, IOException {
        SecretKey privateKey = new SecretKeySpec(masterKey.getPrivateKey(), 0, masterKey.getPrivateKey().length, "ECDSA");
        SecretKey publicKey = new SecretKeySpec(masterKey.getPublicKey(), 0, masterKey.getPublicKey().length, "ECDSA");
        SecretKey chainCode = new SecretKeySpec(masterKey.getChainCode(), 0, masterKey.getChainCode().length, "ECDSA");
        SecretKey id = new SecretKeySpec(masterKey.getId().getBytes(), 0, masterKey.getId().getBytes().length, "ECDSA");

        keyStore.setKeyEntry("AMO_PrivateKey", privateKey, "AMO_PrivateKey".toCharArray(), null);
        keyStore.setKeyEntry("AMO_PublicKey", publicKey, "AMO_PublicKey".toCharArray(), null);
        keyStore.setKeyEntry("AMO_ChainCode", chainCode, "AMO_ChainCode".toCharArray(), null);
        keyStore.setKeyEntry("AMO_ID", id, "AMO_ID".toCharArray(), null);
    }

    public static byte[] getKey(KeyStore keyStore, String alias, char[] password) throws GeneralSecurityException {
        Key key = keyStore.getKey(alias, (password ));
        return key.getEncoded();
    }

    public static HDKeyPair getHDKeyPair(KeyStore keyStore) throws GeneralSecurityException {
        byte[] privateKey = getKey(keyStore, "AMO_PrivateKey", "AMO_PrivateKey".toCharArray());
        byte[] publicKey = getKey(keyStore, "AMO_PublicKey","AMO_PublicKey".toCharArray());
        byte[] chainCode = getKey(keyStore, "AMO_ChainCode","AMO_ChainCode".toCharArray());

        HDKeyPair hdKeyPair = new HDKeyPair(privateKey, publicKey, chainCode);
        return hdKeyPair;
    }

    public static void resetHDKeyPair(HDKeyPair hdKeyPair) {
        if (null != hdKeyPair) {
            if (null != hdKeyPair.getPrivateKey()) {
                Arrays.fill(hdKeyPair.getPrivateKey(), Byte.MIN_VALUE);
            }
            if (null != hdKeyPair.getPublicKey()) {
                Arrays.fill(hdKeyPair.getPublicKey(), Byte.MIN_VALUE);
            }
            if (null != hdKeyPair.getChainCode()) {
                Arrays.fill(hdKeyPair.getChainCode(), Byte.MIN_VALUE);
            }
        }
    }
    public static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data

        return bytes;
    }
}
