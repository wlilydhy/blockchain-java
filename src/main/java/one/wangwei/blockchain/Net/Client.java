package one.wangwei.blockchain.Net;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import com.google.common.collect.Maps;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.block.Blockchain;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.TXInput;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.transaction.UTXOSet;
import lombok.extern.slf4j.Slf4j;
import one.wangwei.blockchain.util.Base58Check;
import one.wangwei.blockchain.wallet.Wallet;
import one.wangwei.blockchain.wallet.WalletUtils;
import one.wangwei.blockchain.util.SerializeUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;


import javax.swing.text.Utilities;
import java.io.*;
import java.net.Socket;
import java.nio.file.SecureDirectoryStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BinaryOperator;

@Slf4j
public class Client {

    public static void main(String[] args) throws IOException, DecoderException {
        /*RocksDBUtils.getInstance().cleanTxBucket();
        RocksDBUtils.getInstance().putIp("127.0.0.1",9999);
        Transaction transaction=Transaction.newCoinbaseTX("1ZNb1h6WRcSiPwJpfydjhRU3E6fV7vxcQ","Atmosphere");
        RocksDBUtils.getInstance().putTransaction(transaction);
        RocksDBUtils.getInstance().closeDB();*/
        /*Map<byte[],byte[]> txBucket = RocksDBUtils.getInstance().getTxBucket();
        //RocksDBUtils.getInstance().closeDB();
        Iterator<Map.Entry<byte[],byte[]>> iterator = txBucket.entrySet().iterator();
        if(!iterator.hasNext()){
            log.info("Txbucket is empty");
        }
        while(iterator.hasNext()){
            Map.Entry<byte[], byte[]> entry = iterator.next();
            //byte[] id = entry.getKey();
            Transaction transaction = (Transaction) SerializeUtils.deserialize(entry.getValue());
            System.out.println(transaction.toString());
            broadcastTransaction(transaction);
        }*/

        /*Map<String,byte[]> blockBucket = RocksDBUtils.getInstance().getBlocksBucket();
        //RocksDBUtils.getInstance().closeDB();
        Iterator<Map.Entry<String,byte[]>> iterator = blockBucket.entrySet().iterator();
        if(!iterator.hasNext()){
            log.info("Txbucket is empty");
        }
        while(iterator.hasNext()){
            Map.Entry<String, byte[]> entry = iterator.next();
            //byte[] id = entry.getKey();
            Block block = (Block) SerializeUtils.deserialize(entry.getValue());
            System.out.println(block.toString());
            broadcastBlock(block);
        }*/

    }


    public static boolean Invoker(BufferedWriter bufferedWriter,BufferedReader bufferedReader,String name) throws IOException, DecoderException {
        if(name.equals("getTxdata")){
            String id = receiveMsg(bufferedReader);
            //byte[] bytes = Hex.decodeHex(id);
            Transaction t1=serchTransaction(id);
            sendTx(bufferedWriter, t1);
            return true;
        }
        if(name.equals("getBlockdata")){
            String id = receiveMsg(bufferedReader);
            Block block=RocksDBUtils.getInstance().getBlock(id);
            //sDBUtils.getInstance().closeDB();
            sendBlock(bufferedWriter, block);
            return true;
        }
        if(name.equals("goodnight")){
            log.info("server want to sleep");
            return false;
        }
        return false;

    }

    /**
     * 从别的节点下载更新本地区块链
     * @throws IOException
     * @throws DecoderException
     */

    public static void downloadBlockchain() throws IOException, DecoderException {
        Map<String ,Integer> ipbucket = RocksDBUtils.getInstance().getIpBucket();
        //sDBUtils.getInstance().closeDB();
        ipbucket.put("127.0.0.1",9999);
        Iterator<Map.Entry<String ,Integer>> iterator = ipbucket.entrySet().iterator();
        if(!iterator.hasNext()){
            log.error("ipbucket is empty");
        }
        int count=0;
        while(iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String ip = entry.getKey();
            Integer port = entry.getValue();
            Socket socket = new Socket(ip, port);
            log.info("socket is "+ip+"  "+port.toString());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if (Connection(bufferedWriter,bufferedReader)) {
                //发送geteblock消息
                sendMsg(bufferedWriter,"getblock");
                //接收发来的 Inv 消息
                String inv = receiveMsg(bufferedReader);
                //如果是 correct 说明下载完毕
                if(inv.equals("correct")){
                    log.info("blockchain's downloading is done!");
                    socket.close();
                    return;
                }
                //接受Inv，然后按次序下载
                if(inv.equals("Inv")){
                    String invmsg = receiveMsg(bufferedReader);
                    Inv blockInv = (Inv) SerializeUtils.deserialize(Hex.decodeHex(invmsg));
                    byte[][] blockid = blockInv.getData();
                    for(int i=0;i<blockid.length;i++){
                        byte[] blockhashbyte = blockid[i];
                        String blockhash = Hex.encodeHexString(blockhashbyte);
                        //发送getdta(blockhash) 消息
                        sendMsg(bufferedWriter,"getBlockdata");
                        sendMsg(bufferedWriter,blockhash);
                        //接受区块消息
                        String BlockString  = receiveMsg(bufferedReader);
                        Block newblock = (Block) SerializeUtils.deserialize(Hex.decodeHex(BlockString));
                        //保存该区块,更新utxo
                        Blockchain blockchain = Blockchain.initBlockchainFromDB();
                        blockchain.addBlock(newblock);
                        new UTXOSet(blockchain).update(newblock);

                    }
                }
                count++;
                //最多跟30个人下载
                if (count >= 30) {
                    return;
                }
            }
            socket.close();
        }
    }





    /**
     * 向数据库获得ipbucket，循环发送交易
     * @param transaction
     * @throws IOException
     * @throws DecoderException
     */
    public static void broadcastTransaction(Transaction transaction) throws IOException, DecoderException {
        Map<String ,Integer> ipbucket = RocksDBUtils.getInstance().getIpBucket();
        //RocksDBUtils.getInstance().closeDB();
        ipbucket.put("127.0.0.1",9999);
        Iterator<Map.Entry<String ,Integer>> iterator = ipbucket.entrySet().iterator();
        if(!iterator.hasNext()){
            log.error("ipbucket is empty");
        }
        int count=0;
        while(iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String ip = entry.getKey();
            Integer port = entry.getValue();
            Socket socket = new Socket(ip, port);
            log.info("socket is "+ip+"  "+port.toString());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //先握手，握手成功在搞别的
            if (Connection(bufferedWriter,bufferedReader)) {
                //发送Inv消息
                sendInv(bufferedWriter, transaction);
                //接收发来的getTxdata消息
                String name = receiveMsg(bufferedReader);
                if(!Invoker(bufferedWriter,bufferedReader,name)){
                    socket.close();
                    continue;
                }
                count++;
                //最多跟30个人广播
                if (count >= 30) {
                    return;
                }
            }
            socket.close();
        }

    }

    /**
     * 向某个节点发送一个区块。首先发送Inv，确认对方的getBlockdata信息之后在发送整个区块数据
     * 向数据库获得ipbucket，循环发送交易
     * @param block
     * @throws IOException
     * @throws DecoderException
     */
    public static void broadcastBlock(Block block) throws IOException, DecoderException {
        Map<String ,Integer> ipbucket = RocksDBUtils.getInstance().getIpBucket();
        ipbucket.put("127.0.0.1",9999);
        Iterator<Map.Entry<String ,Integer>> iterator = ipbucket.entrySet().iterator();
        if(!iterator.hasNext()){
            log.error("ipbucket is empty");
        }
        //System.out.println("why stop2");
        int count=0;
        while(iterator.hasNext()) {
            //System.out.println("why stop3");
            Map.Entry<String ,Integer> entry = iterator.next();
            String ip = entry.getKey();
            Integer port = entry.getValue();
            Socket socket = new Socket(ip,port);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //System.out.println("start broadcast");
            //RocksDBUtils.getInstance().closeDB();
            if(Connection(bufferedWriter,bufferedReader)){
                sendInv(bufferedWriter, block);
                String name = receiveMsg(bufferedReader);
                //log.info(name+" is received");
                if(!Invoker(bufferedWriter,bufferedReader,name)){
                    socket.close();
                    continue;
                }
                count++;
                if (count >= 30) {
                    return;
                }
            }
            socket.close();

        }
        log.info("blockbroatcast is finished");
    }


    /**
     * 查找并返回指定的交易，如果不存在返回null
     * @param Txid
     * @return
     */
    public static Transaction serchTransaction(String Txid){
        Map<String,byte[]> txBucket = RocksDBUtils.getInstance().getTxBucket();
        //RocksDBUtils.getInstance().closeDB();
        Iterator<Map.Entry<String,byte[]>> iterator = txBucket.entrySet().iterator();
        if(!iterator.hasNext()){
            log.info("Txbucket is empty");
        }
        while(iterator.hasNext()){
            Map.Entry<String, byte[]> entry = iterator.next();
            String id = entry.getKey();
            if(id.equals(Txid)){
               return (Transaction) SerializeUtils.deserialize(entry.getValue());
            }
        }
        log.info("can not find the transaction by txid");
        return null;
    }

    public static void sendTx(BufferedWriter bufferedWriter,Transaction transaction){
        byte[] bytes=SerializeUtils.serialize(transaction);
        String str=Hex.encodeHexString(bytes);
        sendMsg(bufferedWriter,str);
    }


    public static void sendBlock(BufferedWriter bufferedWriter,Block block){
        byte[] bytes = SerializeUtils.serialize(block);
        String str = Hex.encodeHexString(bytes);
        sendMsg(bufferedWriter,str);
    }





    public static boolean Connection(BufferedWriter bufferedWriter,BufferedReader bufferedReader) throws DecoderException, IOException {
        //String height =RocksDBUtils.getInstance().getbestHeight();
        String height="0";
        Version version = new Version(height,true);
        //System.out.println(version.getHeight()+"  "+version.getWilling());
        byte[] SedVersion =SerializeUtils.serialize(version);
        String name="Version";
        //发送消息名称
        sendMsg(bufferedWriter,name);
        //发送消息
        sendMsg(bufferedWriter, SedVersion);
        String receive = receiveMsg(bufferedReader);
        Version veract =(Version) SerializeUtils.deserialize(Hex.decodeHex(receive));
        log.info("veract is received "+veract.getHeight()+"  "+veract.getWilling());
        if(veract.getWilling()==true)
            return true;
        else
            return false;

    }

    /**
     * 先发送一个消息名称，然后发送交易id过去
     * @param bufferedWriter
     * @param transaction
     */
    public static void sendInv(BufferedWriter bufferedWriter,Transaction transaction) throws IOException {
        byte[] bytes= transaction.getTxId();
        byte[][] bytes1= new byte[1][1024];
        bytes1[0]=bytes;
        Inv inv=new Inv("transaction",bytes1);
        byte[] sinv=SerializeUtils.serialize(inv);
        //String str = Hex.encodeHexString(sinv);
        String name = "Inv";
        sendMsg(bufferedWriter,name);
        //System.out.println("name is sent");
        sendMsg(bufferedWriter,sinv);

    }

    /**
     * 当发送的inv是一堆交易时
     * @param bufferedWriter
     * @param transaction
     */
    public static void sendInv(BufferedWriter bufferedWriter,Transaction[] transaction){
        byte[][] bytes1= new byte[transaction.length][1024];
        for(int i=0;i<transaction.length;i++){
            bytes1[i]=transaction[i].getTxId();
        }
        Inv inv=new Inv("transaction",bytes1);
        byte[] sinv=SerializeUtils.serialize(inv);
        String str=Hex.encodeHexString(sinv);
        String name = "Inv";
        sendMsg(bufferedWriter,name);
        sendMsg(bufferedWriter,str);
    }


    /**
     * 当发送的inv是一堆区块时
     * @param bufferedWriter
     * @param blocks
     */
    public static void sendInv(BufferedWriter bufferedWriter,Block[] blocks) throws DecoderException {
        byte[][] bytes1= new byte[blocks.length][1024];
        for(int i=0;i<blocks.length;i++){
            bytes1[i]=Hex.decodeHex(blocks[i].getHash());
        }
        Inv inv=new Inv("block",bytes1);
        byte[] sinv=SerializeUtils.serialize(inv);
        String str=Hex.encodeHexString(sinv);
        String name = "Inv";
        sendMsg(bufferedWriter,name);
        sendMsg(bufferedWriter,str);
    }


    /**
     * 当发送的inv是一个区块时
     * @param bufferedWriter
     * @param block
     */
    public static void sendInv(BufferedWriter bufferedWriter,Block block) throws DecoderException {
        byte[] bytes= Hex.decodeHex(block.getHash());
        byte[][] bytes1= new byte[1][1024];
        bytes1[0]=bytes;
        Inv inv=new Inv("block",bytes1);
        byte[] sinv=SerializeUtils.serialize(inv);
        String str=Hex.encodeHexString(sinv);
        String name = "Inv";
        sendMsg(bufferedWriter,name);
        sendMsg(bufferedWriter,str);
    }



    public static void getaddr(BufferedWriter bufferedWriter,BufferedReader bufferedReader) throws IOException, DecoderException {
        sendMsg(bufferedWriter,"getaddr");
        String receivedip = receiveMsg(bufferedReader);
        Map<String,Integer> ipbucket=(Map)SerializeUtils.deserialize(Hex.decodeHex(receivedip));
        Iterator<Map.Entry<String, Integer>> iterator = ipbucket.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String ip = entry.getKey();
            Integer port =entry.getValue();
            RocksDBUtils.getInstance().putIp(ip,port);
            //RocksDBUtils.getInstance().closeDB();
        }
        System.out.println(ipbucket.get("127.0.0.1"));
    }


    public static void sendMsg(BufferedWriter os,byte[] msg){
        try {
            /*OutputStream os = socket.getOutputStream();
            byte[] temp = new byte[msg.length+1];
            temp[msg.length]='\n';
            os.write(msg);*/
            //BufferedWriter os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String Smsg = Hex.encodeHexString(msg);
            Smsg+=System.lineSeparator ();
            os.write(Smsg);
            log.info("send msg is "+Smsg);
            //os.write("\n");
            //System.out.print("send msg is "+Smsg);
            //System.out.println("n ");
            //os.flush();
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMsg(BufferedWriter os,String msg){
        try {
            //BufferedWriter os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            msg+=System.lineSeparator ();
            os.write(msg);
            log.info("send msg is "+msg);
            //os.write("\n");
            //System.out.print("send msg is "+msg);
            //System.out.println("n ");
            //os.flush();
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String receiveMsg(BufferedReader bufferedReader) throws IOException {
        //BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String name=null;
        //String re=null;
        try {
            while((name=bufferedReader.readLine())!=null) {
                //if(name!=null){
                    //re=name;
                //}
                log.info("received msg is " + name);
                return name;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("received msg is " + name);
        return name;
    }


    private static byte[] receiveByteMsg(Socket socket) {
        byte[] receive = new byte[1024];
        try {
            InputStream is = socket.getInputStream();
            is.read(receive);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receive;
    }

    private static boolean byteEqual(byte[] a ,byte[] b){
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

