package one.wangwei.blockchain.pb.protocols.DownloadTx;

import lombok.Setter;
import one.wangwei.blockchain.pb.protocols.DownloadBlocks.DownloadBlocksReply;
import one.wangwei.blockchain.pb.protocols.IRequestReplyProtocol;
import one.wangwei.blockchain.pb.protocols.Protocol;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Manager;
import one.wangwei.blockchain.pb.Strategy.DownloadTx;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.*;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;
import one.wangwei.blockchain.pb.protocols.getData.getDataReply;
import one.wangwei.blockchain.pb.protocols.getData.getDataRequest;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.util.SerializeUtils;
import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Setter
public class DownloadTxProtocol extends Protocol implements IRequestReplyProtocol {
    private static Logger log = Logger.getLogger(DownloadTxProtocol.class.getName());
    private boolean protocolStop = false;

    public static final String protocolName = "DownloadTxProtocol";

    public DownloadTxProtocol(Endpoint endpoint, Manager manager) {
        super(endpoint, manager);
    }

    @Override
    public String getProtocolName() {
        return protocolName;
    }

    @Override
    public void stopProtocol() {
        log.severe("protocol stopped while it is still underway");
        protocolStop = true;
        manager.sessionStopped(endpoint);
    }

    @Override
    public void startAsClient() throws EndpointUnavailable {
        Document doc = new Document();
        doc.append("name", DownloadTxRequest.name);
        doc.append("protocolName", protocolName);
        doc.append("type", Message.Type.Request.toString());
        try {
            sendRequest(new DownloadTxRequest(doc));
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
    public void receiveReply(Message msg) throws EndpointUnavailable, IOException {
        if(msg instanceof DownloadTxReply) {
            List<String> blockHashes = ((DownloadTxReply) msg).getTxHashList();
            stopProtocol();
            if(blockHashes!=null) {
                for (String str : blockHashes) {
                    Utils.getInstance().download(str, "Transaction");
                }
            }

        }

    }

    @Override
    public void receiveRequest(Message msg) throws EndpointUnavailable, DecoderException {
        if (msg instanceof DownloadTxRequest) {
            Document doc = new Document();
            doc.append("name", DownloadTxReply.name);
            doc.append("protocolName", protocolName);
            doc.append("type", Message.Type.Reply.toString());
            Map<String, byte[]> txBucket = RocksDBUtils.getInstance().getTxBucket();
            Iterator<Map.Entry<String, byte[]>> iterator = txBucket.entrySet().iterator();
            List<String> txHashes = new ArrayList<>();
            if (!iterator.hasNext()) {
                doc.append("txHashes", "null");
                log.info("txBucket is empty");
                try {
                    sendReply(new DownloadTxReply(doc));
                } catch (InvalidMessage | DecoderException invalidMessage) {
                    invalidMessage.printStackTrace();
                }

            }
            else {
                while (iterator.hasNext()) {
                    Map.Entry<String, byte[]> entry = iterator.next();
                    txHashes.add(entry.getKey());
                }
                String str = Utils.getInstance().listToString(txHashes);
                doc.append("txHashes", str);
                try {
                    sendReply(new DownloadTxReply(doc));
                } catch (InvalidMessage | DecoderException invalidMessage) {
                    invalidMessage.printStackTrace();
                }
            }
        }
    }

    @Override
    public void sendReply(Message msg) throws EndpointUnavailable {
        endpoint.send(msg);
    }
}
