package one.wangwei.blockchain.pb;

import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.protocols.ICallback;
import one.wangwei.blockchain.pb.protocols.Inv.InvProtocol;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.util.SerializeUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.*;
import java.util.logging.Logger;

/**
 * A singleton class to provide various utility functions. It must always be
 * accessed statically as Utils.getInstance()...
 * 
 * @author aaron
 *
 */
public class Utils {

	private static Logger log = Logger.getLogger(Utils.class.getName());
	private static Utils utils;
	
	/**
	 * Default server port
	 */
	public static final int serverPort = 3100;
	
	/**
	 * Default server host
	 */
	public static final String serverHost = "localhost";
	
	
	/**
	 * Use of a single timer object over the entire system helps
	 * to reduce thread usage.
	 */
	Timer timer = new Timer();
	
	public Utils() {
		timer=new Timer();
	}
	
	public static synchronized Utils getInstance() {
		if(utils==null) utils=new Utils();
		return utils;
	}
	
	/**
	 * Convenience method to set an anonymous method callback
	 * after a timeout delay. Go JavaScript :-)
	 * <br/>
	 * Use this method like: 
	 * <code>
	 * Utils.getInstance().setTimeout(()->{doSomething();},10000);
	 * </code>
	 * @param callback the method to call
	 * @param delay the delay in ms before calling the method
	 */
	public void setTimeout(ICallback callback,long delay) {

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				callback.callback();
			}

		}, delay);
	}
	
	/**
	 * Call before the system exits.
	 */
	public void cleanUp() {
		timer.cancel();
	}

	public String BlockToString(Block block){
		byte[] bytes = SerializeUtils.serialize(block);
		String str = Hex.encodeHexString(bytes);
		return str;
	}

	public Block StringToBlock(String str) throws DecoderException {
		byte[] sblockre = Hex.decodeHex(str);
		Block block = (Block) SerializeUtils.deserialize(sblockre);
		return block;
	}

	public String TxToString(Transaction transaction){
		byte[] bytes=SerializeUtils.serialize(transaction);
		String str=Hex.encodeHexString(bytes);
		return str;
	}

	public Transaction StringToTx(String str) throws DecoderException {
		byte[] sre = Hex.decodeHex(str);
		Transaction transaction = (Transaction) SerializeUtils.deserialize(sre);
		return transaction;
	}

	public String listToString(List<String> list) throws DecoderException {
		byte[] sre = SerializeUtils.serialize(list);
		String str = Hex.encodeHexString(sre);
		return str;
	}

	public List<String> stringToList(String str) throws DecoderException {
		byte[] sre = Hex.decodeHex(str);
		List<String> list = (List<String>) SerializeUtils.deserialize(sre);
		return list;
	}
	/**
	 * 查找并返回指定的交易，如果不存在返回null
	 * @param Txid
	 * @return
	 */
	public Transaction searchTransaction(String Txid){
		Map<String,byte[]> txBucket = RocksDBUtils.getInstance().getTxBucket();
		byte[] bytes=txBucket.get(Txid);
		Transaction transaction = (Transaction) SerializeUtils.deserialize(bytes);
		return transaction;
//		Iterator<Map.Entry<String,byte[]>> iterator = txBucket.entrySet().iterator();
//		if(!iterator.hasNext()){
//			log.info("Txbucket is empty");
//		}
//		while(iterator.hasNext()){
//			Map.Entry<String, byte[]> entry = iterator.next();
//			String id = entry.getKey();
//			if(id.equals(Txid)){
//				return (Transaction) SerializeUtils.deserialize(entry.getValue());
//			}
//		}
//		log.info("can not find the transaction by txid");
//		return null;
	}

	/**
	 * 从区块链中查找交易
	 * @param txid
	 * @return
	 */

	public Transaction findTransaction(String txid){
		Map<String,byte[]> blockBucket = RocksDBUtils.getInstance().getBlocksBucket();
		Iterator<Map.Entry<String,byte[]>> iterator = blockBucket.entrySet().iterator();
		if(!iterator.hasNext()){
			log.info("blockBucket is empty");
		}
		while(iterator.hasNext()){
			Map.Entry<String, byte[]> entry = iterator.next();
			byte[] blockBytes = entry.getValue();
			Block block =(Block)SerializeUtils.deserialize(blockBytes);
			Transaction[] transactions=block.getTransactions();
			for(Transaction t : transactions){
				if(t.getStringOfTxid().equals(txid)){
					return t;
				}
			}
		}
		return null;
 	}




}
