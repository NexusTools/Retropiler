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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;

/**
 *
 * @author kate
 */
public class kOSClient {
    private static final Logger LOG = Logger.getLogger(kOSClient.class.getName());
    
    public static interface ScreenUpdateListener {
        public void onScreenUpdate();
    }
    public static interface FailureToSwitchCPUListener {
        public void onFailureToSwitchCPU(int cpu);
    }
    public static interface CPUChangedListener {
        public void onCPUChange(int cpu);
    }
    public static interface DisconnectListener {
        public void onDisconnect();
    }
    public static interface ScreenOperation {
        public void operate(char[][] screen);
    }
    
    protected static boolean isValid(char c) {
        if(c == '[' || c == ']')
            return false;
        if(c == '\n' || c == '\r' || c == ' ')
            return true;
        return c >= '!' && c <= '~';
    }
    
    private char[][] screen;
    private boolean tryAgain;
    private int desiredCPU;
    private int currentCPU = -1;
    private final TelnetClient telnet;
    private final ArrayList<DisconnectListener> disconnectListeners = new ArrayList();
    private final ArrayList<FailureToSwitchCPUListener> failedToSwitchListeners = new ArrayList();
    private final ArrayList<ScreenUpdateListener> screenUpdateListeners = new ArrayList();
    private final ArrayList<CPUChangedListener> cpuChangedListeners = new ArrayList();
    public kOSClient() {
        telnet = new TelnetClient();
        
        TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", true, true, true, true);
        EchoOptionHandler echoopt = new EchoOptionHandler(true, true, true, true);
        SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);
        WindowSizeOptionHandler sizeopt = new WindowSizeOptionHandler(80, 24, true, true, true, false);

        try
        {
            telnet.addOptionHandler(ttopt);
            telnet.addOptionHandler(echoopt);
            telnet.addOptionHandler(gaopt);
            telnet.addOptionHandler(sizeopt);
        }
        catch (InvalidTelnetOptionException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void addScreenUpdateListener(ScreenUpdateListener listener) {
        synchronized(screenUpdateListeners) {
            if(!screenUpdateListeners.contains(listener))
                screenUpdateListeners.add(listener);
        }
    }
    
    public void removeScreenUpdateListener(ScreenUpdateListener listener) {
        synchronized(screenUpdateListeners) {
            screenUpdateListeners.remove(listener);
        }
    }
    
    public void addCPUChangedListener(CPUChangedListener listener) {
        synchronized(cpuChangedListeners) {
            if(!cpuChangedListeners.contains(listener))
                cpuChangedListeners.add(listener);
        }
    }
    
    public void removeCPUChangedListener(CPUChangedListener listener) {
        synchronized(cpuChangedListeners) {
            cpuChangedListeners.remove(listener);
        }
    }
    
    public void addDisconnectListener(DisconnectListener listener) {
        synchronized(disconnectListeners) {
            if(!disconnectListeners.contains(listener))
                disconnectListeners.add(listener);
        }
    }
    
    public void removeDisconnectListener(DisconnectListener listener) {
        synchronized(disconnectListeners) {
            disconnectListeners.remove(listener);
        }
    }
    
    public void addFailureToSwitchCPUListener(FailureToSwitchCPUListener listener) {
        synchronized(failedToSwitchListeners) {
            if(!failedToSwitchListeners.contains(listener))
                failedToSwitchListeners.add(listener);
        }
    }
    
    public void removeFailureToSwitchCPUListener(FailureToSwitchCPUListener listener) {
        synchronized(failedToSwitchListeners) {
            failedToSwitchListeners.remove(listener);
        }
    }
    
    public void operateOnScreen(ScreenOperation operation) {
        synchronized(this) {
            operation.operate(screen);
        }
    }
    
    private int curX, curY, scanY;
    private void print(char c) {
        if(c == '\r')
            return;
        
        synchronized(this) {
            if(c == '\n')
                newLine();
            else {
                screen[curY][curX] = c;
                curX ++;
                if(curX >= 80)
                    newLine();
            }
        }
    }
    
    private void newLine() {
        curX = 0;
        curY ++;
        if(curY >= 24) {
            curY --;
            scrollUp();
        }
    }
    
    private void scrollUp() {
        for(int y=0; y<23; y++) {
            screen[y] = screen[y+1];
        }
        screen[23] = new char[80];
        scanY--;
        if(scanY < 0)
            scanY = 0;
    }
    
    private void resetScreen() {
        curX = curY = 0;
        synchronized(this) {
            scanY = 0;
            screen = new char[24][];
            for(int y=0; y<24; y++) {
                char line[] = screen[y] = new char[80];
                for(int x = 0; x < 80; x ++) {
                    line[x] = ' ';
                }
            }
        }
    }
    
    public void connect(final String host, final int port) throws IOException {
        try {
            LOG.info("Connecting to Telnet...");
            telnet.connect(host, port);
            InputStream in = telnet.getInputStream();

            int read;
            resetScreen();
            int commandPos = 0;
            byte[] command = new byte[3];
            byte[] buffer = new byte[1024];
            while((read = in.read(buffer)) > 0) {
                boolean screenCleared = false;
                boolean containsInvalid = false;
                for(int i=0; i<read; i++) {
                    char c = (char)(buffer[i]&0xFF);
                    if(!isValid(c)) {
                        containsInvalid = true;
                        break;
                    }
                }
                if(containsInvalid) {
                    StringBuilder b = new StringBuilder();
                    for(int i=0; i<read; i++) {
                        byte by = buffer[i];
                        int t = by&0xFF;
                        char c = (char)t;
                        if(t == 1)
                            continue;

                        if(isValid(c)) {
                            command[0] = 0;
                            b.append(c);
                            print(c);
                        } else {
                            if(command[0] != 0) {
                                command[++commandPos] = by;
                                if(commandPos >= 2) {
                                    try {
                                        switch((command[0]&0xFF)) {
                                            case 238:
                                                if((command[1]&0xFF) == 128){
                                                    if((command[2]&0xFF) == 130) {
                                                        LOG.info(" -- SCREEN RESET -- ");
                                                        screenCleared = true;
                                                        resetScreen();
                                                        continue;
                                                    } else if((command[2]&0xFF) == 134) {
                                                        command[0] = command[2];
                                                        commandPos = 0;
                                                        continue;
                                                    }
                                                }
                                                
                                            case 134:
                                                synchronized(kOSClient.this) {
                                                    curX = Math.min(79, command[2]&0xFF);
                                                    curY = Math.min(23, command[1]&0xFF);
                                                }
                                        }
                                        LOG.log(Level.INFO, "Unknown Command: {0}, {1}, {2}", new Object[]{command[0]&0xFF, command[1]&0xFF, command[2]&0xFF});
                                    } finally {
                                        commandPos = 0;
                                        command[0] = 0;
                                    }
                                }
                            } else {
                                if(t == 238 || t == 134) {
                                    command[0] = by;
                                    commandPos = 0;
                                    continue;
                                }

                                if(t == 128)
                                    continue;

                                b.append('[');
                                b.append(t);
                                b.append(']');
                                print(c);
                            }
                        }
                    }
                    if(b.length() > 0) {
                        System.out.print(b.toString());
                        System.out.flush();
                    }
                    synchronized(screenUpdateListeners) {
                        for(ScreenUpdateListener listener : screenUpdateListeners)
                            listener.onScreenUpdate();
                    }
                } else {
                    command[0] = 0;
                    System.out.write(buffer, 0, read);
                    System.out.flush();
                    
                    for(int i=0; i<read; i++)
                        print((char)(buffer[i]&0xFF));
                    
                    synchronized(screenUpdateListeners) {
                        for(ScreenUpdateListener listener : screenUpdateListeners)
                            listener.onScreenUpdate();
                    }
                }
                if(screenCleared) {
                    int switched = -1;
                    boolean inMainMenu = false;
                    synchronized(kOSClient.this) {
                        scanY = 0;
                        while(scanY <= curY) {
                            String line = new String(screen[scanY++]);
                            LOG.log(Level.INFO, "Scanning: {0}: {1}", new Object[]{scanY, line});
                            if(TERMINAL_TYPE.matcher(line).matches()) {
                                inMainMenu = true;
                                break;
                            }
                        }
                    }
                    
                    if(inMainMenu) {
                        int desired = 0;
                        synchronized(kOSClient.this) {
                            if(currentCPU != 0) {
                                currentCPU = desiredCPU = 0;
                                switched = 0;
                            } else if(desiredCPU > 0) {
                                desired = desiredCPU;
                                desiredCPU = 0;
                            }
                                
                        }
                        if(desired > 0)
                            synchronized(failedToSwitchListeners) {
                                for(FailureToSwitchCPUListener listener : failedToSwitchListeners)
                                    listener.onFailureToSwitchCPU(desired);
                            }
                    
                    } else {
                        synchronized(kOSClient.this) {
                            if(desiredCPU > 0) {
                                switched = currentCPU = desiredCPU;
                                desiredCPU = 0;
                            }
                            if(currentCPU < 0)
                                throw new RuntimeException("Expected Main Menu, kOS Version may be Incompatible.");
                        }
                    }
                    
                    if(switched > -1)
                        synchronized(cpuChangedListeners) {
                            for(CPUChangedListener listener : cpuChangedListeners)
                                listener.onCPUChange(switched);
                        }
                }
            }
            LOG.info("Disconnected from Telnet");
        } finally {
            synchronized(disconnectListeners) {
                for(DisconnectListener listener : disconnectListeners) {
                    listener.onDisconnect();
                }
            }
            try {
                telnet.disconnect();
            } catch(Throwable ex) {}
        }
    }
    
    public void selectCPU(int cpu) {
        synchronized(this) {
            if(desiredCPU > 0)
                throw new RuntimeException("In the middle of switching CPUs");
            if(currentCPU > 0)
                throw new RuntimeException("Selecting a CPU from inside a CPU is not yet implemented.");
            desiredCPU = cpu;
            
            PrintWriter writer = new PrintWriter(telnet.getOutputStream());
            writer.print(cpu + "\r\n");
            writer.flush();
        }
    }
    
    public void deselectCPU() {
        synchronized(this) {
            if(desiredCPU > 0)
                throw new RuntimeException("In the middle of switching CPUs");
            if(currentCPU < 1)
                throw new RuntimeException("Not connected to a CPU");
            
            
            PrintWriter writer = new PrintWriter(telnet.getOutputStream());
            writer.print((char)4);
            writer.print("\r\n");
            writer.flush();
        }
    }
    
    private static final Pattern EMPTY_LINE = Pattern.compile("^\\s*$");
    private static final Pattern COMMENT_LINE = Pattern.compile("^\\s*//$");
    private static final Pattern TERMINAL_TYPE = Pattern.compile("^Terminal: type = (INITIAL_UNSET|VT100), size = \\d+x\\d+\\s*$");
    public void execute(String commands) {
        synchronized(this) {
            if(desiredCPU > 0)
                throw new RuntimeException("In the middle of switching CPUs");
            if(currentCPU < 1)
                throw new RuntimeException("Not connected to a CPU");
            
            PrintWriter writer = new PrintWriter(telnet.getOutputStream());
            Scanner scanner = new Scanner(commands);
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(EMPTY_LINE.matcher(line).matches()
                        || COMMENT_LINE.matcher(line).matches())
                    continue;
                
                writer.print(line);
                writer.print("\r\n");
                writer.flush();
            }
            writer.print("\r\n\r\n");
            writer.flush();
        }
    }
    
    public void disconnect() {
        synchronized(this) {
            try {
                telnet.disconnect();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
