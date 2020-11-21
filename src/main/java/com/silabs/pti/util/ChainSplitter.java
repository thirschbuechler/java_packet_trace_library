// Copyright (c) 2013 Silicon Labs. All rights reserved.

package com.silabs.pti.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain splitter takes multiple 2-way splitters and attaches the
 * output 0 from previous splitter into the input of the next splitter.
 * 
 * Note that this class only makes sense for 2-way splitters, more-than-2-way
 * splitter should not use it.
 * 
 * If you have splitters s0,s1,s2, for example, the resulting chain is:
 * 
 * => input => s0 -> s1 -> s2 ---> output 0
 *               \-----\-----\---> output 1
 *               
 * So basically the output 1 of all the three splitter is concatenated and
 * output 0 is piped.
 * 
 * Created on Mar 10, 2013 
 * @author Timotej Ecimovic
 */
public class ChainSplitter implements ISplitter {

  private final List<ISplitter> splitters = new ArrayList<ISplitter>();
  
  /**
   * Create a chain splitter from the provided array of splitters.
   */
  public ChainSplitter(ISplitter... splitters) {
    for ( int i=1; i<splitters.length; i++ ) {
      splitters[i-1].setCharacterListener(0, splitters[i]);
    }
    for ( ISplitter s: splitters ) 
      this.splitters.add(s);
  }
  
  @Override
  public void received(byte[] ch, int offset, int len) {
    splitters.get(0).received(ch, offset, len);
  }

  @Override
  public void setCharacterListener(int bucket, ICharacterListener l) {
    if ( bucket == 0 ) {
      splitters.get(splitters.size()-1).setCharacterListener(0, l);
    } else {
      for ( ISplitter s: splitters ) 
        s.setCharacterListener(1, l);
    }
  }

  @Override
  public int bucketCount() {
    return splitters.get(0).bucketCount();
  }

  @Override
  public void flush() {
    for ( ISplitter s: splitters ) 
      s.flush();
  }

}
