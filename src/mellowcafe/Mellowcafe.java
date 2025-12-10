package mellowcafe;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Mello
 */

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;

public class Mellowcafe {
   
    public static void main(String[] args) {

        
        try {
            
            FlatLightLaf.setup(); 
            
        } catch (Exception ex) {
         
            System.err.println("Failed to initialize FlatLaf look and feel: " + ex);
        }
      
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

