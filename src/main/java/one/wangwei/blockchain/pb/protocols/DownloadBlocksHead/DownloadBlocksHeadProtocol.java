package one.wangwei.blockchain.pb.protocols.DownloadBlocksHead;

import lombok.Setter;
import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Manager;
import one.wangwei.blockchain.pb.Strategy.DownloadBlocksHead;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.*;
import one.wangwei.blockchain.pb.protocols.DownloadBlocks.DownloadBlocksReply;
import one.wangwei.blockchain.pb.protocols.DownloadBlocks.DownloadBlocksRequest;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;
import one.wangwei.blockchain.store.RocksDBUtils;
import org.apache.commons.codec.DecoderException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Setter
public class DownloadBlocksHeadProtocol extends Protocol implements IRequestReplyProtocol {
    private static Logger log = Logger.getLogger(DownloadBlocksHeadProtocol.class.getName());
    private boolean protocolStop = false;


    /**
     * Name of this protocol.
     */
    public static final String protocolName = "DownloadBlocksHeadProtocol";

    /**
     * Initialise the protocol with an endpoint and manager.
     *
     * @param endpoint
     * @param manager
     */
    public DownloadBlocksHeadProtocol(Endpoint endpoint, Manager manager) {
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
        doc.append("name", DownloadBlocksHeadRequest.name);
        doc.append("protocolName", protocolName);
        doc.append("type", Message.Type.Request.toString());
        try {
            sendRequest(new DownloadBlocksHeadRequest(doc));
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

        if(msg instanceof DownloadBlocksHeadReply) {
           Map<String,byte[]> blockHead = ((DownloadBlocksHeadReply) msg).getBlockHead();
           RocksDBUtils.getInstance().setBlockHeadBucket(blockHead);
           stopProtocol();
        }

    }

    @Override
    public void receiveRequest(Message msg) throws EndpointUnavailable, DecoderException {
        if(msg instanceof DownloadBlocksHeadRequest) {
            Document doc = new Document();
            doc.append("name", DownloadBlocksHeadReply.name);
            doc.append("protocolName", protocolName);
            doc.append("type", Message.Type.Reply.toString());
            Map<String,byte[]> blockHeadBucket = RocksDBUtils.getInstance().getBlockHeadBucket();
            String str = Utils.getInstance().MapToString1(blockHeadBucket);
            doc.append("blockHead",str);
            try {
                sendReply(new DownloadBlocksHeadReply(doc));
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
