/**
 * AdCreater Desktop Mock API Server
 *
 * Vite plugin intercepting /api/* requests for UI development without a running backend.
 * All responses follow yudao-cloud format: { code: 0, data: {...}, message: "Success" }
 *
 * To disable: set VITE_DISABLE_MOCK=true or comment out the plugin in vite.config.ts
 */

import type { Plugin, ViteDevServer } from 'vite'
import type { IncomingMessage, ServerResponse } from 'connect'

// ── helpers ────────────────────────────────────────────────────────
function ok(data: unknown) {
  return JSON.stringify({ code: 0, data, message: 'Success' })
}

function fail(code: number, message: string) {
  return JSON.stringify({ code, data: null, message })
}

function sendJson(res: ServerResponse, status: number, body: string) {
  res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8' })
  res.end(body)
}

function parseBody(req: IncomingMessage): Promise<Record<string, unknown>> {
  return new Promise((resolve) => {
    let raw = ''
    req.on('data', (chunk: Buffer) => { raw += chunk.toString() })
    req.on('end', () => {
      try { resolve(JSON.parse(raw) || {}) } catch { resolve({}) }
    })
  })
}

function getQuery(url: string): URLSearchParams {
  const idx = url.indexOf('?')
  return new URLSearchParams(idx >= 0 ? url.slice(idx) : '')
}

// ── mock data ──────────────────────────────────────────────────────

const templates = [
  { id: 't1', name: '产品展示', type: 'image', thumbnail: '', width: 1500, height: 1500 },
  { id: 't2', name: '促销活动', type: 'image', thumbnail: '', width: 1200, height: 628 },
  { id: 't3', name: '品牌故事', type: 'image', thumbnail: '', width: 1080, height: 1080 },
  { id: 't4', name: '生活方式', type: 'image', thumbnail: '', width: 1200, height: 628 },
  { id: 't5', name: '产品评测', type: 'video', thumbnail: '', width: 1920, height: 1080 },
  { id: 't6', name: '开箱体验', type: 'video', thumbnail: '', width: 1080, height: 1920 },
  { id: 't7', name: '限时特惠', type: 'image', thumbnail: '', width: 1080, height: 1920 },
  { id: 't8', name: '社交分享', type: 'image', thumbnail: '', width: 1080, height: 1080 },
]

const transactions = Array.from({ length: 23 }, (_, i) => ({
  id: `TXN-${String(202605130001 + i)}`,
  type: i <= 18 ? 'CONSUME' : 'CHARGE',
  amount: i <= 18 ? Math.floor(Math.random() * 20) + 1 : [100, 500, 1000, 2000][i - 19],
  description: i <= 18
    ? `广告${['图片', '视频', '文案'][i % 3]}生成消费`
    : `${['100', '500', '1000', '2000'][i - 19]}点数充值`,
  createdAt: new Date(Date.now() - i * 86400000).toISOString(),
}))

// SVG placeholder image (dark gradient with text overlay)
function placeholderImage(width: number, height: number, label: string): string {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}">
    <defs><linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" style="stop-color:#6c5ce7"/><stop offset="100%" style="stop-color:#7d6ff0"/>
    </linearGradient></defs>
    <rect width="${width}" height="${height}" fill="url(#bg)"/>
    <text x="50%" y="45%" text-anchor="middle" fill="white" font-size="${Math.min(width, height) * 0.06}" font-family="sans-serif" font-weight="bold">AdCreater</text>
    <text x="50%" y="58%" text-anchor="middle" fill="rgba(255,255,255,0.8)" font-size="${Math.min(width, height) * 0.04}" font-family="sans-serif">${label} ${width}×${height}</text>
  </svg>`
  return `data:image/svg+xml;base64,${Buffer.from(svg).toString('base64')}`
}

// ── route handlers ─────────────────────────────────────────────────

async function handleRequest(req: IncomingMessage, res: ServerResponse, url: URL): Promise<boolean> {
  const path = url.pathname
  const method = req.method?.toUpperCase() || 'GET'

  // ── Auth ─────────────────────────────────────────────────────────
  if (path === '/api/system/auth/login' && method === 'POST') {
    const body = await parseBody(req)
    const username = body.username as string || ''
    const password = body.password as string || ''
    if (!username || !password) {
      sendJson(res, 400, fail(400, '用户名和密码不能为空'))
      return true
    }
    // Accept any username with password "admin" or matching username (demo mode)
    if (password === 'admin' || password === username) {
      sendJson(res, 200, ok({ access_token: 'mock-jwt-token-adcreater-2026', refresh_token: 'mock-refresh-token' }))
      return true
    }
    sendJson(res, 401, fail(401, '用户名或密码错误'))
    return true
  }

  // ── Billing ──────────────────────────────────────────────────────
  if (path === '/api/billing/balance' && method === 'GET') {
    sendJson(res, 200, ok({ balance: 580 }))
    return true
  }

  if (path === '/api/billing/transaction/page' && method === 'GET') {
    const q = getQuery(url.href)
    const page = parseInt(q.get('page') || '1', 10)
    const pageSize = parseInt(q.get('pageSize') || '10', 10)
    const start = (page - 1) * pageSize
    const records = transactions.slice(start, start + pageSize)
    sendJson(res, 200, ok({ records, total: transactions.length }))
    return true
  }

  if (path === '/api/billing/recharge' && method === 'POST') {
    // Immediate success (no payUrl → treated as instant credit)
    sendJson(res, 200, ok({ payUrl: null, message: '充值成功' }))
    return true
  }

  // ── Templates ────────────────────────────────────────────────────
  if (path === '/api/templates/list' && method === 'GET') {
    sendJson(res, 200, ok(templates))
    return true
  }

  if (path === '/api/templates/page' && method === 'GET') {
    sendJson(res, 200, ok({ records: templates, total: templates.length }))
    return true
  }

  if (path === '/api/templates/sync' && method === 'POST') {
    sendJson(res, 200, ok({ updated: [], deleted: [], message: '已是最新版本' }))
    return true
  }

  // ── Ad Creation ──────────────────────────────────────────────────
  if (path === '/api/ad/copy/translate' && method === 'POST') {
    const body = await parseBody(req)
    const productName = body.productName as string || 'Product'
    const market = body.targetMarket as string || 'US'
    const sellingPoints = body.sellingPoints as string[] || []

    const marketCopy: Record<string, { original: string; translated: string }> = {
      US: {
        original: `${productName} - ${sellingPoints.join('，')}`,
        translated: `${productName} - ${sellingPoints.join(', ')}. High quality, competitive price, fast shipping worldwide.`
      },
      UK: {
        original: `${productName} - ${sellingPoints.join('，')}`,
        translated: `${productName} - ${sellingPoints.join(', ')}. Premium quality with free delivery across the UK.`
      },
      DE: {
        original: `${productName} - ${sellingPoints.join('，')}`,
        translated: `${productName} - ${sellingPoints.join(', ')}. Hochwertige Qualität zu wettbewerbsfähigen Preisen.`
      },
      JP: {
        original: `${productName} - ${sellingPoints.join('，')}`,
        translated: `${productName} - ${sellingPoints.join('、')}。高品質、競争力のある価格、世界中に迅速発送。`
      },
      SA: {
        original: `${productName} - ${sellingPoints.join('，')}`,
        translated: `${productName} - ${sellingPoints.join(', ')}. جودة عالية، سعر تنافسي، شحن سريع لجميع أنحاء العالم.`
      },
      BR: {
        original: `${productName} - ${sellingPoints.join('，')}`,
        translated: `${productName} - ${sellingPoints.join(', ')}. Alta qualidade, preço competitivo, envio rápido para todo o mundo.`
      }
    }
    const copy = marketCopy[market] || marketCopy['US']
    sendJson(res, 200, ok({ original: copy.original, translated: copy.translated }))
    return true
  }

  if (path === '/api/ad/image/generate' && method === 'POST') {
    const body = await parseBody(req)
    const width = (body.width as number) || 1500
    const height = (body.height as number) || 1500
    const style = (body.style as string) || 'modern'
    const label = style.charAt(0).toUpperCase() + style.slice(1)
    // Simulate latency (300-800ms)
    await new Promise(r => setTimeout(r, 300 + Math.random() * 500))
    sendJson(res, 200, ok({ url: placeholderImage(width, height, label), imageUrl: placeholderImage(width, height, label) }))
    return true
  }

  if (path === '/api/ad/video/generate' && method === 'POST') {
    const body = await parseBody(req)
    const duration = (body.duration as number) || 15
    const style = (body.style as string) || 'smooth'
    // Return a still image placeholder for video preview
    const thumb = placeholderImage(1920, 1080, `Video ${duration}s ${style}`)
    await new Promise(r => setTimeout(r, 500 + Math.random() * 1000))
    sendJson(res, 200, ok({ url: thumb, videoUrl: thumb }))
    return true
  }

  if (path === '/api/ad/video/keyframe/generate' && method === 'POST') {
    const body = await parseBody(req)
    const index = (body.index as number) ?? 0
    const style = (body.style as string) || 'modern'
    // Return a scene keyframe placeholder
    const thumb = placeholderImage(640, 360, `Scene ${index + 1}`)
    await new Promise(r => setTimeout(r, 300 + Math.random() * 400))
    sendJson(res, 200, ok({ url: thumb, imageUrl: thumb }))
    return true
  }

  if (path === '/api/ad/video/storyboard/generate' && method === 'POST') {
    const body = await parseBody(req)
    const desc = (body.productDescription as string) || '默认商品'
    const points = desc.split(/[，,。.\n、]/).filter((s: string) => s.trim())
    const storyboards = Array.from({ length: Math.min(points.length || 3, 5) }, (_, i) => ({
      order: i,
      description: points[i]?.trim() || `场景 ${i + 1}：展示产品特点与优势`,
      keyFrames: Array.from({ length: 4 }, (_, j) => ({
        order: j,
        prompt: `${points[i]?.trim() || `场景 ${i + 1}`} - ${['开场远景展示产品全貌', '中景展示核心功能与使用场景', '特写强调产品细节与材质', '品牌标识与行动号召'][j]}`
      }))
    }))
    await new Promise(r => setTimeout(r, 800 + Math.random() * 1200))
    sendJson(res, 200, ok({ storyboards }))
    return true
  }

  if (path === '/api/ad/video/storyboard/regenerate' && method === 'POST') {
    const body = await parseBody(req)
    const desc = (body.description as string) || '场景描述'
    const keyFrames = Array.from({ length: 4 }, (_, j) => ({
      order: j,
      prompt: `${desc} - ${['远景', '中景', '特写', '细节'][j]} (再生版)`
    }))
    await new Promise(r => setTimeout(r, 500 + Math.random() * 800))
    sendJson(res, 200, ok({ keyFrames }))
    return true
  }

  if (path === '/api/ad/video/keyframe/grid' && method === 'POST') {
    const body = await parseBody(req)
    const idx = (body.sceneIndex as number) ?? 0
    const grid = placeholderImage(1280, 960, `Storyboard ${idx + 1} Grid`)
    await new Promise(r => setTimeout(r, 1000 + Math.random() * 1500))
    sendJson(res, 200, ok({ url: grid, imageUrl: grid }))
    return true
  }

  if (path === '/api/ad/projects/recent' && method === 'GET') {
    const projects = [
      { name: '夏季新款运动鞋推广', type: 'image', createdAt: new Date(Date.now() - 2 * 86400000).toISOString(), status: '已完成' },
      { name: '智能手表促销视频', type: 'video', createdAt: new Date(Date.now() - 5 * 86400000).toISOString(), status: '已完成' },
      { name: '美妆礼盒品牌广告', type: 'image', createdAt: new Date(Date.now() - 7 * 86400000).toISOString(), status: '已完成' },
      { name: '无线耳机社交素材', type: 'image', createdAt: new Date().toISOString(), status: '进行中' },
      { name: '家居用品展示视频', type: 'video', createdAt: new Date(Date.now() - 3 * 86400000).toISOString(), status: '失败' },
    ]
    sendJson(res, 200, ok(projects))
    return true
  }

  // ── Delivery ─────────────────────────────────────────────────────
  if (path === '/api/delivery/avatar/generate' && method === 'POST') {
    await new Promise(r => setTimeout(r, 500 + Math.random() * 1000))
    const thumb = placeholderImage(1080, 1920, 'Digital Human')
    sendJson(res, 200, ok({ url: thumb, videoUrl: thumb }))
    return true
  }

  if (path === '/api/delivery/export-sizes' && method === 'POST') {
    const body = await parseBody(req)
    const platforms = body.platforms as string[] || []
    const baseImage = body.baseImage as string || ''
    const urls: Record<string, Record<string, string>> = {}
    const sizeMap: Record<string, Array<{ w: number; h: number }>> = {
      amazon: [{ w: 1500, h: 1500 }, { w: 2000, h: 2000 }, { w: 1600, h: 1600 }],
      facebook: [{ w: 1200, h: 628 }, { w: 1080, h: 1080 }, { w: 1080, h: 1920 }],
      shopee: [{ w: 1024, h: 1024 }, { w: 800, h: 800 }],
      tiktok: [{ w: 1080, h: 1920 }, { w: 720, h: 1280 }],
      google: [{ w: 1200, h: 628 }, { w: 300, h: 250 }, { w: 728, h: 90 }, { w: 160, h: 600 }, { w: 300, h: 600 }],
    }
    for (const p of platforms) {
      urls[p] = {}
      const sizes = sizeMap[p] || []
      for (const s of sizes) {
        urls[p][`${s.w}x${s.h}`] = baseImage || placeholderImage(s.w, s.h, p.toUpperCase())
      }
    }
    sendJson(res, 200, ok({ urls }))
    return true
  }

  // Not matched
  return false
}

// ── vite plugin ─────────────────────────────────────────────────────

export function mockApiServer(): Plugin {
  return {
    name: 'adcreater-mock-api',
    configureServer(server: ViteDevServer) {
      // Disable mock when VITE_DISABLE_MOCK is set
      if (process.env.VITE_DISABLE_MOCK === 'true') {
        console.log('[mock] Mock disabled (VITE_DISABLE_MOCK=true)')
        return
      }
      console.log('[mock] Mock API server enabled — intercepting /api/* requests')
      server.middlewares.use(async (req: IncomingMessage, res: ServerResponse, next: () => void) => {
        const urlStr = (req.url || '/')
        if (!urlStr.startsWith('/api/')) {
          return next()
        }
        const url = new URL(urlStr, 'http://localhost')
        const matched = await handleRequest(req, res, url)
        if (!matched) {
          console.warn(`[mock] No handler for ${req.method} ${url.pathname} — falling through to proxy`)
          next()
        }
      })
    }
  }
}
