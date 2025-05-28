package org.ivy.settlement.ethereum.watcher;

import org.ivy.settlement.ethereum.model.BeaconBlockRecord;
import org.ivy.settlement.ethereum.model.EthReceipt;
import org.ivy.settlement.ethereum.remote.BeaconChainRemoter;
import org.ivy.settlement.ethereum.remote.ExecuteChainRemoter;
import org.ivy.settlement.ethereum.store.BeaconChainStore;
import org.ivy.settlement.ethereum.trie.Trie;
import org.ivy.settlement.infrastructure.async.IrisSettlementWorker;
import org.ivy.settlement.infrastructure.string.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.api.schema.SignedBeaconBlock;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.Spec;
import tech.pegasys.teku.spec.datastructures.state.Checkpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * description:
 * @author emo
 */
public class BeaconWatcher {

    protected static final Logger logger = LoggerFactory.getLogger("eth");

    public static final int MAX_RETRY_TIME = 3;

    private final UInt64 STATE_TRANSIMIT_MAX_SLOTS = UInt64.valueOf(8);

    private final Spec spec;

    BeaconChainRemoter beaconChainRemoter;

    ExecuteChainRemoter executeChainRemoter;

    BeaconChainStore beaconChainStore;

    IrisSettlementWorker watcherWorker;

    ExecutorService executor;

    public BeaconWatcher(Spec spec, List<String> beaconNodeUrls, List<String> executeNodeUrls, BeaconChainStore beaconChainStore) {
        this.spec = spec;
        this.beaconChainRemoter = new BeaconChainRemoter(beaconNodeUrls);
        this.executeChainRemoter = new ExecuteChainRemoter(executeNodeUrls);
        this.beaconChainStore = beaconChainStore;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.watcherWorker = new IrisSettlementWorker("beacon_state_watcher") {
            @Override
            protected void doWork() throws Exception {
                tryUpdateFinalityState();
            }

            @Override
            protected void doException(Throwable e) {
                //e.printStackTrace();
                logger.warn(String.format("[%s] do work warn!", Thread.currentThread().getName()), e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }
        };
    }

    public void start() {
        this.watcherWorker.start();
    }

    public void tryUpdateFinalityState() {
        var remoteFinalitySlot = spec.computeStartSlotAtEpoch(pullRemoteFinalityCheckpoint().getEpoch());
        var finalityState = this.beaconChainStore.getFinalityBeaconState();
        logger.info("tryUpdateFinalityState from  slot {} to slot {}", finalityState.getSlot(), remoteFinalitySlot);


        while (finalityState.getSlot().isLessThanOrEqualTo(remoteFinalitySlot)) {
            var maxStateTransmitTo = finalityState.getSlot().plus(STATE_TRANSIMIT_MAX_SLOTS);
            maxStateTransmitTo = maxStateTransmitTo.isGreaterThan(remoteFinalitySlot) ? remoteFinalitySlot : maxStateTransmitTo;

            var nextSlot = finalityState.getSlot().increment();
            //logger.info("batch try transmit beacon state from  slot {} to slot {}", nextSlot, maxStateTransmitTo);
            var start = System.currentTimeMillis();
            var batchSize = maxStateTransmitTo.min(nextSlot).increment().intValue();
            var pullFuts = new CompletableFuture[batchSize];
            var persisBatch = new ArrayList<BeaconBlockRecord>(batchSize);
            var i = 0;
            while (nextSlot.isLessThanOrEqualTo(maxStateTransmitTo)) {
                pullFuts[i] = pullBeaconBlockRecord(nextSlot);
                nextSlot = nextSlot.increment();
                i++;
            }

            for (var fut : pullFuts) {
                try {
                    var beaconBlockRecord = fut.get();
                    if (beaconBlockRecord == null) continue;
                    persisBatch.add((BeaconBlockRecord) beaconBlockRecord);
                } catch (Exception e) {
                    break;
                }
            }

            logger.info("pull slot from {} to {}, cost {}ms", finalityState.getSlot().longValue(), maxStateTransmitTo.longValue(), System.currentTimeMillis() - start);
            this.beaconChainStore.persist(persisBatch);
            finalityState = this.beaconChainStore.getFinalityBeaconState();
        }
    }

    private Checkpoint pullRemoteFinalityCheckpoint() {
        return beaconChainRemoter.getFinalityCheckpoint("head").get("finalized").asInternalCheckpoint();
    }

    private CompletableFuture<BeaconBlockRecord> pullBeaconBlockRecord(UInt64 slot) {
        var beaconBlockFuture = new CompletableFuture<BeaconBlockRecord>();
        this.executor.execute(() -> {
            var maxRetry = MAX_RETRY_TIME;
            while (maxRetry >= 0) {
                try {
                    var start = System.currentTimeMillis();
                    var response = beaconChainRemoter.getSignedBlock(slot.toString());
                    logger.info("pull BeaconBlock at slot[{}] cost {}ms", slot.longValue(), System.currentTimeMillis() - start);
                    if (response == null) {
                        beaconBlockFuture.complete(null);
                    } else {
                        var signBlock = ((SignedBeaconBlock) response.data).asInternalSignedBeaconBlock(spec);
                        var executionPayloadSummary = signBlock.getMessage().getBody().getOptionalExecutionPayloadSummary().get();
                        var blockNumber = executionPayloadSummary.getBlockNumber();
                        var blockHash = executionPayloadSummary.getBlockHash();
                        var receiptRoot = executionPayloadSummary.getReceiptsRoot();
                        var receipts = pullEthReceipt(slot.longValue(), receiptRoot.toHexString(), blockNumber);
                        var beaconBlock = new BeaconBlockRecord(blockNumber.longValue(), signBlock, receipts);
                        beaconBlockFuture.complete(beaconBlock);
                    }

                    return;
                } catch (Exception e) {
                    logger.warn("get signed block failed!", e);
                    maxRetry--;
                }
            }
            beaconBlockFuture.completeExceptionally(new RuntimeException("get signed block failed!"));
        });
        return beaconBlockFuture;
    }

    private List<EthReceipt> pullEthReceipt(long slot, String exceptRoot, UInt64 blockNumber) {
        var ehtReceipts = executeChainRemoter.getReceiptsByBlockNumber(Numeric.encodeQuantity(blockNumber.bigIntegerValue()));

        while (!exceptRoot.equals(Numeric.toHexString(Trie.calcLocalReceiptsTrie(ehtReceipts)))) {
            logger.warn("pull slot[{}] remote eth block [{}] receipt failed, receipts and receipt root not match, retry!", slot, blockNumber.longValue());
            ehtReceipts = executeChainRemoter.getReceiptsByBlockNumber(Numeric.encodeQuantity(blockNumber.bigIntegerValue()));
        }

        return ehtReceipts;
    }
}
