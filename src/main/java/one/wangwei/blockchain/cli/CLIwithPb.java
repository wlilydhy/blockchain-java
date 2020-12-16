package one.wangwei.blockchain.cli;

import lombok.extern.slf4j.Slf4j;
import one.wangwei.blockchain.Net.Client;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.block.Blockchain;
import one.wangwei.blockchain.pb.Strategy.*;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pow.ProofOfWork;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.TXOutput;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.transaction.UTXOSet;
import one.wangwei.blockchain.util.Base58Check;
import one.wangwei.blockchain.util.SerializeUtils;
import one.wangwei.blockchain.wallet.Wallet;
import one.wangwei.blockchain.wallet.WalletUtils;
import org.apache.commons.cli.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 命令行解析器
 *
 * @author wangwei
 * @date 2018/03/08
 */
@Slf4j
public class CLIwithPb {

    private String[] args;
    private Options options = new Options();

    public CLIwithPb(String[] args) {
        this.args = args;

        Option helpCmd = Option.builder("h").desc("show help").build();
        options.addOption(helpCmd);

        Option address = Option.builder("address").hasArg(true).desc("Source wallet address").build();
        Option sendFrom = Option.builder("from").hasArg(true).desc("Source wallet address").build();
        Option sendTo = Option.builder("to").hasArg(true).desc("Destination wallet address").build();
        Option sendAmount = Option.builder("amount").hasArg(true).desc("Amount to send").build();
        Option TxHash = Option.builder("TxHash").hasArg(true).desc("SPVs TxHash").build();

        options.addOption(address);
        options.addOption(sendFrom);
        options.addOption(sendTo);
        options.addOption(sendAmount);
        options.addOption(TxHash);
    }

    /**
     * 命令行解析入口
     */
    public void parse() {
        this.validateArgs(args);
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            switch (args[0]) {
                //从零开始的区块链建造
                case "createblockchain":
                    String createblockchainAddress = cmd.getOptionValue("address");
                    if (StringUtils.isBlank(createblockchainAddress)) {
                        help();
                    }
                    this.createBlockchain(createblockchainAddress);
                    break;
                //获取余额
                case "getbalance":
                    String getBalanceAddress = cmd.getOptionValue("address");
                    if (StringUtils.isBlank(getBalanceAddress)) {
                        help();
                    }
                    this.getBalance(getBalanceAddress);
                    break;
                //转账交易创建
                case "send":
                    String sendFrom = cmd.getOptionValue("from");
                    String sendTo = cmd.getOptionValue("to");
                    String sendAmount = cmd.getOptionValue("amount");
                    if (StringUtils.isBlank(sendFrom) ||
                            StringUtils.isBlank(sendTo) ||
                            !NumberUtils.isDigits(sendAmount)) {
                        help();
                    }
                    this.send(sendFrom, sendTo, Integer.valueOf(sendAmount));
                    break;
                //直接进行一个矿的挖
                case "mineblock":
                    //从交易池中搞点交易出来，再搞个铸币交易，开挖
                    //光挖也不成，还得广播
                    String coinbaseAddress = cmd.getOptionValue("address");
                    if (StringUtils.isBlank(coinbaseAddress)) {
                        help();
                    }
                    this.mineblock(coinbaseAddress);
                    break;
                //下载区块，懂的都懂
                case "downloadblockchain":
                    this.downloadBlockchain();
                    break;
                //下载区块头，懂得都懂
                case "downloadBlockHead" :
                    this.downloadBlockHead();
                    break;
                //下载交易，懂得都懂
                case "downloadTx" :
                    this.downloadTx();
                    break;
                //下载IP，懂得都懂
                case "getIp" :
                    this.getIp();
                    break;
                //SPV,需要提供交易哈希作为参数
                case "SPV" :
                    String TxHash = cmd.getOptionValue("TxHash");
                    if (StringUtils.isBlank(TxHash)) {
                        help();
                        break;
                    }
                    this.SPV(TxHash);
                    break;
                //创建钱包
                case "createwallet":
                    this.createWallet();
                    break;
                //辅助功能：打印钱包地址
                case "printaddresses":
                    this.printAddresses();
                    break;
                //辅助功能：看看现在区块链啥样的
                case "printchain":
                    this.printChain();
                    break;
                //辅助功能：不知道干啥用的
                case "h":
                    this.help();
                    break;
                default:
                    this.help();
            }
        } catch (Exception e) {
            log.error("Fail to parse cli command ! ", e);
        } finally {
            //RocksDBUtils.getInstance().closeDB();
        }
    }

    /**
     * 从交易池中搞点交易出来，再搞个铸币交易，开挖
     * 光挖也不成，还得广播
     * 然后保存到本地数据库里面子
     * @param address
     * @throws IOException
     * @throws DecoderException
     */

    private void mineblock(String address) throws IOException, DecoderException {
        int maxcount=50;
        Map<String,byte[]> txbucket = RocksDBUtils.getInstance().getTxBucket();
        //System.out.println("txbucket's size is"+txbucket.size());
        maxcount = Math.min(txbucket.size(),maxcount);
        Iterator<Map.Entry<String,byte[]>> iterator = txbucket.entrySet().iterator();
        Blockchain blockchain= new Blockchain(RocksDBUtils.getInstance().getLastBlockHash());
        Transaction transactions[] = new Transaction[maxcount+1];
        int count=0;
        while(iterator.hasNext()) {
            Map.Entry<String, byte[]> entry = iterator.next();
            byte[] transactionByte = entry.getValue();
            transactions[count]= (Transaction) SerializeUtils.deserialize(transactionByte);
            RocksDBUtils.getInstance().deletetransacion(entry.getKey());
            count++;
            if(count>=maxcount){
                break;
            }
        }
        transactions[maxcount]=Transaction.newCoinbaseTX(address,"Bolero!!!");
        //开挖并且广播，这里应该有个中断机制，如果别人挖到了我就停止（线程通信）
        Block block=blockchain.mineBlock(transactions);

        //Client.broadcastBlock(block);
        Utils.getInstance().broadcastBlock(block);
        //更新UTXOset

    }




    /**
     * 验证入参
     *
     * @param args
     */
    private void validateArgs(String[] args) {
        if (args == null || args.length < 1) {
            help();
        }
    }

    /**
     * 创建区块链
     *
     * @param address
     */
    private void createBlockchain(String address) throws DecoderException {
        Blockchain blockchain = Blockchain.createBlockchain(address);
        Utils.getInstance().broadcastBlock(RocksDBUtils.getInstance().getBlock(blockchain.getLastBlockHash()));
        UTXOSet utxoSet = new UTXOSet(blockchain);
        utxoSet.reIndex();
        log.info("Done ! ");

    }

    /**
     * 创建钱包
     *
     * @throws Exception
     */
    private void createWallet() throws Exception {
        Wallet wallet = WalletUtils.getInstance().createWallet();
        log.info("wallet address : " + wallet.getAddress());
    }

    /**
     * 打印钱包地址
     */
    private void printAddresses() {
        Set<String> addresses = WalletUtils.getInstance().getAddresses();
        if (addresses == null || addresses.isEmpty()) {
            log.info("There isn't address");
            return;
        }
        for (String address : addresses) {
            log.info("Wallet address: " + address);
        }
    }

    /**
     * 查询钱包余额
     *
     * @param address 钱包地址
     */
    private void getBalance(String address) throws DecoderException {
        // 检查钱包地址是否合法(check sum)
        try {
            Base58Check.base58ToBytes(address);
        } catch (Exception e) {
            log.error("ERROR: invalid wallet address", e);
            throw new RuntimeException("ERROR: invalid wallet address", e);
        }

        // 得到公钥Hash值
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);

        //Blockchain blockchain = Blockchain.createBlockchain(address);
        Blockchain blockchain = Blockchain.initBlockchainFromDB();
        UTXOSet utxoSet = new UTXOSet(blockchain);
        //Map<String , byte[]> utxos
        TXOutput[] txOutputs = utxoSet.findUTXOs(pubKeyHash);
        int balance = 0;
        if (txOutputs != null && txOutputs.length > 0) {
            for (TXOutput txOutput : txOutputs) {
                balance += txOutput.getValue();
            }
        }
        log.info("Balance of '{}': {}\n", new Object[]{address, balance});
    }

    /**
     * 转账
     *
     * @param from
     * @param to
     * @param amount
     * @throws Exception
     *
    private void send(String from, String to, int amount) throws Exception {
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(from);
        } catch (Exception e) {
            log.error("ERROR: sender address invalid ! address=" + from, e);
            throw new RuntimeException("ERROR: sender address invalid ! address=" + from, e);
        }
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(to);
        } catch (Exception e) {
            log.error("ERROR: receiver address invalid ! address=" + to, e);
            throw new RuntimeException("ERROR: receiver address invalid ! address=" + to, e);
        }
        if (amount < 1) {
            log.error("ERROR: amount invalid ! amount=" + amount);
            throw new RuntimeException("ERROR: amount invalid ! amount=" + amount);
        }
        Blockchain blockchain = Blockchain.createBlockchain(from);
        // 新交易
        Transaction transaction = Transaction.newUTXOTransaction(from, to, amount, blockchain);
        // 奖励(新建一个区块打包这个交易）
        Transaction rewardTx = Transaction.newCoinbaseTX(from, "");
        Block newBlock = blockchain.mineBlock(new Transaction[]{transaction, rewardTx});
        new UTXOSet(blockchain).update(newBlock);
        log.info("Success!");
    }*/

    /**
     * 转账
     *发布一笔交易，放到自己的交易池，然后广播给别人
     *
     * @param from
     * @param to
     * @param amount
     * @throws Exception
     */
    private void send(String from, String to, int amount) throws Exception {
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(from);
        } catch (Exception e) {
            log.error("ERROR: sender address invalid ! address=" + from, e);
            throw new RuntimeException("ERROR: sender address invalid ! address=" + from, e);
        }
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(to);
        } catch (Exception e) {
            log.error("ERROR: receiver address invalid ! address=" + to, e);
            throw new RuntimeException("ERROR: receiver address invalid ! address=" + to, e);
        }
        if (amount < 1) {
            log.error("ERROR: amount invalid ! amount=" + amount);
            throw new RuntimeException("ERROR: amount invalid ! amount=" + amount);
        }
        Blockchain blockchain = Blockchain.initBlockchainFromDB();
        Transaction transaction = Transaction.newUTXOTransaction(from, to, amount, blockchain);
        RocksDBUtils.getInstance().putTransaction(transaction);

        Utils.getInstance().broadcastTransaction(transaction);
        log.info("Success!");
    }

    /**
     * 下载区块
     */
    public void downloadBlockchain() throws IOException {
        ClientManager clientManager = new ClientManager("127.0.0.1",9999, DownloadBlocks.strategyName);
        clientManager.start();
        return;
    }

    /**
     * 下载区块头
     */
    public void downloadBlockHead() throws IOException {
        ClientManager clientManager = new ClientManager("127.0.0.1",9999, DownloadBlocksHead.strategyName);
        clientManager.start();
        return;
    }

    /**
     * 下载交易
     */
    public void downloadTx() throws IOException {
        ClientManager clientManager = new ClientManager("127.0.0.1",9999, DownloadTx.strategyName);
        clientManager.start();
        return;
    }

    /**
     * getIp
     */
    public void getIp() throws IOException {
        ClientManager clientManager = new ClientManager("127.0.0.1",9999, getIp.strategyName);
        clientManager.start();
        return;
    }

    /**
     * SPV
     */
    public void SPV(String str) throws IOException {
        ClientManager clientManager = new ClientManager("127.0.0.1",9999, SPV.strategyName);
        clientManager.setTxHash(str);
        clientManager.start();
        return;
    }

    /**
     * 打印帮助信息
     */
    private void help() {
        System.out.println("Usage:");
        System.out.println("  createwallet - Generates a new key-pair and saves it into the wallet file");
        System.out.println("  printaddresses - print all wallet address");
        System.out.println("  getbalance -address ADDRESS - Get balance of ADDRESS");
        System.out.println("  createblockchain -address ADDRESS - Create a blockchain and send genesis block reward to ADDRESS");
        System.out.println("  printchain - Print all the blocks of the blockchain");
        System.out.println("  send -from FROM -to TO -amount AMOUNT - Send AMOUNT of coins from FROM address to TO");
        System.exit(0);
    }

    /**
     * 打印出区块链中的所有区块
     */
    private void printChain() {
        Blockchain blockchain = Blockchain.initBlockchainFromDB();
        for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext(); ) {
            Block block = iterator.next();
            if (block != null) {
                boolean validate = ProofOfWork.newProofOfWork(block).validate();
                log.info(block.toString() + ", validate = " + validate);
            }
        }
    }

}
