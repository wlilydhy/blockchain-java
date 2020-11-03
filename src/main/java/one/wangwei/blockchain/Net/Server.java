package one.wangwei.blockchain.Net;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.wangwei.blockchain.block.Blockchain;
import one.wangwei.blockchain.cli.CliThread;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.util.SerializeUtils;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.transaction.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;


@Slf4j
public class Server extends Thread{



    @SneakyThrows
    @Override
    public void run() {
        Socket socket=null;
        ServerSocket serverSocket =new ServerSocket(9999);
        while(true) {
            socket = serverSocket.accept();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String name = null;
            while ((name = receiveMsg(bufferedReader)) != null) {
                Invoker(bufferedWriter, bufferedReader, name);
            }
        }
    }

    private void reTransmission(Block block) throws IOException, DecoderException {
        Client.broadcastBlock(block);
    }

    private void reTransmission(Transaction transaction) throws IOException, DecoderException {
        Client.broadcastTransaction(transaction);
    }

    private void Invoker(BufferedWriter bufferedWriter, BufferedReader bufferedReader, String name) throws DecoderException, IOException {
        //握手
        if (name.equals("Version")) {
            connection(bufferedWriter, bufferedReader);
        }
        //发送地址信息
        if (name.equals("getaddr")) {
            Addr(bufferedWriter);
        }
        //发送区块
        if(name.equals("getBlockdata")){
            String id = receiveMsg(bufferedReader);
            Block block=RocksDBUtils.getInstance().getBlock(id);
            //RocksDBUtils.getInstance().closeDB();
            sendBlock(bufferedWriter, block);
        }
        //接受交易/区块
        if (name.equals("Inv")) {
            String str = receiveMsg(bufferedReader);
            byte[] binv = Hex.decodeHex(str);
            Inv inv = (Inv) SerializeUtils.deserialize(binv);
            log.info("inv is " + inv.getCategory());
            //应该先判断该交易是否在交易池中，然后再选择是否getdata
            if (inv.getCategory().equals("transaction")) {
                byte[] tid = inv.getData()[0];
                String id = Hex.encodeHexString(tid);
                Transaction transaction1 = null;//serchTransaction(tid);
                if( transaction1!=null){
                    log.info("Transaction is existed \n"+transaction1.toString());
                    sendMsg(bufferedWriter,"goodnight");
                    return;
                }
                getdata(bufferedWriter, inv.getCategory(), id);
                String re = receiveMsg(bufferedReader);
                byte[] sre = Hex.decodeHex(re);
                Transaction transaction = (Transaction) SerializeUtils.deserialize(sre);
                RocksDBUtils.getInstance().putTransaction(transaction);
                log.info("transaction is received\n"+transaction.toString());
                reTransmission(transaction);

            }
            //应该先判断该区块是否已经存在，然后再选择是否getdata
            else if (inv.getCategory().equals("block")) {
                byte[] blockid = inv.getData()[0];
                String sid = Hex.encodeHexString(blockid);
                //查找区块是否存在
                Block block1 =RocksDBUtils.getInstance().getBlock(sid);
                //RocksDBUtils.getInstance().closeDB();
                if(block1!=null){
                    log.info("Block is existed \n"+block1.toString());
                    sendMsg(bufferedWriter,"goodnight");
                    //RocksDBUtils.getInstance().closeDB();
                    return;
                }
                //sid直接就是区块哈希
                getdata(bufferedWriter, inv.getCategory(), sid);
                String blockre = receiveMsg(bufferedReader);
                byte[] sblockre = Hex.decodeHex(blockre);
                Block block = (Block) SerializeUtils.deserialize(sblockre);
                log.info("block is received \n" + block.toString());
                reTransmission(block);

            }
        }
        //回应客户端下载区块消息，发送相应的inv
        if(name.equals("getblock")){
            uploadBlock(bufferedWriter,bufferedReader);
        }

    }

    private void uploadBlock(BufferedWriter bufferedWriter,BufferedReader bufferedReader) throws DecoderException {
        //读取客户端发来的lasthash，与本地的lasthash做对比
        String Clasthash = receiveMsg(bufferedReader);
        String lasthash = RocksDBUtils.getInstance().getLastBlockHash();
        //RocksDBUtils.getInstance().closeDB();
        //如果一样，表示双方拥有完全一致的区块链，返回correct
        if(lasthash.equals(Clasthash)){
            sendMsg(bufferedWriter,"correct");
            return;
        }
        //本地都没有区块链，咋帮忙，直接爬
        if(lasthash==null){
            log.error("lasthash is empty");
            return;
        }
        //遍历区块链阶段
        List<Block> blockList=null;
        Blockchain blockchain = Blockchain.initBlockchainFromDB();
        //遍历区块
        int maxcount = 50;
        int count=0;
        int flag = 0;
        while(count<maxcount) {
            for (Blockchain.BlockchainIterator blockchainIterator = blockchain.getBlockchainIterator(); blockchainIterator.hashNext(); ) {
                Block block = blockchainIterator.next();
                //找到客户端发来的clasthash的后面一个区块，然后加入list，同时将clasthash后移一位。注意list中采用尾插法
                if (block.getPrevBlockHash().equals(Clasthash)) {
                    blockList.add(block);
                    Clasthash = block.getHash();
                    count++;
                    flag=1;
                    break;
                }
            }
            if(flag == 0) {
                break;
            }
            if(flag==1){
                flag=0;
            }
        }
        //结束遍历区块，这时间我们发送inv
        sendMsg(bufferedWriter,"Inv");
        sendInv(bufferedWriter,(Block[])blockList.toArray());
    }

    /**
     * 当category是 block时 id就是string型，当category 时 transac 时， id应为byte数组，需要在函数外进行转换
     *
     * @param bufferedWriter
     * @param category
     * @param id
     */
    private  void getdata(BufferedWriter bufferedWriter, String category, String id) {
        if (category.equals("transaction")) {
            String name = "getTxdata";
            sendMsg(bufferedWriter, name);
        }
        if (category.equals("block")) {
            String name = "getBlockdata";
            sendMsg(bufferedWriter, name);
        }
        sendMsg(bufferedWriter, id);
    }


    private  void Addr(BufferedWriter bufferedWriter) {
        Map<String, Integer> ipbucket = RocksDBUtils.getInstance().getIpBucket();
        //RocksDBUtils.getInstance().closeDB();
        ipbucket.put("127.0.0.1", 9999);
        byte[] bytesip = SerializeUtils.serialize(ipbucket);
        sendMsg(bufferedWriter, bytesip);

    }


    public  void sendMsg(BufferedWriter os, String msg) {
        try {
            msg += System.lineSeparator();
            os.write(msg);
            log.info("send msg is " + msg);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public  void sendMsg(BufferedWriter os, byte[] msg) {
        try {
            String Smsg = Hex.encodeHexString(msg);
            Smsg += System.lineSeparator();
            os.write(Smsg);
            log.info("send msg is " + Smsg);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  String receiveMsg(BufferedReader bufferedReader) {
        String name = null;
        try {
            while ((name = bufferedReader.readLine()) != null) {
                log.info("received msg is " + name);
                return name;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("received msg is " + name);
        return name;
    }


    /**
     * 当收到version时调用，读取version信息，发送version信息，握手成功返回true
     *
     * @param bufferedWriter
     * @param bufferedReader
     * @throws DecoderException
     * @throws IOException
     */

    private void connection(BufferedWriter bufferedWriter, BufferedReader bufferedReader) throws DecoderException, IOException {
        String str = receiveMsg(bufferedReader);
        log.info("connecting");
        byte[] bytes = Hex.decodeHex(str);
        Version version = (Version) SerializeUtils.deserialize(bytes);
        log.info("received version is " + version.getHeight() + "  " + version.getWilling());
        //String h= RocksDBUtils.getInstance().getbestHeight();
        String h = "0";
        if (version.getWilling()) {
            Version reply = new Version(h, true);
            byte[] Sedreply = SerializeUtils.serialize(reply);
            sendMsg(bufferedWriter, Sedreply);
        }
    }

    /**
     * 先发送一个消息名称，然后发送交易id过去
     * @param bufferedWriter
     * @param transaction
     */
    public  void sendInv(BufferedWriter bufferedWriter,Transaction transaction) throws IOException {
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
    public  void sendInv(BufferedWriter bufferedWriter,Transaction[] transaction){
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
    public  void sendInv(BufferedWriter bufferedWriter,Block[] blocks) throws DecoderException {
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
    public  void sendInv(BufferedWriter bufferedWriter,Block block) throws DecoderException {
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


    public void sendBlock(BufferedWriter bufferedWriter,Block block){
        byte[] bytes = SerializeUtils.serialize(block);
        String str = Hex.encodeHexString(bytes);
        sendMsg(bufferedWriter,str);
    }



    private boolean byteEqual(byte[] a ,byte[] b){
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





