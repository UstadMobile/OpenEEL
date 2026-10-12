# Add a new Launchable Education App

**Intended audience**: this guide is for app developers who want to add their app to an Open 
Educational Experience Launcher. 

The launcher supports web technology based apps (HTML/Javascript)
and Android native apps. Compatible apps use [OPDS 2.0](https://specs.opds.io/opds-2.0.html) to 
provide a collection of available learning units and [xAPI](https://xapi.com/overview/) to send data
on lesson completion back to the launcher.

You can find a full example collection of learning units at [https://demo.openeel.org](https://demo.openeel.org/).

Terminology:
* **Launchable Education App**: an educational app (e.g. math app, language app, assessment app) that 
  can be launched from the apps list in the launcher.
* **Learning Unit**: a distinct unit within a launchable app e.g. a lesson, assessment, etc. Each 
  learning unit is represented by a [publication manifest](https://readium.org/webpub-manifest/).

## Step 1: Create an app manifest and OPDS collection for your app
An Open Educational Experience Launcher gives students and teachers a way to browse a different 
apps, launch them, browse learning units within them, and make/share collections of learning units.

**A) Create a launchable app manifest:** 

Example:
```json
{
  "metadata": {
    "@type": "https://id.openeel.org/schema/launchable-app",
    "title": "Demo Launchable App",
    "author": {
      "name": "Ustad Mobile FZ-LLC",
      "links": {
        "href": "https://www.ustadmobile.com/"
      }
    },
    "identifier": "https://demo.openeel.org/app/en-US",
    "language": "en",
    "modified": "2025-09-29T17:00:00Z"
  },
  "links": [
    {
      "rel": "self",
      "href": "https://demo.openeel.org/en-US/launchable-app-manifest.json",
      "type": "application/opds-publication+json"
    },
    {
      "href": "https://demo.openeel.org/fr-FR/launchable-app-manifest.json",
      "rel": "alternate",
      "language": "fr-FR"
    },
    {
      "rel": "collection",
      "href": "https://demo.openeel.org/en-US/default-collection.json",
      "type": "application/opds+json"
    },
    {
      "rel": "https://id.openeel.org/rel/app-launch-uri",
      "href": "https://demo.openeel.org/"
    },
    {
      "rel": "https://id.openeel.org/rel/appstore-android",
      "href": "https://play.google.com/store/apps/details?id=demo.openeel.org",
      "title": "Get it on Google Play"
    },
    {
      "rel": "terms-of-service",
      "href": "https://demo.openeel.org/terms-privacy.html"
    },
    {
      "rel": "license",
      "href": "http://opensource.org/licenses/MIT"
    }
  ],
  "images": [
    {
      "href": "https://demo.openeel.org/app-icon.png",
      "type": "image/png"
    }
  ]
}
```
For a more options see [README_LAUNCHABLE_APP.md](lib-opds-model/README_LAUNCHABLE_APP.md).

If you have a native Android app, it is recommended to add this intent filter to your AndroidManifest.xml file. Adding
the intent filter below will allow the launcher app to detect if your app is installed. If your app uses 
http(s) links that are not [verified app links](https://developer.android.com/training/app-links/about) 
(e.g. because your app allows users to select their own server) you must add this intent filter for 
the launcher app to be able to open it in your own app instead of the browser.

AndroidManifest.xml intent filter:
```xml
<intent-filter>
    <action android:name="org.openeel.action.LAUNCH" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <data android:scheme="https"/>
    <data android:scheme="http"/>
</intent-filter>
```

**B) Create a default collection of learning units:**

Example (default-collection.json):
```json
{
  "metadata": {
    "title": "Default Lesson collection"
  },

  "links": [
    {
      "rel": "self", 
      "href": "https://demo.openeel.org/en-US/default-collection.json", 
      "type": "application/opds+json"
    }
  ],

  "publications": [
    {
      "metadata": {
        "@type": "http://schema.org/Game",
        "title": "Native Demo 001",
        "author": "Mullah Nasruddin",
        "identifier": "https://demo.openeel.org/en-US/grade/1/learningunits/1/",
        "language": "en",
        "modified": "2015-09-29T17:00:00Z",
        "subject": [
          {
            "name": "Mathematics",
            "scheme": "https://www.bisg.org/#bisac",
            "code": "MAT000000"
          }
        ]
      },
      "links": [
        {"rel": "self", "href": "Lesson-manifest.json", "type": "application/opds-publication+json"},
        {
          "rel": "https://id.openeel.org/rel/tincanxml",
          "href": "tincan.xml",
          "type": "application/xml"
        },
        {
          "rel": "https://id.openeel.org/rel/launchable-app",
          "href": "https://demo.openeel.org/en-US/launchable-app-manifest.json",
          "type": "application/opds-publication+json"
        }
      ],
      "images": [
        {"href": "Lesson-cover.png", "type": "image/png"  }
      ]
    }
  ]
}
```

**C) Create/generate a manifest for each learning unit**

Example (Lesson-manifest.json)
```json
{
  "metadata": {
    "@type": "http://schema.org/Game",
    "title": "Lesson 001",
    "author": "Mullah Nasruddin",
    "identifier": "https://demo.openeel.org/en-US/grade/1/learningunits/1/",
    "language": "en",
    "modified": "2015-09-29T17:00:00Z"
  },
  "links": [
    {
      "rel": "self",
      "href": "https://demo.openeel.org/en-US/grade/1/learningunits/1/manifest.json",
      "type": "application/opds-publication+json"
    },
    {
      "rel": "https://id.openeel.org/rel/tincanxml",
      "href": "tincan.xml",
      "type": "application/xml"
    },
    {
      "rel": "https://id.openeel.org/rel/launchable-app",
      "href": "https://demo.openeel.org/en-US/launchable-app-manifest.json",
      "type": "application/opds-publication+json"
    }
  ],
  "readingOrder": [
    {
      "href": "https://demo.openeel.org/en-US/grade/1/learningunits/1/learningunit.html",
      "type": "text/html"
    }
  ],
  "images": [
    {
      "href": "cover.png",
      "type": "image/png"
    }
  ],
  "resources": [
    {
      "href": "audio.ogg",
      "type": "audio/ogg"
    },
    {
      "href": "video.mp4",
      "type": "video/mp4"
    }
  ]
}
}
```
Notes:
* The ```resources``` section SHOULD list all resources required for the lesson to function offline
  such that they can be downloaded for offline use.

**D) Create a tincan.xml file for each learning unit**

Example:
```xml
<tincan xmlns="http://projecttincan.com/tincan.xsd">
    <activities>
        <activity id="https://demo.openeel.org/en-US/grade/1/learningunits/1/" type="http://activitystrea.ms/schema/1.0/game">
            <name>Lesson 1</name>
            <description lang="en-US">A demo lesson</description>
            <launch lang="en-us">learningunit.html</launch>
        </activity>
    </activities>
</tincan>
```
The tincan.xml file is used as per the [Rustici Launch Method](https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md)
so learning units can send xAPI usage data back to the launcher. 

> [!NOTE]
> If the learning unit requires a native app to be installed and does not support usage via web
> technologies then use an [Intent URI](https://developer.android.com/reference/android/content/Intent#toUri(int)) instead. If the required native app is not installed then
> the launcher will take the user to an app store as per the launchable app manifest. e.g.
> ```xml 
> <launch lang="en-US">intent://demo.openeel.org/en-US/grade/1/learningunits/3/learningunit.html#Intent;scheme=https;category=android.intent.category.BROWSABLE;package=org.openeel.demolaunchableapp;end</launch>
> ```


## Step 2: Send xAPI usage data back to the launcher (HTML/Javascript apps)

Update your HTML/Javascript to check for the presence of the xAPI launch parameters and send data
back to the specified endpoint. This can be done using the [xAPI.js library](https://www.xapijs.dev/). 
This step can be skipped if your app is used exclusively as a native app.

e.g.

```javascript
//Downloaded as per https://github.com/xapijs/xapi/blob/develop/example/browser/module/index.html
import XAPI from "./xapi_module.js";
const { endpoint, auth } = XAPI.getTinCanLaunchData();

//As per https://www.xapijs.dev/xapi-wrapper-library/helpers
const queryParamsObject = XAPI.getSearchQueryParamsAsObject(location.search);

const searchParams = new URLSearchParams(location.search);

const xapi = new XAPI({
    endpoint: endpoint,
    auth: auth
});

function onLessonPassed() {
    const myStatement = {
        actor: queryParamsObject.actor,
        verb: {
            id: "http://adlnet.gov/expapi/verbs/passed",
        },
        object: {
            id: searchParams.get("activity_id")
        },
        result: {
            completion: true,
            success: true,
            score: {
                scaled: 1.0
            }
        }
    };

    console.log("XapiAuJs: Assignable unit: sending statement ");

    xapi.sendStatement({
        statement: myStatement
    }).then((result) => {
        console.log(result);
    }).catch((error) => {
        const errorStr = JSON.stringify(error.toJSON(), null, 2);
        console.log(errorStr);
    });
}
```

# Step 3: Send xAPI usage data back to the launcher (Native Android apps)

Update your app to use the xAPI-IPC library to send xAPI usage data back to the launcher:

```kotlin
@Composable
fun LessonScreen(
    modifier: Modifier = Modifier,
    lesson: LessonDestination
) {
    val context = LocalContext.current.getActivityContext()
    val json = remember {
        Json {
            encodeDefaults = false
        }
    }

    val ipcPackage = lesson.xapiIpcPackage
    val endpointUrl = lesson.endpoint?.let { Url(it) }
    val auth = lesson.auth

    val client = remember(ipcPackage, endpointUrl, auth) {
        if (ipcPackage != null && endpointUrl != null && auth != null) {
            XapiIpcClientBuilder(context, endpointUrl.toString())
                .setAuth(auth)
                .setJson(json)
                .setIpcServicePackageName(ipcPackage)
                .build()
        } else {
            null
        }
    }

    val actorObject = remember(lesson.actor) {
        lesson.actor?.let { json.decodeFromString(XapiAgent.serializer(), it) }
    }

    val activityIdVal = lesson.activity_id
    
    fun onLessonPassed() {
        if(actorObject != null && activityIdVal != null && scoreFloat != null && client != null) {
            val result = client.statements.post(
                listOf(
                    XapiStatement(
                        actor = actorObject,
                        verb = XapiVerb(id = "http://adlnet.gov/expapi/verbs/passed"),
                        `object` = XapiActivity(id = activityIdVal),
                        result = XapiResult(
                            completion = true,
                            success = true,
                            score = XapiResult.Score(
                                scaled = 1.0f
                            )
                        )
                    )
                )
            )

            resultStmtText = result.prettyResultString()
        }else {
            resultStmtText = "Could not send stmt: missing params"
        }
    }
}
```
See the full native demo app (including library dependencies to add) [on GitHub](https://www.github.com/UstadMobile/DemoLaunchableApp/).

# Step 4: Load offline resources when available (Native Android apps)

If you need to load resources dynamically (e.g. images, videos, etc for a particular lesson) update
your app to load resources from the Open Educational Launcher cache if available. When the user
clicks to download a learning unit offline all the URLs as per the manifest resources section are
downloaded.

The provided interceptor will automatically load URLs from the launcher app cache when available
and fallback to loading from the network as usual.

```kotlin
val httpIpcClient = HttpIpcClientBuilder(context)
  .setIpcServicePackageName(ipcPackage)
  .setAuth(auth)
  .build()

val learningUnitHttpClient = appHttpClient
  .newBuilder()
  .addInterceptor(HttpIpcInterceptor(httpIpcClient))
  .build()

/*
 * Now use the OKHttpClient as normal: resources available in the launcher app cache will be loaded
 * locally over IPC, otherwise it will automatically fall back to loading from the network as normal.
 * 
 * The http client can be used with any OKHTTP compatible libraries such as image loaders, media 
 * players, etc.
 */
val httpResponse = learningUnitHttpClient.newCall(
  Request.Builder()
    .url("https://demo.openeel.org/static/books.png")
    .build()
).execute()
```

See the full native demo app (including library dependencies to add) [on GitHub](https://www.github.com/UstadMobile/DemoLaunchableApp/).

# Step 5: Try adding your app on the launcher

Login to the RESPECT launcher as an admin, go the Apps list, click add, then enter the URL for your
launchable app manifest from step 1. You should now be able to browse learning units from the apps 
collections, launch learning units, and receive usage data using xAPI.

