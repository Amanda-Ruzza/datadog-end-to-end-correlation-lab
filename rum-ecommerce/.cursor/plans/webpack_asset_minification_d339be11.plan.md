---
name: Webpack Asset Minification
overview: Set up Webpack in the frontend to minify CSS and JS, extract the inline JS to its own file, and integrate the build into the Docker pipeline for current and future static assets.
todos:
  - id: wp-1
    content: Create package.json and webpack.config.js in frontend/
    status: completed
  - id: wp-2
    content: Extract inline JS to static-src/js/add-to-cart.js, move CSS to static-src/css/, create entry index.js
    status: completed
  - id: wp-3
    content: Update layout.html and catalog.html to reference minified dist/ assets
    status: completed
  - id: wp-4
    content: Update Dockerfile to run Webpack before Maven
    status: completed
  - id: wp-5
    content: Update .gitignore for dist/ and node_modules/
    status: completed
isProject: false
---

# Webpack Asset Minification

## Current State

- 1 CSS file: `frontend/src/main/resources/static/css/classic-jazz.css` (411 lines)
- 1 inline JS snippet in `frontend/src/main/resources/templates/catalog.html` (add-to-cart fetch logic, ~18 lines)
- No standalone JS files, no existing bundler

## Approach

Add a Webpack build step inside `frontend/` that:

- Takes source CSS and JS from a `frontend/src/main/resources/static-src/` directory
- Minifies and outputs to `frontend/src/main/resources/static/dist/`
- Runs as part of the Docker build (before Maven packages the JAR)

## Key files to create/modify

- **`frontend/package.json`** -- Webpack + plugins (css-minimizer, mini-css-extract, terser)
- **`frontend/webpack.config.js`** -- entry point, CSS/JS loaders, output to `static/dist/`
- **`frontend/src/main/resources/static-src/js/add-to-cart.js`** -- extracted from inline script in `catalog.html`
- **`frontend/src/main/resources/static-src/css/classic-jazz.css`** -- move source CSS here
- **`frontend/src/main/resources/static-src/js/index.js`** -- Webpack entry that imports CSS + JS
- **`frontend/src/main/resources/templates/layout.html`** -- reference `/dist/classic-jazz.min.css`
- **`frontend/src/main/resources/templates/catalog.html`** -- replace inline script with `<script src="/dist/bundle.min.js">`
- **`frontend/Dockerfile`** -- add Node + Webpack build stage before Maven build
- **`.gitignore`** -- add `frontend/src/main/resources/static/dist/` and `frontend/node_modules/`