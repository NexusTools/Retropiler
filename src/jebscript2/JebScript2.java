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
package jebscript2;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author aero
 */
public class JebScript2 {

//SAS OFF;
//
//var SPACE = R(0, 304, 0);
//
//function _stage(){
//	STAGE.
//}
//
//function steer(pitch, yaw, roll){
//	LOCK STEERING TO R(pitch, yaw, roll);
//}
//
//function steer_r(rot){
//	LOCK STEERING TO rot;
//}
//
//function throt(power){
//	LOCK THROTTLE TO power;
//}
//
//function rekt(times){
//	var f = 0;
//	while(f < times){
//		print("GET RuKT");
//		f++;
//	}
//}
//
//
//rekt(3);
//print("oh yeah");
//rekt(6);
//print("thats right");
//rekt(5);
//
//print("LELELE");
//steer_r(SPACE);
//throt(1);
//_stage();

    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.github.mibrahim.praxislaf.PraxisLookAndFeel");
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(JebScript2.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(JebScript2.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(JebScript2.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(JebScript2.class.getName()).log(Level.SEVERE, null, ex);
                }
                new UI().setVisible(true);
            }
        });
    }
    
}
