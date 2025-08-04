package me.ildarorama.modbuscollector.support;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestSlave {

    public static void main(String[] args) throws ModbusException {
        ModbusSlave slave = ModbusSlaveFactory.createTCPSlave(5002, 1);
        SimpleProcessImage img = new SimpleProcessImage(1);
        List<SimpleInputRegister> regs = new ArrayList<SimpleInputRegister>(10);
        for (int i = 1; i <= 20; i++) {
            SimpleInputRegister reg = new SimpleInputRegister(i);
            img.addInputRegister(i + 511, reg);
            regs.add(reg);
        }
        UpdaterThread t = new UpdaterThread(regs);
        t.start();
        slave.addProcessImage(1, img);
        slave.open();
    }

    private static class UpdaterThread extends Thread {

        private ByteBuffer bb = ByteBuffer.allocate(4);
        private ShortBuffer sb = bb.asShortBuffer();

        private List<SimpleInputRegister> img;

        UpdaterThread(List<SimpleInputRegister> img) {
            super("Updater");
            setDaemon(true);
            this.img = img;
        }

        private int[] getFloat(float f) {
            long l = Float.floatToIntBits(f) & 0xFFFFFFFF;
            int a1 = (int) l & 0xFFFF;
            int a2 = (int) (l >> 16) & 0xFFFF;
            int[] res = { a1, a2 };
            return res;
        }

        @Override
        public void run() {
            Random rnd = new Random();
            while (!interrupted()) {
                for (int i = 0; i < img.size(); i += 2) {
                    int[] res = getFloat(rnd.nextInt(1000) / 10f);
                    img.get(i).setValue(res[0]);
                    img.get(i + 1).setValue(res[1]);
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
