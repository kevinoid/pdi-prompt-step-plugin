/* This file is part of the Pentaho Data Integration Prompt Step Plugin.
 * It is licensed under the terms of the GNU LGPL version 2.1 or later.
 * The complete text of the license is available in the project documentation.
 *
 * Copyright 2011 Digital Engine Software, LLC
 */
package com.digitalenginesoftware.pdi.steps.prompt.promptstrategies;


public abstract class PromptStrategyInfo {
  private final String strategyID;
  private final String strategyName;
  private final boolean usesChoices;
  
  public PromptStrategyInfo(String strategyTypeID, String strategyTypeName, boolean usesChoices) {
    super();
    this.strategyID = strategyTypeID;
    this.strategyName = strategyTypeName;
    this.usesChoices = usesChoices;
  }

  public String getStrategyID() {
    return strategyID;
  }

  public String getStrategyName() {
    return strategyName;
  }

  public boolean usesChoices() {
    return usesChoices;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((strategyID == null) ? 0 : strategyID.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PromptStrategyInfo other = (PromptStrategyInfo) obj;
    if (strategyID == null) {
      if (other.strategyID != null)
        return false;
    } else if (!strategyID.equals(other.strategyID))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "PromptStrategyType [strategyTypeID=" + strategyID + "]"; //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public abstract PromptStrategy<String> createStrategy();
}
