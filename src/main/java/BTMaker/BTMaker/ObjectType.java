package BTMaker.BTMaker;

import model.*;

public enum ObjectType {
    GEOMETRY(4, GeometryObject.class),
    EVENT(6, EventObject.class),
    BOUNCE(8, BounceObject.class),
    SPRITE(9, SpriteObject.class),
    WATER(10, WaterObject.class),
    CANNON(11, CannonObject.class),
    TRAMPOLINE(12, TrampolineObject.class),
    EGG(13, EggObject.class),
    ENEMY(15, EnemyObject.class);

    public final byte code;
    public final Class<? extends GameObject> cls;

    ObjectType(int code, Class<? extends GameObject> cls) {
        this.code = (byte) code;
        this.cls = cls;
    }

    public static ObjectType fromCode(byte code) {
        for (ObjectType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ObjectType code: " + code);
    }
}
