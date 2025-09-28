package util

actual fun logout(callback: () -> Unit) {
    callback()
}