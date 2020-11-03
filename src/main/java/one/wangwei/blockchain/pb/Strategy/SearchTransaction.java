package one.wangwei.blockchain.pb.Strategy;

import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.HelloWorld.HelloWorldProtocol;
import one.wangwei.blockchain.pb.protocols.SearchTransaction.SearchTransactionProtocol;
import one.wangwei.blockchain.transaction.Transaction;
import org.omg.CORBA.TRANSACTION_MODE;

public class SearchTransaction implements  ConnectionStrategy{

    ClientManager clientManager;
    Endpoint endpoint;
    public static final String strategyName = "HelloWorld";
    public SearchTransaction(ClientManager clientManager, Endpoint endpoint){
        this.clientManager=clientManager;
        this.endpoint=endpoint;
    }
    @Override
    public void algorithmMethod(){
        SearchTransactionProtocol searchTransactionProtocol = new SearchTransactionProtocol(endpoint,clientManager);
        Transaction transaction = Transaction.newCoinbaseTX("1Gsnure8ovCy3SiK6Fth44kDcwrEHjtFuj","goldenBlue");
        searchTransactionProtocol.setTransactionHash(transaction.getStringOfTxid());
        try {
            searchTransactionProtocol.startAsClient();
        } catch (EndpointUnavailable endpointUnavailable) {
            endpointUnavailable.printStackTrace();
        }

    }
}
