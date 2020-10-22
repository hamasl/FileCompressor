public class HuffmanTreeNode extends FrequencyTreeNode {
    private byte content;

    public HuffmanTreeNode(byte content, int frequency) {
        super(frequency);
        this.content = content;
    }

    public byte getContent() {
        return content;
    }

    public void setContent(byte content) {
        this.content = content;
    }
}
