// Copyright (c) 2015 Silicon Labs. All rights reserved.

package com.silabs.pti.adapter;

import com.silabs.pti.util.ICharacterListener;

/**
 * Simple character listener that simply collects the data as UTF-8 string.
 *
 * Created on Jan 28, 2016
 * @author timotej
 */
public class CharacterCollector implements ICharacterListener {

  private final StringBuffer sb = new StringBuffer();

  @Override
  public void received(final byte[] ch, final int offset, final int len) {
    synchronized(sb) {
      sb.append(new String(ch, offset, len));
    }
  }

  /** Returns the collected string, assuming UTF-8. */
  public String text() {
    synchronized(sb) {
      return sb.toString();
    }
  }

  public String textAndClean() {
    synchronized(sb) {
      String text = sb.toString();
      sb.delete(0, sb.length());
      return text;
    }
  }

  /** Returns the collected lines, assuming UTF-8. */
  public String[] lines() {
    synchronized(sb) {
      return sb.toString().split("\\r?\\n");
    }
  }

  /** Returns true if the collected data contains a given text. */
  public boolean contains(final String text) {
    synchronized(sb) {
      return sb.indexOf(text) != -1;
    }
  }

  /** Cleans the text */
  public void clean() {
    synchronized(sb) {
      sb.delete(0, sb.length());
    }
  }
}
