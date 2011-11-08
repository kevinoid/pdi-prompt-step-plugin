/* This file is part of the Pentaho Data Integration Prompt Step Plugin.
 * It is licensed under the terms of the GNU LGPL version 2.1 or later.
 * The complete text of the license is available in the project documentation.
 *
 * Copyright 2011 Digital Engine Software, LLC
 */
package com.digitalenginesoftware.pdi.steps.prompt.promptstrategies;

public class PromptChoice<T> implements Cloneable {
  private final String name;
  private final T value;
  
  public PromptChoice(String name, T value) {
    if (name == null)
      throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$

    this.name = name;
    this.value = value;
  }
  
  public String getName() {
    return name;
  }
  
  public T getValue() {
    return value;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public PromptChoice<T> clone() {
    try {
      return (PromptChoice<T>)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("CloneNotSupportedException thrown by Cloneable!?"); //$NON-NLS-1$
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    
    PromptChoice<?> other = (PromptChoice<?>) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;

    return true;
  }
  
  @Override
  public String toString() {
    return String.format("%s [%s]", name, value);  //$NON-NLS-1$
  }
}
