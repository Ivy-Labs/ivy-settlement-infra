package org.ivy.settlement.ethereum.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.Bytes48;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.teku.api.response.v1.validator.GetNewBlindedBlockResponse;
import tech.pegasys.teku.api.schema.*;
import tech.pegasys.teku.api.schema.altair.BeaconStateAltair;
import tech.pegasys.teku.api.schema.altair.SignedBeaconBlockAltair;
import tech.pegasys.teku.api.schema.bellatrix.BeaconStateBellatrix;
import tech.pegasys.teku.api.schema.bellatrix.SignedBeaconBlockBellatrix;
import tech.pegasys.teku.api.schema.capella.BeaconStateCapella;
import tech.pegasys.teku.api.schema.capella.SignedBeaconBlockCapella;
import tech.pegasys.teku.api.schema.deneb.BeaconStateDeneb;
import tech.pegasys.teku.api.schema.deneb.SignedBeaconBlockDeneb;
import tech.pegasys.teku.api.schema.interfaces.SignedBlock;
import tech.pegasys.teku.api.schema.interfaces.State;
import tech.pegasys.teku.api.schema.phase0.BeaconStatePhase0;
import tech.pegasys.teku.api.schema.phase0.SignedBeaconBlockPhase0;
import tech.pegasys.teku.bls.BLSPublicKey;
import tech.pegasys.teku.ethereum.execution.types.Eth1Address;
import tech.pegasys.teku.ethereum.jackson.Eth1AddressDeserializer;
import tech.pegasys.teku.infrastructure.bytes.Bytes20;
import tech.pegasys.teku.infrastructure.bytes.Bytes4;
import tech.pegasys.teku.infrastructure.jackson.deserializers.bytes.*;
import tech.pegasys.teku.infrastructure.jackson.deserializers.uints.UInt256Deserializer;
import tech.pegasys.teku.infrastructure.jackson.deserializers.uints.UInt256Serializer;
import tech.pegasys.teku.infrastructure.jackson.deserializers.uints.UInt64Deserializer;
import tech.pegasys.teku.infrastructure.jackson.deserializers.uints.UInt64Serializer;
import tech.pegasys.teku.infrastructure.ssz.collections.SszBitvector;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszBitvectorSchema;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.provider.*;
import tech.pegasys.teku.spec.SpecMilestone;

import java.io.IOException;
import java.util.Locale;

/**
 * description:
 * @author carrot
 */
public class JsonParser {


    public static final JsonParser JSON_PARSER = new JsonParser();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonParser() {
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.init();
    }

    private void init() {
        var module = new SimpleModule("TekuJson", new Version(1, 0, 0, (String) null, (String) null, (String) null));

        module.addSerializer(BLSPubKey.class, new BLSPubKeySerializer());
        module.addDeserializer(BLSPubKey.class, new BLSPubKeyDeserializer());

        module.addDeserializer(BLSPublicKey.class, new BLSPublicKeyDeserializer());
        module.addSerializer(BLSPublicKey.class, new BLSPublicKeySerializer());

        module.addDeserializer(BLSSignature.class, new BLSSignatureDeserializer());
        module.addSerializer(BLSSignature.class, new BLSSignatureSerializer());

        module.addKeyDeserializer(Bytes48.class, new Bytes48KeyDeserializer());

        module.addDeserializer(Bytes32.class, new Bytes32Deserializer());

        module.addDeserializer(Bytes4.class, new Bytes4Deserializer());
        module.addSerializer(Bytes4.class, new Bytes4Serializer());

        module.addDeserializer(Eth1Address.class, new Eth1AddressDeserializer());

        module.addSerializer(Bytes20.class, new Bytes20Serializer());
        module.addDeserializer(Bytes20.class, new Bytes20Deserializer());

        module.addDeserializer(Bytes.class, new BytesDeserializer());
        module.addSerializer(Bytes.class, new BytesSerializer());

        module.addDeserializer(Double.class, new DoubleDeserializer());
        module.addSerializer(Double.class, new DoubleSerializer());

        module.addDeserializer(UInt64.class, new UInt64Deserializer());
        module.addSerializer(UInt64.class, new UInt64Serializer());

        module.addDeserializer(UInt256.class, new UInt256Deserializer());
        module.addSerializer(UInt256.class, new UInt256Serializer());

        module.addSerializer(byte[].class, new ByteArraySerializer());
        module.addDeserializer(byte[].class, new ByteArrayDeserializer());

        module.addSerializer(SszBitvector.class, new SszBitvectorSerializer());
        module.addDeserializer(SszBitvector.class, new SszBitvectorDeserializer());

        module.addDeserializer(GetNewBlockResponseV3.class, new GetNewBlockResponseV3Deserializer(this.objectMapper));
        module.addDeserializer(GetStateResponseV3.class, new GetStateResponseV3Deserializer(this.objectMapper));

        module.addDeserializer(GetNewBlindedBlockResponse.class, new GetNewBlindedBlockResponseDeserializer(this.objectMapper));
        module.addSerializer(KZGCommitment.class, new KZGCommitmentSerializer());
        module.addDeserializer(KZGCommitment.class, new KZGCommitmentDeserializer());
        module.addSerializer(KZGProof.class, new KZGProofSerializer());
        module.addDeserializer(KZGProof.class, new KZGProofDeserializer());
        this.objectMapper.registerModule(module);
    }

    public <T> String objectToJSON(T object) {
        try {
            return this.objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> String objectToPrettyJSON(T object) throws JsonProcessingException {
        return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    public <T> T jsonToObject(String json, Class<T> clazz) throws JsonProcessingException {
        return this.objectMapper.readValue(json, clazz);
    }

    public JsonNode readTree(String content) throws JsonProcessingException, JsonMappingException {
        return this.objectMapper.readTree(content);
    }

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }


    public static class GetNewBlockResponseV3 {

        public final tech.pegasys.teku.api.schema.Version version;

        public final SignedBlock data;

        public GetNewBlockResponseV3(tech.pegasys.teku.api.schema.Version version, SignedBlock data) {
            this.version = version;
            this.data = data;
        }

        public GetNewBlockResponseV3(SpecMilestone milestone, SignedBlock data) {
            this(tech.pegasys.teku.api.schema.Version.fromMilestone(milestone), data);
        }
    }

    public static class GetNewBlockResponseV3Deserializer extends JsonDeserializer<GetNewBlockResponseV3> {

        private final ObjectMapper mapper;

        public GetNewBlockResponseV3Deserializer(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        public GetNewBlockResponseV3 deserialize(com.fasterxml.jackson.core.JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            var version = tech.pegasys.teku.api.schema.Version.valueOf(node.findValue("version").asText().toLowerCase(Locale.ROOT));
            SignedBeaconBlock block = switch (version) {
                case bellatrix -> this.mapper.treeToValue(node.findValue("data"), SignedBeaconBlockBellatrix.class);
                case altair -> this.mapper.treeToValue(node.findValue("data"), SignedBeaconBlockAltair.class);
                case phase0 -> this.mapper.treeToValue(node.findValue("data"), SignedBeaconBlockPhase0.class);
                case capella -> this.mapper.treeToValue(node.findValue("data"), SignedBeaconBlockCapella.class);
                case deneb -> this.mapper.treeToValue(node.findValue("data"), SignedBeaconBlockDeneb.class);
                default -> throw new IOException("Milestone was not able to be decoded");
            };
            return new GetNewBlockResponseV3(version, block);
        }
    }

    public static class SszBitvectorDeserializer extends JsonDeserializer<SszBitvector> {

        public SszBitvectorDeserializer() {
        }

        public SszBitvector deserialize(com.fasterxml.jackson.core.JsonParser jp, DeserializationContext ctxt) throws IOException {
            var bytes = Bytes.fromHexString(jp.getValueAsString());
            return SszBitvectorSchema.create(bytes.bitLength()).fromBytes(bytes);
        }
    }

    public static class GetStateResponseStrV3 {

        private String version;

        private String execution_optimistic;

        private String finalized;

        private Object data;


        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getExecution_optimistic() {
            return execution_optimistic;
        }

        public void setExecution_optimistic(String execution_optimistic) {
            this.execution_optimistic = execution_optimistic;
        }

        public String getFinalized() {
            return finalized;
        }

        public void setFinalized(String finalized) {
            this.finalized = finalized;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }



    public static class GetStateResponseV3 {

        public final tech.pegasys.teku.api.schema.Version version;
        @JsonProperty("execution_optimistic")
        public final boolean execution_optimistic;
        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "version"
        )
        @JsonSubTypes({@JsonSubTypes.Type(
                value = BeaconStatePhase0.class,
                name = "phase0"
        ), @JsonSubTypes.Type(
                value = BeaconStateAltair.class,
                name = "altair"
        ), @JsonSubTypes.Type(
                value = BeaconStateBellatrix.class,
                name = "bellatrix"
        ), @JsonSubTypes.Type(
                value = BeaconStateCapella.class,
                name = "capella"
        ), @JsonSubTypes.Type(
                value = BeaconStateDeneb.class,
                name = "deneb"
        )})
        public final State data;

        @JsonCreator
        public GetStateResponseV3(@JsonProperty("version") tech.pegasys.teku.api.schema.Version version, @JsonProperty("execution_optimistic") boolean executionOptimistic, @JsonProperty("data") State data) {
            this.version = version;
            this.execution_optimistic = executionOptimistic;
            this.data = data;
        }
    }


    public static class GetStateResponseV3Deserializer extends JsonDeserializer<GetStateResponseV3> {

        private final ObjectMapper mapper;

        public GetStateResponseV3Deserializer(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        public GetStateResponseV3 deserialize(com.fasterxml.jackson.core.JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            var version = tech.pegasys.teku.api.schema.Version.valueOf(node.findValue("version").asText().toLowerCase(Locale.ROOT));
            var executionOptimistic = node.findValue("execution_optimistic").asBoolean();
            var state = switch (version) {
                case altair -> this.mapper.treeToValue(node.findValue("data"), BeaconStateAltair.class);
                case phase0 -> this.mapper.treeToValue(node.findValue("data"), BeaconStatePhase0.class);
                case bellatrix -> this.mapper.treeToValue(node.findValue("data"), BeaconStateBellatrix.class);
                case capella -> this.mapper.treeToValue(node.findValue("data"), BeaconStateCapella.class);
                case deneb -> this.mapper.treeToValue(node.findValue("data"), BeaconStateDeneb.class);
                default -> throw new IOException("Milestone was not able to be decoded");
            };
            return new GetStateResponseV3(version, executionOptimistic, state);
        }
    }
}
