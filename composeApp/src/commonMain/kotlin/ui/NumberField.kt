package ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun NumberField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit, // posledný parameter = trailing lambda funguje
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            val cleaned = new.replace(',', '.')
            if (cleaned.isEmpty() || cleaned.matches(Regex("^-?\\d*(\\.\\d*)?$"))) {
                onValueChange(cleaned)
            }
        },
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth()
    )
}
