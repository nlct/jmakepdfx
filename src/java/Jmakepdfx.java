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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Vector;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.KeyEvent;

import javax.imageio.ImageIO;

import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.xml.sax.SAXException;

import com.dickimawbooks.texjavahelplib.*;

public class Jmakepdfx extends AbstractCLI
  implements ActionListener,UserCancellationListener
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

      printSyntaxItem(getMessage("syntax.title", "--title", "-t"));
      printSyntaxItem(getMessage("syntax.author", "--author", "-a"));

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
       || arg.equals("--title") || arg.equals("-t")
       || arg.equals("--author") || arg.equals("-a")
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
      else if (isArg(arg, "--title", "-t", returnVals))
      {
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing_value", arg));
         }

         pdfTitle = returnVals[0].toString();
      }
      else if (isArg(arg, "--author", "-a", returnVals))
      {
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing_value", arg));
         }

         pdfAuthor = returnVals[0].toString();
      }
      else
      {
         return false;
      }

      return true;
   }

   @Override
   protected void preCLIProcess() throws InvalidSyntaxException
   {
      try
      {
         properties = JpdfxProperties.fetchProperties(this);
      }
      catch (IOException e)
      {
         warning(e.getMessage());
         properties = new JpdfxProperties(this);
      }
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

   protected void initGuiAndShow() throws IOException,SAXException
   {
      mainFrame = new JFrame(NAME);

      TeXJavaHelpLib helpLib = getHelpLib();

      helpLib.setHelpFontDialogLabel(null);// help chapter not present in manual
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

      int toolbarOrient = properties.getToolBarOrientation();
      toolbar = new JToolBar(toolbarOrient);
      mainFrame.getContentPane().add(toolbar, 
        toolbarOrient == JToolBar.HORIZONTAL ? "North" : "West");

      statusField = new JLabel();
      mainFrame.getContentPane().add(statusField, "South");

      JMenuBar mBar = new JMenuBar();
      mainFrame.setJMenuBar(mBar);

      JMenu fileM = helpLib.createJMenu("menu.file");
      mBar.add(fileM);

      fileChooser = new JFileChooser();
      pdfFilter = new FileNameExtensionFilter(
        getMessage("filter.pdf"), "pdf");
      fileChooser.setFileFilter(pdfFilter);
      fileChooser.setCurrentDirectory(properties.getDefaultDirectoryFile());

      recentM = helpLib.createJMenu("menu.file.recent");
      fileM.add(recentM);

      recentM.add(helpLib.createJMenuItem("menu.file.recent", "clearrecent", this));
      recentM.addSeparator();

      properties.setRecentFiles(recentM, this);

      TJHAbstractAction inputAction = new TJHAbstractAction(helpLib,
        "menu.file", "input", helpLib.getKeyStroke("menu.file.input"),
        helpLib.getDefaultButtonActionOmitKeys())
         {
            @Override
            public void doAction()
            {
               selectInputFile();
            }
         };

      fileM.add(inputAction);
      toolbar.add(inputAction);

      TJHAbstractAction outputAction = new TJHAbstractAction(helpLib,
        "menu.file", "output", helpLib.getKeyStroke("menu.file.output"),
        helpLib.getDefaultButtonActionOmitKeys())
         {
            @Override
            public void doAction()
            {
               selectOutputFile();
            }
         };

      fileM.add(outputAction);
      toolbar.add(outputAction);

      fileM.add(helpLib.createJMenuItem("menu.file", "reset", this,
        helpLib.getKeyStroke("menu.file.reset")));

      fileM.add(helpLib.createJMenuItem("menu.file", "quit", this,
        helpLib.getKeyStroke("menu.file.quit")));

      JMenu toolsM = helpLib.createJMenu("menu.tools");
      mBar.add(toolsM);

      convertAction = new TJHAbstractAction(helpLib,
        "menu.tools", "convert", helpLib.getKeyStroke("menu.tools.convert"),
        helpLib.getDefaultButtonActionOmitKeys())
         {
            @Override
            public void doAction()
            {
               runConversion();
            }
         };

       convertAction.setEnabled(false);

       toolsM.add(convertAction);

      JMenu settingsM = helpLib.createJMenu("menu.settings");
      mBar.add(settingsM);

      propertiesDialog = new PropertiesDialog(this);

      TJHAbstractAction settingsAction = new TJHAbstractAction(helpLib,
        "menu.settings", "editsettings", helpLib.getKeyStroke("menu.settings.editsettings"),
        helpLib.getDefaultButtonActionOmitKeys())
         {
            @Override
            public void doAction()
            {
               openSettings();
            }
         };

      settingsM.add(settingsAction);
      toolbar.add(settingsAction);

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

      appSelector = new JpdfxAppSelector(this);

      JComponent mainComp = Box.createVerticalBox();
      mainFrame.getContentPane().add(new JScrollPane(mainComp), "Center");

      InputTransferHandler inHandler = new InputTransferHandler(this);
      OutputTransferHandler outHandler = new OutputTransferHandler(this);

      JComponent row;

      inputField = new JTextField(FILE_FIELD_SIZE);
      inputField.setEditable(false);
      inputField.setBorder(BorderFactory.createEmptyBorder());

      JButton chooseInButton = helpLib.createJButton("inputfile", "choosein", this,
        helpLib.getIconPrefix("inputfile.choosein", "open"), true, true);

      JLabelGroup labelGrp = new JLabelGroup();
      JLabel inputLabel = helpLib.createJLabel(labelGrp, "inputfile.label", chooseInButton);

      row = createRow(FILE_ROW_HGAP, FILE_ROW_VGAP);
      mainComp.add(row);

      row.setTransferHandler(inHandler);
      inputLabel.setTransferHandler(inHandler);
      inputField.setTransferHandler(inHandler);
      chooseInButton.setTransferHandler(inHandler);

      row.add(inputLabel);
      row.add(inputField);
      row.add(chooseInButton);

      clampCompMaxHeight(row, 0, 0);

      outputField = new JTextField(FILE_FIELD_SIZE);
      outputField.setEditable(false);
      outputField.setBorder(BorderFactory.createEmptyBorder());

      JButton chooseOutButton = helpLib.createJButton("outputfile", "chooseout", this,
        helpLib.getIconPrefix("inputfile.chooseout", "open"), true, true);

      JLabel outputLabel = helpLib.createJLabel(labelGrp, "outputfile.label", outputField);

      row = createRow(FILE_ROW_HGAP, FILE_ROW_VGAP);
      mainComp.add(row);

      row.add(outputLabel);
      row.add(outputField);
      row.add(chooseOutButton);

      clampCompMaxHeight(row, 0, 0);

      row.setTransferHandler(outHandler);
      outputLabel.setTransferHandler(outHandler);
      outputField.setTransferHandler(outHandler);
      chooseOutButton.setTransferHandler(outHandler);

      JComponent infoComp = Box.createVerticalBox();
      infoComp.setAlignmentX(0f);
      infoComp.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), 
        helpLib.getMessage("pdfinfo.title")
       ));

      mainComp.add(infoComp);

      row = createRow();
      infoComp.add(row);

      titleField = new JTextField();
      JLabel titleLabel = helpLib.createJLabel(labelGrp, "pdfinfo.pdftitle", titleField);
      row.add(titleLabel);
      row.add(titleField);

      if (pdfTitle != null)
      {
         titleField.setText(pdfTitle);
      }

      clampCompMaxHeight(row, 0, 0);

      row = createRow();
      infoComp.add(row);

      authorField = new JTextField();
      JLabel authorLabel = helpLib.createJLabel(labelGrp, "pdfinfo.pdfauthor", authorField);
      row.add(authorLabel);
      row.add(authorField);

      if (pdfAuthor != null)
      {
         authorField.setText(pdfAuthor);
      }

      clampCompMaxHeight(row, 0, 0);

      row = createRow();
      infoComp.add(row);

      pageCountField = new JTextField();
      pageCountField.setEditable(false);
      JLabel pageCountLabel = helpLib.createJLabel(labelGrp, "pdfinfo.pagecount", null);

      row.add(pageCountLabel);
      row.add(pageCountField);

      row.add(createHorizontalSpacer(10));

      sizeField = new JTextField();
      sizeField.setEditable(false);
      JLabel sizeLabel = helpLib.createJLabel(labelGrp, "pdfinfo.filesize", null);

      row.add(sizeLabel);
      row.add(sizeField);

      clampCompMaxHeight(row, 0, 0);

      row = createRow();
      mainComp.add(row);

      row.add(helpLib.createJLabel("profile.title"));

      ButtonGroup btnGrp = new ButtonGroup();

      greyButton = helpLib.createJRadioButton("profile", "greyscale",
         properties.isGrayProfile(), btnGrp);
      row.add(greyButton);

      cmykButton = helpLib.createJRadioButton("profile", "cmyk",
         !greyButton.isSelected(), btnGrp);
      row.add(cmykButton);

      iccButton = helpLib.createJCheckBox("profile", "use_icc",
         properties.isUseICC());
      row.add(iccButton);

      row.add(new JButton(convertAction));

      if (inFile != null)
      {
         setInputFile(inFile);
      }

      if (outFile != null)
      {
         setOutputFile(outFile);
      }

      if (inFile == null && outFile == null)
      {
         updateStatus();
      }

      mainFrame.pack();
      mainFrame.setLocationRelativeTo(null);
      mainFrame.setVisible(true);
   }

   protected JComponent createRow()
   {
      Box row = Box.createHorizontalBox();

      row.setAlignmentX(0f);

      return row;
   }

   protected JComponent createRow(int hgap, int vgap)
   {
      JComponent row = new JPanel(new FlowLayout(FlowLayout.LEADING, hgap, vgap));

      row.setAlignmentX(0f);
      row.setBackground(Color.WHITE);
      row.setBorder(BorderFactory.createEtchedBorder());

      return row;
   }

   protected Component createHorizontalSpacer(int size)
   {
      Component comp = Box.createHorizontalStrut(size);
      return comp;
   }

   public void clampCompMaxHeight(JComponent comp, int xpad, int ypad)
   {
      Dimension dim = comp.getPreferredSize();
      dim.width = (int)comp.getMaximumSize().getWidth() + xpad;
      dim.height += ypad;
      comp.setMaximumSize(dim);
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

      if ("choosein".equals(action))
      {
         selectInputFile();
      }
      else if ("chooseout".equals(action))
      {
         selectOutputFile();
      }
      else if ("quit".equals(action))
      {
         quit();
      }
      else if ("clearrecent".equals(action))
      {
         properties.clearRecentList();
         properties.setRecentFiles(recentM, this);
      }
      else if ("license".equals(action))
      {
         licenseDialog.setVisible(true);
      }
      else if ("about".equals(action))
      {
         aboutDialog.setVisible(true);
      }
      else if ("reset".equals(action))
      {
         reset();
      }
      else if (action != null)
      {
         try
         {
            int idx = Integer.parseInt(action);

            if (idx >= 0 && idx < properties.getRecentFileNameCount())
            {
               String filename = properties.getRecentFileName(idx);

               setInputFile(new File(filename));
            }
         }
         catch (NumberFormatException e)
         {
         }
      }
   }

   public JFrame getFrame()
   {
      return mainFrame;
   }

   public JpdfxProperties getProperties()
   {
      return properties;
   }

   public void setStatus(String msg)
   {
      if (statusField != null)
      {
         statusField.setText(msg);
      }
   }

   public void updateStatus()
   {
      if (inFile == null)
      {
         setStatus(getMessageWithFallback("message.select_input",
           "Use {0} to select the input PDF file",
           String.format("%s > %s",
             getMessageWithFallback("menu.file", "File"),
             getMessageWithFallback("menu.file.input", "Input PDF...")
           )
         ));
      }
      else if (outFile == null)
      {
         setStatus(getMessageWithFallback("message.select_output",
           "Use {0} to select the output PDF file",
           String.format("%s > %s",
             getMessageWithFallback("menu.file", "File"),
             getMessageWithFallback("menu.file.output", "Output PDF...")
           )
         ));
      }
      else
      {
         setStatus(getMessageWithFallback("message.file_loaded",
           "Check the required colour profile and click on ''{0}''",
            getMessageWithFallback(convertAction.getDisplayName(), "Convert")
         ));
      }
   }

   public boolean isGrayProfile()
   {
      return greyButton == null ? properties.isGrayProfile() : greyButton.isSelected();
   }

   public boolean isCMYKProfile()
   {
      return cmykButton == null ? properties.isCMYKProfile() : cmykButton.isSelected();
   }

   public boolean isUseICC()
   {
      return iccButton == null ? properties.isUseICC() : iccButton.isSelected();
   }

   public void quit()
   {
// TODO check if process running

      try
      {
         properties.setToolBarOrientation(toolbar.getOrientation());

         if (greyButton.isSelected())
         {
            properties.setGrayProfile();
         }
         else
         {
            properties.setCMYKProfile();
         }

         properties.setUseICC(iccButton.isSelected());

         properties.save();
      }
      catch (IOException e)
      {
         error(getMessageWithFallback("error.properties_save_failed",
               "Failed to save properties"), e);
      }

      System.exit(0);
   }

   public void openSettings()
   {
      propertiesDialog.display();
   }

   public void reset()
   {
      setInputFile(null);
      setOutputFile(null);

      if (pdfTitle == null)
      {
         titleField.setText("");
      }
      else
      {
         titleField.setText(pdfTitle);
      }

      if (pdfAuthor == null)
      {
         authorField.setText("");
      }
      else
      {
         authorField.setText(pdfAuthor);
      }
   }

   public void selectInputFile()
   {
      if (inFile != null)
      {
         fileChooser.setSelectedFile(inFile);
      }

      if (fileChooser.showOpenDialog(mainFrame)
        == JFileChooser.APPROVE_OPTION)
      {
         setInputFile(fileChooser.getSelectedFile());
      }
   }

   public void setInputFile(File file)
   {
      inFile = file;

      if (file == null)
      {
         inputField.setText("");
      }
      else
      {
         inputField.setText(inFile.toString());

         properties.addRecentFile(file);
         properties.setRecentFiles(recentM, this);

         try
         {
            getPdfInfo(inFile);
         }
         catch (Exception e)
         {
            error(mainFrame,
              getMessageWithFallback("error.noinfo",
               "Failed to get PDF information from file ''{0}''.\n{1}",
                inFile, e.getMessage()
              ),
              e);
         }
      }

      convertAction.setEnabled(inFile != null && outFile != null);
      updateStatus();
   }

   public boolean supportsInput(File file)
   {
      return pdfFilter.accept(file);
   }

   public File getCurrentDirectory()
   {
      if (fileChooser == null)
      {
         File parent = inFile == null ? null : inFile.getParentFile();

         return parent == null ? new File(System.getProperty("user.dir")) : parent;
      }
      else if (inFile == null)
      {
         return fileChooser.getCurrentDirectory();
      }
      else
      {
         File parent = inFile.getParentFile();

         return parent == null ? fileChooser.getCurrentDirectory() : parent;
      }
   }

   public void selectOutputFile()
   {
      if (outFile == null)
      {
         String filename = inFile.toString();

         if (filename.endsWith(".pdf"))
         {
            filename = filename.substring(0, filename.length()-4) + "-pdfx.pdf";
            fileChooser.setSelectedFile(new File(filename));
         }
      }
      else
      {
         fileChooser.setSelectedFile(outFile);
      }

      if (fileChooser.showSaveDialog(mainFrame)
        == JFileChooser.APPROVE_OPTION)
      {
         setOutputFile(fileChooser.getSelectedFile());
      }
   }

   public void setOutputFile(File file)
   {
      outFile = file;

      if (file == null)
      {
         outputField.setText("");
      }
      else
      {
         outputField.setText(outFile.toString());
      }

      convertAction.setEnabled(inFile != null && outFile != null);
      updateStatus();
   }

   public boolean supportsOutput(File file)
   {
      return pdfFilter.accept(file);
   }

   /**
    * Gets author, title and page count from a PDF file.
    * The information is put in the applicable JTextField components.
    * @param pdfFile the PDF file
    */
   public void getPdfInfo(File pdfFile) throws IOException,InterruptedException
   {
      if (!pdfFile.exists())
      {
         throw new FileNotFoundException(getMessageWithFallback(
           "error.file_not_found", "File not found: {0}", pdfFile));
      }

      setStatus(getMessageWithFallback("message.getinfo", "Getting PDF info."));

      String gs = getGSApp();

      File file = pdfFile;

      if (!file.isAbsolute())
      {
         file = pdfFile.getAbsoluteFile();
      }

      String filename = toGSFileName(file);

      String dirname = file.getParentFile().getAbsolutePath();

      if (!dirname.endsWith(File.separator))
      {
         dirname += File.separator;
      }

      StringBuilder ps = new StringBuilder();

      ps.append("/PDFContext << >> .PDFInit def (");
      ps.append(filename);
      ps.append(") (r) file ");
      ps.append("PDFContext .PDFStream ");
      ps.append("PDFContext .PDFInfo dup /Title known { /Title get = } { () = } ifelse ");
      ps.append("PDFContext .PDFInfo dup /Author known { /Author get = } { () = } ifelse ");
      ps.append("PDFContext .PDFInfo dup /NumPages known { /NumPages get = } { () = } ifelse ");
      ps.append("PDFContext .PDFClose quit");

      String[] cmd = new String[]
      {
         gs,
         "-dNODISPLAY",
         "--permit-file-read="+dirname,
         "-q",
         "-c",
         ps.toString()
      };

      StringBuilder result = new StringBuilder();

      int exitCode = getHelpLib().execCommandAndWaitFor(
        (File)null, (File)null, true,
        TeXJavaHelpLib.MessageType.WARNING,
        result,
        properties.getMaxProcessTime(),
        Integer.MAX_VALUE,
        this,
        cmd);

      sizeField.setText(formatFileSize(pdfFile.length()));

      if (exitCode == 0)
      {
         String[] split = result.toString().split("\\r?\\n");

         String name = "";
         String title = "";
         String pages = "";

         if (split.length > 0)
         {
            byte[] bytes = split[0].getBytes();

            if (bytes.length > 1
                  && bytes[0] == 0x3F && bytes[1] == 0x3F)
            {
               title = new String(bytes, 2, bytes.length-2, StandardCharsets.UTF_16);
            }
            else
            {
               title = split[0];
            }

            if (split.length > 1)
            {
               bytes = split[1].getBytes();

               if (bytes.length > 1
                     && bytes[0] == 0x3F && bytes[1] == 0x3F)
               {
                  name = new String(bytes, 2, bytes.length-2, StandardCharsets.UTF_16);
               }
               else
               {
                  name = split[1];
               }

               if (split.length > 2)
               {
                  bytes = split[2].getBytes();

                  if (bytes.length > 1
                        && bytes[0] == 0xFE && bytes[1] == 0xFF)
                  {
                     pages = new String(bytes, StandardCharsets.UTF_16);
                  }
                  else
                  {
                     pages = split[2];
                  }
               }
            }
         }

         boolean updateInfo = 
                  (pdfAuthor == null || name.equals(pdfAuthor))
               && (pdfTitle == null || title.equals(pdfTitle));

         if (!updateInfo && (
                  JOptionPane.showConfirmDialog(mainFrame,
                    getMessageWithFallback(
                      "message.confirm.update_info",
                      "Update PDF Info?"
                    ),
                    getMessageWithFallback("message.confirm", "Confirm"),
                    JOptionPane.YES_NO_OPTION,
                   JOptionPane.QUESTION_MESSAGE
                  )
                == JOptionPane.YES_OPTION
               )
            )
         {
            updateInfo = true;
         }

         if (updateInfo)
         {
            authorField.setText(name);
            titleField.setText(title);
         }

         pageCountField.setText(pages);
      }
   }

   /**
    * Converts the filename to a form that GhostScript will accept.
    * @param filename OS filename
    * @return PostScript string
    */
   public static String toGSFileName(String filename)
   {
      int n = filename.length();
   
      StringBuilder builder = new StringBuilder(n);
         
      boolean swapdd = File.separator.equals("\\");
            
      for (int i = 0; i < n; i++)
      {
         int codePoint = filename.codePointAt(i);
    
         if (codePoint == 40 || codePoint == 41)
         {
            // Replace left or right parenthesis with backslash
            // followed by parenthesis
               
            builder.appendCodePoint(92); // backslash
            builder.appendCodePoint(codePoint);
         }     
         else if (swapdd && codePoint == 92)
         {
            // Replace backslash \ with forward slash /

            builder.appendCodePoint(47);
         }
         else
         {
            builder.appendCodePoint(codePoint);
         }
      }

      return builder.toString();
   }

   public File getApp(String appName, String winAppName, String os2AppName)
      throws FileNotFoundException
   {        
      File path = null;

      if (appSelector != null)
      {
         path = appSelector.fetchApplicationPath(
            appName, winAppName, os2AppName,
            getMessageWithFallback(
             "properties.query.location.app",
             "Please specify location of {0} application",
             appName));
      }

      if (path == null || !path.exists())
      {
         throw new FileNotFoundException(getMessageWithFallback(
          "error.process_failed.missing_app",
          "Process can''t run without {0} application. Please provide full path.", appName));
      }  
    
      return path;
   }

   public String getGSApp()
    throws IOException
   {
      String app = properties.getGSApp();

      if (app != null && !app.isEmpty())
      {
         return app;
      }

      File path = null;

      path = getApp("gs", "gswin64c", "gsos2");

      properties.setGSApp(path.getAbsolutePath());

      return path.getAbsolutePath();
   }

   @Override
   public void setProcess(Process process)
   {
   }

   @Override
   public void checkForInterrupt() throws UserCancelledException
   {
// TODO
   }

   /**
    * Converts the filename to a form that GhostScript will accept.
    * @param file the file
    * @return PostScript string
    */
   public static String toGSFileName(File file)
   {
      return toGSFileName(file.getAbsolutePath());
   }

   /**
    * Formats byte size.
    */
   public static String formatFileSize(long size)
   {
      int grp = (int) (Math.log10(size)/Math.log10(1024));

      return String.format("%d %s",
         (int)Math.round(size/Math.pow(1024, grp)), FILE_SIZE_UNITS[grp]);
   }

   public void runConversion()
   {
// TODO start thread
   }

   public void toPdfX() throws IOException
   {
// TODO
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
   String pdfAuthor, pdfTitle;

   JFrame mainFrame;
   JToolBar toolbar;
   JMenu recentM;
   JTextField inputField, outputField;
   JFileChooser fileChooser;
   JTextField titleField, authorField, pageCountField, sizeField;
   JRadioButton greyButton, cmykButton;
   JCheckBox iccButton;
   TJHAbstractAction convertAction;
   JLabel statusField;
   MessageDialog licenseDialog, aboutDialog;
   javax.swing.filechooser.FileFilter pdfFilter;

   JpdfxProperties properties;
   PropertiesDialog propertiesDialog;
   JpdfxAppSelector appSelector;

   public static final int FILE_FIELD_SIZE=32;
   public static final int FILE_ROW_HGAP=5;
   public static final int FILE_ROW_VGAP=10;

   public static final String[] FILE_SIZE_UNITS
      = new String[] {"B", "KB", "MB", "GB", "TB"};

   public static final String ICON_DIR = "icons/";
   public static final String NAME = "jmakepdfx";
   public static final String VERSION = "0.5";
   public static final String VERSION_DATE = "2026-07-07";
}
