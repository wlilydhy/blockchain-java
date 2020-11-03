package one.wangwei.blockchain.pb.protocols.SearchTransaction;

import lombok.Getter;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;
@Getter
public class SearchTransactionRequest extends Message {

    static final public String name = "SearchTransactionRequest";
    private String TransactionHash;

    public SearchTransactionRequest() {
        super(name, SearchTransactionProtocol.protocolName, Type.Request);
    }

    public SearchTransactionRequest(Document doc) throws InvalidMessage {
        super(name, SearchTransactionProtocol.protocolName, Type.Request,doc);
        this.doc=doc;
        this.TransactionHash = (String)doc.get("TransactionHash");
    }

}
