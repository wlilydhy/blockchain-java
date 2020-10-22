package one.wangwei.blockchain.pb.Strategy;

import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.HelloWorld.HelloWorldProtocol;

public class HelloWorld implements ConnectionStrategy{
    ClientManager clientManager;
    Endpoint endpoint;
    public static final String strategyName = "HelloWorld";
    public HelloWorld(ClientManager clientManager,Endpoint endpoint){
        this.clientManager=clientManager;
        this.endpoint=endpoint;
    }
    @Override
    public void algorithmMethod(){
        HelloWorldProtocol helloWorldProtocol = new HelloWorldProtocol(endpoint,clientManager);
        try {
            helloWorldProtocol.startAsClient();
        } catch (EndpointUnavailable endpointUnavailable) {
            endpointUnavailable.printStackTrace();
        }

    }
}
