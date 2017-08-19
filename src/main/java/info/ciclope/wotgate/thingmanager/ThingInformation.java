/*
 *  Copyright (c) 2017, Javier Martínez Villacampa
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package info.ciclope.wotgate.thingmanager;

import info.ciclope.wotgate.things.thing.ThingDescription;

public class ThingInformation {
    private ThingDescription thingDescription;
    private String thingDeploymentId;

    public ThingInformation(ThingDescription thingDescription, String thingDeploymentId) {
        this.thingDescription = thingDescription;
        this.thingDeploymentId = thingDeploymentId;
    }

    public ThingDescription getThingDescription() {
        return thingDescription;
    }

    public void setThingDescription(ThingDescription thingDescription) {
        this.thingDescription = thingDescription;
    }

    public String getThingDeploymentId() {
        return thingDeploymentId;
    }
}
