package com.ustadmobile.libuicompose.org.openeel.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun <T> OpenEelSearchFilterChip(
    options: List<T>,
    selectedOptions: List<T>,
    onSelectedOptionsChanged: (List<T>) -> Unit,
    label: @Composable () -> Unit = {},
    searchText: String,
    onSearchTextChanged: () -> Unit,
) {
    var sheetExpanded by remember { mutableStateOf(false) }


}

//e.g. in header
fun ExampleHeaderDeleteMe(){
    /*
    OpenEelSearchFilterChip(
        options = uiState.availableLanguage,
        selectedOptions = uiState.searchParams.languages,
        onSelectOptionsChanged = viewModel::onLanguageSelectionChanged,
        label = {Text(getEndonym(it))},
        searchText = uiState.languageSearchText,
        onSearchTextChanged = viewModel::onLanguageSearchTextChanged,
    )

    //then do the same for the other options

     */

}