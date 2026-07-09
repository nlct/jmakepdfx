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

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;
import com.dickimawbooks.texjavahelplib.JLabelGroup;
import com.dickimawbooks.texjavahelplib.OkayAction;
import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;

public class PropertiesDialog extends JDialog 
 implements OkayAction
{     
   public PropertiesDialog(Jmakepdfx application)
   {
      super(application.getFrame(),
             application.getMessageWithFallback("properties.title", "Properties"), true);

      this.application = application;

      JpdfxProperties properties = application.getProperties();
      TeXJavaHelpLib helpLib = application.getHelpLib();

      fileChooser = new JFileChooser();

      JComponent row;

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

      row = createRow();
      startDirBox.add(row);

      homeButton = addStartupButton("home", bg, startupChangeListener, setting);
      row.add(homeButton);
      application.clampCompMaxHeight(row, 0, 0);

      row = createRow();
      startDirBox.add(row);

      cwdButton = addStartupButton("cwd", bg, startupChangeListener, setting);
      row.add(cwdButton);
      application.clampCompMaxHeight(row, 0, 0);

      row = createRow();
      startDirBox.add(row);

      lastButton = addStartupButton("last", bg, startupChangeListener, setting);
      row.add(lastButton);
      application.clampCompMaxHeight(row, 0, 0);

      row = createRow();
      startDirBox.add(row);

      customButton = addStartupButton("custom", bg, startupChangeListener, setting);
      customButton.addActionListener(
        new ActionListener()
        {
           @Override
           public void actionPerformed(ActionEvent evt)
           {
              customField.requestFocusInWindow();
           }
        }
      );
      row.add(customButton);

      row.add(Box.createHorizontalStrut(Jmakepdfx.FILE_ROW_HGAP));

      row.add(customField);

      application.clampCompMaxHeight(row, 0, 0);

      JLabelGroup labelGrp = new JLabelGroup();

      JLabel gsLabel = helpLib.createJLabel(labelGrp, "properties.path.gs", null);

      gsField = new FileField(application, this, properties.getGSApp(),
        fileChooser, gsLabel);
      application.clampCompMaxHeight(gsField, 0, 0);

      mainPanel.add(gsField);

      JLabel pdfLabel = helpLib.createJLabel(labelGrp, "properties.path.pdfviewer", null);

      pdfField = new FileField(application, this, properties.getPDFViewer(),
        fileChooser, pdfLabel);
      application.clampCompMaxHeight(pdfField, 0, 0);

      mainPanel.add(pdfField);

      JLabel iccLabel = helpLib.createJLabel(labelGrp, "properties.path.icc", null);

      iccField = new FileField(application, this, properties.getICCFileName(),
         fileChooser, iccLabel);
      application.clampCompMaxHeight(iccField, 0, 0);

      mainPanel.add(iccField);

      row = createRow();
      mainPanel.add(row);

      long millisec = properties.getMaxProcessTime();

      timeoutSpinnerModel = new SpinnerNumberModel(
         Integer.valueOf((int)(millisec/60000L)),
         Integer.valueOf(1), null, Integer.valueOf(1));

      timeoutSpinner = new JSpinner(timeoutSpinnerModel);

      JSpinner.NumberEditor editor = (JSpinner.NumberEditor)timeoutSpinner.getEditor();
      JFormattedTextField field = editor.getTextField();
      int cols = field.getColumns();

      if (cols < 3)
      {
         field.setColumns(3);
      }

      JLabel timeoutLabel = helpLib.createJLabel("properties.timeout", timeoutSpinner);

      row.add(timeoutLabel);
      row.add(timeoutSpinner);
      row.add(helpLib.createJLabel("properties.timeout.minutes"));

      application.clampCompMaxHeight(row, 0, 0);

      JPanel buttonPanel = new JPanel();
      getContentPane().add(buttonPanel, "South");

      buttonPanel.add(helpLib.createOkayButton((OkayAction)this, getRootPane()));
      buttonPanel.add(helpLib.createCancelButton(this));

      try
      {
         buttonPanel.add(helpLib.createHelpDialogButton(this, "sec:settings"));
      }
      catch (IllegalArgumentException | HelpSetNotInitialisedException e)
      {
         application.internalError(null, null, e);
      }

      pack();
      setLocationRelativeTo(application.getFrame());
   }

   protected JComponent createRow()
   {
      JComponent row = new JPanel(new FlowLayout(FlowLayout.LEADING));

      row.setAlignmentX(Component.LEFT_ALIGNMENT);

      return row;
   }

   protected JRadioButton addStartupButton(String tag, ButtonGroup bg,
     ChangeListener startupChangeListener, String setting)
   {
      TeXJavaHelpLib helpLib = application.getHelpLib();

      JRadioButton button = helpLib.createJRadioButton("properties.startup", tag, 
       setting.equals(tag), bg);
      button.addChangeListener(startupChangeListener);
      button.setAlignmentX(Component.LEFT_ALIGNMENT);

      return button;
   }

   public void display()
   {
      JpdfxProperties properties = application.getProperties();

      String defaultDir = properties.getDefaultDirectory();

      if (defaultDir == null)
      {
         defaultDir = "";
      }

      customField.setFileName(defaultDir);

      String setting = properties.getDefaultDirectorySetting();
      
      if (setting == null)
      {
         setting = "cwd";
      }

      if (setting.equals("home"))
      {
         homeButton.setSelected(true);
      }
      else if (setting.equals("cwd"))
      {
         cwdButton.setSelected(true);
      }
      else if (setting.equals("last"))
      {
         lastButton.setSelected(true);
      }
      else
      {
         customButton.setSelected(true);
      }

      customField.setEnabled(customButton.isSelected());

      gsField.setFileName(properties.getGSApp());
      pdfField.setFileName(properties.getPDFViewer());
      iccField.setFileName(properties.getICCFileName());

      long millisec = properties.getMaxProcessTime();
      timeoutSpinnerModel.setValue(Integer.valueOf((int)(millisec/60000L)));

      setVisible(true);
   }

   @Override
   public void okay()
   {
      JpdfxProperties properties = application.getProperties();

      if (homeButton.isSelected())
      {
         properties.setDefaultHomeDir();
      }
      else if (cwdButton.isSelected())
      {
         properties.setDefaultCurrentDir();
      }
      else if (lastButton.isSelected())
      {
         properties.setDefaultLastDir();
      }
      else
      {
         String dirname = customField.getFileName();

         if (dirname.isEmpty())
         {
            application.error(this, application.getMessageWithFallback(
              "error.properties.missing_custom_dir",
              "Custom setting requires a directory name"));

            customField.requestFocusInWindow();

            return;
         }
      }

      properties.setGSApp(gsField.getFileName());
      properties.setPDFViewer(pdfField.getFileName());
      properties.setICCFileName(iccField.getFileName());

      long minutes = timeoutSpinnerModel.getNumber().longValue();
      properties.setMaxProcessTime(minutes * 60000L);

      setVisible(false);
   }

   Jmakepdfx application;
      
   private JFileChooser fileChooser;
      
   private JRadioButton homeButton, cwdButton, lastButton, customButton;

   private FileField customField, gsField, pdfField, iccField;

   private SpinnerNumberModel timeoutSpinnerModel;
   private JSpinner timeoutSpinner;
}
