package poepart1;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Strict parser for a JSON array of objects containing string properties. */
final class JsonMessageParser {
    private final String source;
    private int position;
    private JsonMessageParser(String source) { this.source=source; }
    static List<Map<String,String>> parse(String json) {
        JsonMessageParser parser=new JsonMessageParser(json);
        List<Map<String,String>> result=parser.array();
        parser.space();
        if(parser.position!=json.length()) throw new IllegalArgumentException("Trailing JSON content");
        return result;
    }
    private void space() {
        while(position<source.length() && Character.isWhitespace(source.charAt(position))) position++;
    }
    private boolean take(char c) {
        space();
        if(position<source.length() && source.charAt(position)==c) {position++;return true;}
        return false;
    }
    private void expect(char c) {
        if(!take(c)) throw new IllegalArgumentException("Expected '"+c+"' at position "+position);
    }
    private List<Map<String,String>> array() {
        expect('[');
        List<Map<String,String>> result=new ArrayList<>();
        if(take(']')) return result;
        do {result.add(object());} while(take(','));
        expect(']');
        return result;
    }
    private Map<String,String> object() {
        expect('{');
        Map<String,String> result=new LinkedHashMap<>();
        if(take('}')) return result;
        do {
            String key=string(); expect(':');
            result.put(key,string());
        } while(take(','));
        expect('}');
        return result;
    }
    private String string() {
        expect('"');
        StringBuilder out=new StringBuilder();
        while(position<source.length()) {
            char c=source.charAt(position++);
            if(c=='"') return out.toString();
            if(c=='\\') {
                if(position>=source.length()) throw new IllegalArgumentException("Incomplete escape");
                char e=source.charAt(position++);
                switch(e) {
                    case '"':out.append('"');break;
                    case '\\':out.append('\\');break;
                    case '/':out.append('/');break;
                    case 'b':out.append('\b');break;
                    case 'f':out.append('\f');break;
                    case 'n':out.append('\n');break;
                    case 'r':out.append('\r');break;
                    case 't':out.append('\t');break;
                    case 'u':
                        if(position+4>source.length()) throw new IllegalArgumentException("Invalid unicode escape");
                        try {out.append((char)Integer.parseInt(source.substring(position,position+4),16));}
                        catch(NumberFormatException ex) {throw new IllegalArgumentException("Invalid unicode escape",ex);}
                        position+=4;break;
                    default:throw new IllegalArgumentException("Invalid JSON escape: "+e);
                }
            } else {
                if(c<32) throw new IllegalArgumentException("Unescaped control character");
                out.append(c);
            }
        }
        throw new IllegalArgumentException("Unterminated JSON string");
    }
}
