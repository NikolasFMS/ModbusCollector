package me.ildarorama.modbuscollector.support;

public enum PortSpeedEnum {
    SPEED_115200(115200), SPEED_57600(57600), SPEED_34800(34800), SPEED_19200(19200), SPEED_9600(9600);

    private final int speed;

    PortSpeedEnum(int speed) {
        this.speed = speed;
    }

    public int speed() {
        return speed;
    }
}
