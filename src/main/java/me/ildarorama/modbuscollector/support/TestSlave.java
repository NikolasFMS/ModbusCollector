package me.ildarorama.modbuscollector.support;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestSlave {
    public static void main(String[] args) throws ModbusException {
        var slave = ModbusSlaveFactory.createTCPSlave(5002, 1);
        SimpleProcessImage img = new SimpleProcessImage(1);
        var regs = new ArrayList<SimpleInputRegister>(10);
        for(int i=1; i<11; i++) {
            SimpleInputRegister reg = new SimpleInputRegister(i);
            img.addInputRegister(i, reg);
            regs.add(reg);
        }
        UpdaterThread t = new UpdaterThread(regs);
        t.start();
        slave.addProcessImage(1,img);
        slave.open();
    }

    private static class UpdaterThread extends Thread {
        private List<SimpleInputRegister> img;
        UpdaterThread(List<SimpleInputRegister> img) {
            super("Updater");
            setDaemon(true);
            this.img = img;
        }

        @Override
        public void run() {
            var rnd = new Random();
            while(!interrupted()) {
                for (var reg : img) {
                    reg.setValue(rnd.nextInt());
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
