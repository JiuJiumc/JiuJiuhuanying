package net.insprill.cjm.welcome;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/** Single IO-worker owned. Force intent to disk before paying; uncertain intents never retry. */
public final class WelcomeLedger {
    private final Path file;
    private final Set<UUID> newcomers = new HashSet<>();
    private final Set<String> claims = new HashSet<>();
    private final Map<String,Integer> daily = new HashMap<>();
    public WelcomeLedger(Path file) throws IOException {
        this.file=file;
        Files.createDirectories(file.getParent());
        if(Files.exists(file)) {
            for(String line:Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if(line.isBlank()) continue;
                String[] fields=line.split("\\|",-1);
                try {
                    if(fields.length==2 && fields[0].equals("N")) newcomers.add(UUID.fromString(fields[1]));
                    else if(fields.length==5 && fields[0].equals("P")) {
                        UUID target=UUID.fromString(fields[1]), who=UUID.fromString(fields[2]);
                        LocalDate.parse(fields[3]);
                        double amount=Double.parseDouble(fields[4]);
                        if(!Double.isFinite(amount)||amount<=0) throw new IllegalArgumentException();
                        if(!claims.add(target+"|"+who)) throw new IllegalArgumentException("duplicate intent");
                        daily.merge(who+"|"+fields[3],1,Integer::sum);
                    } else if(fields.length==4 && fields[0].equals("R") && Set.of("PAID","REVIEW").contains(fields[3])) {
                        if(!claims.contains(UUID.fromString(fields[1])+"|"+UUID.fromString(fields[2]))) throw new IllegalArgumentException();
                    } else throw new IllegalArgumentException("invalid journal row");
                } catch(RuntimeException error) { throw new IOException("Welcome journal is invalid; rewards stopped",error); }
            }
        }
    }
    public boolean open(UUID target) throws IOException {
        if(newcomers.contains(target)) return false;
        append("N|"+target);
        newcomers.add(target);
        return true;
    }
    public boolean reserve(UUID target, UUID who, LocalDate day, int limit, double amount) throws IOException {
        if(target.equals(who)||!newcomers.contains(target)||!Double.isFinite(amount)||amount<=0) return false;
        String key=target+"|"+who, dailyKey=who+"|"+day;
        if(claims.contains(key)||daily.getOrDefault(dailyKey,0)>=limit) return false;
        append("P|"+key+"|"+day+"|"+amount);
        claims.add(key); daily.merge(dailyKey,1,Integer::sum);
        return true;
    }
    public void finish(UUID target, UUID who, boolean success) throws IOException {
        append("R|"+target+"|"+who+"|"+(success?"PAID":"REVIEW"));
    }
    private void append(String line) throws IOException {
        byte[] bytes=(line+"\n").getBytes(StandardCharsets.UTF_8);
        try(FileChannel channel=FileChannel.open(file,StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.APPEND)) {
            ByteBuffer buffer=ByteBuffer.wrap(bytes);
            while(buffer.hasRemaining()) channel.write(buffer);
            channel.force(true);
        }
    }
}
