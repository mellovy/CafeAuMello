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
        // Set look and feel
        
        try {
            // Set the desired theme here. 
            // This example uses the popular dark theme:
            FlatLightLaf.setup(); // This will apply the theme
            
        } catch (Exception ex) {
            // Print error if the L&F setup fails (e.g., if the JAR is missing)
            System.err.println("Failed to initialize FlatLaf look and feel: " + ex);
        }
      
        // REMOVE THIS SECTION:
        // try {
        //     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        
        // Launch login frame
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

