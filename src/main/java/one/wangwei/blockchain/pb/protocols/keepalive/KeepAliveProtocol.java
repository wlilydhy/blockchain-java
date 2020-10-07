package one.wangwei.blockchain.pb.protocols.keepalive;

import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Manager;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.IRequestReplyProtocol;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.pb.protocols.Protocol;

import java.util.logging.Logger;

/**
 * Provides all of the protocol logic for both client and server to undertake
 * the KeepAlive protocol. In the KeepAlive protocol, the client sends a
 * KeepAlive request to the server every 20 seconds using
 * {@link one.wangwei.blockchain.pb.Utils#setTimeout(one.wangwei.blockchain.pb.protocols.ICallback, long)}. The server must
 * send a KeepAlive response to the client upon receiving the request. If the
 * client does not receive the response within 20 seconds (i.e. at the next time
 * it is to send the next KeepAlive request) it will assume the server is dead
 * and signal its manager using
 * {@link one.wangwei.blockchain.pb.Manager#endpointTimedOut(Endpoint,Protocol)}. If the server does
 * not receive a KeepAlive request at least every 20 seconds (again using
 * {@link one.wangwei.blockchain.pb.Utils#setTimeout(one.wangwei.blockchain.pb.protocols.ICallback, long)}), it will assume
 * the client is dead and signal its manager. Upon initialisation, the client
 * should send the KeepAlive request immediately, whereas the server will wait
 * up to 20 seconds before it assumes the client is dead. The protocol stops
 * when a timeout occurs.
 *
 * @see {@link one.wangwei.blockchain.pb.Manager}
 * @see {@link one.wangwei.blockchain.pb.Endpoint}
 * @see {@link one.wangwei.blockchain.pb.protocols.Message}
 * @see {@link one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveRequest}
 * @see {@link one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveReply}
 * @see {@link one.wangwei.blockchain.pb.protocols.Protocol}
 * @see {@link one.wangwei.blockchain.pb.protocols.IRequestReplyProtocol}
 * @author aaron
 *
 */
public class KeepAliveProtocol extends Protocol implements IRequestReplyProtocol {
    private static Logger log = Logger.getLogger(KeepAliveProtocol.class.getName());

    /**
     * Name of this protocol.
     */
    public static final String protocolName = "KeepAliveProtocol";
    private boolean replyReceived = false;
    private boolean requestReceived = false;
    private boolean protocolStop = false;

    /**
     * Initialise the protocol with an endopint and a manager.
     *
     * @param endpoint
     * @param manager
     */
    public KeepAliveProtocol(Endpoint endpoint, Manager manager) {
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

    /*
     * Interface methods
     */

    /**
     *
     */
    public void startAsServer() {
        checkClientTimeout();
    }

    /**
     *
     */
    public void checkClientTimeout() {
        // notice manager
        // judge whether protocol has stopped, needed otherwise it will check after
        // endpoint terminated, this thread is still alive.
        if (!protocolStop) {
            Utils.getInstance().setTimeout(() -> {
                if (!requestReceived) {
                    manager.endpointTimedOut(endpoint, this);
                    endpoint.close();
                }
                this.requestReceived = false;
            }, 20000);
        }

    }

    public void checkServerTimeout() {
        // notice manager
        Utils.getInstance().setTimeout(() -> {
            if (!replyReceived) {
                manager.endpointTimedOut(endpoint, this);
                endpoint.close();
            }
            this.replyReceived = false;
        }, 20000);

    }

    /**
     *
     */
    public void startAsClient() throws EndpointUnavailable {

        endpoint.send(new KeepAliveRequest());
        checkServerTimeout();
    }

    /**
     *
     * @param msg
     */
    @Override
    public void sendRequest(Message msg) throws EndpointUnavailable {
        Utils.getInstance().setTimeout(() -> {
            try {
                endpoint.send(msg);
                checkServerTimeout();
                // checkClientTimeout();
            } catch (EndpointUnavailable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }, 20000);

    }

    /**
     *
     * @param msg
     * @throws EndpointUnavailable
     */
    @Override
    public void receiveReply(Message msg) {

        if (msg instanceof KeepAliveReply) {

            this.replyReceived = true;
            try {
                sendRequest(new KeepAliveRequest());
            } catch (EndpointUnavailable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     *
     * @param msg
     * @throws EndpointUnavailable
     */
    @Override
    public void receiveRequest(Message msg) throws EndpointUnavailable {

        if (msg instanceof KeepAliveRequest) {
            this.requestReceived = true;
            sendReply(new KeepAliveReply());

        }
    }

    /**
     *
     * @param msg
     */
    @Override
    public void sendReply(Message msg) throws EndpointUnavailable {
        Utils.getInstance().setTimeout(() -> {
            checkClientTimeout();
        }, 20000);

        endpoint.send(msg);

    }

}
