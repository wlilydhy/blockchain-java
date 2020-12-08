package one.wangwei.blockchain.pb.Strategy;

import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.DownloadTx.DownloadTxProtocol;
import one.wangwei.blockchain.pb.protocols.getIp.getIpProtocol;

import java.lang.management.GarbageCollectorMXBean;

/**
 * @author Dhy
 * 下载区块信息
 */
public class getIp implements ConnectionStrategy{
    ClientManager clientManager;
    Endpoint endpoint;
    public static final String strategyName = "getIp";
    public getIp(ClientManager clientManager, Endpoint endpoint){
        this.clientManager=clientManager;
        this.endpoint=endpoint;
    }
    @Override
    public void algorithmMethod(){
        getIpProtocol getIpProtocol = new getIpProtocol(endpoint,clientManager);
        try {
            getIpProtocol.startAsClient();
        } catch (EndpointUnavailable endpointUnavailable) {
            endpointUnavailable.printStackTrace();
        }

    }
}
