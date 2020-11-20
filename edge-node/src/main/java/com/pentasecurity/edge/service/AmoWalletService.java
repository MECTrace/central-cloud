package com.pentasecurity.edge.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pentasecurity.cryptowallet.JniWrapper;
import com.pentasecurity.cryptowallet.currencies.amo.AmoApiClient;
import com.pentasecurity.cryptowallet.network.NetworkService;
import com.pentasecurity.cryptowallet.network.amo.AmoJsonRpcService;
import com.pentasecurity.cryptowallet.network.amo.LegacyAmoApiClient;
import com.pentasecurity.cryptowallet.utils.PcwfUtils;
import com.pentasecurity.edge.model.AMOWallet;
import com.pentasecurity.edge.util.ConvertUtils;
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

    private int storageId = 2147482647; //Integer.MAX_VALUE - 1000;

	@Value("${edge.edge-id}")
    private String edgeId;

    public void registerTx(String dataId) throws GeneralSecurityException, IOException {
        File file = new File(keyStore);

        if (false == file.exists()) {
        	logger.debug(String.format("%10s %10s %5s %10s", edgeId, "keystore", "none", ""));
            return;
        }

        byte[] parcelID = JniWrapper.GenHashMessage("SHA256", dataId.getBytes());



        String hexParcelID = PcwfUtils.byteArrayToHexString(ConvertUtils.intToByteArray(storageId)) + PcwfUtils.byteArrayToHexString(parcelID);

        logger.debug(String.format("%10s %10s %5s %10s", edgeId, "amo", "id", hexParcelID));

        KeyStore keyStore = KeyStoreUtil.loadKeyStore(file, keyStorePW);
        AmoApiClient amoApiClient = getApiClient();
        AMOWallet amoWallet = new AMOWallet(amoApiClient);

        amoWallet.setKeyStore(keyStore);

        AMOWallet.Wallet wallet = amoWallet.getWallet(1L);
        //HDKeyPair hdKeyPair, WalletAccount account, BigInteger fee, byte[] custody, String target
        String tx = amoWallet.register(wallet.hdKeyPair, wallet.account, BigInteger.valueOf(0), dataId.getBytes(), hexParcelID);
        logger.debug(String.format("%10s %10s %5s %10s", edgeId, "amo", "tx", tx));
    }

    private AmoApiClient getApiClient() {
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