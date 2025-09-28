package util

actual fun logout(callback: () -> Unit) {
    // Pre Wasm jednoducho zavoláme callback
    callback()
}