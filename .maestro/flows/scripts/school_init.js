    /*
 * Sets up and checks the school variables. This can either use the test controller or the school
 * url and name.
 */

function isSetString(value) {
    return typeof value == "string" && value.length > 0 && value != "undefined";
}

function isSetUrl(value) {
    return isSetString(value) && value.startsWith("http");
}

if(!isSetString(SCHOOL_ADMIN_PASSWORD)) {
    throw "SCHOOL_ADMIN_PASSWORD not set. e.g. maestro test -e SCHOOL_ADMIN_PASSWORD=adminpassword . See README";
}

if(isSetString(SCHOOL_NAME)) {
    output.SCHOOL_NAME = SCHOOL_NAME;
}

/*
 * If TESTCONTROLLER_URL is set, then use test controller to add a new school.
 */
if(isSetUrl(TESTCONTROLLER_URL)) {
    console.log("Test controller= " + TESTCONTROLLER_URL);

    const testControllerResponse = http.get(
        TESTCONTROLLER_URL + "testcontroller/start?waitForUrl=" +
            encodeURIComponent("api/directory/school") +
            (isSetString(NAME) ? "&name=" + encodeURIComponent(NAME) : "")
    );

    console.log("Response body = " + testControllerResponse.body);
    const responseJson = json(testControllerResponse.body);

    output.SCHOOL_PORT = responseJson.port;
    const serverUrl = isSetUrl(URL_SUBSTITUTION) ?
        URL_SUBSTITUTION.replace("_PORT_", responseJson.port) : responseJson.url;

    console.log("RESPECT server started on url " + serverUrl);
    console.log("RESPECT dir admin Auth: " + DIR_ADMIN_AUTH_HEADER);

    const timeNow = new Date().toISOString();

    if(!isSetString(SCHOOL_NAME)) {
        output.SCHOOL_NAME = "TestSchool";
    }

    const newSchoolResponse = http.post(serverUrl + "api/directory/school", {
        headers: {
            'Content-Type': 'application/json',
            "Authorization": DIR_ADMIN_AUTH_HEADER
        },
        body: JSON.stringify(
            [
                {
                    "school": {
                        "name": output.SCHOOL_NAME,
                        "self": serverUrl,
                        "xapi": (serverUrl + "api/school/xapi"),
                        "oneRoster": (serverUrl + "api/school/oneroster"),
                        "respectExt": (serverUrl + "api/school/respect"),
                        "rpId": null,
                        "lastModified": timeNow,
                        "stored": timeNow
                    },
                    "dbUrl": "school.db",
                    "adminUsername": "admin",
                    "adminPassword": SCHOOL_ADMIN_PASSWORD
                }
            ]
        )
    });
    console.log("New school server URL requested: status = " + newSchoolResponse.status);

    output.SCHOOL_URL = serverUrl;
}else if(typeof SCHOOL_URL == "string" && SCHOOL_URL.startsWith("http")) {
    output.SCHOOL_URL = SCHOOL_URL;
}else {
    throw "SCHOOL_URL not set AND TESTCONTROLLER_URL not set. See README";
}

output.SCHOOL_URL_ENCODED = encodeURIComponent(output.SCHOOL_URL);

if(!isSetString(output.SCHOOL_NAME)) {
    throw "SCHOOL_NAME not set and not using TESTCONTROLLER_URL. e.g. maestro test -e SCHOOL_NAME=TestSchool . See README";
}

if(isSetString(TEST_APP_URL)) {
    output.TEST_APP_URL = TEST_APP_URL;
}else {
    output.TEST_APP_URL = output.SCHOOL_URL + "static-resources/respect-ds/case_valid/appmanifest.json";
}

