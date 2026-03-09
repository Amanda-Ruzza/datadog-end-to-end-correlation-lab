# Playwright image has Node + Chromium; version must match the playwright npm package.
FROM mcr.microsoft.com/playwright:v1.58.2-noble

WORKDIR /app

COPY load-gen/package.json ./
RUN npm install --omit=dev

COPY load-gen/run.js ./

# Wait for frontend to be up, then every minute run load gen for 40s and sleep 20s.
ENV BASE_URL=http://frontend:8080
CMD ["sh", "-c", "echo 'Waiting for frontend...'; until node -e \"require('http').get(process.env.BASE_URL, r => process.exit(r.statusCode === 200 ? 0 : 1)).on('error', () => process.exit(1))\"; do sleep 2; done && echo 'Frontend ready.' && while true; do timeout 40 node run.js || true; sleep 20; done"]
