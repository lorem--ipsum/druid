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

package org.apache.druid.java.util.metrics;

import com.google.common.collect.ImmutableMap;
import org.apache.druid.java.util.common.StringUtils;
import org.apache.druid.java.util.emitter.service.ServiceEmitter;
import org.apache.druid.java.util.emitter.service.ServiceMetricEvent;
import org.apache.druid.java.util.metrics.cgroups.CgroupDiscoverer;
import org.apache.druid.java.util.metrics.cgroups.Memory;
import org.apache.druid.java.util.metrics.cgroups.ProcSelfCgroupDiscoverer;

import java.util.Map;

public class CgroupMemoryMonitor extends FeedDefiningMonitor
{
  final CgroupDiscoverer cgroupDiscoverer;
  final Map<String, String[]> dimensions;

  public CgroupMemoryMonitor(CgroupDiscoverer cgroupDiscoverer, final Map<String, String[]> dimensions, String feed)
  {
    super(feed);
    this.cgroupDiscoverer = cgroupDiscoverer;
    this.dimensions = dimensions;
  }

  public CgroupMemoryMonitor(final Map<String, String[]> dimensions, String feed)
  {
    this(new ProcSelfCgroupDiscoverer(), dimensions, feed);
  }

  public CgroupMemoryMonitor(final Map<String, String[]> dimensions)
  {
    this(dimensions, DEFAULT_METRICS_FEED);
  }

  public CgroupMemoryMonitor()
  {
    this(ImmutableMap.of());
  }

  @Override
  public boolean doMonitor(ServiceEmitter emitter)
  {
    final Memory memory = new Memory(cgroupDiscoverer);
    final Memory.MemoryStat stat = memory.snapshot(memoryUsageFile(), memoryLimitFile());
    final ServiceMetricEvent.Builder builder = builder();
    MonitorUtils.addDimensionsToBuilder(builder, dimensions);
    emitter.emit(builder.setMetric("cgroup/memory/usage/bytes", stat.getUsage()));
    emitter.emit(builder.setMetric("cgroup/memory/limit/bytes", stat.getLimit()));

    stat.getMemoryStats().forEach((key, value) -> {
      // See https://www.kernel.org/doc/Documentation/cgroup-v1/memory.txt
      // There are inconsistent units for these. Most are bytes.
      emitter.emit(builder.setMetric(StringUtils.format("cgroup/memory/%s", key), value));
    });
    stat.getNumaMemoryStats().forEach((key, value) -> {
      builder().setDimension("numaZone", Long.toString(key));
      value.forEach((k, v) -> emitter.emit(builder.setMetric(StringUtils.format("cgroup/memory_numa/%s/pages", k), v)));
    });
    return true;
  }

  public String memoryUsageFile()
  {
    return "memory.usage_in_bytes";
  }

  public String memoryLimitFile()
  {
    return "memory.limit_in_bytes";
  }
}
