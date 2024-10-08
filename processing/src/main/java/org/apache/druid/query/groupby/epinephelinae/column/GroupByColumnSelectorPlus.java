/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.druid.query.groupby.epinephelinae.column;

import org.apache.druid.query.ColumnSelectorPlus;
import org.apache.druid.query.groupby.epinephelinae.GroupingSelector;
import org.apache.druid.segment.ColumnValueSelector;
import org.apache.druid.segment.DimensionDictionarySelector;

public class GroupByColumnSelectorPlus extends ColumnSelectorPlus<GroupByColumnSelectorStrategy> implements
    GroupingSelector
{
  /**
   * Indicates the offset of this dimension's value within ResultRows.
   */
  private final int resultRowPosition;

  /**
   * Indicates the offset of this dimension's value within the grouping key.
   */
  private final int keyBufferPosition;

  public GroupByColumnSelectorPlus(
      ColumnSelectorPlus<GroupByColumnSelectorStrategy> baseInfo,
      int keyBufferPosition,
      int resultRowPosition
  )
  {
    super(
        baseInfo.getName(),
        baseInfo.getOutputName(),
        baseInfo.getColumnSelectorStrategy(),
        baseInfo.getSelector()
    );
    this.keyBufferPosition = keyBufferPosition;
    this.resultRowPosition = resultRowPosition;
  }

  public int getKeyBufferPosition()
  {
    return keyBufferPosition;
  }

  public int getResultRowPosition()
  {
    return resultRowPosition;
  }

  @Override
  public int getValueCardinality()
  {
    final ColumnValueSelector<?> selector = getSelector();
    if (selector instanceof DimensionDictionarySelector) {
      return ((DimensionDictionarySelector) selector).getValueCardinality();
    }
    return DimensionDictionarySelector.CARDINALITY_UNKNOWN;
  }
}
