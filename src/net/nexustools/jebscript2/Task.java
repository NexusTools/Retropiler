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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 *
 * @author kate
 */
public abstract class Task {
    private static final Executor threadExecutor = Executors.newCachedThreadPool();
    
    private final JDialog dialog;
    private final JLabel text;
    public Task() {
        dialog = null;
        text = null;
    }
    public Task(Window window, String action) {
        dialog = new JDialog(window, "Working");
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setLayout(new BorderLayout(10, 10));
                
        JProgressBar dpb = new JProgressBar(0, 500);
        panel.add(BorderLayout.CENTER, dpb);
        panel.add(BorderLayout.NORTH, text = new JLabel(action));
        dpb.setIndeterminate(true);
        
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setContentPane(panel);
        dialog.setMinimumSize(new Dimension(300, 75));
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(window);
    }

    public void updateText(final String txt) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                text.setText(txt);
            }
        });
    }
    
    public abstract void runInThread() throws Throwable;
    public abstract void runInUI(Throwable error);
    
    public final static void executeThread(Runnable run) {
        threadExecutor.execute(run);
    }
    public final static void executeUI(Runnable run) {
        SwingUtilities.invokeLater(run);
    }
    
    public final void completeProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dialog.setVisible(false);
            }
        });
    }
    
    public final void run() {
        if(dialog != null)
            dialog.setVisible(true);
        threadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runInThread();
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            if(dialog != null)
                                dialog.setVisible(false);
                            runInUI(null);
                        }
                    });
                } catch(final Throwable t) {
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            if(dialog != null)
                                dialog.setVisible(false);
                            runInUI(t);
                        }
                    });
                }
            }
        });
    }
    
}
