package world.respect.datalayer.db.school.xapi.entities

enum class XapiStatementContextActivityJoinTypeEnum(val dbFlag: Int) {

    PARENT(1), GROUPING(2), CATEGORY(4), OTHER(8);

    companion object {

        const val PARENT_FLAG_INT = 1

        const val GROUP_FLAG_INT = 2

        const val CATEGORY_FLAG_INT = 4

        fun fromDbFlag(value: Int): XapiStatementContextActivityJoinTypeEnum {
            return entries.first { it.dbFlag == value }
        }
    }

}