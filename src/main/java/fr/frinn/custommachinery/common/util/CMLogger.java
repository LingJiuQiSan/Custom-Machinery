package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.api.utils.ICMLogger;
import fr.frinn.custommachinery.common.config.CMConfig;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CMLogger implements ICMLogger {

    public static final CMLogger INSTANCE = new CMLogger();

    private BufferedWriter writer;

    public CMLogger() {
        reset();
    }

    @Override
    public void info(String message, Object... args) {
        log("INFO", message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        log("WARN", message, args);
    }

    @Override
    public void error(String message, Object... args) {
        log("ERROR", message, args);
    }

    public void log(String type, String message, Object... args) {
        if(!enableLogging() || !shouldLog(type) || this.writer == null)
            return;

        message = String.format("[%s][%s][%s]: %s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")), EffectiveSide.get(), type, String.format(message, args));
        try {
            this.writer.append(message);
            this.writer.newLine();
            this.writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        try {
            if(this.writer != null)
                this.writer.close();
            File log = new File("logs/custommachinery.log");
            this.writer = Files.newBufferedWriter(log.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Can't create custommachinery.log file");
            e.printStackTrace();
        }
    }

    @Override
    public boolean enableLogging() {
        return CMConfig.INSTANCE.enableLogging.get();
    }

    @Override
    public boolean logMissingOptional() {
        return CMConfig.INSTANCE.logMissingOptional.get();
    }

    @Override
    public boolean logFirstEitherError() {
        return CMConfig.INSTANCE.logFirstEitherError.get();
    }

    @Override
    public boolean shouldLog(String type) {
        return CMConfig.INSTANCE.allowedLogs.get().contains(type);
    }
}
