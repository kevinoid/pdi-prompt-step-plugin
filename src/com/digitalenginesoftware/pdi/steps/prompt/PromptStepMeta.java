/* This file is part of the Pentaho Data Integration Prompt Step Plugin.
 * It is licensed under the terms of the GNU LGPL version 2.1 or later.
 * The complete text of the license is available in the project documentation.
 *
 * Copyright 2011 Digital Engine Software, LLC
 */
package com.digitalenginesoftware.pdi.steps.prompt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.w3c.dom.Node;

import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.ButtonPromptStrategy;
import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.PromptChoice;
import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.PromptStrategy;
import com.digitalenginesoftware.pdi.steps.prompt.promptstrategies.PromptStrategyInfo;

@Step(id = "PromptStep", image = "prompt-step-icon.png", i18nPackageName = "com.digitalenginesoftware.pdi.steps.prompt", name = "PromptStep.StepName", description = "PromptStep.StepDesc", categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Utility")
public class PromptStepMeta extends BaseStepMeta implements Cloneable, StepMetaInterface {
  private static Class<?> PKG = PromptStepMeta.class; // for i18n purposes, needed by Translator2!! // $NON-NLS-1$
  
  public static final String DEFAULT_PROMPT_TYPE_ID = ButtonPromptStrategy.STRATEGY_ID;

  private static final String DEFAULT_RESULT_FIELD_NAME = "PromptResult"; //$NON-NLS-1$

  private static final HashMap<String, String> ICON_ID_TO_NAME;

  private static final HashMap<String, Integer> ICON_ID_TO_INT;

  static {
    ICON_ID_TO_NAME = new HashMap<String, String>(5);
    ICON_ID_TO_NAME.put("error", BaseMessages.getString(PKG, "PromptStep.Icons.Error")); //$NON-NLS-1$ //$NON-NLS-2$
    ICON_ID_TO_NAME.put("information", BaseMessages.getString(PKG, "PromptStep.Icons.Information")); //$NON-NLS-1$ //$NON-NLS-2$
    ICON_ID_TO_NAME.put("question", BaseMessages.getString(PKG, "PromptStep.Icons.Question")); //$NON-NLS-1$ //$NON-NLS-2$
    ICON_ID_TO_NAME.put("warning", BaseMessages.getString(PKG, "PromptStep.Icons.Warning")); //$NON-NLS-1$ //$NON-NLS-2$
    ICON_ID_TO_NAME.put("working", BaseMessages.getString(PKG, "PromptStep.Icons.Working")); //$NON-NLS-1$ //$NON-NLS-2$

    ICON_ID_TO_INT = new HashMap<String, Integer>();
    ICON_ID_TO_INT.put("error", SWT.ICON_ERROR); //$NON-NLS-1$
    ICON_ID_TO_INT.put("information", SWT.ICON_INFORMATION); //$NON-NLS-1$
    ICON_ID_TO_INT.put("question", SWT.ICON_QUESTION); //$NON-NLS-1$
    ICON_ID_TO_INT.put("warning", SWT.ICON_WARNING); //$NON-NLS-1$
    ICON_ID_TO_INT.put("working", SWT.ICON_WORKING); //$NON-NLS-1$
  }

  public static Set<String> getIconIDs() {
    return ICON_ID_TO_NAME.keySet();
  }

  public static String getIconName(String iconID) {
    String iconName = ICON_ID_TO_NAME.get(iconID);
    if (iconName == null)
      throw new IllegalArgumentException("Unrecognized iconName"); //$NON-NLS-1$

    return iconName;
  }

  public static Map<String, String> getIconNames() {
    return ICON_ID_TO_NAME;
  }

  private String choiceNameField;

  private String choiceValueField;

  private List<PromptChoice<String>> promptChoices;

  private String resultFieldName;

  private String promptTypeID;

  private String promptIconID;

  private String promptMessageFormat;

  private String promptTitle;

  public PromptStepMeta() {
    super();
    setDefault();
  }

  @Override
  public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space) {
    if (resultFieldName.length() > 0) {
      ValueMeta resultField = new ValueMeta(resultFieldName, ValueMeta.TYPE_STRING);
      resultField.setOrigin(origin);
      r.addValueMeta(resultField);
    }
  }

  @Override
  public PromptStepMeta clone() {
    PromptStepMeta clone = (PromptStepMeta) super.clone();
    clone.promptChoices = new ArrayList<PromptChoice<String>>(promptChoices);
    return clone;
  }

  public String getChoiceNameField() {
    return choiceNameField;
  }

  public void setChoiceNameField(String choiceNameField) {
    this.choiceNameField = choiceNameField;
  }

  public String getChoiceValueField() {
    return choiceValueField;
  }

  public void setChoiceValueField(String choiceValueField) {
    this.choiceValueField = choiceValueField;
  }

  public List<PromptChoice<String>> getPromptChoices() {
    return Collections.unmodifiableList(promptChoices);
  }

  public void setPromptChoices(List<? extends PromptChoice<String>> promptChoices) {
    this.promptChoices = new ArrayList<PromptChoice<String>>(promptChoices);
  }

  public String getPromptTypeID() {
    return promptTypeID;
  }

  public void setPromptTypeID(String promptTypeID) {
    this.promptTypeID = promptTypeID;
  }

  public Image getPromptIcon() {
    if (promptIconID == null || promptIconID.length() == 0)
      return null;

    final Integer iconIDInt = ICON_ID_TO_INT.get(promptIconID);
    if (iconIDInt == null)
      return null;

    final Image[] icon = new Image[1];
    final Display display = Display.getDefault();
    display.syncExec(new Runnable() {
      @Override
      public void run() {
        icon[0] = display.getSystemImage(iconIDInt);
      }
    });
    return icon[0];
  }

  public String getPromptIconID() {
    return promptIconID;
  }

  public void setPromptIconID(String promptIconID) {
    this.promptIconID = promptIconID;
  }

  public String getPromptMessageFormat() {
    return promptMessageFormat;
  }

  public void setPromptMessage(String promptMessage) {
    this.promptMessageFormat = promptMessage;
  }

  public String getPromptTitle() {
    return promptTitle;
  }

  public void setPromptTitle(String promptTitle) {
    this.promptTitle = promptTitle;
  }

  public String getResultFieldName() {
    return resultFieldName;
  }

  public void setResultFieldName(String resultFieldName) {
    if (resultFieldName == null)
      resultFieldName = ""; //$NON-NLS-1$
    this.resultFieldName = resultFieldName;
  }

  @Override
  public String getXML() throws KettleException {
    String tagIndent = "    "; //$NON-NLS-1$
    String tagIndent2 = tagIndent + tagIndent;
    String tagIndent3 = tagIndent2 + tagIndent;

    // TODO: Look into replacing this with built-in XML serialization
    StringBuffer retval = new StringBuffer();

    retval.append(tagIndent).append(XMLHandler.addTagValue("choice_name_field", choiceNameField)); //$NON-NLS-1$
    retval.append(tagIndent).append(XMLHandler.addTagValue("choice_value_field", choiceValueField)); //$NON-NLS-1$
    retval.append(tagIndent).append(XMLHandler.addTagValue("result_field", resultFieldName)); //$NON-NLS-1$
    retval.append(tagIndent).append(XMLHandler.addTagValue("prompt_type", promptTypeID)); //$NON-NLS-1$
    retval.append(tagIndent).append(XMLHandler.addTagValue("prompt_icon", promptIconID)); //$NON-NLS-1$
    retval.append(tagIndent).append(XMLHandler.addTagValue("prompt_message", promptMessageFormat)); //$NON-NLS-1$
    retval.append(tagIndent).append(XMLHandler.addTagValue("prompt_title", promptTitle)); //$NON-NLS-1$

    retval.append(tagIndent).append("<prompt_choices>").append(Const.CR); //$NON-NLS-1$
    for (PromptChoice<String> choice : promptChoices) {
      retval.append(tagIndent2).append("<prompt_choice>").append(Const.CR); //$NON-NLS-1$
      retval.append(tagIndent3).append(XMLHandler.addTagValue("name", choice.getName())); //$NON-NLS-1$
      retval.append(tagIndent3).append(XMLHandler.addTagValue("value", choice.getValue())); //$NON-NLS-1$
      retval.append(tagIndent2).append("</prompt_choice>").append(Const.CR); //$NON-NLS-1$
    }
    retval.append(tagIndent).append("</prompt_choices>").append(Const.CR); //$NON-NLS-1$

    StreamInterface infoStream = getStepIOMeta().getInfoStreams().get(0);
    retval.append(tagIndent).append(XMLHandler.addTagValue("choice_step_name", infoStream.getStepname())); //$NON-NLS-1$

    return retval.toString();
  }

  @Override
  public void loadXML(Node stepnode, List<DatabaseMeta> loadDatabases, Map<String, Counter> counters)
      throws KettleXMLException {

    choiceNameField = XMLHandler.getTagValue(stepnode, "choice_name_field"); //$NON-NLS-1$
    choiceValueField = XMLHandler.getTagValue(stepnode, "choice_value_field"); //$NON-NLS-1$
    resultFieldName = XMLHandler.getTagValue(stepnode, "result_field"); //$NON-NLS-1$
    promptTypeID = XMLHandler.getTagValue(stepnode, "prompt_type"); //$NON-NLS-1$
    promptIconID = XMLHandler.getTagValue(stepnode, "prompt_icon"); //$NON-NLS-1$
    promptMessageFormat = XMLHandler.getTagValue(stepnode, "prompt_message"); //$NON-NLS-1$
    promptTitle = XMLHandler.getTagValue(stepnode, "prompt_title"); //$NON-NLS-1$

    promptChoices = new ArrayList<PromptChoice<String>>();
    Node choicesNode = XMLHandler.getSubNode(stepnode, "prompt_choices"); //$NON-NLS-1$
    for (Node choiceNode = choicesNode == null ? null : choicesNode.getFirstChild(); choiceNode != null; choiceNode = choiceNode
        .getNextSibling()) {
      String choiceTagName = choiceNode.getNodeName();
      if (!choiceTagName.equals("prompt_choice")) { //$NON-NLS-1$
        continue;
      }

      String choiceName = null;
      String choiceValue = null;
      for (Node choicePartNode = choiceNode == null ? null : choiceNode.getFirstChild(); choicePartNode != null; choicePartNode = choicePartNode
          .getNextSibling()) {
        String partNodeName = choicePartNode.getNodeName();
        String partNodeValue = XMLHandler.getNodeValue(choicePartNode);

        if (partNodeName.equals("name")) { //$NON-NLS-1$
          choiceName = partNodeValue;
        } else if (partNodeName.equals("value")) { //$NON-NLS-1$
          choiceValue = partNodeValue;
        }
      }
      if (choiceName != null) {
        promptChoices.add(new PromptChoice<String>(choiceName, choiceValue));
      }
    }

    String choiceStepname = XMLHandler.getTagValue(stepnode, "choice_step_name"); //$NON-NLS-1$
    StreamInterface infoStream = getStepIOMeta().getInfoStreams().get(0);
    // Set as subject.  Resolved to StepMeta by searchInfoAndTargetSteps
    infoStream.setSubject(choiceStepname);
  }

  @Override
  public void setDefault() {
    String EMPTY = ""; //$NON-NLS-1$

    choiceNameField = EMPTY;
    choiceValueField = EMPTY;
    promptChoices = Collections.emptyList();
    resultFieldName = DEFAULT_RESULT_FIELD_NAME;
    promptTypeID = null;
    promptIconID = null;
    promptMessageFormat = EMPTY;
    promptTitle = EMPTY;
  }

  @Override
  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> loadDatabases, Map<String, Counter> counters)
      throws KettleException {
    choiceNameField = rep.getStepAttributeString(id_step, "choice_name_field"); //$NON-NLS-1$
    choiceValueField = rep.getStepAttributeString(id_step, "choice_value_field"); //$NON-NLS-1$
    resultFieldName = rep.getStepAttributeString(id_step, "result_field"); //$NON-NLS-1$
    promptTypeID = rep.getStepAttributeString(id_step, "prompt_type"); //$NON-NLS-1$
    promptIconID = rep.getStepAttributeString(id_step, "prompt_icon"); //$NON-NLS-1$
    promptMessageFormat = rep.getStepAttributeString(id_step, "prompt_message"); //$NON-NLS-1$
    promptTitle = rep.getStepAttributeString(id_step, "prompt_title"); //$NON-NLS-1$

    int nrChoices = rep.countNrStepAttributes(id_step, "prompt_choice_name"); //$NON-NLS-1$
    promptChoices = new ArrayList<PromptChoice<String>>(nrChoices);
    for (int i = 0; i < nrChoices; ++i) {
      String choiceName = rep.getStepAttributeString(id_step, i, "prompt_choice_name"); //$NON-NLS-1$
      String choiceValue = rep.getStepAttributeString(id_step, i, "prompt_choice_value"); //$NON-NLS-1$

      if (choiceName != null) {
        promptChoices.add(new PromptChoice<String>(choiceName, choiceValue));
      }
    }

    String choiceStepname = rep.getStepAttributeString(id_step, "choice_step_name"); //$NON-NLS-1$
    StreamInterface infoStream = getStepIOMeta().getInfoStreams().get(0);
    // Set as subject.  Resolved to StepMeta by searchInfoAndTargetSteps
    infoStream.setSubject(choiceStepname);
  }

  @Override
  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
    rep.saveStepAttribute(id_transformation, id_step, "choice_name_field", choiceNameField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "choice_value_field", choiceValueField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "result_field", resultFieldName); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "prompt_type", promptTypeID); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "prompt_icon", promptIconID); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "prompt_message", promptMessageFormat); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "prompt_title", promptTitle); //$NON-NLS-1$

    for (int i = 0; i < promptChoices.size(); ++i) {
      PromptChoice<String> choice = promptChoices.get(i);
      rep.saveStepAttribute(id_transformation, id_step, i, "prompt_choice_name", choice.getName()); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, i, "prompt_choice_value", choice.getValue()); //$NON-NLS-1$
    }

    StreamInterface infoStream = getStepIOMeta().getInfoStreams().get(0);
    rep.saveStepAttribute(id_transformation, id_step, "choice_step_name", infoStream.getStepname()); //$NON-NLS-1$
  }

  @Override
  public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev,
      String input[], String output[], RowMetaInterface info) {

    if (resultFieldName == null || resultFieldName.length() == 0) {
      remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG,
          "PromptStep.Log.Warn.ResultFieldUnspecified"), stepMeta)); //$NON-NLS-1$
    } else if (prev.indexOfValue(resultFieldName) >= 0) {
      remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG,
          "PromptStep.Log.Warn.ResultFieldDuplicate"), stepMeta)); //$NON-NLS-1$
    }

    PromptStrategyInfo promptType = null;
    if (promptTypeID == null || promptTypeID.length() == 0) {
      remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG,
          "PromptStep.Log.Warn.PromptTypeUnspecified"), stepMeta)); //$NON-NLS-1$
      
      promptType = PromptStrategy.getStrategyInfo(DEFAULT_PROMPT_TYPE_ID);
    } else {
      promptType = PromptStrategy.getStrategyInfo(getPromptTypeID());
    }

    if (promptMessageFormat == null || promptMessageFormat.length() == 0) {
      remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG,
          "PromptStep.Log.Warn.PromptMessageUnspecified"), stepMeta)); //$NON-NLS-1$
    } else {
      try {
        Object[] dummyRow = new Object[prev.size()];
        String.format(promptMessageFormat, dummyRow);
      } catch (IllegalFormatConversionException ex) {
        // Ignore.  dummyRow does not contain the correct Java types
      } catch (IllegalFormatException ex) {
        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
            PKG,
            "PromptStep.Log.Warn.PromptMessageFormatInvalid"), ex.getLocalizedMessage(), stepMeta)); //$NON-NLS-1$
      }
    }

    List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
    if (infoStreams.get(0).getStepMeta() != null) {
      // Accepting choices from input
      if (choiceNameField == null || choiceNameField.length() == 0) {
        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG,
            "PromptStep.Log.Error.ChoiceNameFieldUnspecified"), stepMeta)); //$NON-NLS-1$
      } else if (info != null && info.indexOfValue(choiceNameField) < 0) {
        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG,
            "PromptStep.Log.Error.ChoiceNameFieldNotFound", //$NON-NLS-1$
            choiceNameField,
            getStepIOMeta().getInfoStepnames()[0]), stepMeta));
      }

      if (choiceValueField == null || choiceValueField.length() == 0) {
        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG,
            "PromptStep.Log.Error.ChoiceValueFieldUnspecified"), stepMeta)); //$NON-NLS-1$
      } else if (info != null && info.indexOfValue(choiceValueField) < 0) {
        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG,
            "PromptStep.Log.Error.ChoiceValueFieldNotFound", //$NON-NLS-1$
            choiceValueField,
            getStepIOMeta().getInfoStepnames()[0]), stepMeta));
      }

      if (promptType != null && !promptType.usesChoices()) {
        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
            PKG,
            "PromptStep.Log.Warn.ChoiceStepIgnored", //$NON-NLS-1$
            infoStreams.get(0).getStepname()), stepMeta));
      }

      // TODO:  Table Input checks that the step exists.  Is that really necessary if it is in InfoStreams?
    } else {
      // Not accepting choices from input
      if (choiceNameField != null && choiceNameField.length() > 0) {
        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
            PKG,
            "PromptStep.Log.Warn.ChoiceNameIgnored"), stepMeta)); //$NON-NLS-1$
      }

      if (choiceValueField != null && choiceValueField.length() > 0) {
        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
            PKG,
            "PromptStep.Log.Warn.ChoiceValueIgnored"), stepMeta)); //$NON-NLS-1$
      }

      if (promptChoices.size() == 0 && promptType != null && promptType.usesChoices()) {
        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
            PKG,
            "PromptStep.Log.Warn.NoChoicesSpecified", //$NON-NLS-1$
            infoStreams.get(0).getStepname()), stepMeta));
      }
    }
    
    if (promptChoices.size() != 0 && promptType != null && !promptType.usesChoices()) {
      remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG,
          "PromptStep.Log.Warn.StaticChoicesIgnored", //$NON-NLS-1$
          infoStreams.get(0).getStepname()), stepMeta));
    }
  }

  @Override
  public PromptStep getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans disp) {
    return new PromptStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
  }

  @Override
  public PromptStepData getStepData() {
    return new PromptStepData();
  }

  // Note:  Info streams must be pre-populated
  // {@see StepIOMeta#setInfoSteps(StepMeta[])}
  @Override
  public StepIOMetaInterface getStepIOMeta() {
    if (ioMeta == null) {
      ioMeta = new StepIOMeta(true, true, false, false, false, false);

      StreamInterface stream = new Stream(StreamType.INFO, null, BaseMessages.getString(
          PKG,
          "PromptStep.Stream.PromptChoices"), StreamIcon.INFO, null); //$NON-NLS-1$
      ioMeta.addStream(stream);
    }

    return ioMeta;
  }

  @Override
  public void resetStepIoMeta() {
    // Do nothing, don't reset as there is no need to do this.
    // FIXME:  Copied from TableInputMeta.  Is this correct?
  }

  @Override
  public void searchInfoAndTargetSteps(List<StepMeta> steps) {
    super.searchInfoAndTargetSteps(steps);

    // Note:  Only expect ~1 info stream, so not worth hashing on step names
    for (StreamInterface stream : getStepIOMeta().getInfoStreams()) {
      stream.setStepMeta(StepMeta.findStep(steps, (String) stream.getSubject()));
    }
  }
}
