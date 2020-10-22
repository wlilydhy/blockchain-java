package one.wangwei.blockchain.pb.protocols.getData;

import lombok.Getter;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.pb.protocols.Version.VersionProtocol;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.Transaction;
import org.apache.commons.codec.DecoderException;

@Getter
public class getDataReply extends Message {
    static final public String name = "getDataReply";
    private Transaction transaction;
    private Block block;
    private String ReplyType;

    /**
     * Initialiser when given message parameters explicitly. Note that
     * in this message there are no additional parameters.
     */
    public getDataReply() {
        super(name, getDataProtocol.protocolName, Type.Reply);
    }

    /**
     * Initialiser when given message parameters in a doc. Must throw
     * InvalidMessag if any of the required parameters are not
     * in the doc, including the appropriate msg parameter.
     * @param doc with the message details
     * @throws InvalidMessage when the doc does not contain all of the required parameters
     */
    public getDataReply(Document doc) throws InvalidMessage, DecoderException {
        super(name, getDataProtocol.protocolName, Type.Reply, doc); // really just testing the name, otherwise nothing more to test
        this.doc = doc;
        this.ReplyType = (String) doc.get("ReplyType");
        if (ReplyType.equals("Block")) {
            //String blockHash = (String) doc.get("Block");
            //this.block = RocksDBUtils.getInstance().getBlock(blockHash);
            this.block= Utils.getInstance().StringToBlock((String) doc.get("Block"));
            //System.out.println("block is"+block.toString());
        }
        else if (ReplyType.equals("Transaction")) {
            //byte[] txHash = (byte[])doc.get("Transaction");
            //this.transaction = RocksDBUtils.getInstance().getTransacion(txHash);
            this.transaction= Utils.getInstance().StringToTx((String) doc.get("Transaction"));
        }
    }


}
