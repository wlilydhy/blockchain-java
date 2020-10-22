package one.wangwei.blockchain.pb.Strategy;

import lombok.Setter;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.Endpoint;
import one.wangwei.blockchain.pb.EndpointUnavailable;
import one.wangwei.blockchain.pb.client.ClientManager;
import one.wangwei.blockchain.pb.protocols.Inv.InvProtocol;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.Transaction;

@Setter
public class SendBlock implements ConnectionStrategy{
    ClientManager clientManager;
    Endpoint endpoint;
    Block block;
    public static final String strategyName = "SendBlock";
    public SendBlock(ClientManager clientManager,Endpoint endpoint){
        this.clientManager=clientManager;
        this.endpoint=endpoint;

        //test
        Transaction transaction = Transaction.newCoinbaseTX("1Gsnure8ovCy3SiK6Fth44kDcwrEHjtFuj","goldenBlue");
        block = Block.newGenesisBlock(transaction);
        RocksDBUtils.getInstance().putBlock(block);
        System.out.println("Block Hash:"+block.getHash());
    }
    @Override
    public void algorithmMethod(){
        InvProtocol invProtocol = new InvProtocol(endpoint,clientManager);
        //传入要send的区块的id
        invProtocol.setBlockHash(block.getHash());
        invProtocol.setRequestType("Block");
        try {
            invProtocol.startAsClient();
        } catch (EndpointUnavailable endpointUnavailable) {
            endpointUnavailable.printStackTrace();
        }

    }

}
