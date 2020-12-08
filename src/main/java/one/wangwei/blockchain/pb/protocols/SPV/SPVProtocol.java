package one.wangwei.blockchain.pb.protocols.SPV;

import lombok.Setter;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Manager;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.*;
import one.wangwei.blockchain.pb.protocols.DownloadTx.DownloadTxProtocol;
import one.wangwei.blockchain.pb.protocols.getData.getDataRequest;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.MerkleTree;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.util.SerializeUtils;
import org.apache.commons.codec.DecoderException;

import java.util.*;
import java.util.logging.Logger;

public class SPVProtocol extends Protocol implements IRequestReplyProtocol {
    private static Logger log = Logger.getLogger(DownloadTxProtocol.class.getName());
    private boolean protocolStop = false;
    @Setter
    private String hash;

    public static final String protocolName = "SPVProtocol";

    /**
     * Initialise the protocol with an endpoint and manager.
     *
     * @param endpoint
     * @param manager
     */
    public SPVProtocol(Endpoint endpoint, Manager manager) {
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
        doc.append("name", getDataRequest.name);
        doc.append("protocolName", protocolName);
        doc.append("type", Message.Type.Request.toString());
        doc.append("Hash",hash);
        try {
            sendRequest(new SPVRequest(doc));
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
    public void receiveReply(Message msg) throws EndpointUnavailable, DecoderException {

        if(msg instanceof SPVReply){
            String str = ((SPVReply) msg).getMerkleProofStr();
            List<MerkleTree.Node> merkleProof = Utils.getInstance().stringToNodeList(str);
            if(Utils.getInstance().checkSPV(merkleProof)){
                //then
            }
            else if(!Utils.getInstance().checkSPV(merkleProof)){
                //then
            }
            stopProtocol();

        }

    }

    @Override
    public void receiveRequest(Message msg) throws EndpointUnavailable, DecoderException {
        //返回MerkelProof
        if(msg instanceof SPVRequest){
            String str = ((SPVRequest) msg).getHash();
            byte[] txId = Base64.getDecoder().decode(str);
            Map<String,byte[]> blockBucket = RocksDBUtils.getInstance().getBlocksBucket();
            Iterator<Map.Entry<String,byte[]>> iterator = blockBucket.entrySet().iterator();
            List<MerkleTree.Node> merkleProof = new ArrayList<>();
            if(!iterator.hasNext()){
                log.info("blockBucket is empty");
            }
            while(iterator.hasNext()){
                Map.Entry<String, byte[]> entry = iterator.next();
                byte[] blockBytes = entry.getValue();
                Block block =(Block) SerializeUtils.deserialize(blockBytes);
                if(Utils.getInstance().findTransaction(str,block)){
                    merkleProof = block.getMerkleTree().MerkleProof(txId);
                    break;
                }
            }
            String merkleProofStr = Utils.getInstance().NodelistToString(merkleProof);
            Document doc = new Document();
            doc.append("name", getDataRequest.name);
            doc.append("protocolName", protocolName);
            doc.append("type", Message.Type.Reply.toString());
            doc.append("merkleProof",merkleProofStr);
            try {
                sendReply(new SPVReply(doc));
            } catch (InvalidMessage invalidMessage) {
                invalidMessage.printStackTrace();
            }


        }

    }

    @Override
    public void sendReply(Message msg) throws EndpointUnavailable {
        endpoint.send(msg);

    }
}
