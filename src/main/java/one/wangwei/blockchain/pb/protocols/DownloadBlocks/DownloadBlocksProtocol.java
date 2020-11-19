package one.wangwei.blockchain.pb.protocols.DownloadBlocks;

import lombok.Setter;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Manager;
import one.wangwei.blockchain.pb.Strategy.DownloadBlocks;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.*;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;
import one.wangwei.blockchain.pb.protocols.getData.getDataReply;
import one.wangwei.blockchain.pb.protocols.getData.getDataRequest;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.util.SerializeUtils;
import org.apache.commons.codec.DecoderException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Setter
public class DownloadBlocksProtocol extends Protocol implements IRequestReplyProtocol {
    private static Logger log = Logger.getLogger(DownloadBlocksProtocol.class.getName());
    private boolean protocolStop = false;


    /**
     * Name of this protocol.
     */
    public static final String protocolName = "DownloadBlocksProtocol";

    /**
     * Initialise the protocol with an endpoint and manager.
     *
     * @param endpoint
     * @param manager
     */
    public DownloadBlocksProtocol(Endpoint endpoint, Manager manager) {
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
        manager.sessionStopped(endpoint);
    }


    @Override
    public void startAsClient() throws EndpointUnavailable {
        Document doc = new Document();
        doc.append("name", DownloadBlocksRequest.name);
        doc.append("protocolName", protocolName);
        doc.append("type", Message.Type.Request.toString());
        try {
            sendRequest(new DownloadBlocksRequest(doc));
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

        if(msg instanceof DownloadBlocksReply) {
            List<String> blockHashes = ((DownloadBlocksReply) msg).getBlockHashList();
            for(String str : blockHashes){
                getDataProtocol getDataProtocol = new getDataProtocol(endpoint,manager);
                getDataProtocol.setRequestType("Block");
                getDataProtocol.setReplyType("Block");
                getDataProtocol.setBlockHash(str);
                getDataProtocol.startAsClient();
            }
            stopProtocol();
        }

    }

    @Override
    public void receiveRequest(Message msg) throws EndpointUnavailable, DecoderException {
        if(msg instanceof DownloadBlocksRequest) {
            Document doc = new Document();
            doc.append("name", DownloadBlocksReply.name);
            doc.append("protocolName", protocolName);
            doc.append("type", Message.Type.Reply.toString());
            Map<String,byte[]> blockBucket = RocksDBUtils.getInstance().getBlocksBucket();
            Iterator<Map.Entry<String,byte[]>> iterator = blockBucket.entrySet().iterator();
            List<String> blockHashes = new ArrayList<>();
            if(!iterator.hasNext()){
                log.info("blockBucket is empty");
            }
            while(iterator.hasNext()){
                Map.Entry<String, byte[]> entry = iterator.next();
                blockHashes.add(entry.getKey());
            }
            String str = Utils.getInstance().listToString(blockHashes);
            doc.append("blockHashes",str);
            try {
                sendReply(new DownloadBlocksReply(doc));
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
