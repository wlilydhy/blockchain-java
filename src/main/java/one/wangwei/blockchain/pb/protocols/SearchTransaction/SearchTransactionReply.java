package one.wangwei.blockchain.pb.protocols.SearchTransaction;

import lombok.Getter;
import one.wangwei.blockchain.pb.Strategy.SearchTransaction;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.transaction.Transaction;
import org.apache.commons.codec.DecoderException;
@Getter
public class SearchTransactionReply extends Message {

    static final public String name = "SearchTransactionReply";
    private Transaction transaction ;

    public SearchTransactionReply() {
        super(name, SearchTransactionProtocol.protocolName, Type.Reply);
    }

    public SearchTransactionReply(Document doc) throws InvalidMessage, DecoderException {
        super(name, SearchTransactionProtocol.protocolName, Type.Reply,doc);
        this.doc=doc;
        this.transaction= Utils.getInstance().StringToTx((String)doc.get("Transaction"));
    }
}
