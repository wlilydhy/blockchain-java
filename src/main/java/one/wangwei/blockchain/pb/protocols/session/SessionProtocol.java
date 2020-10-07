package one.wangwei.blockchain.pb.protocols.session;

import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Manager;
import one.wangwei.blockchain.pb.protocols.IRequestReplyProtocol;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.pb.protocols.Protocol;

import java.util.logging.Logger;

/**
 * Allows the client to request the session to start and to request the session
 * to stop, which in turns allows the sockets to be properly closed at both
 * ends. Actually, either party can make such requests, but usually the client
 * would make the session start request as soon as it connects, and usually the
 * client would make the session stop request. The server may however send a
 * session stop request to the client if it wants (needs) to stop the session,
 * e.g. perhaps the server is becoming overloaded and needs to shed some
 * clients.
 * 
 * @see {@link one.wangwei.blockchain.pb.Manager}
 * @see {@link one.wangwei.blockchain.pb.Endpoint}
 * @see {@link one.wangwei.blockchain.pb.protocols.Protocol}
 * @see {@link one.wangwei.blockchain.pb.protocols.IRequestReplyProtocol}
 * @see {@link one.wangwei.blockchain.pb.protocols.session.SessionStartRequest}
 * @see {@link one.wangwei.blockchain.pb.protocols.session.SessionStartReply}
 * @see {@link one.wangwei.blockchain.pb.protocols.session.SessionStopRequest}
 * @see {@link one.wangwei.blockchain.pb.protocols.session.SessionStopReply}
 * @author aaron
 *
 */
public class SessionProtocol extends Protocol implements IRequestReplyProtocol {
	private static Logger log = Logger.getLogger(SessionProtocol.class.getName());
	
	/**
	 * The unique name of the protocol.
	 */
	public static final String protocolName="SessionProtocol";
	
	// Use of volatile is in case the thread that calls stopProtocol is different
	// to the endpoint thread, although in this case it hardly needed.
	
	/**
	 * Whether the protocol has started, i.e. start request and reply have been sent,
	 * or not.
	 */
	private volatile boolean protocolRunning=false;
	
	/**
	 * Initialise the protocol with an endpoint and manager.
	 * @param endpoint
	 * @param manager
	 */
	public SessionProtocol(Endpoint endpoint, Manager manager) {
		super(endpoint,manager);
	}
	
	/**
	 * @return the name of the protocol.
	 */
	@Override
	public String getProtocolName() {
		return protocolName;
	}

	/**
	 * If this protocol is stopped while it is still in the running
	 * state then this indicates something may be a problem.
	 */
	@Override
	public void stopProtocol() {
		if(protocolRunning) {
			log.severe("protocol stopped while it is still underway");
		}
	}
	
	/*
	 * Interface methods
	 */

	
	/**
	 * Called by the manager that is acting as a client.
	 */
	@Override
	public void startAsClient() throws EndpointUnavailable {
		//  send the server a start session request
		sendRequest(new SessionStartRequest());
	}

	/**
	 * Called by the manager that is acting as a server.
	 */
	@Override
	public void startAsServer() {
		// nothing to do really
	}
	
	/**
	 * Generic stop session call, for either client or server.
	 * @throws EndpointUnavailable if the endpoint is not ready or has terminated
	 */
	public void stopSession() throws EndpointUnavailable {
		sendRequest(new SessionStopRequest());
	}
	
	/**
	 * Just send a request, nothing special.
	 * @param msg
	 */
	@Override
	public void sendRequest(Message msg) throws EndpointUnavailable {
		endpoint.send(msg);
	}

	/**
	 * If the reply is a session start reply then tell the manager that
	 * the session has started, otherwise if its a session stop reply then
	 * tell the manager that the session has stopped. If something weird 
	 * happens then tell the manager that something weird has happened.
	 * @param msg
	 */
	@Override
	public void receiveReply(Message msg) {
		if(msg instanceof SessionStartReply) {
			if(protocolRunning){
				// error, received a second reply?
				manager.protocolViolation(endpoint,this);
				return;
			}
			protocolRunning=true;
			manager.sessionStarted(endpoint);
		} else if(msg instanceof SessionStopReply) {
			if(!protocolRunning) {
				// error, received a second reply?
				manager.protocolViolation(endpoint,this);
				return;
			}
			protocolRunning=false;
			manager.sessionStopped(endpoint);
		}
	}

	/**
	 * If the received request is a session start request then reply and
	 * tell the manager that the session has started. If the received request
	 * is a session stop request then reply and tell the manager that
	 * the session has stopped. If something weird has happened then...
	 * @param msg
	 */
	@Override
	public void receiveRequest(Message msg) throws EndpointUnavailable {
		if(msg instanceof SessionStartRequest) {
			if(protocolRunning) {
				// error, received a second request?
				manager.protocolViolation(endpoint,this);
				return;
			}
			protocolRunning=true;
			sendReply(new SessionStartReply());
			//表示会话开始，这时候可以做一些别的事情
			//会话开始，接下来客户端根据进行具体的业务流程
			manager.sessionStarted(endpoint);
		} else if(msg instanceof SessionStopRequest) {
			if(!protocolRunning) {
				// error, received a second request?
				manager.protocolViolation(endpoint,this);
				return;
			}
			protocolRunning=false;
			sendReply(new SessionStopReply());
			manager.sessionStopped(endpoint);
		}
		
	}

	/**
	 * Just send a reply, nothing special to do.
	 * @param msg
	 */
	@Override
	public void sendReply(Message msg) throws EndpointUnavailable {
		endpoint.send(msg);
	}

	

	
}
