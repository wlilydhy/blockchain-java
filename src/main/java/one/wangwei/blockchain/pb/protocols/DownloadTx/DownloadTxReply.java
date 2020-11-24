package one.wangwei.blockchain.pb.protocols.DownloadTx;

import lombok.Getter;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;
import org.apache.commons.codec.DecoderException;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DownloadTxReply extends Message {
    static final public String name = "DownloadTxReply";
    private List<String> txHashList = new ArrayList<>();

    public DownloadTxReply() {
        super(name, DownloadTxProtocol.protocolName, Type.Reply);
    }

    public DownloadTxReply(Document doc) throws InvalidMessage, DecoderException {
        super(name, getDataProtocol.protocolName, Type.Reply, doc);
        this.doc = doc;
        this.txHashList = Utils.getInstance().stringToList((String) doc.get("txHashes"));
    }
}
