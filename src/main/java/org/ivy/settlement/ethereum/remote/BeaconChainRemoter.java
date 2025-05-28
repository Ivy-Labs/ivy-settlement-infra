package org.ivy.settlement.ethereum.remote;

import org.ivy.settlement.infrastructure.exception.HttpResourceNotFundException;
import org.ivy.settlement.infrastructure.http.HttpClientUtil;
import org.ivy.settlement.infrastructure.string.StringUtils;
import org.ivy.settlement.ethereum.model.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.api.response.v1.beacon.EpochCommitteeResponse;
import tech.pegasys.teku.api.response.v1.beacon.ValidatorResponse;
import tech.pegasys.teku.api.schema.Attestation;
import tech.pegasys.teku.api.schema.BeaconBlock;
import tech.pegasys.teku.api.schema.BeaconBlockHeader;
import tech.pegasys.teku.api.schema.Checkpoint;
import tech.pegasys.teku.bls.BLSPublicKey;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


/**
 * description:
 * @author carrot
 */
public class BeaconChainRemoter {

    private static final Logger logger = LoggerFactory.getLogger("rpc");

    private final String chainName;

    private final AtomicLong currentIndex;

    private List<String> endPoints;


    public BeaconChainRemoter(List<String> initURLs) {
        this.chainName = "beacon";
        this.currentIndex = new AtomicLong();
        this.endPoints = initURLs;
    }


    public String getEndPoint() {
        var index = (int) currentIndex.get() % endPoints.size();
        currentIndex.incrementAndGet();
        return endPoints.get(index);
    }

    public Map<String, Checkpoint> getFinalityCheckpoint(String stateId) throws RuntimeException {
        try {
            var url = getEndPoint() + "/eth/v1/beacon/states/" + stateId + "/finality_checkpoints";
            var data = HttpClientUtil.doGet(url);
            if (StringUtils.isBlank(data)) {
                logger.warn("getFinalityCheckpoint failed, data is null. chainName:{}", chainName);
                throw new RuntimeException("getFinalityCheckpoint failed, data is null.");
            }
            var root = JsonParser.JSON_PARSER.readTree(data);

            var checkpointStone = new HashMap<String, Checkpoint>();
            checkpointStone.put("finalized", JsonParser.JSON_PARSER.jsonToObject(root.get("data").get("finalized").toString(), Checkpoint.class));
            checkpointStone.put("current_justified", JsonParser.JSON_PARSER.jsonToObject(root.get("data").get("current_justified").toString(), Checkpoint.class));
            checkpointStone.put("previous_justified", JsonParser.JSON_PARSER.jsonToObject(root.get("data").get("previous_justified").toString(), Checkpoint.class));
            return checkpointStone;
        } catch (Exception e) {
            logger.warn("BeaconChainAdapter getFinalityCheckpoint failed. chainName:{}.", chainName, e);
            throw new RuntimeException("BeaconChainAdapter getFinalityCheckpoint failed.");
        }
    }

    public UInt64 getLatestSlot() throws RuntimeException {
        try {
            var url = getEndPoint() + "/eth/v1/beacon/headers/head";
            var data = HttpClientUtil.doGet(url);
            if (StringUtils.isBlank(data)) {
                logger.warn("getLatestSlot failed, data is null. chainName:{}", chainName);
                throw new RuntimeException("getLatestSlot failed, data is null.");
            }
            var root = JsonParser.JSON_PARSER.readTree(data);
            return JsonParser.JSON_PARSER.jsonToObject(root.get("data").get("header").get("message").get("slot").asText(), UInt64.class);
        } catch (Exception e) {
            logger.warn("BeaconChainAdapter getFinalityCheckpoint failed. chainName:{}.", chainName, e);
            throw new RuntimeException("BeaconChainAdapter getFinalityCheckpoint failed.");
        }
    }

    public Attestation[] getBlockAttestations(String stateId) throws RuntimeException {
        try {
            var url = getEndPoint() + "/eth/v1/beacon/blocks/" + stateId + "/attestations";
            var data = HttpClientUtil.doGet(url);
            if (StringUtils.isBlank(data)) {
                logger.warn("getBlockAttestations failed, data is null. chainName:{}", chainName);
                throw new RuntimeException("getBlockAttestations failed, data is null.");
            }
            var root = JsonParser.JSON_PARSER.readTree(data);
            return JsonParser.JSON_PARSER.jsonToObject(root.get("data").toString(), Attestation[].class);
        } catch (Exception e) {
            logger.warn("BeaconChainAdapter getBlockAttestations failed. chainName:{}.", chainName, e);
            throw new RuntimeException("BeaconChainAdapter getBlockAttestations failed.");
        }
    }

    public BeaconBlockHeader getBeaconHeader(String stateId) throws RuntimeException {
        try {
            var url = getEndPoint() + "/eth/v1/beacon/headers/" + stateId;
            var data = HttpClientUtil.doGet(url);
            if (StringUtils.isBlank(data)) {
                logger.warn("getBeaconHeader failed, data is null. chainName:{}", chainName);
                throw new RuntimeException("getBeaconHeader failed, data is null.");
            }
            var root = JsonParser.JSON_PARSER.readTree(data);
            return JsonParser.JSON_PARSER.jsonToObject(root.get("data").get("header").get("message").toString(), BeaconBlockHeader.class);
        } catch (Exception e) {
            logger.warn("BeaconChainAdapter getBeaconHeader failed. chainName:{}.", chainName, e);
            throw new RuntimeException("BeaconChainAdapter getBeaconHeader failed.");
        }
    }

    public JsonParser.GetStateResponseV3 getFullBeaconState(String stateId) throws RuntimeException {
        try {
            var url = getEndPoint() + "/eth/v2/debug/beacon/states/" + stateId;
//            var response = HttpClientUtil.doGet(url);
//            if (!HttpCodeEnum.SUCCESS.getCode().equals(response.getCode())) {
//                logger.warn("getFullBeaconState failed. chainName:{},  errorCode:{}, errorMessage:{}.", chainName, response.getCode(), response.getErrorMessage());
//                throw new RuntimeException("getFullBeaconState failed. errorMsg:" + response.getErrorMessage());
//            }
            var data = HttpClientUtil.doGet(url);
            if (StringUtils.isBlank(data)) {
                logger.warn("getFullBeaconState failed, data is null. chainName:{}", chainName);
                throw new RuntimeException("getFullBeaconState failed, data is null.");
            }

            //use 3g memory

            return JsonParser.JSON_PARSER.jsonToObject(data, JsonParser.GetStateResponseV3.class);
        } catch (Exception e) {
            logger.warn("BeaconChainAdapter getFullBeaconState failed. chainName:{}.", chainName, e);
            throw new RuntimeException("BeaconChainAdapter getFullBeaconState failed.");
        }
    }

    public BeaconBlock getBlock(String slotNumberOrBlockRoot) throws RuntimeException {
        try {
            var url = getEndPoint() + "/eth/v2/beacon/blocks/" + slotNumberOrBlockRoot;
            var data = HttpClientUtil.doGet(url);
            if (StringUtils.isBlank(data)) {
                logger.warn("getBlock failed, data is null. chainName:{}", chainName);
                throw new RuntimeException("getBlock failed, data is null.");
            }
            var root = JsonParser.JSON_PARSER.readTree(data);
            return JsonParser.JSON_PARSER.jsonToObject(root.get("data").get("message").toString(), BeaconBlock.class);
        } catch (Exception e) {
            logger.warn("BeaconChainAdapter getBlock failed. chainName:{}.", chainName, e);
            throw new RuntimeException("BeaconChainAdapter getBlock failed.");
        }
    }

    public JsonParser.GetNewBlockResponseV3 getSignedBlock(String slotNumberOrBlockRoot) throws RuntimeException {
        try {
            var url = getEndPoint() + "/eth/v2/beacon/blocks/" + slotNumberOrBlockRoot;
            var data = HttpClientUtil.doGet(url);
            if (StringUtils.isBlank(data)) {
                logger.warn("getBlock failed, data is null. chainName:{}", chainName);
                throw new RuntimeException("getBlock failed, data is null.");
            }
            var root = JsonParser.JSON_PARSER.readTree(data);
            return JsonParser.JSON_PARSER.jsonToObject(root.toString(), JsonParser.GetNewBlockResponseV3.class);
        } catch (HttpResourceNotFundException e) {
            return null;
        } catch (Exception e) {
            logger.warn("BeaconChainAdapter getBlock failed. chainName:{}.", chainName, e);
            throw new RuntimeException("BeaconChainAdapter getBlock failed.");
        }
    }

    public String getSignedBlockJson(String slotNumberOrBlockRoot) throws RuntimeException {
        try {
            var url = getEndPoint() + "/eth/v2/beacon/blocks/" + slotNumberOrBlockRoot;
            var data = HttpClientUtil.doGet(url);
            if (StringUtils.isBlank(data)) {
                logger.warn("getBlock failed, data is null. chainName:{}", chainName);
                throw new RuntimeException("getBlock failed, data is null.");
            }
            return JsonParser.JSON_PARSER.readTree(data).toString();
        } catch (Exception e) {
            logger.warn("BeaconChainAdapter getBlock failed. chainName:{}.", chainName, e);
            throw new RuntimeException("BeaconChainAdapter getBlock failed.");
        }
    }

    public Map<Long, List<Long>> getCommittees(Long slotNumber) throws RuntimeException {
        try {
            var url = getEndPoint() + "/eth/v1/beacon/states/" + slotNumber + "/committees?slot=" + slotNumber;
            var data = HttpClientUtil.doGet(url);
            if (StringUtils.isBlank(data)) {
                logger.warn("getCommittees failed, data is null. chainName:{}", chainName);
                throw new RuntimeException("getCommittees failed, data is null.");
            }
            var root = JsonParser.JSON_PARSER.readTree(data);
            EpochCommitteeResponse[] committeeResponses = JsonParser.JSON_PARSER.jsonToObject(root.get("data").toString(), EpochCommitteeResponse[].class);
            return Arrays.stream(committeeResponses).collect(Collectors.toMap(c -> c.index.longValue(), c -> c.validators.stream().map(UInt64::longValue).collect(Collectors.toList())));
        } catch (Exception e) {
            logger.warn("BeaconChainAdapter getCommittees failed. chainName:{}.", chainName, e);
            throw new RuntimeException("BeaconChainAdapter getCommittees failed.");
        }
    }

    public Map<Long, BLSPublicKey> getValidators(Long slotNumber, List<Long> validatorIndices) throws RuntimeException {
        try {
            var sb = new StringBuilder();
            for (int i = 0; i < validatorIndices.size() - 1; i++) {
                sb.append(validatorIndices.get(i)).append(",");
            }
            sb.append(validatorIndices.get(validatorIndices.size() - 1));
            var url = getEndPoint() + "/eth/v1/beacon/states/" + slotNumber + "/validators?id=" + sb.toString();
            var data =HttpClientUtil.doGet(url);
            if (StringUtils.isBlank(data)) {
                logger.warn("getValidators failed, data is null. chainName:{}", chainName);
                throw new RuntimeException("getCommittees failed, data is null.");
            }
            var root = JsonParser.JSON_PARSER.readTree(data);
            ValidatorResponse[] validatorResponses = JsonParser.JSON_PARSER.jsonToObject(root.get("data").toString(), ValidatorResponse[].class);
            return Arrays.stream(validatorResponses).collect(Collectors.toMap(v -> v.index.longValue(), ValidatorResponse::getPublicKey));
        } catch (Exception e) {
            logger.warn("BeaconChainAdapter getValidators failed. chainName:{}.", chainName, e);
            throw new RuntimeException("BeaconChainAdapter getValidators failed.");
        }
    }

    public void updateEndPoint(List<String> endPoints) {
        this.endPoints = endPoints;
    }
}
