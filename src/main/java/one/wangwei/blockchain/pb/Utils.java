package one.wangwei.blockchain.pb;

import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.block.BlockHead;
import one.wangwei.blockchain.pb.protocols.ICallback;
import one.wangwei.blockchain.pb.protocols.Inv.InvProtocol;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.MerkleTree;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.util.ByteUtils;
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

	public String NodelistToString(List<MerkleTree.Node> list) throws DecoderException {
		byte[] sre = SerializeUtils.serialize(list);
		String str = Hex.encodeHexString(sre);
		return str;
	}

	public List<MerkleTree.Node> stringToNodeList(String str) throws DecoderException {
		byte[] sre = Hex.decodeHex(str);
		List<MerkleTree.Node> list = (List<MerkleTree.Node>) SerializeUtils.deserialize(sre);
		return list;
	}

	public Map<String,Integer> stringToMap(String str) throws DecoderException {
		byte[] sre = Hex.decodeHex(str);
		Map<String,Integer> ipBucket = (Map<String,Integer>) SerializeUtils.deserialize(sre);
		return ipBucket;
	}

	public String MapToString(Map<String,Integer> ipBucket) throws DecoderException {
		byte[] sre = SerializeUtils.serialize(ipBucket);
		String str = Hex.encodeHexString(sre);
		return str;
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


	/**
	 * 从区块链中查找交易
	 * @param txid
	 * @return
	 */

	public boolean findTransaction(String txid,Block block) {

		Transaction[] transactions = block.getTransactions();
		for (Transaction t : transactions) {
			if (t.getStringOfTxid().equals(txid)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * check SPV
	 */
	public boolean checkSPV(List<MerkleTree.Node> merkleProof){

		List<byte[]> merkleRoot = new ArrayList<>();
		Map<String,byte[]> blockHeadBucket = RocksDBUtils.getInstance().getBlockHeadBucket();
		Iterator<Map.Entry<String,byte[]>> iterator = blockHeadBucket.entrySet().iterator();
		if(!iterator.hasNext()){
			log.info("blockBucket is empty");
		}
		while(iterator.hasNext()){
			Map.Entry<String, byte[]> entry = iterator.next();
			byte[] blockHeadBytes = entry.getValue();
			BlockHead blockhead =(BlockHead) SerializeUtils.deserialize(blockHeadBytes);
			merkleRoot.add(blockhead.getMerkleRootHash());
		}

		int flag=0;
		for(byte[] Root : merkleRoot){
			if(byteEqual(Root,merkleProof.get(0).getHash())){
				flag=1;
				break;
			}
		}
		if(flag==0){
			return false;
		}

		for(int i=merkleProof.size()-1;i>=2;i=i-2){
			byte[] rightChildHash = merkleProof.get(i).getHash();
			byte[] leftChildHash = merkleProof.get(i-1).getHash();
			byte[] hash = ByteUtils.merge(leftChildHash, rightChildHash);
			if(!byteEqual(hash,merkleProof.get(i-2).getHash())){
				return false;
			}
		}
		return true;

	}

	/**
	 * byteEqual
	 */
	public boolean byteEqual(byte[] a,byte[] b){
		if(a.length==b.length){
			for(int i=0;i<a.length;i++){
				if(a[i]!=b[i]){
					return false;
				}
			}
			return true;
		}
		return false;
	}



}
