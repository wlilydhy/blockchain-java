package one.wangwei.blockchain.pb;


import one.wangwei.blockchain.pb.Strategy.ConnectionStrategy;
import one.wangwei.blockchain.pb.protocols.Protocol;

/**
 * Manager base class. Methods must be overriden.
 * 
 * @see {@link one.wangwei.blockchain.pb.server.ServerManager}
 * @see {@link one.wangwei.blockchain.pb.client.ClientManager}
 * @author aaron
 *
 */
public class Manager {
	
	/**
	 * The endpoint is ready to use.
	 * @param endpoint
	 */
	public void endpointReady(Endpoint endpoint) {
		
	}
	
	/**
	 * The endpoint close() method has been called and completed.
	 * @param endpoint
	 */
	public void endpointClosed(Endpoint endpoint) {
		
	}
	
	/**
	 * The endpoint has abruptly disconnected. It can no longer
	 * send or receive data.
	 * @param endpoint
	 */
	public void endpointDisconnectedAbruptly(Endpoint endpoint) {
		
	}

	/**
	 * An invalid message was received over the endpoint.
	 * @param endpoint
	 */
	public void endpointSentInvalidMessage(Endpoint endpoint) {
		
	}
	

	/**
	 * The protocol on the endpoint is not responding.
	 * @param endpoint
	 */
	public void endpointTimedOut(Endpoint endpoint,Protocol protocol) {
		
	}

	/**
	 * The protocol on the endpoint has been violated.
	 * @param endpoint
	 */
	public void protocolViolation(Endpoint endpoint,Protocol protocol) {
		
	}

	/**
	 * The session protocol is indicating that a session has started.
	 * @param endpoint
	 */
	public void sessionStarted(Endpoint endpoint) {
		
	}

	/**
	 * The session protocol is indicating that the session has stopped. 
	 * @param endpoint
	 */
	public void sessionStopped(Endpoint endpoint) {
		
	}

	public void VersionStarted(Endpoint endpoint, ConnectionStrategy strategy) {
	}

	public void VersionStopped(Endpoint endpoint) {
	}

	/**
	 * The endpoint has requested a protocol to start. If the protocol
	 * is allowed then the manager should tell the endpoint to handle it
	 * using {@link one.wangwei.blockchain.pb.Endpoint#handleProtocol(Protocol)}
	 * before returning true.
	 * @param protocol
	 * @return true if the protocol was started, false if not (not allowed to run)
	 */
	public boolean protocolRequested(Endpoint endpoint, Protocol protocol) {
		return false;
	}

	


}
