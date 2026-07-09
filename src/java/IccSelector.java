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

public class IccSelector extends JDialog implements OkayAction
{
   public IccSelector(Jmakepdfx application)
   {
      super(application.getFrame(),
             application.getMessageWithFallback("iccselect.title", "Select ICC File"), true);
      this.application = application;
               
      TeXJavaHelpLib helpLib = application.getHelpLib();
         
      fileChooser = new JFileChooser();

      fileField = new FileField(application, this, fileChooser);

      getContentPane().add(fileField, "Center");

      JPanel buttonPanel = new JPanel();
      add(buttonPanel, "South");

      buttonPanel.add(helpLib.createOkayButton((OkayAction)this, getRootPane()));
      buttonPanel.add(helpLib.createCancelButton(this));

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

   public File fetchPath()
   {
      setVisible(true);

      return selectedFile;
   }

   Jmakepdfx application;
   JFileChooser fileChooser;
   FileField fileField;
   File selectedFile;

}

