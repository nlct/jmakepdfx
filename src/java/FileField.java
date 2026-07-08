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

import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.*;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.TJHAbstractAction;

public class FileField extends JPanel
{
   public FileField(Jmakepdfx application, Container parent, JFileChooser fileChooser)
   {
      this(application, parent, null, fileChooser, JFileChooser.FILES_ONLY);
   }

   public FileField(Jmakepdfx application, Container parent, JFileChooser fileChooser,
        int mode)
   {
      this(application, parent, null, fileChooser, mode);
   }

   public FileField(Jmakepdfx application, Container parent, String fileName,
       JFileChooser fileChooser)
   {
      this(application, parent, fileName, fileChooser, JFileChooser.FILES_ONLY);
   }

   public FileField(Jmakepdfx application, Container parent, String fileName,
      JFileChooser fileChooser, int mode)
   {
      super(new FlowLayout(FlowLayout.LEADING));
      this.application = application;
      this.parent = parent;
      this.fileChooser = fileChooser;
      this.mode = mode;

      TeXJavaHelpLib helpLib = application.getHelpLib();

      setAlignmentX(LEFT_ALIGNMENT);

      textField = new JTextField();
      add(textField);

      if (fileName != null)
      {
         textField.setText(fileName);
      }

      TJHAbstractAction chooseAction = new TJHAbstractAction(helpLib,
        "button", "file_choose", helpLib.getKeyStroke("button.file_choose"),
        helpLib.getDefaultButtonActionOmitKeys())
         {
            @Override
            public void doAction()
            {
               openFileChooser();
            }
         };

      button = new JButton(chooseAction);
      add(button);
   }

   public void openFileChooser()
   {
      fileChooser.setFileSelectionMode(mode);

      File file = getFile();

      if (file != null)
      {
         fileChooser.setCurrentDirectory(file.getParentFile());

         fileChooser.setSelectedFile(file);
      }

      fileChooser.setApproveButtonMnemonic(
         application.getHelpLib().getMnemonic("file_chooser.select.mnemonic"));

      if (fileChooser.showDialog(parent,
           application.getMessageWithFallback("file_chooser.select", "Select"))
         == JFileChooser.APPROVE_OPTION)
      {
         textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
   }

   public String getFileName()
   {
      return textField.getText();
   }

   public File getFile()
   {
      String filename = getFileName();

      return filename.isEmpty() ? null : new File(filename);
   }

   public void setFileName(String filename)
   {
      textField.setText(filename);
   }

   public void setFile(File file)
   {
      if (file == null)
      {
         setFileName("");
      }
      else
      {
         setFileName(file.getAbsolutePath());
      }
   }

   public void setCurrentDirectory(String dirPath)
   {
      setCurrentDirectory(new File(dirPath));
   }

   public void setCurrentDirectory(File dir)
   {
      fileChooser.setCurrentDirectory(dir);
   }

   public boolean requestFocusInWindow()
   {
      return textField.requestFocusInWindow();
   }

   public void setEnabled(boolean flag)
   {
      super.setEnabled(flag);

      textField.setEnabled(flag);
      button.setEnabled(flag);
   }

   private JTextField textField;

   private JButton button;

   private JFileChooser fileChooser;
      
   private Container parent;
      
   private int mode;

   Jmakepdfx application;
}
