/*
 * Sets up and checks the school variables. This can either use the test controller or the school
 * url and name.
 *
 * Input variables:
 * TESTCONTROLLER_URL: The URL of the TestServerController to use
 * SCHOOL_ADMIN_PASSWORD: The admin password to set for school
 * SCHOOL_URL: The URL of the school to use
 * SCHOOL_NAME: The name of the school to use (e.g. as it will be typed in the Get Started screen)
 * DIR_ADMIN_AUTH_HEADER: The Authorization header to use for the school directory admin, used to
 * automatically create a school.
 * NO_SCHOOL_CREATION: if set to "true", then the school will not be automatically created (e.g. if
 * running a test of self-school registration).
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

    const serverUrl = isSetUrl(URL_SUBSTITUTION) ?
        URL_SUBSTITUTION.replace("_PORT_", responseJson.port) : responseJson.url;

    console.log("RESPECT server started on url " + serverUrl);
    console.log("RESPECT dir admin Auth: " + DIR_ADMIN_AUTH_HEADER);

    const timeNow = new Date().toISOString();

    if(!isSetString(SCHOOL_NAME)) {
        output.SCHOOL_NAME = "TestSchool";
    }

    if(!isSetString(NO_SCHOOL_CREATION) || NO_SCHOOL_CREATION != "true") {
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
    }else {
        console.log("New school creation disabled e.g. for testing school self-registration." +
            "New school will not be created. SCHOOL_URL will be set anyway to " + serverUrl);
    }

    output.SCHOOL_URL = serverUrl;
}else if(typeof SCHOOL_URL == "string" && SCHOOL_URL.startsWith("http")) {
    output.SCHOOL_URL = SCHOOL_URL;
}else {
    throw "SCHOOL_URL not set AND TESTCONTROLLER_URL not set. See README";
}

if(!isSetString(output.SCHOOL_NAME)) {
    throw "SCHOOL_NAME not set and not using TESTCONTROLLER_URL. e.g. maestro test -e SCHOOL_NAME=TestSchool . See README"
}

