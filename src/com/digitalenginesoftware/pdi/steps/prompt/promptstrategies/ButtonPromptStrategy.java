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
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;

import com.digitalenginesoftware.pdi.steps.prompt.PromptStepMeta;

public class ButtonPromptStrategy<T> extends SWTPromptStrategy<T> {
  private static Class<?> PKG = PromptStepMeta.class; // for i18n purposes, needed by Translator2!! // $NON-NLS-1$

  public static final String STRATEGY_ID = "button"; //$NON-NLS-1$

  public static final String STRATEGY_NAME = BaseMessages.getString(PKG, "PromptStep.PromptType.Button"); //$NON-NLS-1$

  public static final PromptStrategyInfo STRATEGY_INFO = new PromptStrategyInfo(STRATEGY_ID, STRATEGY_NAME, true) {
    @Override
    public ButtonPromptStrategy<String> createStrategy() {
      return new ButtonPromptStrategy<String>();
    }
  };

  protected ButtonPromptStrategy() {
    super(STRATEGY_INFO);
  }

  @Override
  protected Shell buildShell(Image icon, String message, final ValueSelectionListener<T> valueListener) {
    final Shell shell = createShell(valueListener);
    GridLayout shellLayout = new GridLayout(2, false);
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
    iconLabelData.horizontalAlignment = GridData.CENTER;
    iconLabelData.grabExcessHorizontalSpace = true;
    iconLabelData.grabExcessVerticalSpace = true;
    iconLabel.setLayoutData(iconLabelData);

    Label messageLabel = new Label(shell, SWT.NONE);
    messageLabel.setText(message);
    GridData messageLabelData = new GridData();
    messageLabelData.horizontalAlignment = GridData.CENTER;
    messageLabelData.grabExcessHorizontalSpace = true;
    messageLabelData.grabExcessVerticalSpace = true;
    messageLabel.setLayoutData(messageLabelData);

    Composite buttonComposite = new Composite(shell, SWT.NONE);
    buttonComposite.setLayout(new FillLayout());
    GridData buttonCompositeData = new GridData();
    buttonCompositeData.grabExcessHorizontalSpace = true;
    buttonCompositeData.horizontalAlignment = GridData.FILL;
    buttonCompositeData.horizontalSpan = 2;
    buttonComposite.setLayoutData(buttonCompositeData);

    Listener choiceListener = new Listener() {
      @SuppressWarnings("unchecked")
      public void handleEvent(Event event) {
        valueListener.valueSelected((T) event.widget.getData());
        shell.close();
      }
    };

    for (PromptChoice<T> choice : getChoices()) {
      Button choiceButton = new Button(buttonComposite, SWT.PUSH);
      choiceButton.setText(choice.getName());
      choiceButton.setData(choice.getValue());
      choiceButton.addListener(SWT.Selection, choiceListener);
    }

    shell.pack();

    return shell;
  }
}
