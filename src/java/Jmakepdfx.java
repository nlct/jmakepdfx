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
import java.io.InputStream;
import java.io.IOException;

import java.util.List;
import java.util.Vector;

import java.awt.Image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.KeyEvent;

import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.xml.sax.SAXException;

import com.dickimawbooks.texjavahelplib.*;

public class Jmakepdfx extends AbstractCLI implements ActionListener
{
   public Jmakepdfx()
   {
   }

   @Override
   public void printCLIAbout()
   {
      System.out.println(getHelpLib().getAboutInfo(false,
        VERSION,
        VERSION_DATE,
        String.format(
         "Copyright (C) %s Nicola L. C. Talbot (%s)",
          getCopyrightDate(),
          getHelpLib().getInfoUrl(false, "www.dickimaw-books.com")),
         TeXJavaHelpLib.LICENSE_GPL3,
         false, null
      ));
   }

   public String getCopyrightStartYear()
   {
      return "2012";
   }

   public String getCopyrightDate()
   {
      String startYr = getCopyrightStartYear();
      String endYr = VERSION_DATE.substring(0, 4);

      if (startYr.equals(endYr))
      {
         return endYr;
      }
      else
      {
         return String.format("%s-%s", startYr, endYr);
      }
   }

   @Override
   public String getCLIApplicationName()
   {
      return NAME;
   }

   @Override
   public String getCLIApplicationVersion()
   {
      return TeXJavaHelpLib.VERSION;
   }

   @Override
   public String getCLIApplicationVersionDate()
   {
      return TeXJavaHelpLib.VERSION_DATE;
   }

   @Override
   public void printCLISyntax()
   {
      versionInfo();

      System.out.println();
      System.out.println(getMessage("clisyntax.usage",
        getMessage("syntax.options", getCLIApplicationName())));

      System.out.println();

      printSyntaxItem(getMessage("syntax.in", "--in", "-i"));

      printSyntaxItem(getMessage("syntax.out", "--output", "-o"));

      printSyntaxItem(getMessage("syntax.gui", "--[no]gui", "-g"));
      printSyntaxItem(getMessage("syntax.batch", "--batch", "-b"));

      System.out.println();

      printCommonCLISyntax();

      System.out.println();

      System.out.println(getMessage("clisyntax.bugreport",
        "https://github.com/nlct/jmakepdfx"));
   }

   @Override
   protected int getCLIArgCount(String arg)
   {
      if (arg.equals("--in") || arg.equals("-i")
       || arg.equals("--output") || arg.equals("-o")
         )
      {
         return 1;
      }

      return 0;
   }

   @Override
   protected void parseNoSwitchCLIArg(String arg) throws InvalidSyntaxException
   {
      if (inFile == null)
      {
         inFile = new File(arg);
      }
      else if (outFile == null)
      {
         outFile = new File(arg);
      }
      else
      {
         throw new InvalidSyntaxException(
               getMessage("error.syntax.only_one_inout"));
      }
   }

   @Override
   protected boolean parseCLIArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException
   {
      if (arg.equals("--gui") || arg.equals("-g"))
      {
         guiMode = true;
      }
      else if (arg.equals("--nogui") || arg.equals("--batch") || arg.equals("-b"))
      {
         guiMode = false;
      }
      else if (isArg(arg, "--in", "-i", returnVals))
      {
         if (inFile != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.clisyntax.only_one", arg));
         }

         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing_value", arg));
         }

         inFile = new File(returnVals[0].toString());
      }
      else if (isArg(arg, "--output", "-o", returnVals))
      {
         if (outFile != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.clisyntax.only_one", arg));
         }

         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing_value", arg));
         }

         outFile = new File(returnVals[0].toString());
      }
      else
      {
         return false;
      }

      return true;
   }

   @Override
   protected void postCLIProcess() throws InvalidSyntaxException
   {
      if (!isGUIMode())
      {
         if (inFile == null)
         {
            throw new InvalidSyntaxException(getMessage("error.batch_requires_in"));
         }

         if (outFile == null)
         {
            throw new InvalidSyntaxException(getMessage("error.batch_requires_out"));
         }
      }
   }

   @Override
   public boolean isGUIMode()
   {
      return guiMode;
   }

   @Override
   protected void loadDictionaries(MessageSystem msgSys) throws IOException
   {
      msgSys.loadDictionary(
       "/com/dickimawbooks/jmakepdfx/dictionaries/",
       "jmakepdfx");
   }

   public void toPdfX() throws IOException
   {
// TODO
   }

   protected void initGuiAndShow() throws IOException,SAXException
   {
      mainFrame = new JFrame(NAME);

      TeXJavaHelpLib helpLib = getHelpLib();

      helpLib.setHelpSetZipName(NAME.toLowerCase()+"-helpset.tjh");
      helpLib.initHelpSet();

      mainFrame.setIconImages(getAppIcons());

      mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      mainFrame.addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent evt)
         {
            quit();
         }
      });

      JToolBar toolbar = new JToolBar();
      mainFrame.getContentPane().add(toolbar, "North");

      JMenuBar mBar = new JMenuBar();
      mainFrame.setJMenuBar(mBar);

      JMenu fileM = helpLib.createJMenu("menu.file");
      mBar.add(fileM);

      fileM.add(helpLib.createJMenuItem("menu.file", "quit", this,
        KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK)));

      JMenu helpM = helpLib.createJMenu("menu.help");
      mBar.add(helpM);

      TJHAbstractAction manualAction = helpLib.createHelpManualAction();

      helpM.add(new JMenuItem(manualAction));
      toolbar.add(manualAction);

      helpM.add(helpLib.createJMenuItem("menu.help", "about", this));

      aboutDialog = new MessageDialog(mainFrame,
       helpLib.getMessage("about.title", NAME),
       true, helpLib, 
       helpLib.getAboutInfo(true,
        VERSION,
        VERSION_DATE,
        String.format(
         "Copyright (C) %s Nicola L. C. Talbot (%s)",
          getCopyrightDate(),
          helpLib.getInfoUrl(false, "www.dickimaw-books.com")),
         TeXJavaHelpLib.LICENSE_GPL3,
         false, null
      ));

      try
      {
         licenseDialog = helpLib.createLicenseDialog(mainFrame,
             helpLib.getMessage("license.title"));

         helpM.add(helpLib.createJMenuItem("menu.help", "license", this));
      }
      catch (Exception e)
      {
         helpLib.error(e);
      }

      mainFrame.pack();
      mainFrame.setLocationRelativeTo(null);
      mainFrame.setVisible(true);
   }

   public ImageIcon getAppIcon(String name)
   {
      InputStream in = null;
      ImageIcon ic = null;

      try
      {
         in = getClass().getResourceAsStream(ICON_DIR+name);

         if (in != null)
         {
            ic = new ImageIcon(ImageIO.read(in));
         }
      }
      catch (IOException e)
      {
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
               helpLib.debug(e);
            }
         }
      }

      return ic;
   }

   public List<Image> getAppIcons()
   {
      List<Image> list = new Vector<Image>();

      String base = NAME.toLowerCase();

      ImageIcon ic = getAppIcon(base+"-16.png");

      if (ic != null)
      {
         list.add(ic.getImage());
      }

      ic = getAppIcon(base+"-32.png");

      if (ic != null)
      {
         list.add(ic.getImage());
      }

      ic = getAppIcon(base+"-48.png");
   
      if (ic != null)
      {
         list.add(ic.getImage());
      }

      ic = getAppIcon(base+"-64.png");

      if (ic != null)
      {
         list.add(ic.getImage());
      }

      ic = getAppIcon(base+"-80.png");

      if (ic != null)
      {
         list.add(ic.getImage());
      }

      return list;
   }

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if ("quit".equals(action))
      {
         quit();
      }
      else if ("license".equals(action))
      {
         licenseDialog.setVisible(true);
      }
      else if ("about".equals(action))
      {
         aboutDialog.setVisible(true);
      }
   }

   public void quit()
   {
// TODO check if process running
      System.exit(0);
   }

   public static void main(String[] args)
   {
      final Jmakepdfx jmakepdfx = new Jmakepdfx();

      try
      {
         jmakepdfx.initialiseHelpAndParse(args);

         if (jmakepdfx.isGUIMode())
         {
            SwingUtilities.invokeAndWait(new Runnable()
             {
                @Override
                public void run()
                {
                   try
                   {
                      jmakepdfx.initGuiAndShow();
                   }
                   catch (Exception e)
                   {
                      jmakepdfx.error(null, e);
                   }
                }
             }
            );
         }
         else
         {
            jmakepdfx.toPdfX();
         }
      }
      catch (InvalidSyntaxException e)
      {
         jmakepdfx.error(e.getMessage(), null);
      }
      catch (InterruptedException | java.lang.reflect.InvocationTargetException e) 
      {
         String msg = e.getMessage();
   
         if (msg == null)
         {
            if (e.getCause() == null)
            {
               msg = e.toString();
            }
            else
            {
               msg = e.getCause().getMessage();

               if (msg == null)
               {
                  msg = e.toString()+" caused by "+e.getCause();
               }
            }
         }

         if (jmakepdfx.isGUIMode())
         {
            JOptionPane.showMessageDialog(null, msg, "Error",
              JOptionPane.ERROR_MESSAGE);
         }

         System.err.println(e.getMessage());
         e.printStackTrace();
      }
      catch (Throwable e)
      {
         jmakepdfx.error(null, e);
      }

      if (!jmakepdfx.isGUIMode() || jmakepdfx.getExitCode() != 0)
      {
         System.exit(jmakepdfx.getExitCode());
      }
   }

   boolean guiMode = true;
   File inFile, outFile;

   JFrame mainFrame;
   MessageDialog licenseDialog, aboutDialog;

   public static final String ICON_DIR = "icons/";
   public static final String NAME = "jmakepdfx";
   public static final String VERSION = "0.5";
   public static final String VERSION_DATE = "2026-07-07";
}
