package world.respect.datalayer.db.school.xapi.entities

enum class XapiActorEntityTypeEnum(val flag: Int) {

    AGENT(XapiEntityObjectTypeFlags.AGENT),
    GROUP(XapiEntityObjectTypeFlags.GROUP);

    companion object {

        fun fromFlag(value: Int): XapiActorEntityTypeEnum {
            return entries.first { it.flag == value }
        }
    }

}