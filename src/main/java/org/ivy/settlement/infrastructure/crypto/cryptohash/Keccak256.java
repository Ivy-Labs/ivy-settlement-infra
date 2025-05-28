package org.ivy.settlement.infrastructure.crypto.cryptohash;

public class Keccak256 extends KeccakCore {

	/**
	 * Create the engine.
	 */
	public Keccak256()
	{
		super("eth-keccak-256");
	}

	/** @see Digest */
	public Digest copy()
	{
		return copyState(new Keccak256());
	}

	/** @see Digest */
	public int engineGetDigestLength()
	{
		return 32;
	}

	@Override
	protected byte[] engineDigest() {
		return null;
	}

	@Override
	protected void engineUpdate(byte arg0) {
	}

	@Override
	protected void engineUpdate(byte[] arg0, int arg1, int arg2) {
	}
}
