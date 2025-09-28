self.addEventListener('install', (event) => {
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  // len pass-through, žiadna cache
  event.respondWith(fetch(event.request).catch(() => new Response("Offline")));
});
