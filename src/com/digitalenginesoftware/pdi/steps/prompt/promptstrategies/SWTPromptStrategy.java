/* This file is part of the Pentaho Data Integration Prompt Step Plugin.
 * It is licensed under the terms of the GNU LGPL version 2.1 or later.
 * The complete text of the license is available in the project documentation.
 *
 * Copyright 2011 Digital Engine Software, LLC
 */
package com.digitalenginesoftware.pdi.steps.prompt.promptstrategies;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


public abstract class SWTPromptStrategy<T> extends PromptStrategy<T> {

  public SWTPromptStrategy(PromptStrategyInfo strategyInfo) {
    super(strategyInfo);
  }

  protected abstract Shell buildShell(Image icon, String message, ValueSelectionListener<T> valueListener);

  protected Shell createShell(final ValueSelectionListener<T> valueListener) {
    Display display = Display.getDefault();
    Shell parentShell = guessParentShell(display);

    Shell shell;
    int style = SWT.APPLICATION_MODAL | SWT.BORDER | SWT.CLOSE | SWT.DIALOG_TRIM | SWT.RESIZE;
    if (parentShell != null) {
      shell = new Shell(parentShell, style);
      shell.setImage(parentShell.getImage());
    } else {
      shell = new Shell(display, style);
    }
    
    String promptTitle = getPromptTitle();
    if (promptTitle != null)
      shell.setText(promptTitle);

    return shell;
  }

  // Must be called from a display thread
  protected void doPrompt(Image icon, String message, ValueSelectionListener<T> valueListener) {
    Shell shell = buildShell(icon, message, valueListener);

    // Center the shell on the parent window (if any), or primary monitor
    Rectangle parentBounds;
    Composite parent = shell.getParent();
    if (parent != null) {
      parentBounds = parent.getBounds();
    } else {
      parentBounds = shell.getMonitor().getBounds();
    }
    Rectangle shellBounds = shell.getBounds();
    int x = parentBounds.x + (parentBounds.width - shellBounds.width) / 2;
    int y = parentBounds.y + (parentBounds.height - shellBounds.height) / 2;
    shell.setLocation(x, y);

    shell.open();

    Display display = shell.getDisplay();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }

    shell.dispose();
  }

  protected Shell guessParentShell(Display display) {
    Shell guessShell = display.getActiveShell();
    if (guessShell == null) {
      Shell[] shells = display.getShells();
      // TODO: Need better heuristics here?
      if (shells.length > 0) {
        guessShell = shells[0];
      }
    }

    // Find top-level shell
    Shell guessParent = (Shell) guessShell.getParent();
    while (guessParent != null) {
      guessShell = guessParent;
      guessParent = (Shell) guessShell.getParent();
    }

    return guessShell;
  }

  /* (non-Javadoc)
   * @see com.digitalenginesoftware.pdi.steps.prompt.PromptStrategy#prompt(org.eclipse.swt.graphics.Image, java.lang.String)
   */
  @Override
  public PromptResult<T> prompt(final Image icon, final String message) {
    // Collection to hold result from anonymous class
    final ArrayList<PromptResult<T>> results = new ArrayList<PromptResult<T>>(1);
    results.add(null);

    final ValueSelectionListener<T> valueListener = new ValueSelectionListener<T>() {
      @Override
      public void stopRequested() {
        results.set(0, new PromptResult<T>(true, null));
      }

      @Override
      public void valueSelected(T value) {
        results.set(0, new PromptResult<T>(value));
      }
    };

    if (Display.getCurrent() != null) {
      // Already on a display thread
      doPrompt(icon, message, valueListener);
    } else {
      // Not on display thread, invoke on the display thread
      Display.getDefault().syncExec(new Runnable() {
        @Override
        public void run() {
          doPrompt(icon, message, valueListener);
        }
      });
    }

    PromptResult<T> result = results.get(0);
    if (result == null) {
      // Treat user cancellation as a request to stop
      result = new PromptResult<T>(true, null);
    }

    return result;
  }

  protected interface ValueSelectionListener<T> {
    public void stopRequested();

    public void valueSelected(T value);
  }
}
