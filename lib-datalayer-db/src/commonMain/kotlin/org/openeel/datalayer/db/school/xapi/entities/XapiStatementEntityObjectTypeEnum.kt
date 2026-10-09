package world.respect.datalayer.db.school.xapi.entities

/**
 * Represents the possible types of a StatementEntity's object as per XapiStatement.object : it can
 * be an activity, substatement, statement ref, agent, or group as per the xAPI spec. It cannot be a
 * statement.
 */
enum class XapiStatementEntityObjectTypeEnum(
    val flag: Int,
) {

    ACTIVITY(XapiEntityObjectTypeFlags.ACTIVITY),
    SUBSTATEMENT(XapiEntityObjectTypeFlags.SUBSTATEMENT),
    STATEMENT_REF(XapiEntityObjectTypeFlags.STATEMENT_REF),
    AGENT(XapiEntityObjectTypeFlags.AGENT),
    GROUP(XapiEntityObjectTypeFlags.GROUP);

    companion object {

        fun fromFlag(flag: Int): XapiStatementEntityObjectTypeEnum {
            return entries.first { it.flag == flag }
        }

    }
}
