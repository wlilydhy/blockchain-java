package one.wangwei.blockchain.pb.protocols;

import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.Manager;

/**
 * All protocols have an endpoint and a manager.
 * @see {@link one.wangwei.blockchain.pb.Endpoint}
 * @see {@link one.wangwei.blockchain.pb.Manager}
 * @see {@link one.wangwei.blockchain.pb.protocols.session.SessionProtocol}
 * @see {@link one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveProtocol}
 * @author aaron
 *
 */
public class Protocol {
	/**
	 * The protocol name is used when routing messages to this protocol.
	 * It must be unique over all protocols defined.
	 */
	public static final String protocolName = "Protocol";
	
	/**
	 * The endpoint that is handling the protocol.
	 */
	protected Endpoint endpoint;
	
	/**
	 * The manager to report events to.
	 */
	protected Manager manager;
	
	/**
	 * Initialise the protocol with an endpoint and manager.
	 * @param endpoint
	 * @param manager
	 */
	public Protocol(Endpoint endpoint, Manager manager) {
		this.endpoint=endpoint;
		this.manager=manager;
	}
	
	/**
	 * Signal the protocol to stop. More specifically this method
	 * is called when the protocol should not undertake any more
	 * actions, such as processing messages or sending messages.
	 */
	public void stopProtocol() {
		
	}

	/**
	 * Sometimes the static string reference is not reachable, so
	 * this method provides access.
	 * @return the name of the protocol
	 */
	public String getProtocolName() {
		return protocolName;
	}
}
