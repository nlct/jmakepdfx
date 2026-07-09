/*
    Copyright (C) 2026 Nicola L.C. Talbot

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
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
import java.io.IOException;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import com.dickimawbooks.texjavahelplib.UserCancelledException;

public class ConvertWorker extends SwingWorker<File,Void>
{
   public ConvertWorker(Jmakepdfx application)
   {
      this.application = application;
   }

   @Override
   public File doInBackground()
     throws IOException,UserCancelledException,InterruptedException
   {
      application.setErrorBuffering(true);

      File file = application.toPdfX();

      return file;
   }

   @Override
   protected void done()
   {
      long millisecs = application.getMaxProcessTime();

      try
      {
         File file = get(millisecs, TimeUnit.MILLISECONDS);

         if (file != null && application.isAutoViewOn())
         {
            application.viewPDF(file);
         }
      }
      catch (java.util.concurrent.ExecutionException e)
      {
         String errMsgs = application.getErrorBufferContent();

         application.setErrorBuffering(false);

         if (errMsgs != null && !errMsgs.isEmpty())
         {
            application.error(application.mainFrame, errMsgs, e);
         }
         else
         {
            Throwable cause = e.getCause();

            if (cause != null)
            {
               application.error(application.mainFrame, cause.getMessage(), cause);
            }
            else
            {
               application.error(application.mainFrame, e.getMessage(), e);
            }
         }
      }
      catch (InterruptedException e)
      {
         application.error(application.mainFrame, e.getMessage(), e);
      }
      catch (TimeoutException e)
      {
         application.error(application.mainFrame, e.getMessage());
      }
      finally
      {
         application.workerFinished();
      }
   }

   Jmakepdfx application;
}

