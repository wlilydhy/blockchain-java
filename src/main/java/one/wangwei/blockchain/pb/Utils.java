package one.wangwei.blockchain.pb;

import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.protocols.ICallback;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.util.SerializeUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A singleton class to provide various utility functions. It must always be
 * accessed statically as Utils.getInstance()...
 * 
 * @author aaron
 *
 */
public class Utils {
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
}
