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

package info.ciclope.wotgate.thing.component;

public class ThingAddress {
    public static final String ADDRESS_START = "thing.";
    public static final String ADDRESS_THING_CONFIGURATION = ".thingconfiguration";
    public static final String ADDRESS_THING_DESCRIPTION = ".thingdescription";
    public static final String ADDRESS_THING_INTERACTION = ".thinginteraction.";
    public static final String ADDRESS_PROVIDE = ".provide";
    public static final String ADDRESS_GET = ".get";
    public static final String ADDRESS_POST = ".post";
    public static final String ADDRESS_PUT = ".put";
    public static final String ADDRESS_DELETE = ".delete";
    public static final String ADDRESS_TASK = ".task";
    public static final String ADDRESS_THING_INTERACTION_AUTHENTICATION = "gatekeeper.interaction.authentication";

    private ThingAddress() {
    }

    public static String getGetThingConfigurationAddress(String thingName) {
        return ADDRESS_START + thingName + ADDRESS_THING_CONFIGURATION + ADDRESS_GET;
    }

    public static String getProvideThingThingDescriptionAddress(String thingName) {
        return ADDRESS_START + thingName + ADDRESS_THING_DESCRIPTION + ADDRESS_PROVIDE;
    }

    public static String getGetThingThingDescriptionAddress(String thingName) {
        return ADDRESS_START + thingName + ADDRESS_THING_DESCRIPTION + ADDRESS_GET;
    }

    public static String getPutThingThingDescriptionAddress(String thingName) {
        return ADDRESS_START + thingName + ADDRESS_THING_DESCRIPTION + ADDRESS_PUT;
    }

    public static String getGetThingInteractionAddress(String thingName, String interactionName) {
        return ADDRESS_START + thingName + ADDRESS_THING_INTERACTION + interactionName + ADDRESS_GET;
    }

    public static String getPostThingInteractionAddress(String thingName, String interactionName) {
        return ADDRESS_START + thingName + ADDRESS_THING_INTERACTION + interactionName + ADDRESS_POST;
    }

    public static String getPutThingInteractionAddress(String thingName, String interactionName) {
        return ADDRESS_START + thingName + ADDRESS_THING_INTERACTION + interactionName + ADDRESS_PUT;
    }

    public static String getDeleteThingInteractionAddress(String thingName, String interactionName) {
        return ADDRESS_START + thingName + ADDRESS_THING_INTERACTION + interactionName + ADDRESS_DELETE;
    }

    public static String getGetThingActionTaskAddress(String thingName, String interactionName) {
        return ADDRESS_START + thingName + ADDRESS_THING_INTERACTION + interactionName + ADDRESS_TASK + ADDRESS_GET;
    }

    public static String getPutThingActionTaskAddress(String thingName, String interactionName) {
        return ADDRESS_START + thingName + ADDRESS_THING_INTERACTION + interactionName + ADDRESS_TASK + ADDRESS_PUT;
    }

    public static String getDeleteThingActionTaskAddress(String thingName, String interactionName) {
        return ADDRESS_START + thingName + ADDRESS_THING_INTERACTION + interactionName + ADDRESS_TASK + ADDRESS_DELETE;
    }

    public static String getThingInteractionAuthenticationAddress() {
        return ADDRESS_START + ADDRESS_THING_INTERACTION_AUTHENTICATION;
    }

}
