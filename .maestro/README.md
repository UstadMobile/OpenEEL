# RESPECT App Maestro tests

## Development environment setup:
* Complete development environment setup as per main [README](../README.md)
* Install [Maestro CLI](https://github.com/mobile-dev-inc/Maestro/releases).

## Quick run an individual test:

* Build the project as per the main [README](../README.md)
* Start app-server and add a school as per the main project README.
* Install the APK on the Android Emulator or device being used to run tests
  e.g. run project using Android Studio, drag/drop file onto Android emulator, or install using adb command:
```
adb install ./app-android/build/outputs/apk/debug/app-android-debug.apk
```

* Run the server with e2e artifact upload enabled

```
./gradlew app-server:run --args='runserver -P:ktor.e2eartifactupload.enabled=true'
```

* Run test using Maestro CLI (specify the school URL and admin password):
```
maestro test \
    -e SCHOOL_URL=http://192.168.1.2:8094/ \
    -e SCHOOL_ADMIN_PASSWORD=adminpassword \
    -e SCHOOL_NAME=TestSchool \
    .maestro/flows/flow_name.yaml
```

Where:
* ```SCHOOL_URL``` is the URL for the school as used with the addschool command as  as per the main
  [README](../README.md)
* ```SCHOOL_ADMIN_PASSWORD``` is the password for the admin user for the school (also as per addschool command)
* ```SCHOOL_NAME``` is the name of the school (also as per addschool command)


## Run multiple tests (suite)

Running multiple tests with Maestro requires a blank server installation for each test.
[TestServerController](https://github.com/UstadMobile/TestServerController) is used to start/stop a new blank server instance on a free port as
required.

```
export TESTSERVER_CONTROLLER_URL=http://192.168.1.2:8094/
./ci-run-e2e-tests.sh
```

Where:
* 192.168.1.2 is the local IP of the developer's laptop

Note: You can filter which tests to run by setting the environment variable ```MAESTRO_INCLUDE_TAGS```
with arguments as per [Maestro test discovery and tags](https://docs.maestro.dev/maestro-flows/workspace-management/test-discovery-and-tags)
e.g.

```
export TESTSERVER_CONTROLLER_URL=http://192.168.1.2:8094/
export MAESTRO_INCLUDE_TAGS=basic
./ci-run-e2e-tests.sh
```

In Jenkins, add `MAESTRO_INCLUDE_TAGS=smoke` to the job's Properties Content. To run all flows, comment it out
(`#MAESTRO_INCLUDE_TAGS=smoke`) or leave it out.

or set a particular test to run using ```MAESTRO_FLOW``` arg

```
export TESTSERVER_CONTROLLER_URL=http://192.168.1.2:8094/
export MAESTRO_FLOW=./maestro/flows/flow_name.yaml
./ci-run-e2e-tests.sh
```

# Maestro flow environment variables:
* ```TESTCONTROLLER_URL```: sets the [TestServerController](https://github.com/UstadMobile/TestServerController) 
  URL for testserver controller that will create new blank server instance as required. If set, the 
  school url, admin password, and school name will be received from the test server controller
  if not otherwise specified. This is **required** to run more than one test.
* ```SCHOOL_URL```: explicitly set the scohol url to use
* ```SCHOOL_ADMIN_PASSWORD```: explicitly set the school admin password to use
* ```SCHOOL_NAME```: explicitly set the school name to use (used in get started screen)
* ```URL_SUBSTITUTION``` when there is a reverse proxy setup (e.g. to handle HTTPS) then it will 
  replace _PORT_ with the port number created by the test server e.g. https://_PORT_.ustadtesting.ustadmobile.com/
* ```TEST_APP_URL```: A launchable app publication URL (used with 002_browse_lessons_test and 
  assignment tests.

## Available test flows
---
### 001_001_invite_users_using_qr_code_or_link_test

1. Admin generates invite link (QR/link) for teacher
2. Teacher joins using QR/link → creates account
3. Teacher creates class and generates invite code for student
4. Student joins using invite code → waits for approval
5. Teacher approves student → student joins class
6. Teacher generates parent invite link to join class
7. Parent joins using link → adds child to class
---
### 001_002_add_user_direct_test

1. Admin logs into the app
2. Admin adds a new user directly (Parent user)
3. Verify "Family member" field is not visible when role is changed to Teacher
4. Validate "Edit" button functionality
5. Validate mandatory fields and input constraints
6. Add child user via Family member → Add person screen
7. Create account for Parent user (username, password)
8. Create account for Child user (username, assign/manage QR code badge, set password)
9. Create a student user for QR code validation
10. Validate login, password change, and child mode access
11. Verify QR-based login for student
---
### 001_003_login_using_school_link_test

1. User opens app via school link
2. Validate empty and invalid link scenarios
3. Enter valid school URL
4. Perform login with credentials
5. Verify successful access to the app
---
### 001_005_add_school_self_registration_test

1. User adds a new school from login screen
2. Selects host and registers school
3. Creates system administrator account
4. Logs into newly created school
5. Verifies profile and logout/login flow
---
### 002_browse_lessons_test

1. Admin logs in to the App
2. Adds app using external manifest link
3. Verifies app is added successfully
4. Opens app and browses lessons
5. Opens and validates a lesson content
---
### 003_admin_user_assigns_assignment_to_a_class_test

1. Admin setup includes app, class and teacher creation
2. Teacher logs in and accesses class
3. Teacher creates a new assignment
4. Assignment is linked with lesson content
5. Assignment is saved and verified in class
---

