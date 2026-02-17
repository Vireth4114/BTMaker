package BTMaker.BTMaker;

public enum ResourceType {
    IMAGE(2),
    MIDI(3),
    STRINGS(4),
    LAYOUT(5),
    LEVEL(8);

    public final byte code;

    ResourceType(int code) {
        this.code = (byte) code;
    }

    public static ResourceType fromCode(byte code) {
        for (ResourceType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ResourceType code: " + code);
    }
}
