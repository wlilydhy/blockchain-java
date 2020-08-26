package one.wangwei.blockchain.cli;

import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.TXOutput;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.util.SerializeUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Iterator;
import java.util.Map;

public class CliThread extends Thread{

    public void run(String[] args){
        CLI cli = new CLI(args);
        cli.parse();
        //SHOW SOMETHING
        Map<byte[],byte[]> txbucket = RocksDBUtils.getInstance().getTxBucket();
        System.out.println("txbucket's size is"+txbucket.size());
        Iterator<Map.Entry<byte[],byte[]>> iterator = txbucket.entrySet().iterator();
        while(iterator.hasNext()) {
            Transaction transaction = (Transaction) SerializeUtils.deserialize(iterator.next().getValue());
            System.out.println(transaction.toString());
        }
        Map<String ,byte[]> chainstateBucket = RocksDBUtils.getInstance().getChainstateBucket();
        System.out.println("chainstateBucket's size is"+chainstateBucket.size());
        Iterator<Map.Entry<String,byte[]>> iterator1 = chainstateBucket.entrySet().iterator();
        while(iterator1.hasNext()){
            Map.Entry<String , byte[]> entry = iterator1.next();
            String id = entry.getKey();
            byte[] tid = new byte[0];
            try {
                tid = Hex.decodeHex(id);
            } catch (DecoderException e) {
                e.printStackTrace();
            }
            TXOutput[] txOutput = (TXOutput[])SerializeUtils.deserialize(entry.getValue());
            System.out.print("Transaction id is ");
            for(int i=0;i<tid.length;i++){
                System.out.print(tid[i]);
            }
            System.out.println();
            for(TXOutput tx : txOutput) {
                System.out.println(tx.toString());
            }
        }
    }

    public void start(String[] argss) {
        this.run(argss);
    }
}
