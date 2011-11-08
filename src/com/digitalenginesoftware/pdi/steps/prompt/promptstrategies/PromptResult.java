/* This file is part of the Pentaho Data Integration Prompt Step Plugin.
 * It is licensed under the terms of the GNU LGPL version 2.1 or later.
 * The complete text of the license is available in the project documentation.
 *
 * Copyright 2011 Digital Engine Software, LLC
 */
package com.digitalenginesoftware.pdi.steps.prompt.promptstrategies;

public class PromptResult<T> {
  private final boolean stopRequested;
  private final T value;
  
  public PromptResult(T value) {
    this.stopRequested = false;
    this.value = value;
  }
  
  public PromptResult(boolean stopRequested, T value) {
    this.stopRequested = stopRequested;
    this.value = value;
  }
  
  public boolean isStopRequested() {
    return stopRequested;
  }
  
  public T getValue() {
    return value;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (stopRequested ? 1231 : 1237);
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    PromptResult<?> other = (PromptResult<?>) obj;
    if (stopRequested != other.stopRequested)
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
}
