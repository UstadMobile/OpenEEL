package world.respect.clitools

import nl.adaptivity.xmlutil.serialization.XML
import org.koin.dsl.module
import world.respect.clitools.domain.testlaunchableapp.GetXapiStatementsFromLearningUnitTestUseCase
import world.respect.clitools.domain.testlaunchableapp.RunLearningUnitTestUseCase
import world.respect.clitools.domain.testlaunchableapp.SelectRandomPublicationUseCase
import world.respect.clitools.domain.testlaunchableapp.TestLaunchableAppUseCaseImpl
import world.respect.datalayer.http.school.opds.OpdsPublicationDataSourceHttpClient
import world.respect.datalayer.school.opds.OpdsPublicationDataSource
import world.respect.shared.domain.launchapp.GetAndroidPackageIdForLaunchableAppUseCase
import world.respect.shared.domain.launchapp.getlaunchoptionsforpublication.GetLaunchOptionsForPublicationUseCase
import world.respect.shared.domain.testlaunchableapp.TestLaunchableAppUseCase

val cliKoinAppModule = module {
    single<SelectRandomPublicationUseCase> {
        SelectRandomPublicationUseCase(
            httpClient = get(),
            json = get(),
        )
    }

    single<RunLearningUnitTestUseCase> {
        RunLearningUnitTestUseCase()
    }

    single<GetXapiStatementsFromLearningUnitTestUseCase> {
        GetXapiStatementsFromLearningUnitTestUseCase(
            opdsPublicationDataSource = get(),
            getLaunchOptionsForPublicationUseCase = get(),
        )
    }

    single<XML> {
        XML.v1 {
            recommended_1_0_0()
        }
    }

    single<OpdsPublicationDataSource> {
        OpdsPublicationDataSourceHttpClient(
            httpClient = get(),
            publicationValidationHelper = null,
            json = get(),
        )
    }

    single<GetLaunchOptionsForPublicationUseCase> {
        GetLaunchOptionsForPublicationUseCase(
            httpClient = get(),
            xml = get(),
            opdsPublicationDataSource = get(),
        )
    }

    single<GetAndroidPackageIdForLaunchableAppUseCase> {
        GetAndroidPackageIdForLaunchableAppUseCase()
    }

    single<TestLaunchableAppUseCase>(createdAtStart = true) {
        TestLaunchableAppUseCaseImpl(
            selectRandomPublicationUseCase = get(),
            runLearningUnitTestUseCase = get(),
            httpClient = get(),
            getXapiStatementsFromLearningUnitTestUseCase = get(),
            json = get(),
            getAndroidPackageIdForLaunchableAppUseCase = get(),
        )
    }
}
