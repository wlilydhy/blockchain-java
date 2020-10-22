package one.wangwei.blockchain.cli;

import com.google.common.collect.Maps;
import one.wangwei.blockchain.Net.Server;
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

            Server serverThread = new Server();
            CliThread cliThread = new CliThread();
            //String[] argss = {"createwallet"};
            //1DRDoamPwRDQa1775dVig7X8BitJm1273D +10
            //1Gsnure8ovCy3SiK6Fth44kDcwrEHjtFuj 10
            //18b76o68gGKg8ndXHDTDWdbG1DkGzMwfvZ 10 -10

            //String[] argss = {"createblockchain", "-address", "18b76o68gGKg8ndXHDTDWdbG1DkGzMwfvZ"};
            //0000fcc80177312ea7cd9b3db9497af6cae1d69a746332571f14d3c687eee1d0

            //String[] argss = {"mineblock", "-address" ,"1Gsnure8ovCy3SiK6Fth44kDcwrEHjtFuj"};


            //String[] argss = {"printaddresses"};
            //String[] argss = {"getbalance", "-address", "18b76o68gGKg8ndXHDTDWdbG1DkGzMwfvZ"};

            //String[] argss = {"send", "-from", "1Gsnure8ovCy3SiK6Fth44kDcwrEHjtFuj", "-to", "1DRDoamPwRDQa1775dVig7X8BitJm1273D", "-amount", "10"};
            String[] argss ={"printchain"};
            serverThread.start();
            cliThread.start(argss);


            }
         catch (Exception e) {
            e.printStackTrace();
        }



    }
}
