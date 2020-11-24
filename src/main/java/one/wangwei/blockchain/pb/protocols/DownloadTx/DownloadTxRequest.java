package one.wangwei.blockchain.pb.protocols.DownloadTx;

import lombok.Getter;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;

@Getter
public class DownloadTxRequest extends Message {
    static final public String name = "DownloadTxRequest";

    public DownloadTxRequest() {
        super(name, DownloadTxProtocol.protocolName, Type.Request);
    }

    public DownloadTxRequest(Document doc) throws InvalidMessage {

        super(name, getDataProtocol.protocolName, Type.Request, doc);
        this.doc = doc;

    }
}
