/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infencoder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Rodot
 */
public class InfFrame extends javax.swing.JFrame {

    InfEncoder encoder = new InfEncoder();

    String gameName = null;
    BufferedImage gameIcon = null;
    DefaultListModel<BufferedImage> slides = new DefaultListModel();
    FileFilter imageFileFilter = new FileNameExtensionFilter(
            "Image file (jpg, jpeg, png, bmp, wbmp, gif)",
            "jpg", "jpeg", "png", "bmp", "wbmp", "gif");
    FileFilter infFileFilter = new FileNameExtensionFilter("INF File", "INF");
    File defaultDirectory = null;
    String settingsPath = System.getProperty("user.dir") + File.separator + "settings.ser";

    final int iconWidth = 19;
    final int iconHeight = 18;
    final int slideWidth = 84;
    final int slideHeight = 32;

    /**
     * Creates new form InfFrame
     */
    public InfFrame() {
        initComponents();
        this.setLocationRelativeTo(null); //center the frame on screen
        gameName = nameTextField.getText();
        slidesList.setModel(slides);
        slidesList.getSelectionModel().setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        slidesList.setTransferHandler(new ListItemTransferHandler());
        slidesList.setDropMode(DropMode.INSERT);
        slidesList.setDragEnabled(true);
        slidesList.setCellRenderer(new ListCellRenderer<BufferedImage>() {
            private final JPanel panel = new JPanel(new BorderLayout());
            private final JLabel icon = new JLabel((Icon) null);

            @Override
            public Component getListCellRendererComponent(
                    JList list, BufferedImage value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                BufferedImage bitmap = (BufferedImage) list.getModel().getElementAt(index);
                icon.setIcon(new ImageIcon(bitmap));
                panel.add(icon, BorderLayout.CENTER);
                panel.setBorder(new EmptyBorder(3, 3, 3, 3));
                panel.setBackground(isSelected ? list.getSelectionBackground()
                        : list.getBackground());
                return panel;
            }
        });

        //System.out.println(settingsPath);
        try {
            FileInputStream fileIn = new FileInputStream(settingsPath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            defaultDirectory = (File) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            //System.out.println("Can't read settings file");
        } catch (ClassNotFoundException c) {
            //System.out.println("Settings Class not found");
        }

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    FileOutputStream fileOut = new FileOutputStream(settingsPath);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(defaultDirectory);
                    out.close();
                    fileOut.close();
                } catch (IOException i) {
                    //System.out.println("Can't save settings to file");
                }
                System.exit(0);
            }
        });
    }

    void importIcon() {
        importGameIconLabel.setIcon(null);
        BufferedImage bitmap = null;
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(imageFileFilter);
        fileChooser.setCurrentDirectory(defaultDirectory);
        int returnVal = fileChooser.showOpenDialog(InfFrame.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            defaultDirectory = file;
            try {
                bitmap = ImageIO.read(file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(InfFrame.this, "Can't open the selected file",
                        "File error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (bitmap == null) {
                JOptionPane.showMessageDialog(InfFrame.this,
                        file.getName() + " is not a valid image.",
                        "File error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if ((bitmap.getWidth() != iconWidth)
                    || (bitmap.getHeight() != iconHeight)) {
                JOptionPane.showMessageDialog(InfFrame.this,
                        "Game icon must be " + Integer.toString(iconWidth) + " x "
                        + Integer.toString(iconHeight) + " pixels.\n"
                        + file.getName() + " is "
                        + Integer.toString(bitmap.getWidth()) + " x "
                        + Integer.toString(bitmap.getHeight()),
                        "Wrong size error", JOptionPane.ERROR_MESSAGE);
                gameIcon = null;
                return;
            } else {
                bitmap = threshold(bitmap, 255 / 2);
                gameIcon = bitmap;
                importGameIconLabel.setText("");
                ImageIcon gameIconPreview = new ImageIcon(gameIcon);
                importGameIconLabel.setIcon(gameIconPreview);
            }
        }
    }

    void importSlides() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(imageFileFilter);
        fileChooser.setCurrentDirectory(defaultDirectory);
        fileChooser.setMultiSelectionEnabled(true);
        int returnVal = fileChooser.showOpenDialog(InfFrame.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                openSlide(file);
            }
        }
    }

    void openSlide(File file
    ) {
        if(slides.size() > 250){
            return;
        }
        defaultDirectory = file;
        BufferedImage bitmap = null;
        try {
            bitmap = ImageIO.read(file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(InfFrame.this,
                    "Can't open the file " + file.getName(),
                    "File error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (bitmap == null) {
            JOptionPane.showMessageDialog(InfFrame.this,
                    file.getName() + " is not a valid image.",
                    "File error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((bitmap.getWidth() != slideWidth)
                || (bitmap.getHeight() != slideHeight)) {
            JOptionPane.showMessageDialog(InfFrame.this,
                    "Slides must be " + Integer.toString(slideWidth) + " x "
                    + Integer.toString(slideHeight) + " pixels.\n"
                    + file.getName() + " is "
                    + Integer.toString(bitmap.getWidth()) + " x "
                    + Integer.toString(bitmap.getHeight()),
                    "Wrong size error", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            bitmap = threshold(bitmap, 255 / 2);
            slides.addElement(bitmap);
        }
    }
    
    void removeAllSlides(){
        slides.clear();
    }

    void exportInf() {
        gameName = nameTextField.getText();
        //check that data is present

        if (gameName.isEmpty()) {
            JOptionPane.showMessageDialog(InfFrame.this, "Please specify a game name.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (gameIcon == null) {
            JOptionPane.showMessageDialog(InfFrame.this, "Please add a game icon",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (slides.isEmpty()) {
            JOptionPane.showMessageDialog(InfFrame.this, "Please add at least a slide",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        //encode data
        BufferedImage[] slidesArray = new BufferedImage[slides.size()];
        for (int i = 0; i < slides.size(); i++) {
            slidesArray[i] = slides.getElementAt(i);
        }
        List<Byte> output = encoder.generateOutput(
                gameName, gameIcon, slidesArray);
        //selecting file so save the data to
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(defaultDirectory);
        fileChooser.setFileFilter(infFileFilter);
        int returnVal = fileChooser.showOpenDialog(InfFrame.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getPath();
            String name = fileChooser.getSelectedFile().getName();
            //System.out.println(path);
            if (path.toLowerCase().indexOf(".inf", path.length() - 4) < 0) {
                path = path + ".INF";
            }
            File outFile = new File(path);
            defaultDirectory = outFile;
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                for (byte thisByte : output) {
                    out.write(thisByte);
                }
                JOptionPane.showMessageDialog(InfFrame.this,
                        outFile.getName() + " successfully saved.",
                        "File saved", JOptionPane.PLAIN_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(InfFrame.this,
                        "Can't save to the file " + outFile.getName(),
                        "File error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    BufferedImage threshold(BufferedImage bitmap, int threshold) {
        if (bitmap == null) {
            return null;
        }
        BufferedImage returnValue = new BufferedImage(bitmap.getWidth(), bitmap.getHeight(), bitmap.getType());
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int rgb = bitmap.getRGB(x, y);
                int red = (rgb >> 16) & 0x000000FF;
                int green = (rgb >> 8) & 0x000000FF;
                int blue = (rgb) & 0x000000FF;
                int value = (red + green + blue) / 3;
                if (value > threshold) {
                    returnValue.setRGB(x, y, 0xcedde7);
                } else {
                    returnValue.setRGB(x, y, 404040);
                }
            }
        }
        return returnValue;
    }

    void importInf() {

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        importIconButton = new javax.swing.JButton();
        nameTextField = new javax.swing.JTextField();
        addSlideButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        slidesList = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        importGameIconPanel = new javax.swing.JPanel();
        importGameIconLabel = new javax.swing.JLabel();
        removeAllSlidesButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Gamebuino .INF encoder");

        importIconButton.setText("Import game icon");
        importIconButton.setToolTipText("Used in the loader's game brower (must be 19*18px)");
        importIconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importIconButtonActionPerformed(evt);
            }
        });

        nameTextField.setText("Game Name");
        nameTextField.setToolTipText("Your game's name (18 characters max)");

        addSlideButton.setText("Add slides");
        addSlideButton.setToolTipText("Pictures shown when the game is selected. Can be used for description, story, help, etc. (must be 84*32px)");
        addSlideButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSlideButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        slidesList.setToolTipText("Drag and drop your slides to reorganize them");
        jScrollPane1.setViewportView(slidesList);

        jButton1.setText("Export .INF");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        importGameIconPanel.setBackground(new java.awt.Color(255, 255, 255));
        importGameIconPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        importGameIconLabel.setForeground(new java.awt.Color(153, 153, 153));
        importGameIconLabel.setText("<no game icon>");
        importGameIconLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        importGameIconPanel.add(importGameIconLabel);

        removeAllSlidesButton.setText("Remove all slides");
        removeAllSlidesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllSlidesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(importGameIconPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nameTextField)
                    .addComponent(importIconButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(addSlideButton, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(removeAllSlidesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(importIconButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(importGameIconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addSlideButton)
                    .addComponent(removeAllSlidesButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void importIconButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importIconButtonActionPerformed
        importIcon();
    }//GEN-LAST:event_importIconButtonActionPerformed

    private void addSlideButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSlideButtonActionPerformed
        importSlides();
    }//GEN-LAST:event_addSlideButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        exportInf();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void removeAllSlidesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllSlidesButtonActionPerformed
        removeAllSlides();
    }//GEN-LAST:event_removeAllSlidesButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            /*for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
             if ("Nimbus".equals(info.getName())) {
             javax.swing.UIManager.setLookAndFeel(info.getClassName());
             break;
             }
             }*/
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());

        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(InfFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InfFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InfFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InfFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InfFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSlideButton;
    private javax.swing.JLabel importGameIconLabel;
    private javax.swing.JPanel importGameIconPanel;
    private javax.swing.JButton importIconButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton removeAllSlidesButton;
    private javax.swing.JList slidesList;
    // End of variables declaration//GEN-END:variables
}
