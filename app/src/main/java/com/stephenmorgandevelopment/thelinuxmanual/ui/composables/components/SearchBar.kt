@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.searchBarTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.utils.getString

@Composable
fun SearchBar(
    modifier: Modifier,
    onTextUpdate: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        modifier = modifier.padding(3.dp),
        value = text,
        onValueChange = {
            text = it
            onTextUpdate(it)
        },
        singleLine = true,
        textStyle = searchBarTextStyle,
        shape = RoundedCornerShape(6.dp),
        label = { Text(getString(R.string.search_command)) },
        colors = TextFieldDefaults.colors(),
    )
}

@Preview
@Composable
private fun PreviewSearchBar() {
    SearchBar(Modifier) { }
}