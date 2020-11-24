package one.wangwei.blockchain.pb.Strategy;

import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.DownloadTx.DownloadTxProtocol;

/**
 * @author Dhy
 * 下载区块信息
 */
public class DownloadTx implements ConnectionStrategy{
    ClientManager clientManager;
    Endpoint endpoint;
    public static final String strategyName = "DownloadTx";
    public DownloadTx(ClientManager clientManager, Endpoint endpoint){
        this.clientManager=clientManager;
        this.endpoint=endpoint;
    }
    @Override
    public void algorithmMethod(){
        DownloadTxProtocol downloadTxProtocol = new DownloadTxProtocol(endpoint,clientManager);
        try {
            downloadTxProtocol.startAsClient();
        } catch (EndpointUnavailable endpointUnavailable) {
            endpointUnavailable.printStackTrace();
        }

    }
}
