package one.wangwei.blockchain.pb.Strategy;

import lombok.Setter;
import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.DownloadBlocks.DownloadBlocksProtocol;
import one.wangwei.blockchain.pb.protocols.SPV.SPVProtocol;

public class SPV implements ConnectionStrategy{

    ClientManager clientManager;
    Endpoint endpoint;
    String TxHash;
    public static final String strategyName = "SPV";
    public SPV(ClientManager clientManager, Endpoint endpoint){
        this.clientManager=clientManager;
        this.endpoint=endpoint;
    }
    public SPV(ClientManager clientManager, Endpoint endpoint,String str){
        this.clientManager=clientManager;
        this.endpoint=endpoint;
        this.TxHash=str;
    }
    @Override
    public void algorithmMethod(){
        SPVProtocol spvProtocol = new SPVProtocol(endpoint,clientManager);
        spvProtocol.setHash(TxHash);
        try {
            spvProtocol.startAsClient();
        } catch (EndpointUnavailable endpointUnavailable) {
            endpointUnavailable.printStackTrace();
        }

    }
}
