public class BitString {
    private long bits;
    private int bitsUsed;

    public BitString(long bits, int bitsUsed) {
        this.bits = bits;
        this.bitsUsed = bitsUsed;
    }

    public BitString(BitString bitString) {
        this.bits = bitString.bits;
        this.bitsUsed = bitString.bitsUsed;
    }

    public BitString() {

    }

    public long getBits() {
        return bits;
    }

    public void setBits(long bits) {
        this.bits = bits;
    }

    public int getBitsUsed() {
        return bitsUsed;
    }

    public void setBitsUsed(int bitsUsed) {
        this.bitsUsed = bitsUsed;
    }

    /**
     * Adds a bit string to the end of the bit string
     * 
     * @param b bit string to be added
     * @return this, returns the same bit string
     */
    public BitString appendBitString(BitString b) {
        bits = (bits << b.getBitsUsed()) + b.getBits();
        bitsUsed += b.bitsUsed;
        return this;
    }

    /**
     * Adds a bit to the end of the bit string
     *
     * @param bit, to be added either 0b0 or 0b1
     */
    public BitString appendBit(long bit) {
        bits = (bits << 1L) + bit;
        bitsUsed++;
        return this;
    }

    /**
     * Pops the number of bits of the end of the bitstring
     *
     * @param number, the number of bits to be popped
     * @return the bitstring that was popped of
     */
    public BitString popBits(int number) {
        if (bitsUsed - number < 0)
            throw new IllegalArgumentException("number of bits used cannot be less than 0");
        // Masks away the bits
        long poppedBits = bits & ((1L << number) - 1L);
        bits = (bits >> number);
        bitsUsed -= number;
        return new BitString(poppedBits, number);
    }

    /**
     * Implemented as shift for arrays in JS It removes the first number of bits
     * from the bitstring and returns them
     *
     * @param number the numbers of bits to be removed
     * @return new BitString, containing the removed bits
     */
    public BitString shiftBits(int number) {
        if (bitsUsed - number < 0)
            throw new IllegalArgumentException("number of bits used cannot be less than 0");
        long shiftedBits = bits >> (bitsUsed - number) & ((1L << number) - 1);
        bits = bits & ((1L << bitsUsed - number) - 1L);
        bitsUsed -= number;
        return new BitString(shiftedBits, number);
    }

    /**
     * Gets the first number of bits from the bit string, but does not remove them
     * 
     * @param number
     * @return
     */
    public BitString getBitsFromFront(int number) {
        long shiftedBits = bits >> (bitsUsed - number);
        shiftedBits = shiftedBits & ((1L << number) - 1);
        return new BitString(shiftedBits, number);
    }
}
