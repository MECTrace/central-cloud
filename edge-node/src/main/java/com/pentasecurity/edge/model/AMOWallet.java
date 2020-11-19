package com.pentasecurity.edge.model;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.pentasecurity.cryptowallet.currencies.Algorithm;
import com.pentasecurity.cryptowallet.currencies.CoinType;
import com.pentasecurity.cryptowallet.currencies.DefinedCurrency;
import com.pentasecurity.cryptowallet.currencies.amo.AmoApiClient;
import com.pentasecurity.cryptowallet.currencies.amo.AmoTransactionService;
import com.pentasecurity.cryptowallet.currencies.amo.AmoTransactionServiceImpl;
import com.pentasecurity.cryptowallet.currencies.amo.AmoWalletAccountService;
import com.pentasecurity.cryptowallet.exceptions.InvalidMnemonicException;
import com.pentasecurity.cryptowallet.key.HDKeyPair;
import com.pentasecurity.cryptowallet.key.HDKeyPairService;
import com.pentasecurity.cryptowallet.key.JniHDKeyPairService;
import com.pentasecurity.cryptowallet.key.JniMnemonicService;
import com.pentasecurity.cryptowallet.key.MnemonicService;
import com.pentasecurity.cryptowallet.key.amo.AmoHDKeyPairService;
import com.pentasecurity.cryptowallet.transaction.Transaction;
import com.pentasecurity.cryptowallet.utils.PcwfUtils;
import com.pentasecurity.cryptowallet.wallet.Account;
import com.pentasecurity.cryptowallet.wallet.WalletAccount;
import com.pentasecurity.cryptowallet.wallet.WalletAccountKeyID;
import com.pentasecurity.edge.util.KeyStoreUtil;

public class AMOWallet {
    private String network = "TESTNET";
    private KeyStore keyStore = null;

    public class Wallet {
        public HDKeyPair hdKeyPair;
        public WalletAccount account;

        public void reset() {
            KeyStoreUtil.resetHDKeyPair(hdKeyPair);
        }
    }

    private HDKeyPairService hdKeyPairService = new AmoHDKeyPairService();
    private JniHDKeyPairService jniHDKeyPairService = new JniHDKeyPairService();
    private MnemonicService mnemonicService = new JniMnemonicService();
    private AmoWalletAccountService amoWalletAccountService;
    private AmoTransactionService transactionService;

    private AmoApiClient apiClient;
    private CoinType coinType;

    public AMOWallet(AmoApiClient apiClient) {
        this.apiClient = apiClient;
        amoWalletAccountService = new AmoWalletAccountService(null, null);
        transactionService  = new AmoTransactionServiceImpl(hdKeyPairService, null, apiClient);
        if (null != network && network.equals("MAINNET")) {
            coinType = DefinedCurrency.getCoinType(DefinedCurrency.AMO);
        } else {
            coinType = DefinedCurrency.getCoinType(DefinedCurrency.AMOT);
        }
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }
    public boolean isInit() {
        return null != keyStore;
    }

    public boolean validateMnemonic(String mnemonic) {
        if (null != mnemonic) {
            List<String> list = Arrays.asList(mnemonic.split(" "));
            try {
                mnemonicService.validateMnemonic(list);
                return true;
            } catch (InvalidMnemonicException e) {
            }
        }
        return false;
    }

    public HDKeyPair deriveHDMasterKey(List<String> mnemonic) {
        return jniHDKeyPairService.deriveHDMasterKey(mnemonic, "", Algorithm.p256);
    }

    public List<String> generateMnemonic(String strMnemonic) {
        List<String> mnemonic;
        if (StringUtils.isEmpty(strMnemonic)) {
            mnemonic = mnemonicService.generateMnemonic(128);
        } else {
            //String strMnemonic = "alley endorse panda flight color initial clinic width sure stairs clutch uncle"; //1
            mnemonic = Arrays.asList(strMnemonic.split(" "));
        }
        return mnemonic;
    }

    public HDKeyPair deriveMasterHDKeyPair(List<String> mnemonic) {
        return jniHDKeyPairService.deriveHDMasterKey(mnemonic, "", Algorithm.p256);
    }

    public Wallet getWallet(HDKeyPair masterKey) {
        HDKeyPair keyPair;
        String hdPathString = "0/0";
        int[] path = PcwfUtils.getHDPath(hdPathString);

        DefinedCurrency amo = DefinedCurrency.AMO;
        if (null != network && network.equals("MAINNET")) {
            keyPair = hdKeyPairService.deriveHDKeyPair(masterKey, "44H/484H/0H");
        } else if (null != network && network.equals("TESTNET")) {
            keyPair = hdKeyPairService.deriveHDKeyPair(masterKey, "44H/1H/0H");
            amo = DefinedCurrency.AMOT;
        } else {
            throw new UnsupportedOperationException("network is supported.");
        }

        HDKeyPair hdKeyPair = hdKeyPairService.deriveHDKeyPair(keyPair, path, Algorithm.p256);
        String address = amoWalletAccountService.getAddress(hdKeyPair.getPublicKey(), amo, false);
        WalletAccount walletAccount = new WalletAccount(new WalletAccountKeyID(keyPair.getId(), path), coinType.getName(), address);

        Wallet wallet = new Wallet();
        wallet.account = walletAccount;
        wallet.hdKeyPair = hdKeyPair;

        KeyStoreUtil.resetHDKeyPair(keyPair);

        return wallet;
    }

    public Wallet getWallet(Long memberId) throws GeneralSecurityException {
        int path1 = (int)(memberId / Integer.MAX_VALUE);
        int path2 = (int)(memberId - (memberId / Integer.MAX_VALUE));

        int[] path = PcwfUtils.getHDPath(String.format("%d/%d", path1, path2));
        HDKeyPair keyPair;
        DefinedCurrency amo = DefinedCurrency.AMO;

        HDKeyPair masterKey = KeyStoreUtil.getHDKeyPair(keyStore);
        if (null != network && network.equals("MAINNET")) {
            keyPair = hdKeyPairService.deriveHDKeyPair(masterKey, "44H/484H/0H");
        } else if (null != network && network.equals("TESTNET")) {
            keyPair = hdKeyPairService.deriveHDKeyPair(masterKey, "44H/1H/0H");
            amo = DefinedCurrency.AMOT;
        } else {
            throw new UnsupportedOperationException("network is supported.");
        }

        KeyStoreUtil.resetHDKeyPair(masterKey);

        String keyId = keyPair.getId();
        HDKeyPair hdKeyPair = hdKeyPairService.deriveHDKeyPair(keyPair, path, Algorithm.p256);
        KeyStoreUtil.resetHDKeyPair(keyPair);

        String address = amoWalletAccountService.getAddress(hdKeyPair.getPublicKey(), amo, false);
        WalletAccount walletAccount = new WalletAccount(new WalletAccountKeyID(keyId, path), coinType.getName(), address);

        Wallet wallet = new Wallet();
        wallet.account = walletAccount;
        wallet.hdKeyPair = hdKeyPair;

        return wallet;

    }

    public String Transfer(HDKeyPair hdKeyPair, WalletAccount fromAccount, Account toAccount, BigInteger sendAmount, BigInteger fee) {

        Transaction transaction = transactionService.createTransfer(DefinedCurrency.AMO,
                fromAccount,
                hdKeyPair.getPrivateKey(),
                toAccount,
                sendAmount,
                fee);

        String txHash = apiClient.sendTransaction(transaction.getRawTransaction());

        return txHash;
    }

    public String Request(HDKeyPair hdKeyPair, WalletAccount account, BigInteger fee, String parcelId, BigInteger payment, String dealer, BigInteger dealerFee) {
        return Request(hdKeyPair, account, account.getAddress(), fee, parcelId, payment, dealer, dealerFee, null);
    }

    public String Request(HDKeyPair hdKeyPair, WalletAccount account, String recipient, BigInteger fee, String parcelId, BigInteger payment, String dealer, BigInteger dealerFee) {
        return Request(hdKeyPair, account, recipient, fee, parcelId, payment, dealer, dealerFee, null);
    }

    public String Request(HDKeyPair hdKeyPair, WalletAccount account, BigInteger fee, String parcelId, BigInteger payment, String dealer, BigInteger dealerFee, Map<String, Object> extra ) {
        return Request(hdKeyPair, account, account.getAddress(), fee, parcelId, payment, dealer, dealerFee, extra);
    }

    public String Request(HDKeyPair hdKeyPair, WalletAccount account, String recipient, BigInteger fee, String parcelId, BigInteger payment, String dealer, BigInteger dealerFee, Map<String, Object> extra ) {

        Transaction transaction = transactionService.createRequest(DefinedCurrency.AMO,
                account,
                recipient,
                hdKeyPair.getPrivateKey(),
                fee,
                parcelId,
                payment,
                dealer,
                dealerFee,
                extra);

        String txHash = apiClient.sendTransaction(transaction.getRawTransaction());

        return txHash;
    }

    public String Cancel(HDKeyPair hdKeyPair, WalletAccount fromAccount, BigInteger fee, String target) {

        Transaction transaction = transactionService.createCancel(DefinedCurrency.AMO,
                fromAccount,
                hdKeyPair.getPrivateKey(),
                fee,
                target,
                null);

        String txHash = apiClient.sendTransaction(transaction.getRawTransaction());

        return txHash;
    }

    public String register(HDKeyPair hdKeyPair, WalletAccount account, BigInteger fee, byte[] custody, String proxy_account, String target, Map<String, Object> extra) {

        Transaction transaction = transactionService.createRegister(DefinedCurrency.AMO,
                account,
                hdKeyPair.getPrivateKey(),
                proxy_account,
                fee,
                target,
                custody,
                extra);

        String txHash = apiClient.sendTransaction(transaction.getRawTransaction());

        return txHash;
    }
    public String register(HDKeyPair hdKeyPair, WalletAccount account, BigInteger fee, byte[] custody, String proxy_account, String target) {
        return register(hdKeyPair, account, fee, custody, proxy_account, target, null);
    }
    public String register(HDKeyPair hdKeyPair, WalletAccount account, BigInteger fee, byte[] custody, String target, Map<String, Object> extra) {
        return register(hdKeyPair, account, fee, custody, null, target, extra);
    }
    public String register(HDKeyPair hdKeyPair, WalletAccount account, BigInteger fee, byte[] custody, String target) {
        return register(hdKeyPair, account, fee, custody, null, target, null);
    }
    public String setup(HDKeyPair hdKeyPair, WalletAccount account, BigInteger fee, int storageId, String url, BigInteger registration_fee, BigInteger hosting_fee) {
        Transaction transaction = transactionService.createSetup(DefinedCurrency.AMO,
                account,
                hdKeyPair.getPrivateKey(),
                fee,
                storageId,
                url,
                registration_fee,
                hosting_fee);

        String txHash = apiClient.sendTransaction(transaction.getRawTransaction());

        return txHash;
    }
    public String close(HDKeyPair hdKeyPair, WalletAccount account, BigInteger fee, int storageId) {
        Transaction transaction = transactionService.createClose(DefinedCurrency.AMO,
                account,
                hdKeyPair.getPrivateKey(),
                fee,
                storageId);
        String txHash = apiClient.sendTransaction(transaction.getRawTransaction());
        return txHash;
    }
}
