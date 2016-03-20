/*
 * Copyright (C) 2016 NexusTools
 *
 * Retropiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 or any later version.
 * 
 * Retropiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Retropiler.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package net.nexustools.jebscript2;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author kate
 */
public class Parser {
    
    public static String compile(String source) throws IOException{
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(result, true);
        parse(new BufferedReader(new StringReader(source)), p);
        return new String(result.toByteArray());
    }
    
    public static final Pattern RAW = Pattern.compile("^@Raw\\s+(.+)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern REPLACE = Pattern.compile("^@ReplaceLine\\s+\\{$", Pattern.CASE_INSENSITIVE);
    public static final Pattern REGEX_REPLACE = Pattern.compile("^@ReplaceRegex\\s+(.+)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern INCLUDE = Pattern.compile("^@Include\\s+\"(.+)\"$", Pattern.CASE_INSENSITIVE);
    public static final Pattern INCLUDERAW = Pattern.compile("^@Includeraw\\s+\"(.+)\"$", Pattern.CASE_INSENSITIVE);
    public static final Pattern REGEX_REPLACE_CONTENT = Pattern.compile("^/([^/]+)/(i?)\\s+(.+)$");
    public static final Pattern BRACKET = Pattern.compile("[\\{\\}\"'/]");
    
    public static final ScriptEngine SCRIPT_ENGINE;
    static {
        ScriptEngineManager manager = new ScriptEngineManager();
        SCRIPT_ENGINE = manager.getEngineByMimeType("text/javascript");
        Logger.getLogger(Parser.class.getName()).log(Level.INFO, "Using {0} to provide JavaScript integraiton.", SCRIPT_ENGINE.getClass().getName());
    }
    
    public static interface Replacer {
        public String replace(String input);
    }
    public static final class ReplaceRegex implements Replacer {
        public final String replacement;
        public final Pattern pattern;
        public ReplaceRegex(String pattern, String flags, String replacement) {
            // TODO: Support more flags than just i...
            this.pattern = Pattern.compile(pattern, flags.equals("i") ? Pattern.CASE_INSENSITIVE : 0);
            this.replacement = replacement;
        }
        @Override
        public String replace(String input) {
            Matcher matcher = pattern.matcher(input);
            if(matcher.matches()) {
                String output = replacement;
                for(int i=0; i<=matcher.groupCount(); i++)
                    output = output.replace("$" + i, matcher.group(i));
                return output;
            }
            return input;
        }
    }
    
    private static void parse(BufferedReader reader, PrintStream out) throws IOException {
        parse(reader, out, new ArrayList());
    }
    private static void parse(BufferedReader reader, PrintStream out, ArrayList<Replacer> replacers) throws IOException {
        String line;
        while((line = reader.readLine()) != null) {
            Matcher matcher = REPLACE.matcher(line);
            if(matcher.matches()) {
                int braces = 1;
                String quotes = null;
                StringBuilder source = new StringBuilder();
                source.append("function replace(input) {");
                while(braces > 0 && (line = reader.readLine()) != null) {
                    matcher = BRACKET.matcher(line);
                    while(matcher.find()) {
//                        System.out.println(braces);
                        switch(matcher.group()) {
                            case "{":
                                if(quotes == null)
                                    braces ++;
                                break;
                                
                            case "}":
                                if(quotes == null)
                                    braces --;
                                break;
                                
                            case "\"":
                                if(quotes != null && quotes.equals("\""))
                                    quotes = null;
                                else if(quotes == null)
                                    quotes = "\"";
                                break;
                                
                            case "'":
                                if(quotes != null && quotes.equals("'"))
                                    quotes = null;
                                else if(quotes == null)
                                    quotes = "'";
                                break;
                                
                            case "/":
                                if(quotes != null && quotes.equals("/"))
                                    quotes = null;
                                else if(quotes == null)
                                    quotes = "/";
                                break;
                            
                            default:
                                throw new RuntimeException("Internal Fuckup");
                        }
                        
//                        System.out.println(matcher.group());
//                        System.out.println(braces);
                    }
                    source.append('\n');
                    source.append(line);
                }
                
//                System.out.println(source);
//                System.exit(0);
                
                Compilable compilingEngine = (Compilable) SCRIPT_ENGINE;
                CompiledScript cscript;
                try {
                    cscript = compilingEngine.compile(source.toString());
                } catch (ScriptException ex) {
                    throw new RuntimeException(ex);
                }
                
                final Bindings bindings = SCRIPT_ENGINE.createBindings();
                try {
                    cscript.eval(bindings);
                } catch (ScriptException ex) {
                    throw new RuntimeException(ex);
                }
                SCRIPT_ENGINE.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                try {
                    ((Invocable)SCRIPT_ENGINE).invokeFunction("replace", "Test");
                } catch (NoSuchMethodException | ScriptException  ex) {
                    throw new RuntimeException(ex);
                }
                
                replacers.add(new Replacer() {
                    @Override
                    public String replace(String input) {
                        SCRIPT_ENGINE.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                        try {
                            return ((Invocable)SCRIPT_ENGINE).invokeFunction("replace", input).toString();
                        } catch (ScriptException | NoSuchMethodException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
                continue;
            }
            
            matcher = REGEX_REPLACE.matcher(line);
            if(matcher.matches()) {
                String content = matcher.group(1);
                matcher = REGEX_REPLACE_CONTENT.matcher(content);
                if(!matcher.matches())
                    throw new RuntimeException(content + " does not match format.");
                
                replacers.add(new ReplaceRegex(matcher.group(1), matcher.group(2), matcher.group(3)));
            } else {
                matcher = INCLUDE.matcher(line);
                if(matcher.matches()){
                    InputStream is;
                    String st = matcher.group(1);
                    if(st.startsWith("res"))
                        is = Parser.class.getResourceAsStream(st.substring(6));
                    else
                        is = new URL(st).openStream();
                    
                    parse(new BufferedReader(new InputStreamReader(is)), out, replacers);
                } else {
                    matcher = INCLUDERAW.matcher(line);
                    if(matcher.matches()){
                        InputStream is;
                        String st = matcher.group(1);
                        if(st.startsWith("res"))
                            is = Parser.class.getResourceAsStream(st.substring(6));
                        else
                            is = new URL(st).openStream();
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        int r = 0;
                        byte[] b = new byte[8192];
                        while((r=is.read(b))>-1)bout.write(b,0,r);
                        out.print(new String(bout.toByteArray()));
                        is.close();

                    }else{
                        matcher = RAW.matcher(line);
                        if(matcher.matches())
                            out.println(matcher.group(1));
                        else {
                            for(Replacer replacer : replacers)
                                line = replacer.replace(line);
                            out.println(line);
                        }
                    }
                }
            }
        }
    }
}
