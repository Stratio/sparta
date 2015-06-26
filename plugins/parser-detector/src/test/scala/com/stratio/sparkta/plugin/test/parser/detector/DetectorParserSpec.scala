/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.test.parser.detector

import com.stratio.sparkta.plugin.parser.detector.DetectorParser
import com.stratio.sparkta.sdk.{Event, Input}
import org.junit.runner.RunWith
import org.scalatest.WordSpecLike
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DetectorParserSpec extends WordSpecLike {

  val inputField = Input.RawDataKey
  val outputsFields = Seq("plate", "alarm_code")
  val outputsFields2 = Seq("plate")
  val outputsFields3 = Seq("odometerNum")

  "A DetectorParser" should {
    "parse to map with real json" in {
      val myJson1 =
        """{"operation":"insert","streamName":"detectorStream","session_id":"1432534720229",
          |"request_id":"1432558355402","request":"","timestamp":1432558355402,"columns":[{"column":"id",
          |"value":"714098557795172482"},{"column":"connection_id","value":"714098345336897663"},
          |{"column":"event_index","value":"147"},{"column":"asset","value":"351777046962500"},
          |{"column":"recorded_at","value":"1432558321000"},{"column":"recorded_at_ms","value":"1432558321000"},
          |{"column":"received_at","value":"1432558354000"},{"column":"lat","value":"1.95154"},{"column":"lon",
          |"value":"41.46387"},{"column":"id_group","value":"3703"},{"column":"id_vehicle","value":"4117"},
          |{"column":"serial_num","value":"9000104939"},{"column":"plate","value":"4117 AAA"},
          |{"column":"company_vehicle","value":"79"},{"column":"ou_vehicle","value":"217"},{"column":"company_root",
          |"value":"3"},{"column":"id_mcustomer","value":"12737"},{"column":"driver","value":"DRIVER 12737"},
          |{"column":"rpm_event_timestamp"},{"column":"rpm_event_0"},{"column":"rpm_event_1"},
          |{"column":"rpm_event_2"},{"column":"rpm_event_3"},{"column":"rpm_event_4"},{"column":"rpm_event_5"},
          |{"column":"rpm_event_6"},{"column":"rpm_event_7"},{"column":"rpm_event_avg"},
          |{"column":"score_emissions_timestamp_0"},{"column":"score_emissions_lat_0"},
          |{"column":"score_emissions_lon_0"},{"column":"score_emissions_consumption_0"},
          |{"column":"score_emissions_timestamp_1"},{"column":"score_emissions_lat_1"},
          |{"column":"score_emissions_lon_1"},{"column":"score_emissions_consumption_1"},
          |{"column":"alarm_timestamp"},{"column":"alarm_code","value": 0.0},{"column":"alarm_imei"},
          |{"column":"alarm_lat"},{"column":"alarm_lon"},{"column":"alarm_sat_number"},{"column":"alarm_speed"},
          |{"column":"alarm_direction"},{"column":"alarm_modem_csq"},{"column":"alarm_cell_id"},
          |{"column":"alarm_ignition"},{"column":"alarm_batt_tension"},{"column":"alarm_detl"},{"column":"odometer",
          |"value":"33000.89"},{"column":"ignition"},{"column":"volt_batt_tension"},{"column":"vel_timestamp"},
          |{"column":"vel_0"},{"column":"vel_1"},{"column":"vel_2"},{"column":"vel_3"},{"column":"vel_4"},
          |{"column":"vel_5"},{"column":"vel_6"},{"column":"vel_7"},{"column":"vel_avg"},
          |{"column":"consumption_timestamp_0"},{"column":"consumption_lat_0"},{"column":"consumption_lon_0"},
          |{"column":"consumption_0"},{"column":"consumption_timestamp_1"},{"column":"consumption_lat_1"},
          |{"column":"consumption_lon_1"},{"column":"consumption_1"},{"column":"consumption_timestamp_2"},
          |{"column":"consumption_lat_2"},{"column":"consumption_lon_2"},{"column":"consumption_2"},
          |{"column":"emissions_timestamp_0"},{"column":"emissions_lat_0"},{"column":"emissions_lon_0"},
          |{"column":"emissions_co2_0"},{"column":"emissions_timestamp_1"},{"column":"emissions_lat_1"},
          |{"column":"emissions_lon_1"},{"column":"emissions_co2_1"},{"column":"emissions_timestamp_2"},
          |{"column":"emissions_lat_2"},{"column":"emissions_lon_2"},{"column":"emissions_co2_2"},
          |{"column":"score_consumption_timestamp_0"},{"column":"score_consumption_lat_0"},
          |{"column":"score_consumption_lon_0"},{"column":"score_consumption_kpi_0"},
          |{"column":"score_consumption_timestamp_1"},{"column":"score_consumption_lat_1"},
          |{"column":"score_consumption_lon_1"},{"column":"score_consumption_kpi_1"},
          |{"column":"score_consumption_timestamp_2"},{"column":"score_consumption_lat_2"},
          |{"column":"score_consumption_lon_2"},{"column":"score_consumption_kpi_2"},{"column":"stratio_timestamp_0",
          |"value":"1432558355"},{"column":"stratio_timestamp_1"},{"column":"stratio_timestamp_2"}],
          |"userDefined":true}""".stripMargin

      val e1 = new Event(Map(Input.RawDataKey -> myJson1))
      val e2 = new Event(Map())
      val result = new DetectorParser("name", 1, inputField, outputsFields, Map()).parse(e1)
      val badResult = new DetectorParser("name", 1, inputField, outputsFields2, Map()).parse(e1)
      val badResult2 = new DetectorParser("name", 1, inputField, outputsFields2, Map()).parse(e1)

      assertResult(true)(result.keyMap.contains("plate"))
      assertResult(false)(badResult.keyMap.contains("plate"))
      assertResult(false)(badResult2.keyMap.contains("plate"))
    }
  }
}
