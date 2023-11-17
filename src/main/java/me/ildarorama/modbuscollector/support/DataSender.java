package me.ildarorama.modbuscollector.support;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DataSender extends Thread {
    private static final Logger log = LoggerFactory.getLogger(DataSender.class);
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private final SettingsManager manager = SettingsManager.getInstance();
    private final ArrayBlockingQueue<DeviceResponse> queue = new ArrayBlockingQueue<>(100);

    public DataSender() {
        super("Sender thread");
        setDaemon(true);
    }

    public void send(DeviceResponse response) {
        if (!queue.offer(response)) {
            log.info("Не возможно отправить метрики. Очередь заполненна");
        }
    }

    public void run() {
        log.info("Служба отправки метрик запущена....");
        while (!Thread.interrupted()) {
            DeviceResponse response;
            try {
                response = queue.poll(100, TimeUnit.MILLISECONDS);
                if (response != null) {
                    process(response);
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void process(DeviceResponse response) {
        String dataSourceUrl = manager.getDataSourceUrl();
        if (dataSourceUrl != null && !dataSourceUrl.isEmpty()) {
            HttpResponse resp = null;
            try {
                URI uri = new URIBuilder(new URI(dataSourceUrl))
                        .addParameter("a1", Float.toString(response.getA1()))
                        .addParameter("a2", Float.toString(response.getA2()))
                        .addParameter("a3", Float.toString(response.getA3()))
                        .addParameter("a4", Float.toString(response.getA4()))
                        .addParameter("a5", Float.toString(response.getA5()))
                        .addParameter("a6", Float.toString(response.getA6()))
                        .addParameter("a7", Float.toString(response.getA7()))
                        .addParameter("a8", Float.toString(response.getA8()))
                        .addParameter("timestamp", response.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME))
                        .build();
                resp = httpclient.execute(new HttpGet(uri));
                if (resp.getStatusLine() == null && resp.getStatusLine().getStatusCode() != 200) {
                    log.error("Failure response {}", uri.toString());
                }
            } catch (Exception e) {
                log.error("Can not send data", e);
            } finally {
                EntityUtils.consumeQuietly(resp.getEntity());
            }
        }
    }
}
