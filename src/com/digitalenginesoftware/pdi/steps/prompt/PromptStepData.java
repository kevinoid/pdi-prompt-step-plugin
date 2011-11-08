/* This file is part of the Pentaho Data Integration Prompt Step Plugin.
 * It is licensed under the terms of the GNU LGPL version 2.1 or later.
 * The complete text of the license is available in the project documentation.
 *
 * Copyright 2011 Digital Engine Software, LLC
 */
package com.digitalenginesoftware.pdi.steps.prompt;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.PromptStrategy;

public class PromptStepData extends BaseStepData implements StepDataInterface {

  private RowMetaInterface outputRowMeta;

  private PromptStrategy<String> promptStrategy;

  public RowMetaInterface getOutputRowMeta() {
    return outputRowMeta;
  }

  public void setOutputRowMeta(RowMetaInterface outputRowMeta) {
    this.outputRowMeta = outputRowMeta;
  }

  public PromptStrategy<String> getPromptStrategy() {
    return promptStrategy;
  }

  public void setPromptStrategy(PromptStrategy<String> promptStrategy) {
    this.promptStrategy = promptStrategy;
  }
}
