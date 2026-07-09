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

import javax.swing.*;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.OkayAction;

public class JpdfxAppSelector extends JDialog implements OkayAction
{
   public JpdfxAppSelector(Jmakepdfx application)
   {
      super(application.getFrame(), 
            application.getMessageWithFallback("appselect.title", "Select Application"),
             true);
      this.application = application;

      TeXJavaHelpLib helpLib = application.getHelpLib();

      messageLabel = helpLib.createJLabel("appselect.pathlabel");

      getContentPane().add(messageLabel, "North");

      fileChooser = new JFileChooser();

      fileField = new FileField(application, this, fileChooser);

      getContentPane().add(fileField, "Center");

      JPanel buttonPanel = new JPanel();
      add(buttonPanel, "South");

      buttonPanel.add(helpLib.createOkayButton((OkayAction)this, getRootPane()));
      buttonPanel.add(helpLib.createCancelButton(this));

      String arch = System.getProperty("os.name").toLowerCase();

      if (arch.startsWith("win"))
      {
         exeSuffix = ".exe";
      }
      else
      {
         exeSuffix = "";
      }

      pack();
   }

   @Override
   public void okay()
   {
      selectedFile = fileField.getFile();

      if (selectedFile == null)
      {
         application.error(this,
            application.getMessageWithFallback("error.no_file", "No file specified"));
      }
      else
      {
         setVisible(false);
      }
   }

   public File fetchApplicationPath(String appName, String messageText)
   {
      return fetchApplicationPath(appName, null, messageText);
   }

   public File fetchApplicationPath(String appName, String altAppName, String messageText)
   {
      return fetchApplicationPath(appName, null, null, messageText);
   }

   public File fetchApplicationPath(String appName, String altName1,
      String altName2, String messageText)
   {
      selectedFile = null;

      File file = findApp(appName, altName1, altName2);

      if (file != null)
      {
         fileChooser.setCurrentDirectory(file.getParentFile());
         fileChooser.setSelectedFile(file);

         fileField.setFileName(file.getAbsolutePath());
      }
      else
      {
         fileField.setFileName(appName+exeSuffix);
      }

      messageLabel.setText(messageText);

      setLocationRelativeTo(application.getFrame());
      setVisible(true);

      return selectedFile;
   }

   public File fetchApplicationPath(String messageText)
   {
      selectedFile = null;

      fileChooser.setSelectedFile(null);
      fileField.setFileName("");

      messageLabel.setText(messageText);

      setLocationRelativeTo(application.getFrame());
      setVisible(true);

      return selectedFile;
   }

   protected File findApp(String name)
   {
      return findApp(name, null);
   }

   protected File findApp(String name, String altName)
   {
      return findApp(name, null, null);
   }

   protected File findApp(String name, String altName, String altName2)
   {
      String path = System.getenv("PATH");

      String filename = name + exeSuffix;
      String filename2 = (altName == null ? null : altName + exeSuffix);
      String filename3 = (altName2 == null ? null : altName2 + exeSuffix);

      String[] split = path.split(File.pathSeparator);

      for (int i = 0; i < split.length; i++)
      {
         File file = new File(split[i], filename);

         if (file.exists())
         {
            return file;
         }

         if (filename2 != null)
         {
            file = new File(split[i], filename2);

            if (file.exists())
            {
               return file;
            }
         }

         if (filename3 != null)
         {
            file = new File(split[i], filename3);

            if (file.exists())
            {
               return file;
            }
         }
      }

      return null;
   }

   Jmakepdfx application;
   JLabel messageLabel;
   JFileChooser fileChooser;
   FileField fileField;
   File selectedFile;
   String exeSuffix;
}
