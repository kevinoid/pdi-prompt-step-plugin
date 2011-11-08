/* This file is part of the Pentaho Data Integration Prompt Step Plugin.
 * It is licensed under the terms of the GNU LGPL version 2.1 or later.
 * The complete text of the license is available in the project documentation.
 *
 * Copyright 2011 Digital Engine Software, LLC
 */
package com.digitalenginesoftware.pdi.steps.prompt;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.PromptChoice;
import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.PromptStrategy;
import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.PromptStrategyInfo;
import com.digitalenginesoftware.swt.SWTUtils;

public class PromptStepDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = PromptStepMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private PromptStepMeta stepMeta;

  // Note:  Array index corresponds to position in wPromptType
  private ArrayList<String> promptIconIDs;

  // Note:  Array index corresponds to position in wPromptType
  private ArrayList<PromptStrategyInfo> promptTypes;

  private CCombo wChoiceStep;

  private CCombo wChoiceNameField;

  private CCombo wChoiceValueField;

  private Text wFieldName;

  private Group wInputChoiceGroup;

  private Group wLookGroup;

  private TableView wPromptChoices;

  private Combo wPromptIcon;

  private TextVar wPromptMessage;

  private TextVar wPromptTitle;

  private Combo wPromptType;

  private Group wStaticChoiceGroup;

  public PromptStepDialog(Shell parent, Object stepMeta, TransMeta transMeta, String sname) {
    super(parent, (PromptStepMeta) stepMeta, transMeta, sname);
    this.stepMeta = (PromptStepMeta) stepMeta;
  }

  protected void createBottomButtons(Composite parent) {
    wOK = new Button(parent, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$

    wCancel = new Button(parent, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

    BaseStepDialog.positionBottomButtons(parent, new Button[] {
        wOK,
        wCancel
    }, Const.MARGIN, null);

    // Add listeners
    lsOK = new Listener() {
      @Override
      public void handleEvent(Event e) {
        ok();
      }
    };
    lsCancel = new Listener() {
      @Override
      public void handleEvent(Event e) {
        cancel();
      }
    };

    wOK.addListener(SWT.Selection, lsOK);
    wCancel.addListener(SWT.Selection, lsCancel);
  }

  protected void createMetaControls(Composite parent, ModifyListener lsMod) {
    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Result field name
    Label wlFieldName = new Label(parent, SWT.RIGHT);
    wlFieldName.setText(BaseMessages.getString(PKG, "PromptStep.Options.ResultField")); //$NON-NLS-1$
    props.setLook(wlFieldName);
    FormData fdlFieldName = new FormData();
    fdlFieldName.left = new FormAttachment(0, 0);
    fdlFieldName.top = new FormAttachment(wStepname, margin);
    fdlFieldName.right = new FormAttachment(middle, -margin);
    wlFieldName.setLayoutData(fdlFieldName);

    wFieldName = new Text(parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wFieldName);
    FormData fdFieldName = new FormData();
    fdFieldName.left = new FormAttachment(middle, 0);
    fdFieldName.top = new FormAttachment(wStepname, margin);
    fdFieldName.right = new FormAttachment(100, 0);
    wFieldName.setLayoutData(fdFieldName);

    // Group for options related to the look of the prompt
    wLookGroup = new Group(parent, SWT.NONE);
    wLookGroup.setText(BaseMessages.getString(PKG, "PromptStep.Options.AppearanceGroup")); //$NON-NLS-1$
    props.setLook(wLookGroup);
    FormData fdlLookGroup = new FormData();
    fdlLookGroup.left = new FormAttachment(0, 0);
    fdlLookGroup.top = new FormAttachment(wFieldName, margin);
    fdlLookGroup.right = new FormAttachment(100, 0);
    wLookGroup.setLayoutData(fdlLookGroup);
    FormLayout flLookGroup = new FormLayout();
    flLookGroup.marginWidth = Const.FORM_MARGIN;
    flLookGroup.marginHeight = Const.FORM_MARGIN;
    wLookGroup.setLayout(flLookGroup);

    // Prompt type
    Label wlPromptType = new Label(wLookGroup, SWT.RIGHT);
    wlPromptType.setText(BaseMessages.getString(PKG, "PromptStep.Options.PromptType")); //$NON-NLS-1$
    props.setLook(wlPromptType);
    FormData fdlPromptType = new FormData();
    fdlPromptType.left = new FormAttachment(0, 0);
    fdlPromptType.top = new FormAttachment(0, 0);
    fdlPromptType.right = new FormAttachment(middle, -margin);
    wlPromptType.setLayoutData(fdlPromptType);

    wPromptType = new Combo(wLookGroup, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wPromptType);
    FormData fdPromptType = new FormData();
    fdPromptType.left = new FormAttachment(middle, 0);
    fdPromptType.top = new FormAttachment(0, margin);
    fdPromptType.right = new FormAttachment(100, 0);
    wPromptType.setLayoutData(fdPromptType);

    promptTypes = new ArrayList<PromptStrategyInfo>(PromptStrategy.getStrategyInfos());
    Collections.sort(promptTypes, new Comparator<PromptStrategyInfo>() {
      private final Collator collator = Collator.getInstance();

      @Override
      public int compare(PromptStrategyInfo o1, PromptStrategyInfo o2) {
        if (o1 == null && o2 == null) {
          return 0;
        } else if (o1 == null) {
          return -1;
        } else if (o2 == null) {
          return 1;
        } else {
          return collator.compare(o1.getStrategyName(), o2.getStrategyName());
        }
      }
    });
    for (PromptStrategyInfo promptType : promptTypes)
      wPromptType.add(promptType.getStrategyName());

    // Set defaults
    if (wPromptType.getItemCount() > 0)
      wPromptType.select(0);

    // Prompt title
    Label wlPromptTitle = new Label(wLookGroup, SWT.RIGHT);
    wlPromptTitle.setText(BaseMessages.getString(PKG, "PromptStep.Options.PromptTitle")); //$NON-NLS-1$
    wlPromptTitle.setToolTipText(BaseMessages.getString(PKG, "PromptStep.Options.PromptTitle.Tooltip")); //$NON-NLS-1$
    props.setLook(wlPromptTitle);
    FormData fdlPromptTitle = new FormData();
    fdlPromptTitle.left = new FormAttachment(0, 0);
    fdlPromptTitle.top = new FormAttachment(wPromptType, margin);
    fdlPromptTitle.right = new FormAttachment(middle, -margin);
    wlPromptTitle.setLayoutData(fdlPromptTitle);

    wPromptTitle = new TextVar(transMeta, wLookGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wPromptTitle);
    FormData fdPromptTitle = new FormData();
    fdPromptTitle.left = new FormAttachment(middle, 0);
    fdPromptTitle.top = new FormAttachment(wPromptType, margin);
    fdPromptTitle.right = new FormAttachment(100, 0);
    wPromptTitle.setLayoutData(fdPromptTitle);

    // Prompt message
    Label wlPromptMessage = new Label(wLookGroup, SWT.RIGHT);
    wlPromptMessage.setText(BaseMessages.getString(PKG, "PromptStep.Options.PromptMessage")); //$NON-NLS-1$
    wlPromptMessage.setToolTipText(BaseMessages.getString(PKG, "PromptStep.Options.PromptMessage.Tooltip")); //$NON-NLS-1$
    props.setLook(wlPromptMessage);
    FormData fdlPromptMessage = new FormData();
    fdlPromptMessage.left = new FormAttachment(0, 0);
    fdlPromptMessage.top = new FormAttachment(wPromptTitle, margin);
    fdlPromptMessage.right = new FormAttachment(middle, -margin);
    wlPromptMessage.setLayoutData(fdlPromptMessage);

    wPromptMessage = new TextVar(transMeta, wLookGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wPromptMessage);
    FormData fdPromptMessage = new FormData();
    fdPromptMessage.left = new FormAttachment(middle, 0);
    fdPromptMessage.top = new FormAttachment(wPromptTitle, margin);
    fdPromptMessage.right = new FormAttachment(100, 0);
    wPromptMessage.setLayoutData(fdPromptMessage);

    // Prompt icon
    Label wlPromptIcon = new Label(wLookGroup, SWT.RIGHT);
    wlPromptIcon.setText(BaseMessages.getString(PKG, "PromptStep.Options.PromptIcon")); //$NON-NLS-1$
    props.setLook(wlPromptIcon);
    FormData fdlPromptIcon = new FormData();
    fdlPromptIcon.left = new FormAttachment(0, 0);
    fdlPromptIcon.top = new FormAttachment(wPromptMessage, 0);
    fdlPromptIcon.right = new FormAttachment(middle, -margin);
    wlPromptIcon.setLayoutData(fdlPromptIcon);

    wPromptIcon = new Combo(wLookGroup, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wPromptIcon);
    FormData fdPromptIcon = new FormData();
    fdPromptIcon.left = new FormAttachment(middle, 0);
    fdPromptIcon.top = new FormAttachment(wPromptMessage, margin);
    fdPromptIcon.right = new FormAttachment(100, 0);
    wPromptIcon.setLayoutData(fdPromptIcon);

    ArrayList<Map.Entry<String, String>> promptIconIDNames = new ArrayList<Map.Entry<String, String>>(PromptStepMeta
        .getIconNames().entrySet());
    Collections.sort(promptIconIDNames, new Comparator<Map.Entry<String, String>>() {
      private final Collator collator = Collator.getInstance();

      @Override
      public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
        if (o1 == null && o2 == null) {
          return 0;
        } else if (o1 == null) {
          return -1;
        } else if (o2 == null) {
          return 1;
        } else {
          return collator.compare(o1.getValue(), o2.getValue());
        }
      }
    });

    promptIconIDs = new ArrayList<String>(promptIconIDNames.size() + 1);
    promptIconIDs.add(null);
    wPromptIcon.add(BaseMessages.getString(PKG, "PromptStep.Icons.None")); //$NON-NLS-1$
    for (Map.Entry<String, String> promptIconIDName : promptIconIDNames) {
      promptIconIDs.add(promptIconIDName.getKey());
      wPromptIcon.add(promptIconIDName.getValue());
    }

    if (wPromptIcon.getItemCount() > 0)
      wPromptIcon.select(0);

    // Group for options related to prompt choices read from an input step
    wInputChoiceGroup = new Group(parent, SWT.NONE);
    wInputChoiceGroup.setText(BaseMessages.getString(PKG, "PromptStep.Options.InputChoiceGroup")); //$NON-NLS-1$
    props.setLook(wInputChoiceGroup);
    FormData fdlChoiceGroup = new FormData();
    fdlChoiceGroup.left = new FormAttachment(0, 0);
    fdlChoiceGroup.top = new FormAttachment(wLookGroup, margin);
    fdlChoiceGroup.right = new FormAttachment(100, 0);
    wInputChoiceGroup.setLayoutData(fdlChoiceGroup);
    FormLayout flChoiceGroup = new FormLayout();
    flChoiceGroup.marginWidth = Const.FORM_MARGIN;
    flChoiceGroup.marginHeight = Const.FORM_MARGIN;
    wInputChoiceGroup.setLayout(flChoiceGroup);

    // Choice step
    Label wlChoiceStep = new Label(wInputChoiceGroup, SWT.RIGHT);
    wlChoiceStep.setText(BaseMessages.getString(PKG, "PromptStep.Options.ChoiceStep")); //$NON-NLS-1$
    props.setLook(wlChoiceStep);
    FormData fdlChoiceStep = new FormData();
    fdlChoiceStep.left = new FormAttachment(0, 0);
    fdlChoiceStep.top = new FormAttachment(0, 0);
    fdlChoiceStep.right = new FormAttachment(middle, -margin);
    wlChoiceStep.setLayoutData(fdlChoiceStep);

    wChoiceStep = new CCombo(wInputChoiceGroup, SWT.BORDER);
    props.setLook(wChoiceStep);
    FormData fdChoiceStep = new FormData();
    fdChoiceStep.left = new FormAttachment(middle, 0);
    fdChoiceStep.top = new FormAttachment(0, margin);
    fdChoiceStep.right = new FormAttachment(100, 0);
    wChoiceStep.setLayoutData(fdChoiceStep);

    List<StepMeta> previousSteps = transMeta.findPreviousSteps(transMeta.findStep(stepname));
    for (StepMeta stepMeta : previousSteps) {
      wChoiceStep.add(stepMeta.getName());
    }

    // Choice name field
    Label wlChoiceNameField = new Label(wInputChoiceGroup, SWT.RIGHT);
    wlChoiceNameField.setText(BaseMessages.getString(PKG, "PromptStep.Options.ChoiceNameField")); //$NON-NLS-1$
    props.setLook(wlChoiceNameField);
    FormData fdlChoiceNameField = new FormData();
    fdlChoiceNameField.left = new FormAttachment(0, 0);
    fdlChoiceNameField.top = new FormAttachment(wChoiceStep, margin);
    fdlChoiceNameField.right = new FormAttachment(middle, -margin);
    wlChoiceNameField.setLayoutData(fdlChoiceNameField);

    wChoiceNameField = new CCombo(wInputChoiceGroup, SWT.BORDER);
    props.setLook(wChoiceNameField);
    FormData fdChoiceNameField = new FormData();
    fdChoiceNameField.left = new FormAttachment(middle, 0);
    fdChoiceNameField.top = new FormAttachment(wChoiceStep, margin);
    fdChoiceNameField.right = new FormAttachment(100, 0);
    wChoiceNameField.setLayoutData(fdChoiceNameField);

    // Choice value field
    Label wlChoiceValueField = new Label(wInputChoiceGroup, SWT.RIGHT);
    wlChoiceValueField.setText(BaseMessages.getString(PKG, "PromptStep.Options.ChoiceValueField")); //$NON-NLS-1$
    props.setLook(wlChoiceValueField);
    FormData fdlChoiceValueField = new FormData();
    fdlChoiceValueField.left = new FormAttachment(0, 0);
    fdlChoiceValueField.top = new FormAttachment(wChoiceNameField, margin);
    fdlChoiceValueField.right = new FormAttachment(middle, -margin);
    wlChoiceValueField.setLayoutData(fdlChoiceValueField);

    wChoiceValueField = new CCombo(wInputChoiceGroup, SWT.BORDER);
    props.setLook(wChoiceValueField);
    FormData fdChoiceValueField = new FormData();
    fdChoiceValueField.left = new FormAttachment(middle, 0);
    fdChoiceValueField.top = new FormAttachment(wChoiceNameField, margin);
    fdChoiceValueField.right = new FormAttachment(100, 0);
    wChoiceValueField.setLayoutData(fdChoiceValueField);

    // Group for options related to statically-defined prompt choices
    wStaticChoiceGroup = new Group(parent, SWT.NONE);
    wStaticChoiceGroup.setText(BaseMessages.getString(PKG, "PromptStep.Options.StaticChoiceGroup")); //$NON-NLS-1$
    props.setLook(wStaticChoiceGroup);
    FormData fdlStaticChoiceGroup = new FormData();
    fdlStaticChoiceGroup.left = new FormAttachment(0, 0);
    fdlStaticChoiceGroup.top = new FormAttachment(wInputChoiceGroup, margin);
    fdlStaticChoiceGroup.right = new FormAttachment(100, 0);
    wStaticChoiceGroup.setLayoutData(fdlStaticChoiceGroup);
    FormLayout flStaticChoiceGroup = new FormLayout();
    flStaticChoiceGroup.marginWidth = Const.FORM_MARGIN;
    flStaticChoiceGroup.marginHeight = Const.FORM_MARGIN;
    wStaticChoiceGroup.setLayout(flStaticChoiceGroup);

    // Prompt choices
    Label wlPromptChoices = new Label(wStaticChoiceGroup, SWT.RIGHT);
    wlPromptChoices.setText(BaseMessages.getString(PKG, "PromptStep.Options.StaticChoices")); //$NON-NLS-1$
    wlPromptChoices.setToolTipText(BaseMessages.getString(PKG, "PromptStep.Options.StaticChoices.Tooltip")); //$NON-NLS-1$
    props.setLook(wlPromptChoices);
    FormData fdlPromptChoices = new FormData();
    fdlPromptChoices.left = new FormAttachment(0, 0);
    fdlPromptChoices.top = new FormAttachment(0, 0);
    fdlPromptChoices.right = new FormAttachment(middle, -margin);
    wlPromptChoices.setLayoutData(fdlPromptChoices);

    wPromptChoices = new TableView(transMeta, wStaticChoiceGroup, SWT.FULL_SELECTION | SWT.MULTI, new ColumnInfo[] {
        new ColumnInfo(BaseMessages.getString(PKG, "PromptStep.Options.StaticChoices.ChoiceName"), //$NON-NLS-1$
            ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "PromptStep.Options.StaticChoices.ChoiceValue"), //$NON-NLS-1$
            ColumnInfo.COLUMN_TYPE_TEXT, false)
    }, 1, lsMod, props);
    props.setLook(wPromptChoices);
    FormData fdPromptChoices = new FormData();
    fdPromptChoices.left = new FormAttachment(middle, 0);
    fdPromptChoices.top = new FormAttachment(0, 0);
    fdPromptChoices.right = new FormAttachment(100, 0);
    fdPromptChoices.bottom = new FormAttachment(100, 0);
    wPromptChoices.setLayoutData(fdPromptChoices);

    // Setup listeners
    // Add default modify listeners
    wChoiceStep.addModifyListener(lsMod);
    wChoiceNameField.addModifyListener(lsMod);
    wChoiceValueField.addModifyListener(lsMod);
    wFieldName.addModifyListener(lsMod);
    //wPromptChoices.addModifyListener(lsMod);
    wPromptMessage.addModifyListener(lsMod);
    wPromptTitle.addModifyListener(lsMod);
    wPromptType.addModifyListener(lsMod);

    // Reload the choice fields when the choice step changes
    wChoiceStep.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent evt) {
        loadChoiceFieldsFromStep(((CCombo) evt.widget).getText());
      }
    });

    // Enable/disable controls appropriate to the selected prompt type
    wPromptType.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        updateChoiceEnabled();
      }
    });
  }

  protected void createStepname(Shell parent, ModifyListener lsMod) {
    int margin = Const.MARGIN;
    int middle = props.getMiddlePct();

    wlStepname = new Label(parent, SWT.RIGHT);
    wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); //$NON-NLS-1$
    props.setLook(wlStepname);
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment(0, 0);
    fdlStepname.top = new FormAttachment(0, margin);
    fdlStepname.right = new FormAttachment(middle, -margin);
    wlStepname.setLayoutData(fdlStepname);
    wStepname = new Text(parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wStepname.setText(stepname);
    props.setLook(wStepname);
    wStepname.addModifyListener(lsMod);
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment(middle, 0);
    fdStepname.top = new FormAttachment(0, margin);
    fdStepname.right = new FormAttachment(100, 0);
    wStepname.setLayoutData(fdStepname);
  }

  protected Shell createShell(Shell parent) {
    Shell newShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
    props.setLook(newShell);

    setShellImage(newShell, stepMeta);

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    newShell.setLayout(formLayout);
    newShell.setText(getTitle());

    // Create listeners
    ModifyListener lsMod = new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        baseStepMeta.setChanged();
      }
    };
    changed = baseStepMeta.hasChanged();

    createStepname(newShell, lsMod);

    createMetaControls(newShell, lsMod);

    createBottomButtons(newShell);
    newShell.setDefaultButton(wOK);

    // FIXME:  Ugh.  This needs to find a better place and not depend on the layout
    ((FormData) wStaticChoiceGroup.getLayoutData()).bottom = new FormAttachment(wOK, -Const.MARGIN);

    // Detect X or ALT-F4 or something that kills this window...
    newShell.addShellListener(new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    loadFrom(stepMeta);

    setSize(newShell);

    baseStepMeta.setChanged(changed);

    return newShell;
  }

  protected String[] getFieldNamesFromStep(String stepName) {
    StepMeta choiceStep = transMeta.findStep(stepName);
    if (choiceStep == null)
      return new String[0];

    StepMeta promptStep = stepMeta.getParentStepMeta();
    if (promptStep == null)
      return new String[0];

    RowMetaInterface rowMeta;
    try {
      rowMeta = transMeta.getStepFields(choiceStep, promptStep, null);
    } catch (KettleStepException e) {
      return new String[0];
    }

    return rowMeta.getFieldNames();
  }

  protected List<PromptChoice<String>> getChoicesFromTable() {
    int nrNonEmpty = wPromptChoices.nrNonEmpty();
    ArrayList<PromptChoice<String>> choices = new ArrayList<PromptChoice<String>>(nrNonEmpty);
    for (int i = 0; i < nrNonEmpty; ++i) {
      TableItem item = wPromptChoices.getNonEmpty(i);
      String name = item.getText(1);
      String value = item.getText(2);
      if (value.length() == 0)
        value = null;
      choices.add(new PromptChoice<String>(name, value));
    }
    return choices;
  }

  protected void setChoicesToTable(List<PromptChoice<String>> choices) {
    wPromptChoices.removeAll();
    for (PromptChoice<String> choice : choices) {
      String name = choice.getName();
      String value = choice.getValue();
      String[] itemText = new String[] {
          name,
          value == null ? "" : value //$NON-NLS-1$
      };
      wPromptChoices.add(itemText);
    }
    if (wPromptChoices.getItemCount() > 1) {
      // Remove blank TableItem added by removeAll()
      wPromptChoices.remove(0);
    }
    wPromptChoices.setRowNums();
    wPromptChoices.optWidth(true);
  }

  protected String getTitle() {
    return BaseMessages.getString(PKG, "PromptStep.StepName"); //$NON-NLS-1$
  }

  protected void updateChoiceEnabled() {
    PromptStrategyInfo promptType = promptTypes.get(wPromptType.getSelectionIndex());
    if (promptType.usesChoices()) {
      SWTUtils.setDescendantsEnabled(wInputChoiceGroup, true);
      SWTUtils.setDescendantsEnabled(wStaticChoiceGroup, true);
    } else {
      SWTUtils.setDescendantsEnabled(wInputChoiceGroup, false);
      SWTUtils.setDescendantsEnabled(wStaticChoiceGroup, false);
    }
  }

  protected void loadFrom(PromptStepMeta targetStepMeta) {
    // Extracted to variable to minimize NON-NLS comments 
    String EMPTY = ""; //$NON-NLS-1$
    
    String fieldName = targetStepMeta.getResultFieldName();
    String promptID = targetStepMeta.getPromptTypeID();
    int promptTypeInd = -1;
    try {
      PromptStrategyInfo promptType = PromptStrategy.getStrategyInfo(promptID);
      for (int i = 0; i < promptTypes.size(); ++i) {
        if (promptTypes.get(i).equals(promptType)) {
          promptTypeInd = i;
          break;
        }
      }
    } catch (IllegalArgumentException ex) {
      // Bad strategy ID got loaded, ignore it and keep default value
    }
    String promptTitle = targetStepMeta.getPromptTitle();
    String message = targetStepMeta.getPromptMessageFormat();
    String iconID = targetStepMeta.getPromptIconID();
    String iconName = EMPTY;
    if (iconID != null && iconID.length() > 0)
      iconName = PromptStepMeta.getIconName(iconID);
    StepIOMetaInterface stepIOMeta = targetStepMeta.getStepIOMeta();
    String[] infoStepnames = stepIOMeta.getInfoStepnames();
    String infoStepname = infoStepnames.length > 0 ? infoStepnames[0] : null;
    String choiceNameField = targetStepMeta.getChoiceNameField();
    String choiceValueField = targetStepMeta.getChoiceValueField();
    List<PromptChoice<String>> choices = targetStepMeta.getPromptChoices();

    wFieldName.setText(fieldName == null ? EMPTY : fieldName);
    if (promptTypeInd >= 0)
      wPromptType.select(promptTypeInd);
    wPromptMessage.setText(message == null ? EMPTY : message);
    wPromptTitle.setText(promptTitle == null ? EMPTY : promptTitle);
    wPromptIcon.setText(iconName);
    wChoiceStep.setText(infoStepname == null ? EMPTY : infoStepname);
    wChoiceNameField.setText(choiceNameField == null ? EMPTY : choiceNameField);
    wChoiceValueField.setText(choiceValueField == null ? EMPTY : choiceValueField);
    setChoicesToTable(choices);

    updateChoiceEnabled();
  }

  protected void loadChoiceFieldsFromStep(String stepName) {
    String[] choiceFieldNames = getFieldNamesFromStep(stepName);
    Arrays.sort(choiceFieldNames);

    wChoiceNameField.setItems(choiceFieldNames);
    wChoiceValueField.setItems(choiceFieldNames);
  }

  protected void saveTo(PromptStepMeta targetStepMeta) {
    PromptStrategyInfo promptType = promptTypes.get(wPromptType.getSelectionIndex());
    String promptTypeID = promptType.getStrategyID();
    List<PromptChoice<String>> choices = getChoicesFromTable();
    String choiceStepName = wChoiceStep.getText();
    String promptIconID = null;
    if (wPromptIcon.getSelectionIndex() > 0)
      promptIconID = promptIconIDs.get(wPromptIcon.getSelectionIndex());

    targetStepMeta.setResultFieldName(wFieldName.getText());
    targetStepMeta.setPromptTypeID(promptTypeID);
    targetStepMeta.setPromptMessage(wPromptMessage.getText());
    targetStepMeta.setPromptTitle(wPromptTitle.getText());
    targetStepMeta.setPromptIconID(promptIconID);
    targetStepMeta.setChoiceNameField(wChoiceNameField.getText());
    targetStepMeta.setChoiceValueField(wChoiceValueField.getText());

    StepIOMetaInterface stepIOMeta = targetStepMeta.getStepIOMeta();
    StepMeta choiceStepMeta = null;
    if (choiceStepName.length() > 0) {
      choiceStepMeta = transMeta.findStep(choiceStepName);
      // FIXME:  Warn here?  TableInput doesn't.
    }
    // Note:  Can't change number of InfoSteps, set null when unused
    stepIOMeta.setInfoSteps(new StepMeta[] {
      choiceStepMeta
    });

    targetStepMeta.setPromptChoices(choices);
  }

  protected PromptStepMeta getPreviewMeta() {
    PromptStepMeta previewMeta = stepMeta.clone();

    saveTo(previewMeta);

    return previewMeta;
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();
    shell = createShell(parent);

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }

    return stepname;
  }

  protected void cancel() {
    stepname = null;
    baseStepMeta.setChanged(changed);
    dispose();
  }

  protected void ok() {
    saveTo(stepMeta);
    stepname = wStepname.getText();
    dispose();
  }

  protected void preview() {
    StepMetaInterface previewMeta = getPreviewMeta();

    TransMeta previewTransMeta = TransPreviewFactory.generatePreviewTransformation(
        transMeta,
        previewMeta,
        wStepname.getText());

    EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(),
        BaseMessages.getString(PKG, "InputBaseDialog.Preview.SizeDialog.Title"), //$NON-NLS-1$
        BaseMessages.getString(PKG, "InputBaseDialog.Preview.SizeDialog.Message")); //$NON-NLS-1$
    int previewSize = numberDialog.open();
    if (previewSize > 0) {
      TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewTransMeta, new String[] {
        wStepname.getText()
      }, new int[] {
        previewSize
      });
      progressDialog.open();

      Trans trans = progressDialog.getTrans();
      String loggingText = progressDialog.getLoggingText();

      if (!progressDialog.isCancelled()) {
        if (trans.getResult() != null && trans.getResult().getNrErrors() > 0) {
          EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(
              PKG,
              "System.Dialog.PreviewError.Title"), //$NON-NLS-1$
              BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), //$NON-NLS-1$
              loggingText, true);
          etd.setReadOnly();
          etd.open();
        }
      }

      PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(),
          progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()),
          loggingText);
      prd.open();
    }
  }
}
