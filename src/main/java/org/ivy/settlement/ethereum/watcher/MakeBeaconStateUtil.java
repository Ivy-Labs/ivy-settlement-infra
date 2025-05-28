package org.ivy.settlement.ethereum.watcher;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.ivy.settlement.infrastructure.string.Numeric;
import org.ivy.settlement.ethereum.model.BeaconBlockRecord;
import org.ivy.settlement.ethereum.remote.BeaconChainRemoter;
import org.ivy.settlement.ethereum.remote.ExecuteChainRemoter;
import org.ivy.settlement.ethereum.trie.Trie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.api.schema.SignedBeaconBlock;
import tech.pegasys.teku.bls.BLSSignatureVerifier;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.Spec;
import tech.pegasys.teku.spec.SpecFactory;
import tech.pegasys.teku.spec.datastructures.state.beaconstate.BeaconState;
import tech.pegasys.teku.spec.networks.Eth2Network;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * description:
 * @author carrot
 */
public class MakeBeaconStateUtil {

    private static final Logger logger = LoggerFactory.getLogger("utils");

    static BeaconChainRemoter BEACON_ADAPTER = new BeaconChainRemoter(List.of("https://eth2-beacon-mainnet.nodereal.io/v1/57a8053417b14e57ade08cf92a1bfb68"));

    static ExecuteChainRemoter ETH_ADAPTER = new ExecuteChainRemoter(List.of("https://eth-mainnet.nodereal.io/v1/57a8053417b14e57ade08cf92a1bfb68"));

    public static void main(String[] args) {
        execute();
        //load();
    }


    public static long getNextExecutionBlockHeight(Spec spec, BeaconState finalityState) {
        var nextSlot = finalityState.getSlot().increment();
        while (true) {
            try {
                var beaconBlockRecord = pullBeaconBlockRecord(spec, nextSlot);
                if (beaconBlockRecord == null) {
                    logger.info("slot {} block is empty!", nextSlot.longValue());
                    nextSlot = nextSlot.increment();
                    continue;
                }
                logger.info("pull next block[{}] success!", nextSlot.longValue());
                spec.processBlock(
                        finalityState,
                        Objects.requireNonNull(beaconBlockRecord).getSignedBeaconBlock(spec),
                        BLSSignatureVerifier.SIMPLE,
                        Optional.empty()
                );
                return beaconBlockRecord.getNumber();
            } catch (Exception e) {
                logger.info("getNextExecutionBlockHeight error!", e);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                throw new RuntimeException(e);
            }

        }
    }


    public static void execute() {

        var spec = SpecFactory.create(Eth2Network.MAINNET.configName());
        var checkpoint = BEACON_ADAPTER.getFinalityCheckpoint("head").get("finalized").asInternalCheckpoint();
        var header = BEACON_ADAPTER.getBeaconHeader(checkpoint.getRoot().toHexString());
        logger.info("pull header success!");
        logger.info("get checkpoint [{}]", checkpoint.getRoot().toHexString());
        var finalityState = getBeaconStateByRoot(spec, header.state_root);
        long executionHeight = 0;
        while (true) {
            try {
                executionHeight = getNextExecutionBlockHeight(spec, finalityState) - 1;
                break;
            } catch (Exception e) {

                logger.error("BeaconValidator execute failed with unknown error", e);
            }
        }

        logger.info("will write [{}] file", executionHeight);
        try {
            FileOutputStream stream = new FileOutputStream("beacon_finality_state_snapshot.dat");
            finalityState.sszSerialize(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("write [{}] file success", executionHeight);

    }

    private static BeaconState getBeaconStateByRoot(Spec spec, Bytes32 stateRoot) {
        try {
            var start = System.currentTimeMillis();
            var response = BEACON_ADAPTER.getFullBeaconState(stateRoot.toHexString());
            var remoteEnd = System.currentTimeMillis();
            logger.info("remote pull beacon state cost {}s", (remoteEnd - start) / 1000);
            var beaconState = ((tech.pegasys.teku.api.schema.BeaconState) response.data).asInternalBeaconState(spec);
            logger.info("transfer beacon state cost {}ms", System.currentTimeMillis() - remoteEnd);

            if (!beaconState.hashTreeRoot().equals(stateRoot)) {
                logger.warn("BeaconValidator getVersionedStateByRoot, get state with wrong state root");
                throw new RuntimeException("BeaconValidator getVersionedStateByRoot, get state with wrong state root");
            }

            return beaconState;
        } catch (Exception e) {
            logger.warn("BeaconValidator getVersionedStateByRoot failed with unknown error", e);
            throw new RuntimeException(e);
        }
    }

    private static BeaconBlockRecord pullBeaconBlockRecord(Spec spec, UInt64 slot) {
        var start = System.currentTimeMillis();
        var response = BEACON_ADAPTER.getSignedBlock(slot.toString());
        if (response == null) return null;

        logger.info("pull BeaconBlock at slot[{}] cost {}ms", slot.longValue(), System.currentTimeMillis() - start);

        var signBlock = ((SignedBeaconBlock) response.data).asInternalSignedBeaconBlock(spec);
        var blockNumber = signBlock.getMessage().getBody().getOptionalExecutionPayloadSummary().get().getBlockNumber();
        var receipts = ETH_ADAPTER.getReceiptsByBlockNumber(Numeric.encodeQuantity(blockNumber.bigIntegerValue()));

        if (!signBlock.getMessage().getBody().getOptionalExecutionPayloadSummary().get().getReceiptsRoot().toHexString().equals(Numeric.toHexString(Trie.calcLocalReceiptsTrie(receipts)))) {
            logger.warn("BeaconValidator pullBeaconBlockRecord failed, receipts and receipt root not match!");
            throw new RuntimeException("BeaconValidator pullBeaconBlockRecord failed, receipts 和 receipt root 不匹配");
        }
//        var receipts = new ArrayList<EthReceipt>(web3jReceipts.size());
//        for (var each : web3jReceipts) {
//            receipts.add(EthReceipt.fromWeb3jReceipt(each));
//        }

        return new BeaconBlockRecord(blockNumber.longValue(), signBlock, receipts);
    }

    public static void load() {
        String file = "beacon_finality_state_snapshot.dat";
        var spec = SpecFactory.create(Eth2Network.MAINNET.configName());
        var path = "D:\\it\\project\\work\\blockchain\\ivy\\ivy-cross-core\\src\\main\\resources\\beacon_finality_state_snapshot.dat";
        try {

            byte[] bytes = Files.readAllBytes(Paths.get(path));
            var bs = spec.deserializeBeaconState(Bytes.of(bytes));
            System.out.println("read slot:" + bs.getSlot().longValue());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
