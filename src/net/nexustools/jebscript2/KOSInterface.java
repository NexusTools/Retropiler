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
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author aero
 */
public class KOSInterface {

    static OutputStream out;
    static InputStream in;

    static boolean ready = false;
    static boolean connecting = false;
    static boolean constat = false;

    static String cpuid = "1";

    static String ip = "localhost";
    static int port = 0;

    public static void connect() throws IOException {
        if (connecting) {
            return;
        }
//        bt = 0;
        connecting = true;
        Socket c = new Socket(ip, port);
        out = c.getOutputStream();
        in = c.getInputStream();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean sready = false;
                    int r = 0;
                    String line = "";
                    while ((r = in.read()) > -1) {
//                        System.err.println(r);
                        if (r == 255) {
                            out.write('\r');
                            out.write('\n');
                            out.flush();
                        }
                        btr++;
                        constat = !constat;
                        char c = (char) r;
                        if (c == '>' && !sready) {
                            out.write((cpuid + "\r\n").getBytes());
                        }
                        if (c == '}' && line.length() > 5 && line.substring(1).startsWith("Detaching from")) {
                            break;
                        }
                        if (c == '\n') {
                            if (line.startsWith("Proceed")) {
                                sready = true;
                            }
                            line = "";
                        } else {
                            line += c;
                        }
                        if (r == 23) {
                            ready = true;
                            //System.err.println("READY");
                        } else if (sready) {
//                            System.err.println(r);
                            ready = true;
                        }
//                        System.err.println(r);
                        //System.err.write(r);
                        //System.err.flush();
//                        try {
//                            Thread.sleep(1);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(KOSInterface.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(KOSInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
                connecting = ready = constat = false;
            }

        }).start();
    }
    public static long bts = 0, btr = 0;
    private static final Pattern EMPTY_LINE = Pattern.compile("^\\s*$");
    private static final Pattern COMMENT_LINE = Pattern.compile("^\\s*//$");

    public static void exec(String commands) throws IOException {
        System.out.println(commands);

        int t = 0;
        if (!ready) {
            System.out.println("Waiting until Ready");
            while (!ready) {
                try {
                    Thread.sleep(10);
                    t++;
                    if (t == 100) {
                        ready = true;
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(KOSInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
//        out.write(commands.getBytes());

        Scanner scanner = new Scanner(commands);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!EMPTY_LINE.matcher(line).matches()
                    && !COMMENT_LINE.matcher(line).matches())
                out.write((line + "\r\n").getBytes("utf8"));
        }
        scanner.close();

        //out.write(commands.getBytes("utf8"));
//        for(byte b : commands.getBytes("utf8")){
//            bts++;
//            constat = !constat;
//            out.write(b);
//            try {
//                Thread.sleep(3);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(KOSInterface.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        bts += 4;
        constat = true;
        out.write("\r\n\r\n".getBytes("utf8"));
        out.flush();
    }
}
