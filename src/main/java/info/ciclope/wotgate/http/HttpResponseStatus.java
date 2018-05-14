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

package info.ciclope.wotgate.http;

public class HttpResponseStatus {

    private HttpResponseStatus() {
    }

    public static final Integer OK = 200;
    public static final Integer CREATED = 201;
    public static final Integer ACCEPTED = 202;
    public static final Integer NO_CONTENT = 204;
    public static final Integer BAD_REQUEST = 400;
    public static final Integer UNAUTHORIZED = 401;
    public static final Integer FORBIDDEN = 403;
    public static final Integer RESOURCE_NOT_FOUND = 404;
    public static final Integer CONFLICT = 409;
    public static final Integer GONE = 410;
    public static final Integer INTERNAL_ERROR = 500;
    public static final Integer NOT_IMPLEMENTED = 501;
}
