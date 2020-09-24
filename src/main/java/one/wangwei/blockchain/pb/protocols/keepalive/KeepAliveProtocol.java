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
 * 
 * @see {@link one.wangwei.blockchain.pb.Manager}
 * @see {@link one.wangwei.blockchain.pb.Endpoint}
 * @see {@link one.wangwei.blockchain.pb.protocols.Message}
 * @see {@link one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveRequest}
 * @see {@link one.wangwei.blockchain.pb.protocols.Protocol}
 * @author aaron
 *
 */
public class KeepAliveProtocol extends Protocol implements IRequestReplyProtocol {
	private static Logger log = Logger.getLogger(KeepAliveProtocol.class.getName());
	
	/**
	 * Name of this protocol. 
	 */
	public static final String protocolName="KeepAliveProtocol";
	
	/**
	 * Initialise the protocol with an endopint and a manager.
	 * @param endpoint
	 * @param manager
	 */
	public KeepAliveProtocol(Endpoint endpoint, Manager manager) {
		super(endpoint,manager);
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
		
	}
	
	/*
	 * Interface methods
	 */
	
	/**
	 * 
	 */
	public void startAsServer() {

		
	}
	
	/**
	 * 
	 */
	public void checkClientTimeout() {
		
	}
	
	/**
	 * 
	 */
	public void startAsClient() throws EndpointUnavailable {

		sendRequest(new KeepAliveRequest());

		
	}

	/**
	 * 
	 * @param msg
	 */
	@Override
	public void sendRequest(Message msg) throws EndpointUnavailable {
		endpoint.send(msg);
		
	}

	/**
	 * 
	 * @param msg
	 */
	@Override
	public void receiveReply(Message msg) throws EndpointUnavailable {
		if(msg instanceof KeepAliveReply){
			sendRequest(new KeepAliveReply());
		}
		
	}

	/**
	 *
	 * @param msg
	 * @throws EndpointUnavailable 
	 */
	@Override
	public void receiveRequest(Message msg) throws EndpointUnavailable {
		if(msg instanceof KeepAliveRequest) {
			sendReply(new KeepAliveReply());
		}
	}

	/**
	 * 
	 * @param msg
	 */
	@Override
	public void sendReply(Message msg) throws EndpointUnavailable {
		endpoint.send(msg);
	}
	
	
}
