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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
 * @author kate
 */
public class TelnetViewer extends JComponent {
    public static final Font CHAR_FONT;
    public static final Dimension CHAR_DIMENSION;
    static {
        CHAR_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        Component comp = new Component() {};
        FontMetrics metrics = comp.getFontMetrics(CHAR_FONT);
        CHAR_DIMENSION = new Dimension(metrics.charWidth('A'), metrics.getHeight());
    }
    
    private final kOSClient.ScreenUpdateListener listener = new kOSClient.ScreenUpdateListener() {
        @Override
        public void onScreenUpdate() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    repaint();
                }
            });
        }
    };
    
    private final kOSClient client;
    private boolean disconnected = false;
    public TelnetViewer(kOSClient client) {
        this.client = client;
        client.addScreenUpdateListener(listener);
        Dimension dim = new Dimension(CHAR_DIMENSION.width * 80, CHAR_DIMENSION.height * 24);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setMaximumSize(dim);
        
        client.addDisconnectListener(new kOSClient.DisconnectListener() {
            @Override
            public void onDisconnect() {
                disconnected = true;
                SwingUtilities.invokeLater(() -> {
                    repaint();
                });
            }
        });
    }

    @Override
    public void paint(final Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(CHAR_FONT);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if(disconnected)
            g.drawString("Disconnected.", 0, CHAR_DIMENSION.height);
        else
            client.operateOnScreen(new kOSClient.ScreenOperation() {
                @Override
                public void operate(char[][] screen) {
                    for(int x = 0; x < 80; x ++) {
                        for(int y = 0; y < 24; y ++) {
                            g.drawString(String.valueOf(screen[y][x]), x*CHAR_DIMENSION.width, y*CHAR_DIMENSION.height+CHAR_DIMENSION.height);
                        }
                    }
                }
            });
    }
    
    
    
}
