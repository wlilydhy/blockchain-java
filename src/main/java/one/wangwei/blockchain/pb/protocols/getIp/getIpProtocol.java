package one.wangwei.blockchain.pb.protocols.getIp;

import lombok.Setter;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Manager;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.*;
import one.wangwei.blockchain.pb.protocols.Inv.InvReply;
import one.wangwei.blockchain.pb.protocols.Inv.InvRequest;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.Transaction;
import org.apache.commons.codec.DecoderException;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

@Setter
public class getIpProtocol extends Protocol implements IRequestReplyProtocol {

    private static Logger log = Logger.getLogger(getIpProtocol.class.getName());
    private boolean protocolStop = false;

    public static final String protocolName = "getIpProtocol";

    /**
     * Initialise the protocol with an endpoint and manager.
     *
     * @param endpoint
     * @param manager
     */
    public getIpProtocol(Endpoint endpoint, Manager manager) {
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
        doc.append("name", getIpRequest.name);
        doc.append("protocolName", protocolName);
        doc.append("type", Message.Type.Request.toString());
        try {
            sendRequest( new getIpRequest(doc));
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
        if(msg instanceof getIpReply){
            String ipString = ((getIpReply) msg).getIpString();
            Map<String,Integer> ipBucket = Utils.getInstance().stringToMap(ipString);
            Map<String,Integer> MyIpBucket = RocksDBUtils.getInstance().getIpBucket();
            Iterator<Map.Entry<String,Integer>> iterator = ipBucket.entrySet().iterator();
            if(!iterator.hasNext()){
                log.info("blockBucket is empty");
            }
            while(iterator.hasNext()) {
                Map.Entry<String, Integer> entry = iterator.next();
                String ip = entry.getKey();
                if(MyIpBucket.get(ip)==null){
                    RocksDBUtils.getInstance().putIp(entry.getKey(),entry.getValue());
                }
            }
        }
        stopProtocol();

    }

    @Override
    public void receiveRequest(Message msg) throws EndpointUnavailable, DecoderException {
        if(msg instanceof getIpRequest) {
            Map<String,Integer> ipBucket = RocksDBUtils.getInstance().getIpBucket();
            String ipString = Utils.getInstance().MapToString(ipBucket);
            Document doc = new Document();
            doc.append("name", getIpRequest.name);
            doc.append("protocolName", protocolName);
            doc.append("type", Message.Type.Reply.toString());
            doc.append("ipString",ipString);
            try {
                sendReply( new getIpReply(doc));
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
