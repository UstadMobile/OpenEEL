package org.openeel.libxapi.test

import org.openeel.lib.xapi.ext.idStr
import org.openeel.lib.xapi.model.XapiActivityDefinition
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiActor
import org.openeel.lib.xapi.model.XapiAgent
import org.openeel.lib.xapi.model.XapiContextActivities
import org.openeel.lib.xapi.model.XapiGroup
import org.openeel.lib.xapi.model.XapiObjectType
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementRef
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/*
 * Exact equality checks as provided by the data classes don't make sense here: e.g. the member list
 * is unordered as per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2422-when-the-actor-objecttype-is-group
 * so an assertEquals would fail when actually the result is entirely compliant with the spec. Or a
 * statement may include a useless Result object with no properties (as they are all optional).
 *
 * These assertion statements check for a canonical equality as per the xAPI spec.
 */
fun assertXapiStatementCanonicallyEqual(
    expected: XapiStatement,
    actual : XapiStatement,
    idOnlyFormat: Boolean = false,
    messagePrefix: String = "",
) {

    assertEquals(expected.id, actual.id)
    assertXapiActorCanonicallyEqual(expected.actor, actual.actor, idOnlyFormat = idOnlyFormat)
    assertXapiVerbCanonicallyEqual(expected.verb, actual.verb, idOnlyFormat = idOnlyFormat)
    val expectedStmtObject = expected.`object`

    when(expectedStmtObject) {
        is XapiActivity -> {
            val actualObject = actual.`object`
            assertTrue(actualObject is XapiActivity)
            assertEquals(
                expected = XapiObjectType.Activity,
                actual = actual.`object`.objectType ?: XapiObjectType.Activity,
                message = "When Xapi object is an Activity, then the objectType must be null or Activity"
            )

            val expectedDefinition = expectedStmtObject.definition
            if(expectedDefinition != null && !idOnlyFormat) {
                val actualDefinition = actualObject.definition
                assertNotNull(actualDefinition)
                assertXapiActivityDefinitionCanonicallyEqual(expectedDefinition, actualDefinition)
            }

            if(idOnlyFormat) {
                assertNull(actualObject.definition)
            }
        }

        is XapiStatement -> {
            assertEquals(
                XapiObjectType.SubStatement, actual.`object`.objectType,
                message = "$messagePrefix When Xapi object is a statement, then objectType must be a SubStatement"
            )

            assertXapiStatementCanonicallyEqual(
                expected = expectedStmtObject,
                actual = actual.`object` as XapiStatement,
                idOnlyFormat = idOnlyFormat,
            )
        }

        is XapiAgent -> {
            assertXapiActorCanonicallyEqual(
                expected = expectedStmtObject,
                actual = actual.`object` as XapiAgent,
                idOnlyFormat = idOnlyFormat,
            )
        }

        is XapiGroup -> {
            assertXapiActorCanonicallyEqual(
                expected = expectedStmtObject,
                actual = actual.`object` as XapiGroup,
                idOnlyFormat = idOnlyFormat,
            )
        }

        is XapiStatementRef -> {
            val actualStatementObject = actual.`object`
            assertTrue(actualStatementObject is XapiStatementRef)
            assertEquals(expectedStmtObject.id, actualStatementObject.id)
        }
    }

    val expectedResult = expected.result
    val actualResult = actual.result
    if(expectedResult != null) {
        assertNotNull(actualResult)
        assertEquals(expectedResult.completion, actualResult.completion)
        assertEquals(expectedResult.success, actualResult.success)
        assertEquals(expectedResult.score, actualResult.score)
        assertEquals(expectedResult.duration, actualResult.duration)
        assertEquals(expectedResult.response, actualResult.response)
        assertEquals(expectedResult.extensions, actualResult.extensions)
    }else {
        assertNull(actual.result)
    }

    val expectedContext = expected.context
    val actualContext = actual.context
    if(expectedContext != null) {
        assertNotNull(actualContext)

        val expectedInstructor = expectedContext.instructor
        if(expectedInstructor != null) {
            val actualInstructor = actualContext.instructor
            assertNotNull(actualInstructor)
            assertXapiActorCanonicallyEqual(expectedInstructor, actualInstructor, idOnlyFormat = idOnlyFormat)
        }else {
            assertNull(actualContext.instructor)
        }

        assertEquals(expectedContext.registration, actualContext.registration)
        assertEquals(expectedContext.language, actualContext.language)
        assertEquals(expectedContext.platform, actualContext.platform)
        assertEquals(expectedContext.revision, actualContext.revision)

        val expectedTeam = expectedContext.team
        if(expectedTeam != null) {
            val actualTeam = actualContext.team
            assertNotNull(actualTeam)
            assertXapiActorCanonicallyEqual(expectedTeam, actualTeam, idOnlyFormat = idOnlyFormat)
        }else {
            assertNull(actualContext.team)
        }

        assertContextActivityCanonicallyEqual(
            expected = expectedContext.contextActivities,
            actual = actualContext.contextActivities,
            idOnlyFormat = idOnlyFormat,
        )
    }else {
        assertNull(actualContext)
    }

    val expectedAuthority = expected.authority
    if(expectedAuthority != null) {
        val actualAuthority = actual.authority
        assertNotNull(actualAuthority ,"Expected statement has authority $expectedAuthority")
        assertXapiActorCanonicallyEqual(expectedAuthority, actualAuthority, idOnlyFormat = idOnlyFormat)
    }else {
        assertNull(actual.authority)
    }
    assertEquals(expected.version, actual.version)
}

fun assertContextActivityCanonicallyEqual(
    expected: XapiContextActivities?,
    actual: XapiContextActivities?,
    idOnlyFormat: Boolean = false,
) {
    fun assertContextActivityMatch(
        expected: List<XapiActivity>?,
        actual: List<XapiActivity>?,
    ) {
        if(expected != null) {
            assertNotNull(actual, "Actual expected to have context activity")
            assertEquals(expected.size, actual.size)

            expected.forEach { expectedActivity ->
                val actualActivity = actual.firstOrNull {
                    it.id == expectedActivity.id
                }
                assertNotNull(actualActivity)

                //The statement received might have only the id. The canonical response (actual)
                //will include the definition if available, so we do not assert that if the
                //expected activity definition is null that the actual activity definition will
                //also be null
                val actualDefinition = actualActivity.definition
                if(!idOnlyFormat) {
                    expectedActivity.definition?.also {
                        assertNotNull(actualDefinition)
                        assertXapiActivityDefinitionCanonicallyEqual(it, actualDefinition)
                    }
                }else {
                    assertNull(actualDefinition)
                }
            }
        }else {
            assertNull(actual)
        }
    }

    if(expected != null) {
        assertNotNull(actual)
        assertContextActivityMatch(expected.parent, actual.parent)
        assertContextActivityMatch(expected.grouping, actual.grouping)
        assertContextActivityMatch(expected.category, actual.category)
        assertContextActivityMatch(expected.other, actual.other)
    }else {
        assertNull(actual)
    }
}

fun assertXapiVerbCanonicallyEqual(
    expected: XapiVerb,
    actual: XapiVerb,
    idOnlyFormat: Boolean = false,
) {
    assertEquals(expected.id, actual.id)
    if(!idOnlyFormat)
        assertLangMapEquals(expected.display, actual.display)
}

fun assertLangMapEquals(
    expected: Map<String, String>?,
    actual: Map<String, String>?,
) {
    //As per the xAPI spec: empty objects SHOULD be avoided. Therefor we will treat an empty object
    //and a missing object (null) as the same.
    assertEquals(expected?.size ?: 0, actual?.size ?: 0)
    expected?.forEach { (key, value) ->
        assertEquals(value, actual?.get(key))
    }
}

fun assertXapiActivityDefinitionCanonicallyEqual(
    expected: XapiActivityDefinition,
    actual: XapiActivityDefinition
) {
    fun assertInteractionListMatches(
        expected: List<XapiActivityDefinition.Interaction>?,
        actual: List<XapiActivityDefinition.Interaction>?,
    ) {
        assertEquals(expected?.size, actual?.size)
        expected?.forEachIndexed { index, interaction ->
            assertEquals(interaction.id, actual!![index].id)
            assertLangMapEquals(interaction.description, actual[index].description)
        }
    }


    assertLangMapEquals(expected.name, actual.name)
    assertLangMapEquals(expected.description, actual.description)
    assertEquals(expected.type, actual.type)
    assertEquals(expected.extensions, actual.extensions,
        "Actual Xapi Activity extensions must match expected")
    assertEquals(expected.moreInfo, actual.moreInfo)
    assertEquals(expected.interactionType, actual.interactionType)
    assertEquals(expected.correctResponsesPattern, actual.correctResponsesPattern)
    assertInteractionListMatches(expected.choices, actual.choices)
    assertInteractionListMatches(expected.scale, actual.scale)
    assertInteractionListMatches(expected.source, actual.source)
    assertInteractionListMatches(expected.target, actual.target)
    assertInteractionListMatches(expected.steps, actual.steps)
}

fun assertXapiActorCommonPropsMatch(
    expected: XapiActor,
    actual: XapiActor,
) {
    assertEquals(expected.name, actual.name)
    assertEquals(expected.mbox, actual.mbox)
    assertEquals(expected.mbox_sha1sum, actual.mbox_sha1sum)
    assertEquals(expected.openid, actual.openid)

    if(expected is XapiAgent) {
        assertTrue(
            actual.objectType == null ||
            actual.objectType == XapiObjectType.Agent,
            message = "When expected actor ($expected) is an XapiAgent, then the actual must be null (by default) or XapiAgent as per spec",
        )
    }

    if(expected is XapiGroup) {
        assertEquals(actual.objectType, XapiObjectType.Group)
    }

    assertEquals(expected.account?.name, actual.account?.name)
    assertEquals(expected.account?.homePage, actual.account?.homePage)
}

fun assertXapiActorCanonicallyEqual(
    expected: XapiActor,
    actual: XapiActor,
    idOnlyFormat: Boolean = false,
) {
    assertXapiActorCommonPropsMatch(expected, actual)

    assertEquals(expected is XapiAgent, actual is XapiAgent)
    assertEquals(expected is XapiGroup, actual is XapiGroup)

    //When checking for id only format actual, identified groups member property should be omitted
    val idOnlyFormatForIdentifiedGroup = expected is XapiGroup && expected.isIdentified
            && idOnlyFormat

    if(expected is XapiGroup && actual is XapiGroup && !idOnlyFormatForIdentifiedGroup) {
        /**
         * As per the xAPI spec : an identified group can be referenced without including the members
         * themselves.
         */
        expected.member?.size?.also { expectedMemberSize ->
            assertEquals(expectedMemberSize, actual.member?.size,
                "Expected and actual member size should match in group ${expected.idStr}")
        }

        /**
         * As per the spec: order does not matter:
         * An LRS returning a Statement MAY return the list of Group members in any order.
         * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2422-when-the-actor-objecttype-is-group
         */
        expected.member?.forEach { expectedMember ->
            val memberInActual = actual.member?.firstOrNull {
                it.idStr == expectedMember.idStr
            } ?: throw AssertionError("Member $expectedMember not found in other group")
            assertXapiActorCanonicallyEqual(
                expected = expectedMember,
                actual = memberInActual
            )
        }
    }

    if(idOnlyFormatForIdentifiedGroup && actual is XapiGroup) {
        assertNull(actual.member)
    }
}
