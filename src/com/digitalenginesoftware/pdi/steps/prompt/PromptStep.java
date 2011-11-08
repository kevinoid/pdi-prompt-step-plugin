/* This file is part of the Pentaho Data Integration Prompt Step Plugin.
 * It is licensed under the terms of the GNU LGPL version 2.1 or later.
 * The complete text of the license is available in the project documentation.
 *
 * Copyright 2011 Digital Engine Software, LLC
 */
package com.digitalenginesoftware.pdi.steps.prompt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.ButtonPromptStrategy;
import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.PromptChoice;
import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.PromptResult;
import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.PromptStrategy;

public class PromptStep extends BaseStep implements StepInterface {
  private static Class<?> PKG = PromptStepMeta.class; // for i18n purposes, needed by Translator2!! // $NON-NLS-1$

  public PromptStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
    super(s, stepDataInterface, c, t, dis);
  }

  protected List<PromptChoice<String>> getChoicesFromStream(PromptStepMeta meta, StreamInterface stream)
      throws KettleException {
    RowSet infoRows = findInputRowSet(stream.getStepname());
    if (infoRows == null) {
      // Occurs when the hop is disabled
      return Collections.emptyList();
    }

    // Note:  Must get a row to ensure the RowMeta is set
    Object[] infoRow = getRowFrom(infoRows);

    RowMetaInterface infoRowMeta = infoRows.getRowMeta();
    if (infoRowMeta == null) {
      return Collections.emptyList();
    }

    int choiceNameIdx = infoRowMeta.indexOfValue(meta.getChoiceNameField());
    if (choiceNameIdx < 0) {
      log.logError(BaseMessages.getString(PKG, "PromptStep.Error.ChoiceNameFieldNotFound", //$NON-NLS-1$
          meta.getChoiceNameField(),
          infoRows.getOriginStepName()));
      return Collections.emptyList();
    }

    int choiceValueIdx = infoRowMeta.indexOfValue(meta.getChoiceValueField());
    if (choiceValueIdx < 0) {
      log.logError(BaseMessages.getString(PKG, "PromptStep.Error.ChoiceValueFieldNotFound", //$NON-NLS-1$
          meta.getChoiceValueField(),
          infoRows.getOriginStepName()));
      return Collections.emptyList();
    }

    ArrayList<PromptChoice<String>> choices = new ArrayList<PromptChoice<String>>();
    for (; infoRow != null; infoRow = getRowFrom(infoRows)) {
      String name = infoRowMeta.getString(infoRow, choiceNameIdx);
      if (name == null) {
        log.logBasic(BaseMessages.getString(PKG, "PromptStep.Warn.SkippingNullChoiceName")); //$NON-NLS-1$
        continue;
      }
      String value = infoRowMeta.getString(infoRow, choiceValueIdx);
      choices.add(new PromptChoice<String>(name, value));
    }

    return choices;
  }

  @Override
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    PromptStepMeta meta = (PromptStepMeta) smi;
    PromptStepData data = (PromptStepData) sdi;

    // Need to get a row to populate InputRowMeta
    Object[] row = getRow();
    if (row == null) {
      setOutputDone();
      return false;
    }

    RowMetaInterface inputRowMeta = getInputRowMeta();
    RowMetaInterface outputRowMeta;
    PromptStrategy<String> promptStrategy;
    if (first) {
      first = false;

      if (inputRowMeta != null)
        outputRowMeta = inputRowMeta.clone();
      else
        outputRowMeta = new RowMeta();

      meta.getFields(outputRowMeta, getStepname(), null, null, this);
      data.setOutputRowMeta(outputRowMeta);

      String promptTypeID = meta.getPromptTypeID();
      if (promptTypeID == null || promptTypeID.length() == 0) {
        log.logBasic(BaseMessages.getString(PKG, "PromptStep.Warn.NoValidPromptTypeFallback")); //$NON-NLS-1$
        promptTypeID = ButtonPromptStrategy.STRATEGY_ID;
      }
      try {
        promptStrategy = PromptStrategy.createStrategy(promptTypeID);
      } catch (IllegalArgumentException ex) {
        log.logBasic(BaseMessages.getString(PKG, "PromptStep.Warn.NoValidPromptTypeFallback")); //$NON-NLS-1$
        promptStrategy = PromptStrategy.createStrategy(PromptStepMeta.DEFAULT_PROMPT_TYPE_ID);
      }
      data.setPromptStrategy(promptStrategy);

      String promptTitle = meta.getPromptTitle();
      if (promptTitle != null)
        promptStrategy.setPromptTitle(environmentSubstitute(promptTitle));

      Image promptIcon = meta.getPromptIcon();
      if (promptIcon != null)
        promptStrategy.setPromptIcon(promptIcon);

      ArrayList<PromptChoice<String>> choices = new ArrayList<PromptChoice<String>>(meta.getPromptChoices());
      for (StreamInterface infoStream : meta.getStepIOMeta().getInfoStreams()) {
        if (infoStream.getStepMeta() != null) {
          choices.addAll(getChoicesFromStream(meta, infoStream));
        }
      }
      promptStrategy.setChoices(choices);
    } else {
      outputRowMeta = data.getOutputRowMeta();
      promptStrategy = data.getPromptStrategy();
    }

    String format = meta.getPromptMessageFormat();
    if (format == null)
      format = ""; //$NON-NLS-1$
    format = environmentSubstitute(format);

    String message;
    try {
      message = String.format(format, row);
    } catch (IllegalFormatException ex) {
      if (log.isBasic())
        log.logBasic(BaseMessages.getString(PKG, "PromptStep.Warn.FormatPromptMessage", ex.getLocalizedMessage())); //$NON-NLS-1$
      message = format;
    }

    PromptResult<String> promptResult = promptStrategy.prompt(message);

    if (promptResult.isStopRequested()) {
      if (log.isDetailed())
        logDetailed(BaseMessages.getString(PKG, "PromptStep.Info.StopRequested")); //$NON-NLS-1$

      setOutputDone();
      return false;
    }

    if (meta.getResultFieldName().length() > 0) {
      int inputRowSize = inputRowMeta == null ? 0 : inputRowMeta.size();
      row = RowDataUtil.addValueData(row, inputRowSize, promptResult.getValue());
    }

    if (log.isRowLevel())
      logRowlevel(BaseMessages.getString(PKG, "InputBase.Log.Row.PutoutRow") + outputRowMeta.getString(row)); //$NON-NLS-1$

    putRow(outputRowMeta, row);

    return true;
  }
}
