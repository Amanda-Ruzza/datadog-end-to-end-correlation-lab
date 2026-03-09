/**
 * Classic Jazz load generator — simulates users: catalog, add to cart, checkout.
 * Run for ~40s to generate traffic for Datadog RUM/APM/Logs once instrumented.
 * Usage: BASE_URL=http://frontend:8080 node run.js
 */

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

let shutdownRequested = false;
let browserRef = null;
function requestShutdown() {
  shutdownRequested = true;
  if (browserRef) browserRef.close().catch(() => {});
}
process.on('SIGTERM', requestShutdown);
process.on('SIGINT', requestShutdown);

const FIRST_NAMES = ['Miles', 'John', 'Ella', 'Duke', 'Billie', 'Charlie', 'Sarah', 'Louis', 'Maria', 'Antonio', 'Carmen', 'João', 'Sofia', 'Luis', 'Elena', 'Nikita', 'Yuki', 'Olga', 'Hans', 'Ingrid'];
const LAST_NAMES = ['Davis', 'Coltrane', 'Fitzgerald', 'Ellington', 'Holiday', 'Parker', 'Vaughan', 'Armstrong', 'Da Silva', 'Santos', 'Garcia', 'Mueller', 'Tanaka', 'Kim', 'Rossi', 'Bergström', 'Novák', 'Papadopoulos', 'O\'Brien', 'Van Dijk'];

function randomChoice(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

/** Create one fake user via signup so the frontend writes user_context.json (for RUM). Runs once per 40s run. */
async function createFakeUserAndSignup(page) {
  const first = randomChoice(FIRST_NAMES);
  const last = randomChoice(LAST_NAMES);
  const email = `synthetic-user-${first.toLowerCase().replace(/'/g, '')}-${last.toLowerCase().replace(/'/g, '')}-${Date.now()}@example.com`;

  // 1) Direct HTTP POST to frontend signup so the request definitely reaches the server and user_context.json is written.
  try {
    const body = new URLSearchParams({ firstName: first, lastName: last, email });
    const res = await fetch(BASE_URL + '/auth/signup', {
      method: 'POST',
      body: body.toString(),
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      redirect: 'manual',
    });
    if (res.status === 302 || res.status === 200) {
      return; // success
    }
  } catch (e) {
    if (!String(e?.message || e).includes('has been closed')) console.error('Fake user signup (HTTP) error:', e.message);
  }

  // 2) Fallback: browser form submit (in case direct POST has cookie/session issues).
  try {
    await page.goto(BASE_URL + '/auth', { waitUntil: 'domcontentloaded', timeout: 15000 });
    await page.waitForSelector('#signup-first', { state: 'visible', timeout: 5000 }).catch(() => {});
    await page.fill('#signup-first', first);
    await page.fill('#signup-last', last);
    await page.fill('#signup-email', email);
    await page.locator('form[action*="signup"]').locator('button[type="submit"]').click();
    await page.waitForURL((url) => url.pathname === '/' || url.pathname === '/auth', { timeout: 15000 }).catch(() => {});
  } catch (e) {
    if (!String(e?.message || e).includes('has been closed')) console.error('Fake user signup (browser) error:', e.message);
  }
}

/**
 * Generate various fake errors: JS errors, HTTP errors, resource errors.
 * Runs once per 40s cycle.
 */
async function errorSession(page) {
  try {
    // 1. JS Errors (Console errors & Exceptions)
    await page.evaluate(() => {
      console.error("Simulated console error: Failed to load analytics script");
      // Throw random exception
      if (Math.random() > 0.5) {
        setTimeout(() => { throw new Error("Uncaught JS Exception: Button click handler failed"); }, 100);
      }
    });

    // 2. HTTP Errors (404s, 500s) via navigation
    const errorPaths = ['/debug/error', '/debug/not-found', '/nonexistent-page-123'];
    const path = randomChoice(errorPaths);
    await page.goto(BASE_URL + path, { timeout: 10000 }).catch(() => {});
    await sleep(500);

    // 3. Resource Errors (Broken Image)
    await page.goto(BASE_URL + '/', { waitUntil: 'domcontentloaded' });
    await page.evaluate(() => {
      const img = document.createElement('img');
      img.src = '/static/missing-image-' + Date.now() + '.jpg';
      document.body.appendChild(img);
    });
    await sleep(500);

    // 4. Bad form submission (Empty cart checkout)
    await page.goto(BASE_URL + '/cart');
    // If cart is empty, try to checkout anyway (might trigger validation error if button exists)
    // Or just try to POST directly to checkout with invalid data
    await page.evaluate(async () => {
        try {
            await fetch('/cart/checkout', { method: 'POST', body: new FormData() });
        } catch(e) {}
    });

  } catch (e) {
    // Ignore errors generated during error session
  }
}

async function oneUserSession(page, startTime, deadlineMs) {
  if (Date.now() - startTime >= deadlineMs) return false;

  try {
    // 1. Go to catalog
    await page.goto(BASE_URL + '/', { waitUntil: 'networkidle', timeout: 15000 });
    await sleep(randomInt(500, 1500));

    // 2. Add one or two random albums to cart (click "Add to cart" buttons)
    const addForms = page.locator('form.add-to-cart');
    const count = await addForms.count();
    if (count === 0) return true;

    const howMany = randomInt(1, Math.min(2, count));
    for (let i = 0; i < howMany; i++) {
      if (Date.now() - startTime >= deadlineMs) return false;
      const idx = randomInt(0, count - 1);
      await addForms.nth(idx).locator('button[type="submit"]').click();
      await page.waitForURL(/\/cart/, { timeout: 10000 });
      await sleep(randomInt(300, 800));
      await page.goto(BASE_URL + '/', { waitUntil: 'domcontentloaded', timeout: 10000 });
      await sleep(randomInt(200, 600));
    }

    // 3. We're on cart after adding; fill checkout and place order (guest) only if form is present (cart not empty)
    await sleep(randomInt(400, 1000));
    const checkoutFirst = page.locator('#checkout-first');
    const formVisible = await checkoutFirst.waitFor({ state: 'visible', timeout: 5000 }).then(() => true).catch(() => false);
    if (formVisible) {
      const first = randomChoice(FIRST_NAMES);
      const last = randomChoice(LAST_NAMES);
      const email = `synthetic-user-${first.toLowerCase()}-${Date.now()}@example.com`;
      await page.fill('#checkout-first', first);
      await page.fill('#checkout-last', last);
      await page.fill('#checkout-email', email);
      await sleep(randomInt(200, 500));
      await page.click('form[action*="/cart/checkout"] button[type="submit"]');
      await page.waitForLoadState('networkidle');
    }
  } catch (e) {
    if (!String(e?.message || e).includes('has been closed')) console.error('Session error:', e.message);
  }
  return true;
}

async function main() {
  const deadlineMs = 40_000; // 40 seconds
  const startTime = Date.now();

  const { chromium } = await import('playwright');
  const browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
  });
  browserRef = browser;

  let sessions = 0;
  try {
    // First: create one fake user via signup
    if (!shutdownRequested) {
      const signupCtx = await browser.newContext({
        userAgent: 'LoadGen/1.0 (Datadog practice)',
        viewport: { width: 1280, height: 720 },
      });
      const signupPage = await signupCtx.newPage();
      await createFakeUserAndSignup(signupPage);
      try { await signupCtx.close(); } catch (e) {}
    }

    // Second: run one error session (JS errors, 404s, etc.)
    if (!shutdownRequested) {
      const errCtx = await browser.newContext({
        userAgent: 'LoadGen/1.0 (Datadog practice)',
        viewport: { width: 1280, height: 720 },
      });
      const errPage = await errCtx.newPage();
      await errorSession(errPage);
      try { await errCtx.close(); } catch (e) {}
    }

    while (!shutdownRequested && Date.now() - startTime < deadlineMs) {
      const context = await browser.newContext({
        userAgent: 'LoadGen/1.0 (Datadog practice)',
        viewport: { width: 1280, height: 720 },
      });
      const page = await context.newPage();

      const ok = await oneUserSession(page, startTime, deadlineMs);
      try {
        await context.close();
      } catch (e) {
        if (!String(e?.message || e).includes('has been closed')) throw e;
      }
      sessions++;
      if (!ok || shutdownRequested) break;
    }

    // (Fake user signup already done at start of run.)
  } finally {
    try {
      await browser.close();
    } catch (e) {
      if (!String(e?.message || e).includes('has been closed')) throw e;
    }
  }

  console.log(`Load gen finished: ${sessions} session(s) + 1 fake user + 1 error session in ${((Date.now() - startTime) / 1000).toFixed(1)}s`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
