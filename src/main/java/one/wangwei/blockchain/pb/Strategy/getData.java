package one.wangwei.blockchain.pb.Strategy;

import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.DownloadBlocks.DownloadBlocksProtocol;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;

/**
 * @author Dhy
 * 下载区块信息
 */
public class getData implements ConnectionStrategy{
    ClientManager clientManager;
    Endpoint endpoint;
    String hash;
    String type;
    public static final String strategyName = "getData";
    public getData(ClientManager clientManager, Endpoint endpoint){
        this.clientManager=clientManager;
        this.endpoint=endpoint;
    }
    public getData(ClientManager clientManager, Endpoint endpoint,String hash,String type){
        this.clientManager=clientManager;
        this.endpoint=endpoint;
        this.hash = hash;
        this.type = type;
    }
    @Override
    public void algorithmMethod(){
        getDataProtocol getDataProtocol = new getDataProtocol(endpoint,clientManager);
        getDataProtocol.setReplyType(type);
        getDataProtocol.setRequestType(type);
        if (type.equals("Block")) {
           getDataProtocol.setBlockHash(hash);
        }
        else if (type.equals("Transaction")) {
           getDataProtocol.setTransactionHash(hash);
        }
        try {
            getDataProtocol.startAsClient();
        } catch (EndpointUnavailable endpointUnavailable) {
            endpointUnavailable.printStackTrace();
        }

    }
}
