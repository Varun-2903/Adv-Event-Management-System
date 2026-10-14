# Event Management Frontend

React and Vite web client for the Event Management System.

## Run locally

Requires Node.js and npm.

```sh
npm ci
npm run dev
```

The development server runs at `http://localhost:5173` and uses `http://localhost:8080` as the default API host.

## Configure the API

Set `VITE_API_HOST` to the deployed backend's origin, without an `/api` suffix. For example:

```text
VITE_API_HOST=https://your-api.onrender.com
```

For a static deployment, build with `npm ci && npm run build` and publish the `dist` directory.
