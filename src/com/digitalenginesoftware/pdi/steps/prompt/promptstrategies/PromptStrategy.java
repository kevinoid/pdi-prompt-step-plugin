/* This file is part of the Pentaho Data Integration Prompt Step Plugin.
 * It is licensed under the terms of the GNU LGPL version 2.1 or later.
 * The complete text of the license is available in the project documentation.
 *
 * Copyright 2011 Digital Engine Software, LLC
 */
package com.digitalenginesoftware.pdi.steps.prompt.promptstrategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.graphics.Image;


public abstract class PromptStrategy<T> {
  private static HashMap<String, PromptStrategyInfo> strategyIDToInfo;

  static {
    ArrayList<PromptStrategyInfo> strategyInfos = new ArrayList<PromptStrategyInfo>(4);
    strategyInfos.add(ButtonPromptStrategy.STRATEGY_INFO);
    strategyInfos.add(ComboPromptStrategy.STRATEGY_INFO);
    strategyInfos.add(LongTextBoxPromptStrategy.STRATEGY_INFO);
    strategyInfos.add(ShortTextBoxPromptStrategy.STRATEGY_INFO);

    strategyIDToInfo = new HashMap<String, PromptStrategyInfo>(4);
    for (PromptStrategyInfo strategyInfo : strategyInfos) {
      strategyIDToInfo.put(strategyInfo.getStrategyID(), strategyInfo);
    }
  }

  public static PromptStrategy<String> createStrategy(String strategyID) {
    return getStrategyInfo(strategyID).createStrategy();
  }

  public static PromptStrategyInfo getStrategyInfo(String strategyID) {
    PromptStrategyInfo info = strategyIDToInfo.get(strategyID);
    if (info == null)
      throw new IllegalArgumentException("Unrecognized strategyID"); //$NON-NLS-1$

    return info;
  }

  public static Collection<PromptStrategyInfo> getStrategyInfos() {
    return Collections.unmodifiableCollection(strategyIDToInfo.values());
  }

  private Image promptIcon;

  private String promptTitle;

  private final PromptStrategyInfo strategyInfo;

  private ArrayList<PromptChoice<T>> choices;

  protected PromptStrategy(PromptStrategyInfo strategyInfo) {
    this.strategyInfo = strategyInfo;
    this.choices = new ArrayList<PromptChoice<T>>();
  }

  public boolean addChoice(PromptChoice<T> choice) {
    return choices.add(choice);
  }

  public List<PromptChoice<T>> getChoices() {
    return Collections.unmodifiableList(choices);
  }

  public Image getPromptIcon() {
    return promptIcon;
  }

  public void setPromptIcon(Image promptIcon) {
    this.promptIcon = promptIcon;
  }

  public String getPromptTitle() {
    return promptTitle;
  }

  public void setPromptTitle(String promptTitle) {
    this.promptTitle = promptTitle;
  }

  public PromptStrategyInfo getStrategyInfo() {
    return strategyInfo;
  }

  public PromptResult<T> prompt(String message) {
    return prompt(promptIcon, message);
  }

  public abstract PromptResult<T> prompt(Image icon, String message);

  public boolean removeChoice(PromptChoice<T> choice) {
    return choices.remove(choice);
  }

  public void setChoices(Collection<? extends PromptChoice<T>> choices) {
    this.choices = new ArrayList<PromptChoice<T>>(choices);
  }
}
