package one.wangwei.blockchain.pb.client;


import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import one.wangwei.blockchain.Net.Inv;
import one.wangwei.blockchain.pb.*;
import one.wangwei.blockchain.pb.Strategy.*;
import one.wangwei.blockchain.pb.protocols.HelloWorld.HelloWorldProtocol;
import one.wangwei.blockchain.pb.protocols.IRequestReplyProtocol;
import one.wangwei.blockchain.pb.protocols.Inv.InvProtocol;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Protocol;
import one.wangwei.blockchain.pb.protocols.Version.VersionProtocol;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;
import one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveProtocol;
import one.wangwei.blockchain.pb.protocols.session.SessionProtocol;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * Manages the connection to the server and the client's state.
 * 
 * @see {@link one.wangwei.blockchain.pb.Manager}
 * @see {@link one.wangwei.blockchain.pb.Endpoint}
 * @see {@link one.wangwei.blockchain.pb.protocols.Protocol}
 * @see {@link one.wangwei.blockchain.pb.protocols.IRequestReplyProtocol}
 * @author aaron
 *
 */

public class ClientManager extends Manager {
	private static Logger log = Logger.getLogger(ClientManager.class.getName());
	private SessionProtocol sessionProtocol;
	private KeepAliveProtocol keepAliveProtocol;
	private VersionProtocol versionProtocol;
	private HelloWorldProtocol helloWorldProtocol;
	private InvProtocol invProtocol;
	private getDataProtocol getDataProtocol;
	private Socket socket;
	private ConnectionStrategy strategy;
	private String StrategyString;
	private Endpoint endpoint;
	
	public ClientManager(String host,int port,String strategyString) throws UnknownHostException, IOException {

		//socket=new Socket(InetAddress.getByName(host),port);
		//socket=new Socket(host,port);
		//Endpoint endpoint = new Endpoint(socket,this);
		StrategyString = strategyString;

		//初始化连接策略
//		switch (strategyString) {
//			case SendBlock.strategyName:
//				this.strategy=new SendBlock(this,endpoint);
//				break;
//			case HelloWorld.strategyName:
//				this.strategy=new HelloWorld(this,endpoint);
//				break;
//			default:
//				System.out.println("InvalidStrategy");
//				break;
//				//throw new InvalidMessage();
//
//		}
		//endpoint.start();

		// simulate the client shutting down after 2mins
		// this will be removed when the client actually does something
		// controlled by the user
//		Utils.getInstance().setTimeout(()->{
//			try {
//				sessionProtocol.stopSession();
//			} catch (EndpointUnavailable e) {
//				//ignore...
//			}
//		}, 120000);
//
//		try {
//			// just wait for this thread to terminate
//			endpoint.join();
//		} catch (InterruptedException e) {
//			// just make sure the ioThread is going to terminate
//			endpoint.close();
//		}
//		Utils.getInstance().cleanUp();
	}


	@SneakyThrows
	@Override
	public void run(){
		socket=new Socket("127.0.0.1",9999);
		Endpoint endpoint = new Endpoint(socket,this);
		//StrategyString = strategyString;

		//初始化连接策略
		switch (StrategyString) {
			case SendBlock.strategyName:
				this.strategy=new SendBlock(this,endpoint);
				break;
			case HelloWorld.strategyName:
				this.strategy=new HelloWorld(this,endpoint);
				break;
			case SendTransaction.strategyName:
				this.strategy=new SendTransaction(this,endpoint);
				break;

			default:
				System.out.println("InvalidStrategy");
				break;
			//throw new InvalidMessage();

		}
		endpoint.start();
	}

	/**
	 * The endpoint is ready to use.
	 * @param endpoint
	 */
	@Override
	//每次新建protocol都要在这里添加
	public void endpointReady(Endpoint endpoint) {
		log.info("connection with server established");
		//sessionprotocol导入并初始化
		sessionProtocol = new SessionProtocol(endpoint,this);
		try {
			// we need to add it to the endpoint before starting it
			endpoint.handleProtocol(sessionProtocol);
			//endpoint send
			sessionProtocol.startAsClient();
		} catch (EndpointUnavailable e) {
			log.severe("connection with server terminated abruptly");
			endpoint.close();
		} catch (ProtocolAlreadyRunning e) {
			// hmmm, so the server is requesting a session start?
			log.warning("server initiated the session protocol... weird");
		}
		//keepAliceProtocol导入并初始化
		keepAliveProtocol = new KeepAliveProtocol(endpoint,this);
		try {
			// we need to add it to the endpoint before starting it
			endpoint.handleProtocol(keepAliveProtocol);
			keepAliveProtocol.startAsClient();
		} catch (ProtocolAlreadyRunning | EndpointUnavailable e) {
			// hmmm, so the server is requesting a session start?
			log.warning("server initiated the session protocol... weird");
		}

		versionProtocol =new VersionProtocol(endpoint,this);
		try {
			endpoint.handleProtocol(versionProtocol);
		} catch (ProtocolAlreadyRunning protocolAlreadyRunning) {
			protocolAlreadyRunning.printStackTrace();
		}

		helloWorldProtocol=new HelloWorldProtocol(endpoint,this);
		try {
			endpoint.handleProtocol(helloWorldProtocol);
		} catch (ProtocolAlreadyRunning protocolAlreadyRunning) {
			protocolAlreadyRunning.printStackTrace();
		}

		invProtocol = new InvProtocol(endpoint,this);
		try {
			endpoint.handleProtocol(invProtocol);
		} catch (ProtocolAlreadyRunning protocolAlreadyRunning) {
			protocolAlreadyRunning.printStackTrace();
		}

		getDataProtocol = new getDataProtocol(endpoint,this);
		try {
			endpoint.handleProtocol(getDataProtocol);
		} catch (ProtocolAlreadyRunning protocolAlreadyRunning) {
			protocolAlreadyRunning.printStackTrace();
		}

	}
	
	/**
	 * The endpoint close() method has been called and completed.
	 * @param endpoint
	 */
	public void endpointClosed(Endpoint endpoint) {
		log.info("connection with server terminated");
	}
	
	/**
	 * The endpoint has abruptly disconnected. It can no longer
	 * send or receive data.
	 * @param endpoint
	 */
	@Override
	public void endpointDisconnectedAbruptly(Endpoint endpoint) {
		log.severe("connection with server terminated abruptly");
		endpoint.close();
	}

	/**
	 * An invalid message was received over the endpoint.
	 * @param endpoint
	 */
	@Override
	public void endpointSentInvalidMessage(Endpoint endpoint) {
		log.severe("server sent an invalid message");
		endpoint.close();
	}
	

	/**
	 * The protocol on the endpoint is not responding.
	 * @param endpoint
	 */
	@Override
	public void endpointTimedOut(Endpoint endpoint,Protocol protocol) {
		log.severe("server has timed out");
		endpoint.close();
	}

	/**
	 * The protocol on the endpoint has been violated.
	 * @param endpoint
	 */
	@Override
	public void protocolViolation(Endpoint endpoint,Protocol protocol) {
		log.severe("protocol with server has been violated: "+protocol.getProtocolName());
		endpoint.close();
	}

	/**
	 * The session protocol is indicating that a session has started.
	 * @param endpoint
	 */
	@Override
	public void sessionStarted(Endpoint endpoint) {
		log.info("session has started with server");
		VersionProtocol versionProtocol= new VersionProtocol(endpoint,this);
		try {
			versionProtocol.startAsClient();
		} catch (EndpointUnavailable endpointUnavailable) {
			endpointUnavailable.printStackTrace();
		}
		// we can now start other protocols with the server
	}

	/**
	 * The session protocol is indicating that the session has stopped. 
	 * @param endpoint
	 */
	@Override
	public void sessionStopped(Endpoint endpoint) {
		log.info("session has stopped with server");
		endpoint.close(); // this will stop all the protocols as well
	}


	//这里可以作为抽象的入口，在这里选择到底做什么业务流程。
	public void VersionStarted(){
		//log.info("version has started with server");
		strategy.algorithmMethod();
	}



	/**
	 * The endpoint has requested a protocol to start. If the protocol
	 * is allowed then the manager should tell the endpoint to handle it
	 * using {@link one.wangwei.blockchain.pb.Endpoint#handleProtocol(Protocol)}
	 * before returning true.
	 * @param protocol
	 * @return true if the protocol was started, false if not (not allowed to run)
	 */
	@Override
	public boolean protocolRequested(Endpoint endpoint, Protocol protocol) {
		// the only protocols in this system are this kind...
		try {
			((IRequestReplyProtocol)protocol).startAsClient();
			endpoint.handleProtocol(protocol);
			return true;
		} catch (EndpointUnavailable e) {
			// very weird... should log this
			return false;
		} catch (ProtocolAlreadyRunning e) {
			// even more weird... should log this too
			return false;
		}
	}

}
