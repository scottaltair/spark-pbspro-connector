/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.deploy.pbs

import org.apache.spark.pbs.Utils

private[pbs] case class PbsServerState() {
  val serverStatus: String = {
    "Active".r.findFirstMatchIn(Utils.qstat("", "-B")) match {
      case Some(_) =>
        "running"
      case None =>
        "down"
    }
  }

  val _sparkJobs: List[PbsJobInfo] = {
    Utils.qstat("", "").split("\n").toList.map(PbsJobInfo.getJobInfo).flatten
  }

  val executors: List[PbsExecutorInfo] = {
    _sparkJobs.flatMap({
      case x: PbsExecutorInfo => Some(x)
      case _ => None
    })
  }

  val drivers: List[PbsDriverInfo] = {
    _sparkJobs.flatMap({
      case x: PbsDriverInfo => Some(x)
      case _ => None
    })
  }

  val runningDrivers: List[PbsDriverInfo] = drivers.filter(_.isRunning)
  val completedDrivers: List[PbsDriverInfo] = drivers.filter(_.isCompleted) // TODO

  val applications: List[PbsApplicationInfo] = {
    runningDrivers.map(driver =>
        new PbsApplicationInfo(driver, executors.filter(_.driverId == driver.jobId)))
  }

  val runningApplications: List[PbsApplicationInfo] = applications.filter(_.isRunning)
  val completedApplications: List[PbsApplicationInfo] = applications.filter(_.isCompleted) // TODO
}