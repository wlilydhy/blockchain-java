package one.wangwei.blockchain.pb.Strategy;

import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.Inv.InvProtocol;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.Transaction;

public class SendTransaction implements ConnectionStrategy{
    ClientManager clientManager;
    Endpoint endpoint;
    Transaction transaction;
    public static final String strategyName = "SendTransaction";
    public SendTransaction(ClientManager clientManager,Endpoint endpoint,Transaction transaction){
        this.clientManager=clientManager;
        this.endpoint=endpoint;
        this.transaction=transaction;

        //test
//        transaction = Transaction.newCoinbaseTX("1Gsnure8ovCy3SiK6Fth44kDcwrEHjtFuj","goldenBlue");
//        RocksDBUtils.getInstance().putTransaction(transaction);
////        Transaction ky = RocksDBUtils.getInstance().getTransacion(transaction.getStringOfTxid());
////        if(ky==null){
////            System.out.println("null");
////        }
////        else if(ky!=null){
////            System.out.println("not null");
////        }
//        System.out.println("Transaction Hash:"+transaction.getStringOfTxid());
    }
    @Override
    public void algorithmMethod(){
        InvProtocol invProtocol = new InvProtocol(endpoint,clientManager);
        //传入要send的区块的id
        invProtocol.setTransactionHash(transaction.getStringOfTxid());
        invProtocol.setRequestType("Transaction");
        try {
            invProtocol.startAsClient();
        } catch (EndpointUnavailable endpointUnavailable) {
            endpointUnavailable.printStackTrace();
        }

    }
}
