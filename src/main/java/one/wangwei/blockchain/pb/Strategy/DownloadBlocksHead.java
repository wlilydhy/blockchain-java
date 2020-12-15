package one.wangwei.blockchain.pb.Strategy;

import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.DownloadBlocks.DownloadBlocksProtocol;
import one.wangwei.blockchain.pb.protocols.DownloadBlocksHead.DownloadBlocksHeadProtocol;

/**
 * @author Dhy
 * 下载区块信息
 */
public class DownloadBlocksHead implements ConnectionStrategy{
    ClientManager clientManager;
    Endpoint endpoint;
    public static final String strategyName = "DownloadBlocksHead";
    public DownloadBlocksHead(ClientManager clientManager, Endpoint endpoint){
        this.clientManager=clientManager;
        this.endpoint=endpoint;
    }
    @Override
    public void algorithmMethod(){
        DownloadBlocksHeadProtocol downloadBlocksHeadProtocol = new DownloadBlocksHeadProtocol(endpoint,clientManager);
        try {
            downloadBlocksHeadProtocol.startAsClient();
        } catch (EndpointUnavailable endpointUnavailable) {
            endpointUnavailable.printStackTrace();
        }

    }
}
