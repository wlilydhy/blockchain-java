package one.wangwei.blockchain.cli;

import com.google.common.collect.Maps;
import one.wangwei.blockchain.Net.Server;
import one.wangwei.blockchain.pb.server.ServerManager;
import one.wangwei.blockchain.store.RocksDBUtils;
import one.wangwei.blockchain.transaction.TXOutput;
import one.wangwei.blockchain.transaction.Transaction;
import one.wangwei.blockchain.util.SerializeUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.chrono.IsoEra;
import java.util.Iterator;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        //CLI cli = new CLI(args);
        //cli.parse();
        try {
//            RocksDBUtils.getInstance().cleanChainStateBucket();
//            RocksDBUtils.getInstance().cleanBlockBucket();
//            RocksDBUtils.getInstance().cleanTxBucket();
//            RocksDBUtils.getInstance().cleanIpBucket();

            ServerManager serverManager = new ServerManager(9999);

            CliThread cliThread = new CliThread();
            //String[] argss = {"createwallet"};
            //1Pg1RhR6r6VBDLPaL5D8Gd5dxTzLL1YiVG
            //1Dg9aN46gJ7BqLHrnXdozaLjb4meLZkTUv
            //17FnHPctyTSMQCSXnBkEKPiBw4eq2dD8Ly

            //String[] argss = {"createblockchain", "-address", "1Pg1RhR6r6VBDLPaL5D8Gd5dxTzLL1YiVG"};
            //000098e728658c6d86b2aa01ea9e51ffa01ade05d6957dec3aba50e5d98b2dba

            //String[] argss = {"mineblock", "-address" ,"17FnHPctyTSMQCSXnBkEKPiBw4eq2dD8Ly"};
            //String[] argss = {"printaddresses"};
            //String[] argss = {"getbalance", "-address", "1Pg1RhR6r6VBDLPaL5D8Gd5dxTzLL1YiVG"};

            //String[] argss = {"send", "-from", "1Dg9aN46gJ7BqLHrnXdozaLjb4meLZkTUv", "-to", "1Pg1RhR6r6VBDLPaL5D8Gd5dxTzLL1YiVG", "-amount", "10"};
            //String[] argss ={"printchain"};

            //String[] argss = {"downloadblockchain"};

            //String[] argss = {"downloadBlockHead"};

            //String[] argss = {"downloadTx"};

            String[] argss = {"getIp"};
            serverManager.start();
            cliThread.start(argss);

            }
         catch (Exception e) {
            e.printStackTrace();
        }



    }
}
