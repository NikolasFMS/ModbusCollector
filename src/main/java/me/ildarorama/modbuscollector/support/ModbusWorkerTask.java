package me.ildarorama.modbuscollector.support;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.AbstractModbusMaster;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class ModbusWorkerTask extends Task<DeviceResponse> {
    private static final Logger log = LoggerFactory.getLogger(ModbusWorkerTask.class);
    private AbstractModbusMaster conn = null;
    private volatile boolean running = true;
    private volatile int period;
    private volatile int slave;
    private final SettingsManager settings;

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

                InputRegister[] registers = conn.readInputRegisters(slave, 1,10);
                if (registers == null || registers.length != 10) {
                    throw new IllegalStateException("Invalid response");
                }
                DeviceResponse resp = parseResponse(registers);
                updateValue(resp);

                Thread.sleep(period);
            } catch (InterruptedException e) {
                log.info("Завершение опроса");
            } catch (Exception e) {
                log.info("Ошибка при опросе устройства", e);
                closeConnection();
                updateMessage("Не соединен");
            }
        }
        return null;
    }

    private DeviceResponse parseResponse(InputRegister[] registers) {
        var resp = new DeviceResponse();
        resp.setTimestamp(LocalDateTime.now());
        resp.setA1(registers[0].getValue());
        resp.setA2(registers[1].getValue());
        resp.setA3(registers[2].getValue());
        resp.setA4(registers[3].getValue());
        resp.setA5(registers[4].getValue());
        resp.setA6(registers[5].getValue());
        resp.setA7(registers[6].getValue());
        resp.setA8(registers[7].getValue());
        resp.setA9(registers[8].getValue());
        resp.setA10(registers[9].getValue());

        return resp;
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
            Thread.sleep(10000);
        } catch (InterruptedException ignored) {}
    }

    private AbstractModbusMaster openConnection() throws Exception {
        var port = settings.getPort();
        if (port == null || port.isBlank()) {
            throw new ModbusException("Серийный порт не выбран");
        }

        AbstractModbusMaster master;
        SerialParameters params = new SerialParameters();
        params.setPortName(settings.getPort());
        params.setBaudRate(settings.getSpeed());
        params.setParity("N");
        params.setStopbits(1);
        params.setDatabits(8);
        //master = new ModbusSerialMaster(params, 1000);
        master = new ModbusTCPMaster("127.0.0.1", 5002);
        master.connect();
        return master;
    }
}
