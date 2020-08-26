package one.wangwei.blockchain.block;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.wangwei.blockchain.transaction.MerkleTree;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Blockhead {
    private String hash;
    private String prevBlockHash;
    private long timeStamp;
    private long nonce;
    private MerkleTree merkleTree;
}
