package world.respect.shared.domain.opds.validator

import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import world.respect.shared.domain.validator.Validator
import java.net.URI

abstract class AbstractJsonSchemaValidator(
    private val schemaUrl: String,
) : Validator {

    private val factory by lazy {
        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
    }

    protected val schema: JsonSchema by lazy {
        factory.getSchema(URI(schemaUrl)).also {
            it.initializeValidators()
        }
    }

}