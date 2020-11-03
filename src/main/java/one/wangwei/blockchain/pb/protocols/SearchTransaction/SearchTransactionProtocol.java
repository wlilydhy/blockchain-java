package one.wangwei.blockchain.pb.protocols.SearchTransaction;

import lombok.Setter;
import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.Manager;
import one.wangwei.blockchain.pb.Strategy.SearchTransaction;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.*;
import one.wangwei.blockchain.pb.protocols.Inv.InvRequest;
import one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveProtocol;
import one.wangwei.blockchain.transaction.Transaction;
import org.apache.commons.codec.DecoderException;

import java.util.logging.Logger;

public class SearchTransactionProtocol extends Protocol implements IRequestReplyProtocol {

    private static Logger log = Logger.getLogger(SearchTransactionProtocol.class.getName());
    private boolean protocolStop = false;
    @Setter
    private String TransactionHash;

    public static final String protocolName = "SearchTransactionProtocol";


    /**
     *
     */
    @Override
    public void stopProtocol() {
        log.severe("protocol stopped while it is still underway");
        protocolStop = true;
        manager.sessionStopped(endpoint);
    }

    /**
     * Initialise the protocol with an endpoint and manager.
     *
     * @param endpoint
     * @param manager
     */
    public SearchTransactionProtocol(Endpoint endpoint, Manager manager) {
        super(endpoint, manager);
    }

    @Override
    public void startAsClient() throws EndpointUnavailable {
        Document doc = new Document();
        doc.append("name", InvRequest.name);
        doc.append("protocolName", protocolName);
        doc.append("type", Message.Type.Request.toString());
        doc.append("TransactionHash",TransactionHash);
        try {
            SearchTransactionRequest searchTransactionRequest=new SearchTransactionRequest(doc);
            sendRequest(searchTransactionRequest);
        } catch (InvalidMessage invalidMessage) {
            invalidMessage.printStackTrace();
        }

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
        if (msg instanceof SearchTransactionReply) {
            Transaction transaction = ((SearchTransactionReply) msg).getTransaction();
            System.out.println("transaction is" + transaction.toString());
            stopProtocol();
        }
    }

    @Override
    public void receiveRequest(Message msg) throws EndpointUnavailable, DecoderException {
        if(msg instanceof SearchTransactionRequest) {
            String transactionHash = ((SearchTransactionRequest) msg).getTransactionHash();
            Transaction transaction = Utils.getInstance().searchTransaction(transactionHash);
            String strTx = Utils.getInstance().TxToString(transaction);
            Document doc = new Document();
            doc.append("name", InvRequest.name);
            doc.append("protocolName", protocolName);
            doc.append("type", Message.Type.Request.toString());
            doc.append("Transaction",strTx);
            try {
                SearchTransactionReply searchTransactionReply=new SearchTransactionReply(doc);
                sendRequest(searchTransactionReply);
            } catch (InvalidMessage invalidMessage) {
                invalidMessage.printStackTrace();
            }

        }
    }

    @Override
    public void sendReply(Message msg) throws EndpointUnavailable {
        endpoint.send(msg);
    }
}
