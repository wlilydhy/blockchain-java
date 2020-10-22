package one.wangwei.blockchain.pb.protocols.Inv;

import lombok.Setter;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Manager;
import one.wangwei.blockchain.pb.Strategy.ConnectionStrategy;
import one.wangwei.blockchain.pb.protocols.*;
import one.wangwei.blockchain.pb.protocols.Version.VersionReply;
import one.wangwei.blockchain.pb.protocols.Version.VersionRequest;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;
import one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveProtocol;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.Transaction;


import java.util.logging.Logger;

@Setter
public class InvProtocol extends Protocol implements IRequestReplyProtocol {

    private static Logger log = Logger.getLogger(KeepAliveProtocol.class.getName());
    private boolean protocolStop = false;
    private String RequestType;
    private String ReplyType;
    private byte[] TransactionHash;
    private String BlockHash;

    public static final String protocolName = "InvProtocol";

    /**
     * Initialise the protocol with an endpoint and manager.
     *
     * @param endpoint
     * @param manager
     */
    public InvProtocol(Endpoint endpoint, Manager manager) {
        super(endpoint, manager);
    }


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
        manager.sessionStopped(endpoint);
    }

    @Override
    public void startAsClient() throws EndpointUnavailable {
        Document doc = new Document();
        doc.append("name", InvRequest.name);
        doc.append("protocolName", protocolName);
        doc.append("type", Message.Type.Request.toString());
        doc.append("RequestType",RequestType);
        if(RequestType=="Block") {
            doc.append("Hash", BlockHash);
        }
        else if(RequestType=="Transaction"){
            doc.append("Hash",TransactionHash);
        }
        try {
            sendRequest(new InvRequest(doc));
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
        if(msg instanceof InvReply){
            log.info("data is existed");
            stopProtocol();

        }

    }

    @Override
    public void receiveRequest(Message msg) throws EndpointUnavailable {
        if(msg instanceof InvRequest) {
            String requestType = ((InvRequest) msg).getRequestType();

            if(requestType.equals("Block")){
                String BlockHash = ((InvRequest) msg).getBlockHash();
                //检查是否在数据库中已经存在了该区块
                Block block = RocksDBUtils.getInstance().getBlock(BlockHash);
                //如果存在就告诉对方已经存在
                if(block!=null){
                    log.info("Block is existed \n"+block.toString());
                    sendReply(new InvReply());
                    return;
                }
                //不存在直接请求数据，进入getdata环节
                getDataProtocol getDataProtocol = new getDataProtocol(endpoint,manager);
                getDataProtocol.setRequestType("Block");
                getDataProtocol.setReplyType("Block");
                getDataProtocol.setBlockHash(BlockHash);
                getDataProtocol.startAsClient();

            }

            if(requestType.equals("Transaction")){
                byte[] TxHash = ((InvRequest) msg).getTxHash();
                //检查是否在数据库中已经存在了该区块
                Transaction transaction = RocksDBUtils.getInstance().getTransacion(TxHash);
                //如果存在就告诉对方已经存在
                if(transaction!=null){
                    log.info("transaction is existed \n"+transaction.toString());
                    sendReply(new InvReply());
                    return;
                }
                //不存在直接请求数据，进入getdata环节
                getDataProtocol getDataProtocol = new getDataProtocol(endpoint,manager);
                getDataProtocol.setRequestType("Transaction");
                getDataProtocol.setReplyType("Transaction");
                getDataProtocol.setTransactionHash(TxHash);
                getDataProtocol.startAsClient();
            }

        }

    }

    @Override
    public void sendReply(Message msg) throws EndpointUnavailable {
        endpoint.send(msg);
    }
}
