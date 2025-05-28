package org.ivy.settlement.ethereum.model;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPList;
import org.ivy.settlement.infrastructure.rlp.RLPModel;
import org.ivy.settlement.infrastructure.anyhow.Assert;

/**
 * description:
 * @author carrot
 */
public class LatestUploadBlobState extends RLPModel {

    public static final int UPLOAD_SUCCESS = 1;

    public static final int UPLOAD_RESIGN = 2;

    private long currentNumber;

    private int status;

    public LatestUploadBlobState(byte[] rlpEncoded) {
        super(rlpEncoded);
    }

    public LatestUploadBlobState(long currentNumber, int status) {
        super(null);
        this.currentNumber = currentNumber;
        this.status = status;
        this.rlpEncoded = rlpEncoded();
    }

    public long getCurrentNumber() {
        return currentNumber;
    }

    public int getStatus() {
        return status;
    }

    public void reset(long currentNumber, int status) {
        Assert.ensure((currentNumber == this.currentNumber || currentNumber == this.currentNumber + 1), "en except number change, current is {}, update is {}", this.currentNumber, currentNumber);
        if (currentNumber == this.currentNumber) {
            Assert.ensure(this.status == UPLOAD_RESIGN && status == UPLOAD_SUCCESS, "un except state change, current status is {}, update status is {}", this.status, status);
            this.status = status;
        } else if (currentNumber == this.currentNumber + 1) {
            Assert.ensure(this.status == UPLOAD_SUCCESS, "un except state change for number[{}], current status[{}] is not success, can't not change to next", this.currentNumber, this.status);
            this.currentNumber = currentNumber;
            this.status = status;
        }
        this.rlpEncoded = null;
    }

    @Override
    protected byte[] rlpEncoded() {
        var encodeArray = new byte[2][];
        encodeArray[0] = RLP.encodeElement(ByteUtil.longToBytes(this.currentNumber));
        encodeArray[1] = RLP.encodeElement(ByteUtil.intToBytes(this.status));
        return RLP.encodeList(encodeArray);
    }

    @Override
    protected void rlpDecoded() {
        RLPList rlpList = (RLPList) RLP.decode2(rlpEncoded).get(0);
        this.currentNumber = ByteUtil.byteArrayToLong(rlpList.get(0).getRLPData());
        this.status = ByteUtil.byteArrayToInt(rlpList.get(1).getRLPData());
    }

    public LatestUploadBlobState copy() {
        return new LatestUploadBlobState(this.currentNumber, this.status);
    }
}
