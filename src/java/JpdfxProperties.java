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

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

public class JpdfxProperties extends Properties
{
   public JpdfxProperties(File propFile, File recentFile,
      boolean loadFile, Jmakepdfx application)
     throws IOException
   {
      super();

      app = application;

      this.propFile = propFile;
      this.recentFile = recentFile;

      BufferedReader reader = null;

      recentList = new Vector<String>();

      if (propFile != null && propFile.exists() && loadFile)
      {
         // if propFile exists, load it

         try
         {
            reader = Files.newBufferedReader(propFile.toPath());

            load(reader);
         }
         finally
         {
            if (reader != null)
            {
               reader.close();
               reader = null;
            }
         }
      }
      else
      {
         // set default values

         setDefaults();
      }

      if (recentFile != null && recentFile.exists())
      {
         try
         {
            reader = Files.newBufferedReader(recentFile.toPath());

            loadRecentFiles(reader);
         }
         finally
         {
            if (reader != null)
            {
               reader.close();
            }
         }
      }
   }

   public JpdfxProperties(Jmakepdfx application)
   {
      super();

      app = application;

      propFile       = null;
      recentFile     = null;

      setDefaults();
   }

   protected void setDefaults()
   {
      setProperty("directory.setting", "cwd");
      setProperty("toolbarorient", ""+JToolBar.HORIZONTAL);
   }

   public static JpdfxProperties fetchProperties(Jmakepdfx application)
     throws IOException
   {
      File parent = null;

      File settings   = null;
      File recent     = null;

      String home = System.getProperty("user.home");

      boolean loadFile = (home != null);

      if (loadFile)
      {
         // first check if directory ~/.jmakepdfx exists

         parent = new File(home, ".jmakepdfx");

         if (!parent.isDirectory())
         {
            // check if directory ~/jmakepdfx-settings exists

            parent = new File(home, "jmakepdfx-settings");

            if (!parent.isDirectory())
            {
               parent = null;
            }
         }
      }

      if (parent != null)
      {
         settings = new File(parent, baseName);
         recent = new File(parent, recentName);
      }
      else
      {
         loadFile = false;
      }

      return new JpdfxProperties(settings, recent, loadFile, application);
   }

   public void save()
      throws IOException
   {
      if (propFile == null)
      {
         String home = System.getProperty("user.home");

         if (home == null)
         {
            throw new IOException(app.getMessageWithFallback(
              "error.home_null", "NULL home directory"));
         }

         File parent;

         if (System.getProperty("os.name").equals("Windows"))
         {
            parent = new File(home, "jmakepdfx-settings");
         }
         else
         {
            parent = new File(home, ".jmakepdfx");
         }

         if (!parent.mkdir())
         {
            throw new IOException(app.getMessageWithFallback("error.io.cant_mkdir",
               "Can''t create directory: {0}",
               parent.getAbsolutePath()));
         }

         propFile = new File(parent, baseName);
      }

      String setting = getDefaultDirectorySetting();

      if (setting == null)
      {
         setting = "home";
      }

      if (setting.equals("home"))
      {
         setDefaultDirectory(System.getProperty("user.home"));
      }
      else if (setting.equals("cwd"))
      {
         setDefaultDirectory(System.getProperty("user.dir"));
      }
      else if (setting.equals("last"))
      {
         setDefaultDirectory(app.getCurrentDirectory());
      }

      BufferedWriter writer = null;
      PrintWriter out = null;

      try
      {
         writer = Files.newBufferedWriter(propFile.toPath());
         out = new PrintWriter(writer);

         store(out, "jmakepdfx-gui properties");
      }
      finally
      {
         if (out != null)
         {
            out.close();
            out = null;
         }

         if (writer != null)
         {
            writer.close();
            writer = null;
         }
      }

      if (recentFile == null)
      {
         recentFile = new File(propFile.getParentFile(), recentName);
      }

      try
      {
         writer = Files.newBufferedWriter(recentFile.toPath());
         out = new PrintWriter(writer);

         for (int i = 0, n = recentList.size(); i < n; i++)
         {
            out.println(recentList.get(i));
         }
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }

         if (writer != null)
         {
            writer.close();
         }
      }

   }

   private void loadRecentFiles(BufferedReader in)
     throws IOException
   {
      String line;

      while ((line = in.readLine()) != null)
      {
         recentList.add(line);
      }
   }

   public void addRecentFile(File file)
   {
      addRecentFile(file.getAbsolutePath());
   }

   public void addRecentFile(String fileName)
   {
      recentList.remove(fileName); // just in case it's already in the list
      recentList.add(fileName);
   }

   public void setRecentFiles(JMenu menu, ActionListener listener)
   {
      if (recentList == null) return;

      for (int i = menu.getMenuComponentCount()-1; i >= 0; i--)
      {
         Component comp = menu.getMenuComponent(i);

         if (comp instanceof JPopupMenu.Separator)
         {
            break;
         }

         menu.remove(i);
      }

      int lastIdx = recentList.size()-1;

      int n = Math.min(MAX_RECENT_FILES-1, lastIdx);

      for (int i = 0; i <= n; i++)
      {
         File file = new File(recentList.get(lastIdx-i));
         String num = ""+i;
         JMenuItem item = new JMenuItem(num+": "+file.getName());
         item.setMnemonic(num.charAt(0));
         item.setToolTipText(file.getAbsolutePath());
         item.setActionCommand(num);
         item.addActionListener(listener);

         menu.add(item);
      }
   }

   public int getRecentFileNameCount()
   {
      return recentList.size();
   }

   public String getRecentFileName(int i)
   {
      return recentList.get(recentList.size()-1-i);
   }

   public void clearRecentList()
   {
      recentList.clear();
   }

   public File getDefaultDirectoryFile()
   {
      String prop = getProperty("directory.setting", "cwd");
      String filename;

      if (prop.equals("home"))
      {
         filename = System.getProperty("user.home");
      }
      else if (prop.equals("cwd"))
      {
         filename = System.getProperty("user.dir");
      }
      else
      {
         filename = getDefaultDirectory();
      }

      if (filename == null || filename.isEmpty())
      {
         filename = System.getProperty("user.dir");
      }

      return new File(filename);
   }

   public String getDefaultDirectory()
   {
      return getProperty("directory");
   }

   public void setDefaultDirectory(String dir)
   {
      if (dir != null)
      {
         setProperty("directory", dir);
      }
   }

   public void setDefaultDirectory(File dir)
   {
      if (dir != null)
      {
         setProperty("directory", dir.getAbsolutePath());
      }
   }

   public String getDefaultDirectorySetting()
   {
      return getProperty("directory.setting");
   }

   public void setDefaultDirectorySetting(String setting)
   {
      if (setting != null)
      {
         setProperty("directory.setting", setting);
      }
   }

   public void setDefaultHomeDir()
   {
      setDefaultDirectorySetting("home");
   }

   public void setDefaultCurrentDir()
   {
      setDefaultDirectorySetting("cwd");
   }

   public void setDefaultLastDir()
   {
      setDefaultDirectorySetting("last");
   }

   public void setDefaultCustomDir(String dir)
   {
      if (dir == null)
      {
         setDefaultDirectorySetting("last");
      }
      else
      {
         setDefaultDirectorySetting("custom");
         setDefaultDirectory(dir);
      }
   }

   public String getPdfViewer()
   {
      return getProperty("pdfviewer");
   }

   public void setPdfViewer(String path)
   {
      if (path == null)
      {
         setProperty("pdfviewer", "");
      }
      else
      {
         setProperty("pdfviewer", path);
      }
   }

   public int getToolBarOrientation()
   {
       String val = getProperty("toolbarorient");

       if (val == null)
       {
          return JToolBar.HORIZONTAL;
       }

       try
       {
          return Integer.parseInt(val);
       }
       catch (NumberFormatException e)
       {
       }

       return JToolBar.HORIZONTAL;
   }

   public void setToolBarOrientation(int orient)
   {
      setProperty("toolbarorient", ""+orient);
   }

   public String getToolBarPosition()
   {
      String val = getProperty("toolbarpos");

      return (val == null ? "North" : val);
   }

   public void setToolBarPosition(String pos)
   {
      if (pos == null)
      {
         setProperty("toolbarpos", "North");
      }
      else
      {
         setProperty("toolbarpos", pos);
      }
   }

   public String getICCFileName()
   {
      return getProperty("iccfile");
   }

   public void setICCFileName(String iccfile)
   {
      setProperty("iccfile", iccfile);
   }

   public String getGSApp()
   {
      return getProperty("gs");
   }

   public void setGSApp(String gspath)
   {
      setProperty("gs", gspath);
   } 

   public String getPDFViewer()
   {
      return getProperty("pdfviewer");
   }

   public void setPDFViewer(String path)
   {
      setProperty("pdfviewer", path);
   } 

   public String getProfile()
   {
      String profile = getProperty("profile");

      // default to gray

      if (profile == null)
      {
         profile = "gray";
      }

      if (!(profile.equals("gray") || profile.equals("cmyk")))
      {
         throw new IllegalArgumentException(app.getMessageWithFallback(
           "error.syntax.invalid_value",
           "Invalid {0} value: {1}",
           "profile", profile));
      }

      return profile;
   }

   public boolean isUseICC()
   {
      return Boolean.parseBoolean(getProperty("useicc", 
       isGrayProfile() ? "false" : "true"));
   }

   public void setUseICC(boolean useICC)
   {
      setProperty("useicc", useICC ? "true" : "false");
   }

   public boolean isGrayProfile()
   {
      return getProfile().equals("gray");
   }

   public boolean isCMYKProfile()
   {
      return getProfile().equals("cmyk");
   }

   public void setGrayProfile()
   {
      setProperty("profile", "gray");
   }

   public void setCMYKProfile()
   {
      setProperty("profile", "cmyk");
   }

   public void setProfile(String profile)
   {
      if (!(profile.equals("gray") || profile.equals("cmyk")))
      {
         throw new IllegalArgumentException(app.getMessageWithFallback(
           "error.syntax.invalid_value",
           "Invalid {0} value: {1}",
           "profile", profile));
      }

      setProperty("profile", profile);
   }

   public long getMaxProcessTime()
   {
      String prop = getProperty("timeout");

      long timeout = DEFAULT_TIMEOUT;

      if (prop != null)
      {
         try
         {
            timeout = Long.parseLong(prop);
         }
         catch (NumberFormatException e)
         {
         }
      }

      return timeout;
   }

   public void setMaxProcessTime(long timeout)
   {
      if (timeout <= 0)
      {
         throw new IllegalArgumentException(app.getMessageWithFallback(
           "error.syntax.invalid_value",
           "Invalid {0} value: {1}",
           "timeout", timeout));
      }

      setProperty("timeout", ""+timeout);
   }

   private File propFile;

   private static final String baseName = "jmakepdfx.prop";
   private static String recentName = "recentfiles";

   private static final int MAX_RECENT_FILES=10;

   private Jmakepdfx app;

   private File recentFile;

   private Vector<String> recentList;

   private static final long DEFAULT_TIMEOUT = 1000 * 60 * 5; // 5 minutes
}
