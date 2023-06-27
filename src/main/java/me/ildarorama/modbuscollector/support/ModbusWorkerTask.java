package me.ildarorama.modbuscollector.support;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.AbstractModbusMaster;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.time.LocalDateTime;

public class ModbusWorkerTask extends Task<DeviceResponse> {
    private static final Logger log = LoggerFactory.getLogger(ModbusWorkerTask.class);
    private AbstractModbusMaster conn = null;
    private volatile boolean running = true;
    private volatile int period;
    private volatile int slave;
    private final SettingsManager settings;
    private ByteBuffer bb = ByteBuffer.allocate(4);
    private ShortBuffer sb = bb.asShortBuffer();

    public ModbusWorkerTask() {
        super();
        settings = SettingsManager.getInstance();
        refreshConfig();
    }

    public void stopWorker() {
        log.info("Stopping worker thread");
        running = false;
    }

    public void refreshConfig() {
        period = (int) (settings.getPeriod() * 1000D);
        slave = settings.getSlave();
    }

    @Override
    public DeviceResponse call() {
        log.info("Запуск опроса устройства");
        while (running) {
            try {
                if (conn == null) {
                    conn = openConnection();
                    updateMessage("Соединен");
                }

                InputRegister[] registers = conn.readInputRegisters(slave, 512,16);
                if (registers == null || registers.length != 16) {
                    throw new IllegalStateException("Invalid response");
                }
                DeviceResponse resp = parseResponse(registers);
                updateValue(resp);
                if (running) {
                    Thread.sleep(period);
                }
            } catch (InterruptedException e) {
                log.info("Завершение опроса");
            } catch (Exception e) {
                log.info("Ошибка при опросе устройства: " + e.getMessage(), e);
                closeConnection();
                updateMessage("Не соединен");
            }
        }
        return null;
    }

    private DeviceResponse parseResponse(InputRegister[] registers) {
        DeviceResponse resp = new DeviceResponse();
        resp.setTimestamp(LocalDateTime.now());
        resp.setA1(getFloat(registers,0));
        resp.setA2(getFloat(registers,2));
        resp.setA3(getFloat(registers,4));
        resp.setA4(getFloat(registers,6));
        resp.setA5(getFloat(registers,8));
        resp.setA6(getFloat(registers,10));
        resp.setA7(getFloat(registers,12));
        resp.setA8(getFloat(registers,14));
        return resp;
    }

    private float getFloat(InputRegister[] registers, int offset) {
        int i = registers[offset + 1].getValue();
        int j = registers[offset].getValue();
        sb.put(0, (short) i);
        sb.put(1, (short) j);
        float f = Float.intBitsToFloat(bb.getInt(0));
        return Math.round(f*100) / 100f;
    }

    private void closeConnection() {
        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception e) {
                log.error("Ошибка завершения связи", e);
            } finally {
                conn = null;
            }
        }
        try {
            log.info("Не возможно установить соединение. Ожидание 10 сек");
            if (running) {
                Thread.sleep(10000);
            }
        } catch (InterruptedException ignored) {}
    }

    private AbstractModbusMaster openConnection() throws Exception {
        String port = settings.getPort();
        if (port == null || port.isEmpty()) {
            throw new ModbusException("Серийный порт не выбран");
        }

        AbstractModbusMaster master;
        if ("y".equals(System.getProperty("test", "n"))) {
            master = new ModbusTCPMaster("127.0.0.1", 5002);
        } else {
            SerialParameters params = new SerialParameters();
            params.setPortName(settings.getPort());
            params.setBaudRate(settings.getSpeed().speed());
            params.setParity("N");
            params.setStopbits(1);
            params.setDatabits(8);
            master = new ModbusSerialMaster(params, 300);
        }
        master.connect();
        return master;
    }
}
