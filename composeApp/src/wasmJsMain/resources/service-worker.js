diff --git a//dev/null b/composeApp/src/wasmJsMain/resources/service-worker.js
index 0000000000000000000000000000000000000000..874ac8318334886829803d6de03fc5489cfc925f 100644
--- a//dev/null
+++ b/composeApp/src/wasmJsMain/resources/service-worker.js
@@ -0,0 +1,52 @@
+const CACHE_NAME = 'repsirodesk-cache-v1';
+const OFFLINE_URLS = [
+  './',
+  'index.html',
+  'composeApp.js',
+  'styles.css',
+  'manifest.webmanifest',
+  'icons/icon-192.png',
+  'icons/icon-512.png',
+];
+
+self.addEventListener('install', (event) => {
+  event.waitUntil(
+    caches.open(CACHE_NAME).then((cache) => cache.addAll(OFFLINE_URLS))
+  );
+  self.skipWaiting();
+});
+
+self.addEventListener('activate', (event) => {
+  event.waitUntil(
+    caches.keys().then((keys) =>
+      Promise.all(keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key)))
+    )
+  );
+  self.clients.claim();
+});
+
+self.addEventListener('fetch', (event) => {
+  if (event.request.method !== 'GET') {
+    return;
+  }
+
+  event.respondWith(
+    caches.match(event.request).then((cachedResponse) => {
+      if (cachedResponse) {
+        return cachedResponse;
+      }
+
+      return fetch(event.request)
+        .then((response) => {
+          if (!response || response.status !== 200 || response.type === 'opaque') {
+            return response;
+          }
+
+          const responseClone = response.clone();
+          caches.open(CACHE_NAME).then((cache) => cache.put(event.request, responseClone));
+          return response;
+        })
+        .catch(() => caches.match('index.html'));
+    })
+  );
+});
