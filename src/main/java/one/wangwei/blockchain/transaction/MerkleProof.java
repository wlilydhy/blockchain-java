package one.wangwei.blockchain.transaction;

import java.util.ArrayList;
import java.util.List;

public class MerkleProof {


    public static void main(String[] args) {

        //首先创建一堆交易
        int count=30000;
        List<Transaction> list = new ArrayList<>();
        for(Integer i =0; i<count;i++) {
            Transaction transaction = Transaction.newCoinbaseTX("1Gsnure8ovCy3SiK6Fth44kDcwrEHjtFuj",i.toString());
            list.add(transaction);
            //System.out.println("hash="+transaction.getTxId().toString());
        }
        byte[][] TxBytes = new byte[count][];
        for(int i=0;i<count;i++){
            TxBytes[i]=list.get(i).getTxId();
        }

        int times=0;
        long sum=0;
        while(times<50) {
            long start = System.currentTimeMillis();
            MerkleTree merkleTree = new MerkleTree(TxBytes);
            //merkleTree.print();
            //System.out.println("merkle proof=" + merkleTree.getRoot().getHash());
            //System.out.println("txHash=" + TxBytes[4].toString());

            merkleTree.MerkleProof(TxBytes[4]);
            long end = System.currentTimeMillis();
            System.out.println("第"+times+"次 time used :" + (end - start));
            sum=sum+end-start;
            times++;
        }
        sum=sum/50;
        System.out.println("平均耗时："+sum);


    }








}
