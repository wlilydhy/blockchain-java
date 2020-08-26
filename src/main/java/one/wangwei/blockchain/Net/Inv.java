package one.wangwei.blockchain.Net;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Inv {
    //private final static String name="Inv";
    private String category;
    private byte[][] data;
}
