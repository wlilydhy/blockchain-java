package one.wangwei.blockchain.pb.protocols.HelloWorld;

import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Manager;
import one.wangwei.blockchain.pb.protocols.*;

import java.util.logging.Logger;

public class HelloWorldProtocol extends Protocol implements IRequestReplyProtocol {
    private static Logger log = Logger.getLogger(HelloWorldProtocol.class.getName());
    private boolean protocolStop = false;

    /**
     * Name of this protocol.
     */
    public static final String protocolName = "HelloWorld";

    /**
     * Initialise the protocol with an endpoint and manager.
     *
     * @param endpoint
     * @param manager
     */
    public HelloWorldProtocol(Endpoint endpoint, Manager manager) {
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

        sendRequest(new HelloWorldRequest());

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

        if(msg instanceof HelloWorldReply) {

            System.out.println("HelloWorld!");
        }

    }

    @Override
    public void receiveRequest(Message msg) throws EndpointUnavailable {

        if(msg instanceof HelloWorldRequest) {
            sendReply(new HelloWorldReply());
        }

    }

    @Override
    public void sendReply(Message msg) throws EndpointUnavailable {
        endpoint.send(msg);

    }
}
