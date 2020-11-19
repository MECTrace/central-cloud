package com.pentasecurity.edge.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pentasecurity.cryptowallet.currencies.amo.AmoApiClient;
import com.pentasecurity.cryptowallet.network.NetworkService;
import com.pentasecurity.cryptowallet.network.amo.AmoJsonRpcService;
import com.pentasecurity.cryptowallet.network.amo.LegacyAmoApiClient;
import com.pentasecurity.edge.model.AMOWallet;
import com.pentasecurity.edge.util.KeyStoreUtil;

@Service
public class AmoWalletService {
	Logger logger = LoggerFactory.getLogger("mainLogger");

    private String keyStore = "./edgenode.jceks";
    private char[] keyStorePW = "edgenode".toCharArray();

    /* AMO Node */
    private String SERVER_URL = "http://172.105.213.114:26657";
    private String USER = "REPLACED";
    private String PASSWORD = "REPLACED";

    private int storageId = 2147482648; //Integer.MAX_VALUE - 999;

    @PostConstruct
    public void init() {
    	try {
        	createKeyStore();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    public void createKeyStore() throws IOException, GeneralSecurityException {
        File file = new File(keyStore);

        if (file.exists()) {
            logger.debug("The keystore exists and cannot be created.");
        	return;
        }
        AMOWallet amoWallet = new AMOWallet(null);
        String mnemonic = null;  //  or mnemonic list
        List<String> list = amoWallet.generateMnemonic(mnemonic);
        KeyStoreUtil.createKeyStore(file, keyStorePW, list);
        Arrays.fill(keyStorePW, Character.MIN_VALUE);
        logger.debug(String.format("Key store creation was successful. [%s]", file.getAbsoluteFile()));
    }

    public void createStorage() throws IOException, GeneralSecurityException {
        File file = new File(keyStore);

        if (false == file.exists()) {
        	logger.debug("The keystore not exists");
            return;
        }

        KeyStore keyStore = KeyStoreUtil.loadKeyStore(file, keyStorePW);

        AmoApiClient amoApiClient = getApiClient();
        AMOWallet amoWallet = new AMOWallet(amoApiClient);

        amoWallet.setKeyStore(keyStore);
        Arrays.fill(keyStorePW, Character.MIN_VALUE);

        AMOWallet.Wallet wallet = amoWallet.getWallet(1L);
        //HDKeyPair hdKeyPair, WalletAccount account, BigInteger fee, int storageId, String url, BigInteger registration_fee, BigInteger hosting_fee
        String tx = amoWallet.setup(wallet.hdKeyPair, wallet.account, BigInteger.valueOf(0), storageId, "", BigInteger.valueOf(0), BigInteger.valueOf(0));
        System.out.println(String.format("StorageID: %d    tx: %s", storageId, tx));
    }

    public AmoApiClient getApiClient() {
        AmoJsonRpcService amoJsonRpcService = getService(SERVER_URL, USER, PASSWORD, AmoJsonRpcService.class);
        return new LegacyAmoApiClient(amoJsonRpcService);
    }

    private <T> T getService(String url, String user, String passwd, Class<T> clazz) {
        NetworkService apiService = new NetworkService();
        apiService//.HTTPLogging(true)
                .basicAuth(user, passwd)
                .baseUrl(url);

        return apiService.createService(clazz);
    }
}