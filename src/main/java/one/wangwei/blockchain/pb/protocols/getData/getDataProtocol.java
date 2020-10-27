package one.wangwei.blockchain.pb.protocols.getData;

import lombok.Setter;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.*;
import one.wangwei.blockchain.pb.Strategy.ConnectionStrategy;
import one.wangwei.blockchain.pb.Strategy.SendBlock;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.*;
import one.wangwei.blockchain.pb.protocols.Version.VersionReply;
import one.wangwei.blockchain.pb.protocols.Version.VersionRequest;
import one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveProtocol;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.Transaction;
import org.apache.commons.codec.DecoderException;

import java.util.logging.Logger;

@Setter
public class getDataProtocol extends Protocol implements IRequestReplyProtocol {
    private static Logger log = Logger.getLogger(getDataProtocol.class.getName());
    private boolean protocolStop = false;
    private String RequestType;
    private String ReplyType;
    private String TransactionHash;
    private String BlockHash;

    /**
     * Name of this protocol.
     */
    public static final String protocolName = "getDataProtocol";

    /**
     * Initialise the protocol with an endpoint and manager.
     *
     * @param endpoint
     * @param manager
     */
    public getDataProtocol(Endpoint endpoint, Manager manager) {
        super(endpoint, manager);
    }

    /**
     * @return the name of the protocol
     */
    @Override
    public String getProtocolName() {
        return protocolName;
    }

    /**
     *
     */
    @Override
    public void stopProtocol() {
        log.severe("protocol stopped while it is still underway");
        protocolStop = true;
        //endpoint.close();
    }


    @Override
    public void startAsClient() throws EndpointUnavailable {
        Document doc = new Document();
        doc.append("name", getDataRequest.name);
        doc.append("protocolName", protocolName);
        doc.append("type", Message.Type.Request.toString());
        doc.append("RequestType",this.RequestType);
        if(this.RequestType.equals("Block")) {
            doc.append("Hash", this.BlockHash);
        }
        else if(this.RequestType.equals("Transaction")) {
            doc.append("Hash", this.TransactionHash);
        }
        try {
            sendRequest(new getDataRequest(doc));
        } catch (InvalidMessage invalidMessage) {
            invalidMessage.printStackTrace();
        }

    }

    @Override
    public void startAsServer() throws EndpointUnavailable {

    }

    @Override
    public void sendRequest(Message msg) throws EndpointUnavailable {
        endpoint.send(msg);

    }

    @Override
    public void receiveReply(Message msg) throws EndpointUnavailable {

        //收到新区块/新交易之后 应该做持久化
        if(msg instanceof getDataReply) {
            if(((getDataReply) msg).getReplyType().equals("Block")){
                Block block=((getDataReply) msg).getBlock();
                RocksDBUtils.getInstance().putBlock(block);
                log.info("block is received\n"+block.toString());
            }
            if(((getDataReply) msg).getReplyType().equals("Transaction")) {
                Transaction transaction = ((getDataReply) msg).getTransaction();
                RocksDBUtils.getInstance().putTransaction(transaction);
                log.info("transaction is received\n" + transaction.toString());
            }
        }

        //retransmission (广播转发)

    }

    @Override
    public void receiveRequest(Message msg) throws EndpointUnavailable, DecoderException {
        if(msg instanceof getDataRequest) {
            Document doc = new Document();
            doc.append("name", getDataReply.name);
            doc.append("protocolName", protocolName);
            doc.append("type", Message.Type.Reply.toString());
            if(((getDataRequest) msg).getRequestType().equals("Block")){
                //向数据库查找该区块
                String blockHash=((getDataRequest) msg).getBlockHash();
                Block block= RocksDBUtils.getInstance().getBlock(blockHash);
                String str = Utils.getInstance().BlockToString(block);

                /*test
                Transaction transaction = Transaction.newCoinbaseTX("1Gsnure8ovCy3SiK6Fth44kDcwrEHjtFuj","ojbk");
                Block block = Block.newGenesisBlock(transaction);
                String str = Utils.getInstance().BlockToString(block);*/

                //序列化后写入消息
                doc.append("Block",str);
                doc.append("ReplyType","Block");
            }
            if(((getDataRequest) msg).getRequestType().equals("Transaction")){
                //向数据库查找该交易
                String TxHash=((getDataRequest) msg).getTxHash();
                Transaction transaction= RocksDBUtils.getInstance().getTransacion(TxHash);
                String str = Utils.getInstance().TxToString(transaction);
                //序列化后写入消息
                doc.append("Transaction",str);
                doc.append("ReplyType","Transaction");

            }
            try {
                sendReply(new getDataReply(doc));
            } catch (InvalidMessage | DecoderException invalidMessage) {
                invalidMessage.printStackTrace();
            }
        }

    }

    @Override
    public void sendReply(Message msg) throws EndpointUnavailable {
        endpoint.send(msg);
    }
}
