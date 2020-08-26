package one.wangwei.blockchain.Net;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Version {
    //private double version;
    private final static String name="Version";
    private String Height;
    private boolean Willing;
    public boolean getWilling(){
        return Willing;
    }
}
