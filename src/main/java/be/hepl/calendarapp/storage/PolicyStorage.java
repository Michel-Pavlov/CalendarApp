package be.hepl.calendarapp.storage;

import be.hepl.calendarapp.model.Policy;
import be.hepl.calendarapp.model.Rule;
import be.hepl.calendarapp.model.Metric;
import be.hepl.calendarapp.model.ComparatorOp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PolicyStorage {
    private final Path file;

    public PolicyStorage(Path file){
        this.file = file;
    }

    public Policy loadOrDefault(Policy fallback){
        try {
            if (!Files.exists(file)) return fallback;
            String json = Files.readString(file, StandardCharsets.UTF_8);
            return fromJson(json, fallback);
        } catch (Exception e){
            return fallback;
        }
    }

    public void save(Policy p){
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, toJson(p), StandardCharsets.UTF_8);
        } catch (Exception ignored) {}
    }

    // ---- mini JSON (sans dépendance) ----
    private static String esc(String s){ return s.replace("\\","\\\\").replace("\"","\\\""); }

    private static String toJson(Policy p){
        StringBuilder sb = new StringBuilder();
        sb.append("{\"name\":\"").append(esc(p.name())).append("\",");
        sb.append("\"defaultColor\":\"").append(esc(p.defaultColor())).append("\",");
        sb.append("\"rules\":[");
        boolean first = true;
        for (Rule r : p.rules()){
            if (!first) sb.append(",");
            first = false;
            sb.append("{")
                    .append("\"metric\":\"").append(r.metric.name()).append("\",")
                    .append("\"op\":\"").append(r.op.name()).append("\",")
                    .append("\"a\":").append(r.a).append(",")
                    .append("\"b\":").append(r.b).append(",")
                    .append("\"colorHex\":\"").append(esc(r.colorHex)).append("\",")
                    .append("\"priority\":").append(r.priority)
                    .append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static Policy fromJson(String json, Policy fallback){
        try {
            Map<String,Object> map = parse(json);
            String name = (String) map.getOrDefault("name", "Custom");
            String defColor = (String) map.getOrDefault("defaultColor", "#FFFFFF");
            Policy p = new Policy(name);
            p.setDefaultColor(defColor);
            Object rulesObj = map.get("rules");
            if (rulesObj instanceof List<?> list){
                for (Object o : list){
                    if (o instanceof Map<?,?> m){
                        Metric metric = Metric.valueOf((String)m.get("metric"));
                        ComparatorOp op = ComparatorOp.valueOf((String)m.get("op"));
                        double a = ((Number)m.get("a")).doubleValue();
                        double b = ((Number)m.get("b")).doubleValue();
                        String colorHex = (String)m.get("colorHex");
                        int prio = ((Number)m.get("priority")).intValue();
                        p.rules().add(new Rule(metric, op, a, b, colorHex, prio));
                    }
                }
            }
            return p;
        } catch (Exception e){
            return fallback;
        }
    }

    // ---- très simple parser JSON (suffisant pour notre format) ----
    @SuppressWarnings("unchecked")
    private static Map<String,Object> parse(String s) throws IOException {
        return (Map<String,Object>) new MiniJson().parse(s);
    }

    // MiniJson interne (objets / tableaux / nombres / strings / bool / null)
    private static class MiniJson {
        private String s; private int i;
        Object parse(String src) throws IOException { s=src; i=0; skip(); return readValue(); }

        private void skip(){ while (i<s.length() && Character.isWhitespace(s.charAt(i))) i++; }
        private char ch(){ return s.charAt(i); }

        private Object readValue() throws IOException {
            skip();
            if (i>=s.length()) throw new IOException("EOF");
            char c = ch();
            if (c=='{') return readObj();
            if (c=='[') return readArr();
            if (c=='"') return readStr();
            if (c=='t' || c=='f') return readBool();
            if (c=='n') { i+=4; return null; } // null
            return readNum();
        }
        private Map<String,Object> readObj() throws IOException {
            Map<String,Object> m = new LinkedHashMap<>();
            i++; skip();
            if (ch()=='}'){ i++; return m; }
            while (true){
                String key = readStr(); skip();
                if (ch()!=':') throw new IOException(": expected");
                i++; Object val = readValue(); m.put(key, val); skip();
                if (ch()=='}'){ i++; break; }
                if (ch()!=',') throw new IOException(", expected");
                i++; skip();
            }
            return m;
        }
        private List<Object> readArr() throws IOException {
            List<Object> a = new ArrayList<>(); i++; skip();
            if (ch()==']'){ i++; return a; }
            while (true){
                Object v = readValue(); a.add(v); skip();
                if (ch()==']'){ i++; break; }
                if (ch()!=',') throw new IOException(", expected");
                i++; skip();
            }
            return a;
        }
        private String readStr() throws IOException {
            if (ch()!='\"') throw new IOException("\" expected");
            i++; StringBuilder sb=new StringBuilder();
            while (i<s.length()){
                char c = s.charAt(i++);
                if (c=='\"') break;
                if (c=='\\'){
                    char e = s.charAt(i++);
                    switch (e){
                        case '\"': sb.append('\"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            int code = Integer.parseInt(s.substring(i,i+4),16);
                            i+=4; sb.append((char)code); break;
                        default: sb.append(e);
                    }
                } else sb.append(c);
            }
            return sb.toString();
        }
        private Boolean readBool() throws IOException {
            if (s.startsWith("true", i)){ i+=4; return Boolean.TRUE; }
            if (s.startsWith("false", i)){ i+=5; return Boolean.FALSE; }
            throw new IOException("bool expected");
        }
        private Number readNum(){
            int j=i; while (j<s.length()){
                char c=s.charAt(j);
                if ("-+.eE0123456789".indexOf(c)>=0) j++; else break;
            }
            String t=s.substring(i,j); i=j;
            if (t.contains(".")||t.contains("e")||t.contains("E")) return Double.parseDouble(t);
            try { return Integer.parseInt(t); } catch(Exception e){ return Long.parseLong(t); }
        }
    }
}
