package one.wangwei.blockchain.store;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.transaction.TXOutput;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.util.SerializeUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.Iterator;
import java.util.Map;

/**
 * 存储工具类
 *
 * @author wangwei
 * @date 2018/02/27
 */
@Slf4j
public class    RocksDBUtils {

    /**
     * 区块链数据文件
     */
    private static final String DB_FILE = "blockchain.db";
    /**
     * 区块桶Key
     */
    private static final String BLOCKS_BUCKET_KEY = "blocks";
    /**
     * 链状态桶Key
     */
    private static final String CHAINSTATE_BUCKET_KEY = "chainstate";

    /**
     * IP状态桶Key
     */
    private static final String IP_BUCKET_KEY = "ip";

    /**
     * transaction状态桶Key
     */
    private static final String Tx_BUCKET_KEY = "Tx";


    /**
     * 最新一个区块
     */
    private static final String LAST_BLOCK_KEY = "l";

    /**
     * 当前区块长度
     */
    private static final String bestHeight = "h";

    private volatile static RocksDBUtils instance;

    public static RocksDBUtils getInstance() {
        if (instance == null) {
            synchronized (RocksDBUtils.class) {
                if (instance == null) {
                    instance = new RocksDBUtils();
                }
            }
        }
        return instance;
    }
//    private static final RocksDBUtils instance = new RocksDBUtils();
//    public static RocksDBUtils getInstance(){
//        return instance;
//    }

    private RocksDB db;

    /**
     * block buckets
     */
    @Getter
    private Map<String, byte[]> blocksBucket;

    /**
     * chainstate buckets
     */
    @Getter
    private Map<String, byte[]> chainstateBucket;

    @Getter
    private Map<String, Integer> ipBucket;

    @Getter
    private Map<String, byte[]> txBucket;



    private RocksDBUtils() {
        openDB();
        initBlockBucket();
        initChainStateBucket();
        initIpBucket();
        initTxBucket();
    }

    /**
     * 打开数据库
     */
    private void openDB() {
        try {
            db = RocksDB.open(DB_FILE);
        } catch (RocksDBException e) {
            log.error("Fail to open db ! ", e);
            throw new RuntimeException("Fail to open db ! ", e);
        }
    }

    /**
     * 初始化 blocks 数据桶
     */
    private void initBlockBucket() {
        try {
            byte[] blockBucketKey = SerializeUtils.serialize(BLOCKS_BUCKET_KEY);//BLOCKS_BUCKET_KEY="block"
            byte[] blockBucketBytes = db.get(blockBucketKey);
            if (blockBucketBytes != null) {
                blocksBucket = (Map) SerializeUtils.deserialize(blockBucketBytes);
            } else {
                blocksBucket = Maps.newHashMap();
                db.put(blockBucketKey, SerializeUtils.serialize(blocksBucket));
            }
        } catch (RocksDBException e) {
            log.error("Fail to init block bucket ! ", e);
            throw new RuntimeException("Fail to init block bucket ! ", e);
        }
    }


    /**
     * 初始化 Tx 数据桶
     */
    private void initTxBucket() {
        try {
            byte[] TxBucketKey = SerializeUtils.serialize(Tx_BUCKET_KEY);//BLOCKS_BUCKET_KEY="block"
            byte[] TxBucketBytes = db.get(TxBucketKey);
            if (TxBucketBytes != null) {
                txBucket = (Map) SerializeUtils.deserialize(TxBucketBytes);
            } else {
                txBucket = Maps.newHashMap();
                db.put(TxBucketKey, SerializeUtils.serialize(txBucket));
            }
        } catch (RocksDBException e) {
            log.error("Fail to init transaction bucket ! ", e);
            throw new RuntimeException("Fail to init transaction bucket ! ", e);
        }
    }



    /**
     * 初始化 chainstate 数据桶
     */
    private void initChainStateBucket() {
        try {
            byte[] chainstateBucketKey = SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY);
            byte[] chainstateBucketBytes = db.get(chainstateBucketKey);
            if (chainstateBucketBytes != null) {
                chainstateBucket = (Map) SerializeUtils.deserialize(chainstateBucketBytes);
            } else {
                chainstateBucket = Maps.newHashMap();
                db.put(chainstateBucketKey, SerializeUtils.serialize(chainstateBucket));
            }
        } catch (RocksDBException e) {
            log.error("Fail to init chainstate bucket ! ", e);
            throw new RuntimeException("Fail to init chainstate bucket ! ", e);
        }
    }


    /**
     * 初始化 IP 数据桶
     */
    private void initIpBucket() {
        try {
            byte[] ipBucketKey = SerializeUtils.serialize(IP_BUCKET_KEY);
            byte[] ipBucketBytes = db.get(ipBucketKey);
            if (ipBucketBytes != null) {
                ipBucket = (Map) SerializeUtils.deserialize(ipBucketBytes);
            } else {
                ipBucket = Maps.newHashMap();
                db.put(ipBucketKey, SerializeUtils.serialize(ipBucket));
            }
        } catch (RocksDBException e) {
            log.error("Fail to init chainstate bucket ! ", e);
            throw new RuntimeException("Fail to init chainstate bucket ! ", e);
        }
    }


    /**
     * 保存最新一个区块的Hash值
     *
     * @param tipBlockHash
     */
    public void putLastBlockHash(String tipBlockHash) {
        try {
            blocksBucket.put(LAST_BLOCK_KEY, SerializeUtils.serialize(tipBlockHash));
            db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(blocksBucket));
            //this.closeDB();
        } catch (RocksDBException e) {
            log.error("Fail to put last block hash ! tipBlockHash=" + tipBlockHash, e);
            throw new RuntimeException("Fail to put last block hash ! tipBlockHash=" + tipBlockHash, e);
        }
    }


    /**
     * 保存当前最长链长度
     *
     * @param height
     */
    public void putbestHeight(String height) {
        try {
            blocksBucket.put(bestHeight,height.getBytes());
            db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(blocksBucket));
            //this.closeDB();
        } catch (RocksDBException e) {
            log.error("Fail to put best height", e);
            throw new RuntimeException("Fail to put best height" , e);
        }
    }


    /**
     * 保存一个新的IP
     *
     * @param ip
     * @param port
     */
    public void putIp(String ip,Integer port) {
        try {
            initIpBucket();
            boolean exist=false;
            Iterator<Map.Entry<String, Integer>> iterator = ipBucket.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String,Integer> entry = iterator.next();
                String ipz = entry.getKey();
                if(ipz.equals(ip)){
                    exist=true;
                }
            }
            if(!exist){
                ipBucket.put(ip, port);
                db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(ipBucket));
            }
            //this.closeDB();
        } catch (RocksDBException e) {
            log.error("Fail to put a new ip " + ip, e);
            throw new RuntimeException("Fail to put a new ip", e);
        }
    }


    /**
     * 查询最新一个区块的Hash值
     *
     * @return
     */
    public String getLastBlockHash() {
        byte[] lastBlockHashBytes = blocksBucket.get(LAST_BLOCK_KEY);
        if (lastBlockHashBytes != null) {
            //this.closeDB();
            return (String) SerializeUtils.deserialize(lastBlockHashBytes);
        }
        //this.closeDB();
        return "";
    }


    /**
     * 查询最长链的长度
     *
     * @return
     */
    public String getbestHeight() {
        byte[] heightbytes = blocksBucket.get(bestHeight);
        if (heightbytes != null) {
            //this.closeDB();
            return (String) SerializeUtils.deserialize(heightbytes);
        }
        //this.closeDB();
        return "0";
    }


    /**
     * 保存交易
     *
     * @param transaction
     */
    public void putTransaction(Transaction transaction) {
        try {
            txBucket.put(transaction.getStringOfTxid(), SerializeUtils.serialize(transaction));
            db.put(SerializeUtils.serialize(Tx_BUCKET_KEY), SerializeUtils.serialize(txBucket));
            //this.closeDB();
            log.info("putting transaction is done");
        } catch (RocksDBException e) {
            log.error("Fail to put Transaction ! Transaction=" + transaction.toString(), e);
            throw new RuntimeException("Fail to put Transaction ! Transaction=" + transaction.toString(), e);
        }
    }

    /**
     * 查询交易
     *
     * @param TxHash
     * @return
     */
    public Transaction getTransacion(String TxHash) {
        byte[] TxBytes = txBucket.get(TxHash);
        if (TxBytes != null) {
            return (Transaction) SerializeUtils.deserialize(TxBytes);
        }
        return null;
        //throw new RuntimeException("Fail to get transaction ! TxHash=" + TxHash);

    }



    /**
     * 保存区块
     *
     * @param block
     */
    public void putBlock(Block block) {
        try {
            blocksBucket.put(block.getHash(), SerializeUtils.serialize(block));
            db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(blocksBucket));
        } catch (RocksDBException e) {
            log.error("Fail to put block ! block=" + block.toString(), e);
            throw new RuntimeException("Fail to put block ! block=" + block.toString(), e);
        }
    }

    /**
     * 查询区块
     *
     * @param blockHash
     * @return
     */
    public Block getBlock(String blockHash) {
        byte[] blockBytes = blocksBucket.get(blockHash);
        if (blockBytes != null) {
            //this.closeDB();
            return (Block) SerializeUtils.deserialize(blockBytes);
        }
        //this.closeDB();
        return null;
        //throw new RuntimeException("Fail to get block ! blockHash=" + blockHash);
    }




    /**
     * 清空block bucket
     */
    public void cleanBlockBucket() {
        try {
            blocksBucket.clear();
            db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY),SerializeUtils.serialize(blocksBucket));
            //this.closeDB();
        } catch (Exception e) {
            log.error("Fail to clear block bucket ! ", e);
            throw new RuntimeException("Fail to clear block bucket ! ", e);
        }
    }

    /**
     * 清空ipbucket
     */
    public void cleanIpBucket() {
        try {
            ipBucket.clear();
            db.put(SerializeUtils.serialize(IP_BUCKET_KEY),SerializeUtils.serialize(ipBucket));
            //this.closeDB();
        } catch (Exception e) {
            log.error("Fail to clear IP bucket ! ", e);
            throw new RuntimeException("Fail to clear IP bucket ! ", e);
        }
    }




    /**
     * 清空chainstate bucket
     */
    public void cleanChainStateBucket() {
        try {
            chainstateBucket.clear();
            db.put(SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY),SerializeUtils.serialize(chainstateBucket));
            //this.closeDB();
        } catch (Exception e) {
            log.error("Fail to clear chainstate bucket ! ", e);
            throw new RuntimeException("Fail to clear chainstate bucket ! ", e);
        }
    }

    /**
     * 清空chainstate bucket
     */
    public void cleanTxBucket() {
        try {
            txBucket.clear();
            db.put(SerializeUtils.serialize(Tx_BUCKET_KEY),SerializeUtils.serialize(txBucket));
            //db.close();
        } catch (Exception e) {
            log.error("Fail to clean chainstate bucket ! ", e);
            throw new RuntimeException("Fail to clean chainstate bucket ! ", e);
        }
    }


    /**
     * 保存UTXO数据
     *
     * @param key   交易ID
     * @param utxos UTXOs
     */
    public void putUTXOs(String key, TXOutput[] utxos) {
        try {
            chainstateBucket.put(key, SerializeUtils.serialize(utxos));
            db.put(SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY), SerializeUtils.serialize(chainstateBucket));
            //this.closeDB();
            log.info("utxo is putting");
        } catch (Exception e) {
            log.error("Fail to put UTXOs into chainstate bucket ! key=" + key, e);
            throw new RuntimeException("Fail to put UTXOs into chainstate bucket ! key=" + key, e);
        }
    }


    /**
     * 查询UTXO数据
     *
     * @param key 交易ID
     */
    public TXOutput[] getUTXOs(String key) {
        byte[] utxosByte = chainstateBucket.get(key);
        if (utxosByte != null) {
            //this.closeDB();
            return (TXOutput[]) SerializeUtils.deserialize(utxosByte);
        }
        //this.closeDB();
        return null;
    }


    /**
     * 删除 UTXO 数据
     *
     * @param key 交易ID
     */
    public void deleteUTXOs(String key) {
        try {
            chainstateBucket.remove(key);
            db.put(SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY), SerializeUtils.serialize(chainstateBucket));
            //this.closeDB();
        } catch (Exception e) {
            log.error("Fail to delete UTXOs by key ! key=" + key, e);
            throw new RuntimeException("Fail to delete UTXOs by key ! key=" + key, e);
        }
    }

    /**
     * 删除 IP 数据
     *
     * @param ip 交易ID
     */
    public void deleteip(String ip) {
        try {
            ipBucket.remove(ip);
            db.put(SerializeUtils.serialize(IP_BUCKET_KEY), SerializeUtils.serialize(ipBucket));
            //this.closeDB();
        } catch (Exception e) {
            log.error("Fail to delete ip by key ! key=" + ip, e);
            throw new RuntimeException("Fail to delete ip by key ! key=" + ip, e);
        }
    }

    /**
     * 删除 Transaction 数据
     *
     * @param Txid 交易ID
     */
    public void deletetransacion(String Txid) {
        try {
            txBucket.remove(Txid);
            db.put(SerializeUtils.serialize(Tx_BUCKET_KEY), SerializeUtils.serialize(txBucket));
            //this.closeDB();
        } catch (Exception e) {
            log.error("Fail to delete transaction by key ! " , e);
            throw new RuntimeException("Fail to delete transaction by key ! " , e);
        }
    }






    /**
     * 关闭数据库
     */
    public void closeDB() {
        try {
            db.close();
        } catch (Exception e) {
            log.error("Fail to close db ! ", e);
            throw new RuntimeException("Fail to close db ! ", e);
        }
    }
}
