package world.respect.datalayer.shared.params

/**
 * Order option used to sort list results
 *
 * @param name the name of the option used as the http query parameter value for the order option
 * @param flag an integer flag normally used in SQL queries as the subject of WHEN statements.
 *
 */
class OrderOption(
    val name: String,
    val flag: Int,
) {

    companion object {

        const val UID_ASC_FLAG = 10

        val UID_ASC = OrderOption("uid_asc", UID_ASC_FLAG)

        const val UID_DESC_FLAG = 11

        val UID_DESC = OrderOption("uid_desc", UID_DESC_FLAG)


        const val STORED_ASC_FLAG = 20

        val STORED_ASC = OrderOption("stored_asc", STORED_ASC_FLAG)

    }


}
