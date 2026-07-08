/*
    Copyright (C) 2026 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.dickimawbooks.jmakepdfx;

import java.io.File;

import java.awt.Component;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;

public class PropertiesDialog extends JDialog
{     
   public PropertiesDialog(Jmakepdfx application)
   {
      super(application.getFrame(),
             application.getMessageWithFallback("properties.title", "Properties"), true);

      this.application = application;

      JpdfxProperties properties = application.getProperties();
      TeXJavaHelpLib helpLib = application.getHelpLib();

      String defaultDir = properties.getDefaultDirectory();

      if (defaultDir == null)
      {
         defaultDir = "";
      }

      String setting = properties.getDefaultDirectorySetting();
      
      if (setting == null)
      {
         setting = "cwd";
      }

      Box mainPanel = Box.createVerticalBox();
      getContentPane().add(new JScrollPane(mainPanel), "Center");

      mainPanel.setBorder(BorderFactory.createLoweredBevelBorder());
      
      mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      Box startDirBox = Box.createVerticalBox();
      startDirBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      startDirBox.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createEtchedBorder(),
         application.getMessageWithFallback("properties.startup", "Startup Directory")));
      mainPanel.add(startDirBox);

      ButtonGroup bg = new ButtonGroup();

      customField = new FileField(application, this, defaultDir,
        fileChooser, JFileChooser.DIRECTORIES_ONLY);

      ChangeListener startupChangeListener = new ChangeListener()
       {
          @Override
          public void stateChanged(ChangeEvent evt)
          {
             customField.setEnabled(customButton != null && customButton.isSelected());
          }
       };

      homeButton = helpLib.createJRadioButton("properties.startup", "home", 
       setting.equals("home"), bg);
      homeButton.addChangeListener(startupChangeListener);
      homeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      startDirBox.add(homeButton);

      cwdButton = helpLib.createJRadioButton("properties.startup", "cwd", 
       setting.equals("cwd"), bg);
      cwdButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      cwdButton.addChangeListener(startupChangeListener);
      startDirBox.add(cwdButton);

      lastButton = helpLib.createJRadioButton("properties.startup", "last", 
       setting.equals("last"), bg);
      lastButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      lastButton.addChangeListener(startupChangeListener);
      startDirBox.add(lastButton);

      JComponent row = createRow();
      startDirBox.add(row);

      customButton = helpLib.createJRadioButton("properties.startup", "custom", 
       setting.equals("custom"), bg);
      customButton.addChangeListener(startupChangeListener);
      customButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      row.add(customButton);

      fileChooser = new JFileChooser();

      row.add(customField);

      application.clampCompMaxHeight(row, 0, 0);

      timeoutSpinnerModel = new SpinnerNumberModel(
         Long.valueOf(properties.getMaxProcessTime()),
         Long.valueOf(1L), null, Long.valueOf(1000L));

      timeoutSpinner = new JSpinner(timeoutSpinnerModel);

      pack();
      setLocationRelativeTo(application.getFrame());
   }

   protected JComponent createRow()
   {
      JComponent row = new JPanel(new FlowLayout(FlowLayout.LEADING));

      row.setAlignmentX(Component.LEFT_ALIGNMENT);

      return row;
   }

   public void display()
   {
// TODO
      setVisible(true);
   }

   Jmakepdfx application;
      
   private JFileChooser fileChooser;
      
   private JRadioButton homeButton, cwdButton, lastButton, customButton;

   private FileField customField, gsField, pdfField, iccField;

   private SpinnerNumberModel timeoutSpinnerModel;
   private JSpinner timeoutSpinner;
}
