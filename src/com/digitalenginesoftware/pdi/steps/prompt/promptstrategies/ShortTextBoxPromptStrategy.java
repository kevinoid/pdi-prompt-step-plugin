/* This file is part of the Pentaho Data Integration Prompt Step Plugin.
 * It is licensed under the terms of the GNU LGPL version 2.1 or later.
 * The complete text of the license is available in the project documentation.
 *
 * Copyright 2011 Digital Engine Software, LLC
 */
package com.digitalenginesoftware.pdi.steps.prompt.promptstrategies;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;

import com.digitalenginesoftware.pdi.steps.prompt.PromptStepMeta;

public class ShortTextBoxPromptStrategy extends SWTPromptStrategy<String> {
  private static Class<?> PKG = PromptStepMeta.class; // for i18n purposes, needed by Translator2!! // $NON-NLS-1$
  
  public static final String STRATEGY_ID = "shorttextbox"; //$NON-NLS-1$

  public static final String STRATEGY_NAME = BaseMessages.getString(PKG, "PromptStep.PromptType.ShortTextBox"); //$NON-NLS-1$

  public static final PromptStrategyInfo STRATEGY_INFO = new PromptStrategyInfo(STRATEGY_ID, STRATEGY_NAME, false) {
    @Override
    public ShortTextBoxPromptStrategy createStrategy() {
      return new ShortTextBoxPromptStrategy();
    }
  };

  protected ShortTextBoxPromptStrategy() {
    super(STRATEGY_INFO);
  }

  @Override
  protected Shell buildShell(Image icon, String message, final ValueSelectionListener<String> valueListener) {
    final Shell shell = createShell(valueListener);
    GridLayout shellLayout = new GridLayout(3, false);
    shellLayout.marginTop = Const.FORM_MARGIN;
    shellLayout.marginRight = Const.FORM_MARGIN;
    shellLayout.marginBottom = Const.FORM_MARGIN;
    shellLayout.marginLeft = Const.FORM_MARGIN;
    shellLayout.horizontalSpacing = Const.MARGIN;
    shellLayout.verticalSpacing = Const.MARGIN;
    shell.setLayout(shellLayout);

    Label iconLabel = new Label(shell, SWT.NONE);
    if (icon != null)
      iconLabel.setImage(icon);
    GridData iconLabelData = new GridData();
    iconLabelData.grabExcessVerticalSpace = true;
    iconLabel.setLayoutData(iconLabelData);

    Label messageLabel = new Label(shell, SWT.NONE);
    messageLabel.setText(message);
    GridData messageLabelData = new GridData();
    messageLabelData.horizontalAlignment = GridData.CENTER;
    messageLabelData.grabExcessVerticalSpace = true;
    messageLabel.setLayoutData(messageLabelData);

    final Text choiceText = new Text(shell, SWT.SINGLE | SWT.BORDER);
    GridData choiceTextData = new GridData();
    choiceTextData.horizontalAlignment = GridData.FILL;
    choiceTextData.grabExcessHorizontalSpace = true;
    choiceTextData.grabExcessVerticalSpace = true;
    choiceText.setLayoutData(choiceTextData);

    Composite buttonComposite = new Composite(shell, SWT.NONE);
    buttonComposite.setLayout(new FillLayout());
    GridData buttonCompositeData = new GridData();
    buttonCompositeData.grabExcessHorizontalSpace = true;
    buttonCompositeData.horizontalAlignment = GridData.FILL;
    buttonCompositeData.horizontalSpan = 3;
    buttonComposite.setLayoutData(buttonCompositeData);

    Button okButton = new Button(buttonComposite, SWT.PUSH);
    okButton.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
    okButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event evt) {
        valueListener.valueSelected(choiceText.getText());
        shell.close();
      }
    });
    shell.setDefaultButton(okButton);

    Button cancelButton = new Button(buttonComposite, SWT.PUSH);
    cancelButton.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
    cancelButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event evt) {
        valueListener.valueSelected(null);
        shell.close();
      }
    });

    shell.pack();

    return shell;
  }
}
