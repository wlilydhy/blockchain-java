package one.wangwei.blockchain.pb.protocols.Version;

import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Manager;
import one.wangwei.blockchain.pb.Strategy.ConnectionStrategy;
import one.wangwei.blockchain.pb.Strategy.HelloWorld;
import one.wangwei.blockchain.pb.Strategy.SendBlock;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.*;
import one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveProtocol;

import java.util.logging.Logger;

public class VersionProtocol extends Protocol implements IRequestReplyProtocol {
    private static Logger log = Logger.getLogger(KeepAliveProtocol.class.getName());
    private boolean protocolStop = false;

    /**
     * Name of this protocol.
     */
    public static final String protocolName = "VersionProtocol";

    /**
     * Initialise the protocol with an endpoint and manager.
     *
     * @param endpoint
     * @param manager
     */
    public VersionProtocol(Endpoint endpoint, Manager manager) {
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
        //get blockCount
        long BlockCount = 0;
        doc.append("name", VersionRequest.name);
        doc.append("protocolName", protocolName);
        doc.append("type", Message.Type.Request.toString());
        doc.append("version","1.0");
        doc.append("BlockCount",BlockCount);
        try {
            sendRequest(new VersionRequest(doc));
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

        //接下来是调用环节了
        if(msg instanceof VersionReply) {
            String version = ((VersionReply) msg).getVersion();
            //对比version

            //getBlockCount
            Integer BlockCount = 0;
            if(BlockCount<((VersionReply) msg).getBlockCount()){
                //调用下载区块的模块
            }
            //下一层(选择策略)
            //ConnectionStrategy strategy = new HelloWorld((ClientManager)manager,endpoint);
            //ConnectionStrategy strategy = new SendBlock((ClientManager)manager,endpoint);
            manager.VersionStarted();

        }

    }

    @Override
    public void receiveRequest(Message msg) throws EndpointUnavailable {
        //这里要判断version是否一致
        //block数量是否一致
        //并返回相关信息，主动权应该在receiveReply的一方
        if(msg instanceof VersionRequest) {
            String version = ((VersionRequest) msg).getVersion();
            //System.out.println("version"+version);
            Document doc = new Document();
            //get BlockCount
            long BlockCount = 0;
            doc.append("name", VersionReply.name);
            doc.append("protocolName", protocolName);
            doc.append("type", Message.Type.Reply.toString());
            doc.append("version","1.0");
            doc.append("BlockCount",BlockCount);
            try {
                sendReply(new VersionReply(doc));
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
