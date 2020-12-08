package one.wangwei.blockchain.pb.Strategy;

import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.DownloadBlocks.DownloadBlocksProtocol;

public class SPV implements ConnectionStrategy{

    ClientManager clientManager;
    Endpoint endpoint;
    public static final String strategyName = "SPV";
    public SPV(ClientManager clientManager, Endpoint endpoint){
        this.clientManager=clientManager;
        this.endpoint=endpoint;
    }
    @Override
    public void algorithmMethod(){
        DownloadBlocksProtocol downloadBlocksProtocol = new DownloadBlocksProtocol(endpoint,clientManager);
        try {
            downloadBlocksProtocol.startAsClient();
        } catch (EndpointUnavailable endpointUnavailable) {
            endpointUnavailable.printStackTrace();
        }

    }
}
