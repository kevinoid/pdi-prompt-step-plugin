/* This file is part of the Pentaho Data Integration Prompt Step Plugin.
 * It is licensed under the terms of the GNU LGPL version 2.1 or later.
 * The complete text of the license is available in the project documentation.
 *
 * Copyright 2011 Digital Engine Software, LLC
 */
package com.digitalenginesoftware.swt;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class SWTUtils {
  // Suppress default constructor to prevent instantiation
  private SWTUtils() {
  }

  public static void setDescendantsEnabled(Control control, boolean enabled) {
    control.setEnabled(enabled);

    if (control instanceof Composite) {
      Composite comp = (Composite) control;
      for (Control child : comp.getChildren()) {
        setDescendantsEnabled(child, enabled);
      }
    }
  }
}
