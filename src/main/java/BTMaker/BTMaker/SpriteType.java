package BTMaker.BTMaker;

public enum SpriteType {
    STATIC(0),
    COMPOUND(1),
    ANIMATED(2);

    public final byte code;

    SpriteType(int code) {
        this.code = (byte) code;
    }

    public static SpriteType fromCode(byte code) {
        for (SpriteType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SpriteType code: " + code);
    }
}
