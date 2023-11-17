package me.ildarorama.modbuscollector.support;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.application.Platform;

public class InMemoryLogAppender extends AppenderBase<ILoggingEvent> {
    private final SettingsManager settingsManager;

    public InMemoryLogAppender() {
        settingsManager = SettingsManager.getInstance();
    }

    @Override
    protected void append(ILoggingEvent event) {
        Platform.runLater(() -> settingsManager.getLog().add(0, event));
    }

}