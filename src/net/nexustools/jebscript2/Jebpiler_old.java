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

import java.util.ArrayList;

/**
 * 
http://paste.ubuntu.com/15422421/
 * 
 * 
 *
 * @author aero
 */
public class Jebpiler_old {
    public static String compile(String source){
        String[] sl = source.split("\n");
        String result = "";
        for(String s : sl){
            char lc = '\n';
            String line = "";
            boolean inquote = false;
            for(char c : s.toCharArray()){
                if(lc == '=' && lc != c){
                    if(line.trim().startsWith("var")){
                        line = line.trim().substring(3);
                        line = "local " + line.trim() + " is ";
                        lc = ' ';
                    }else{
                        line = "set " + line.trim() + " to ";
                        lc = ' ';
                    }
                }
                switch(c){
                    case ' ':
                        if(inquote){
                            line +=c;
                        }else{
                            if(c != lc)
                                line += c;
                        }
                        break;
                    case '\t':
                    case '\r':
                        if(inquote)
                            line+= c;
                        break;
                    case ';':
                        line += '.';
                        break;
                    case '/':
                        if(!inquote && lc == c)
                            break;
                        else if(inquote)
                            line += c;
                        break;
                    case '|':
                        if(inquote)
                            line += '|';
                        else{
                            if(lc == c){
                                line += "or";
                            }
                        }
                        break;
                    case '&':
                        if(inquote)
                            line += '&';
                        else{
                            if(lc == c){
                                line += "and";
                            }
                        }
                        break;
                    case '=':
                        if(inquote)
                            line += '=';
                        else{
                            if(lc == c){
                                line += "=";
                            }
                        }
                        break;
                    
                    case '+':
                        if(inquote)
                            line += '+';
                        else{
                            if(c == lc){
                                line = line.trim();
                                line = line.substring(0, line.length()-1);
                                line = "set " + line + " to " + line + " + 1";
                            }else{
                                line += '+';
                            }
                        }
                        break;
                        
                    case '-':
                        if(inquote)
                            line += '-';
                        else{
                            if(c == lc){
                                line = line.trim();
                                line = line.substring(0, line.length()-1);
                                line = "set " + line + " to " + line + " - 1";
                            }else{
                                line += '-';
                            }
                        }
                        break;
                        
                    default: 
                        line += c;
                        break;
                        
//                    case ')':
//                    case '(':
//                        if(inquote)
//                            line+= c;
//                        else
//                            line+=' ';
//                        break;
                    case '}':
                        line += "}.";
                        break;
                    case '"':
                        inquote = !inquote;
                        line += c;
                        break;
                }
                
                lc = c;
            }
            if(line.trim().startsWith("function")){
//                System.out.println(line.trim());
                if(line.trim().charAt(line.trim().length()-1)=='{')
                    line = line.trim().substring(0, line.trim().length()-1);
                line = line.trim();
                String oline = line;
                line = line.substring(line.indexOf('(')+1);
                line = line.substring(0, line.lastIndexOf(')'));
//                System.out.println(line);
                String[] bits = line.split(" ");
                ArrayList<String> params = new ArrayList<String>();
                for(int i = 0; i < bits.length; i++){
                    String[] t = bits[i].split("\\,");
                    for(String q : t)
                        if(q.length()>0) params.add(q);
                }
                oline = oline.substring(8).trim();
                oline = oline.substring(0,oline.indexOf('('));
                result += "function " + oline + " {\r\n";
                for(String gs : params)
                    result += "parameter " + gs + ".\r\n";
            }else if(line.trim().startsWith("while")){
                line = line.trim();
                line = line.substring(5);
                line = "until not " + line.trim();
                result += line + "\r\n";
            }else if(line.trim().startsWith("each")){
                line = line.replace(":", "IN");
                result += "FOR " + line.trim() + "\r\n";
            }
            
            else if(line.trim().length()>0){
//                System.out.println("LIN:"+line.trim());
                result += line + "\r\n";
            }
        }
        return result;
    }
}
