/**
 * AdCreater Desktop Mock API Server
 *
 * Vite plugin intercepting /api/* and /app-api/* requests for UI development without a running backend.
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
  if ((path === '/api/billing/balance' || path === '/app-api/billing/balance') && method === 'GET') {
    sendJson(res, 200, ok({ balance: 580 }))
    return true
  }

  if ((path === '/api/billing/transaction/page' || path === '/app-api/billing/transaction/page') && method === 'GET') {
    const q = getQuery(url.href)
    const page = parseInt(q.get('pageNo') || q.get('page') || '1', 10)
    const pageSize = parseInt(q.get('pageSize') || '10', 10)
    const start = (page - 1) * pageSize
    const records = transactions.slice(start, start + pageSize)
    sendJson(res, 200, ok({ records, total: transactions.length }))
    return true
  }

  if ((path === '/api/billing/recharge' || path === '/app-api/billing/recharge') && method === 'POST') {
    // Immediate success (no payUrl → treated as instant credit)
    sendJson(res, 200, ok({ payUrl: null, message: '充值成功' }))
    return true
  }

  // ── Templates ────────────────────────────────────────────────────
  if ((path === '/api/templates/list' || path === '/app-api/template/list') && method === 'GET') {
    sendJson(res, 200, ok(templates))
    return true
  }

  if (path === '/api/templates/page' && method === 'GET') {
    sendJson(res, 200, ok({ records: templates, total: templates.length }))
    return true
  }

  if ((path === '/api/templates/sync' || path === '/app-api/template/sync') && method === 'POST') {
    sendJson(res, 200, ok({ updated: [], deleted: [], message: '已是最新版本' }))
    return true
  }

  // ── Ad Marketing ──────────────────────────────────────────────────
  if (path === '/api/ad/marketing/analyze' && method === 'POST') {
    const body = await parseBody(req)
    const market = body.targetMarket as string || 'US'
    const productName = body.productName as string || 'Product'

    const marketAnalysis: Record<string, {
      riskLevel: string;
      cultureNotes: string;
      coreStrategy: string;
      exampleAdCopy: string;
    }> = {
      US: {
        riskLevel: 'safe',
        cultureNotes: '美国消费者注重效率和产品品质。建议突出产品的实用价值和性价比，使用英制单位（inch/lb），避免过于夸张的营销语言。Amazon US 平台要求图片白底、无文字覆盖。',
        coreStrategy: '痛点反差策略：先呈现目标用户日常遇到的问题场景，再展示产品如何高效解决，配合限时折扣制造紧迫感。',
        exampleAdCopy: `Introducing ${productName} — the smarter way to solve your daily challenges. Designed with premium materials and engineered for reliability. Whether you're at home or on the go, experience unmatched performance that fits seamlessly into your lifestyle. Limited stock available — order today.`
      },
      UK: {
        riskLevel: 'safe',
        cultureNotes: '英国消费者偏好低调、幽默的营销风格。避免过于激进的促销语言（如"buy now"），改用更礼貌的邀请式表达。注意英式拼写差异（colour, centre），并符合ASA广告标准。',
        coreStrategy: '权威背书策略：通过产品认证、评测数据和用户评价建立信任，结合英式幽默传达产品价值。',
        exampleAdCopy: `${productName} — thoughtfully crafted for those who appreciate quality. Trusted by thousands across the UK, our product combines innovation with everyday practicality. Free delivery on all orders.`
      },
      DE: {
        riskLevel: 'safe',
        cultureNotes: '德国市场重视技术规格、环保认证（Blue Angel, CE）和数据透明度。避免模糊的营销语言，需提供具体参数。退货政策必须清晰（德国消费者退货率较高）。',
        coreStrategy: '技术权威策略：详细展示产品技术参数、认证和质量检测结果，用数据说服理性消费者。',
        exampleAdCopy: `${productName} — zertifizierte Qualität und Präzision. Jedes Produkt durchläuft strenge Qualitätskontrollen und erfüllt alle EU-Normen. Nachhaltig produziert, entwickelt für den Langzeiteinsatz.`
      },
      JP: {
        riskLevel: 'warning',
        cultureNotes: '日本市场对产品外观要求极高（"美品"文化）。包装必须完美无瑕，任何瑕疵都可能导致退货。建议使用敬语风格，强调"安心"和"信頼"。避免直接比较竞争对手。',
        coreStrategy: '安心信赖策略：强调产品质量保证、售后服务和用户评价，配合"限定"概念制造稀缺感。',
        exampleAdCopy: `${productName} — 信頼の品質、安心の選択。厳選された素材と日本の職人技術が融合した逸品です。万が一の不良品には交換保証付き。数量限定販売。`
      },
      SA: {
        riskLevel: 'warning',
        cultureNotes: '沙特市场严格遵守伊斯兰文化规范。女性模特需佩戴头巾，避免任何酒精、猪肉相关图像或文字。斋月期间消费模式变化显著。使用阿拉伯语更佳，数字用阿拉伯文数字。',
        coreStrategy: '文化认同策略：强调产品符合伊斯兰价值观，突出家庭和社交场景，配合节日促销节点。',
        exampleAdCopy: `${productName} — quality you can trust for your family. Designed to meet the highest standards of craftsmanship and reliability. Special Ramadan offers available.`
      },
      BR: {
        riskLevel: 'safe',
        cultureNotes: '巴西消费者热衷社交媒体分享，偏好鲜艳色彩和情感化营销。分期付款（"parcelamento"）是常见支付方式。Mercado Livre 是主要电商平台，需注意葡萄牙语本地化。',
        coreStrategy: '社交从众策略：利用社交媒体口碑和KOL推荐，结合分期付款降低购买门槛，制造"大家都在用"的从众效应。',
        exampleAdCopy: `${productName} — a escolha de quem busca qualidade sem complicação. Junte-se a milhares de clientes satisfeitos. Parcele em até 12x no cartão. Entrega rápida para todo o Brasil.`
      }
    }

    const data = marketAnalysis[market] || marketAnalysis['US']
    await new Promise(r => setTimeout(r, 400 + Math.random() * 600))
    sendJson(res, 200, ok({
      riskLevel: data.riskLevel,
      cultureNotes: data.cultureNotes,
      coreStrategy: data.coreStrategy,
      exampleAdCopy: data.exampleAdCopy
    }))
    return true
  }

  // ── Ad Copy Translate ────────────────────────────────────────────
  if ((path === '/api/ad/copy/translate' || path === '/app-api/ad/copy/translate') && method === 'POST') {
    const body = await parseBody(req)
    const productTitle = body.productTitle as string || 'Product'
    const market = body.targetMarket as string || 'US'
    await new Promise(r => setTimeout(r, 300 + Math.random() * 500))
    sendJson(res, 200, ok({
      translatedTitle: `[${market}] ${productTitle}`,
      localizedCopy: `Experience the next generation of ${productTitle} — designed for the ${market} market with localized messaging that resonates with your target audience. Premium quality, exceptional performance.`
    }))
    return true
  }

  // ── Ad Marketing - Regenerate Copy ───────────────────────────────
  if (path === '/api/ad/marketing/regenerate-copy' && method === 'POST') {
    const body = await parseBody(req)
    const market = body.targetMarket as string || 'US'
    const productName = body.productName as string || 'Product'
    const cultureNotes = body.cultureNotes as string || ''
    const coreStrategy = body.coreStrategy as string || ''

    const regeneratedCopies: Record<string, string> = {
      US: `Updated: ${productName} — redesigned with your refined strategy in mind. ${coreStrategy.slice(0, 40)}... Experience the difference with our premium quality and thoughtful design.`,
      UK: `Updated: ${productName} — thoughtfully refined for the discerning UK customer. ${coreStrategy.slice(0, 40)}... Discover quality that speaks for itself.`,
      DE: `Aktualisiert: ${productName} — nach Ihren Vorgaben optimiert. ${coreStrategy.slice(0, 40)}... Qualität, die überzeugt.`,
      JP: `更新版：${productName} — ご要望に合わせて最適化。${coreStrategy.slice(0, 30)}…信頼の品質をお届けします。`,
      SA: `Updated: ${productName} — refined according to your specifications. ${coreStrategy.slice(0, 40)}... Quality for your peace of mind.`,
      BR: `Atualizado: ${productName} — otimizado conforme suas preferências. ${coreStrategy.slice(0, 40)}... Qualidade que faz a diferença.`
    }

    const exampleAdCopy = regeneratedCopies[market] || regeneratedCopies['US']
    await new Promise(r => setTimeout(r, 300 + Math.random() * 400))
    sendJson(res, 200, ok({
      riskLevel: body.riskLevel || 'safe',
      cultureNotes: cultureNotes,
      coreStrategy: coreStrategy,
      exampleAdCopy: exampleAdCopy
    }))
    return true
  }

  if ((path === '/api/ad/image/generate' || path === '/app-api/ad/image/generate') && method === 'POST') {
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

  if ((path === '/api/ad/video/generate' || path === '/app-api/ad/video/generate') && method === 'POST') {
    const body = await parseBody(req)
    const duration = (body.duration as number) || 15
    const style = (body.style as string) || 'smooth'
    // Return a still image placeholder for video preview
    const thumb = placeholderImage(1920, 1080, `Video ${duration}s ${style}`)
    await new Promise(r => setTimeout(r, 500 + Math.random() * 1000))
    sendJson(res, 200, ok({ url: thumb, videoUrl: thumb }))
    return true
  }

  if ((path === '/api/ad/video/keyframe/generate' || path === '/app-api/ad/video/keyframe/generate') && method === 'POST') {
    const body = await parseBody(req)
    const index = (body.index as number) ?? 0
    const style = (body.style as string) || 'modern'
    // Return a scene keyframe placeholder
    const thumb = placeholderImage(640, 360, `Scene ${index + 1}`)
    await new Promise(r => setTimeout(r, 300 + Math.random() * 400))
    sendJson(res, 200, ok({ url: thumb, imageUrl: thumb }))
    return true
  }

  if ((path === '/api/ad/video/storyboard/generate' || path === '/app-api/ad/video/storyboard/generate') && method === 'POST') {
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

  if ((path === '/api/ad/video/storyboard/regenerate' || path === '/app-api/ad/video/storyboard/regenerate') && method === 'POST') {
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

  if ((path === '/api/ad/video/keyframe/grid' || path === '/app-api/ad/video/keyframe/grid') && method === 'POST') {
    const body = await parseBody(req)
    const idx = (body.sceneIndex as number) ?? 0
    const grid = placeholderImage(1280, 960, `Storyboard ${idx + 1} Grid`)
    await new Promise(r => setTimeout(r, 1000 + Math.random() * 1500))
    sendJson(res, 200, ok({ url: grid, imageUrl: grid }))
    return true
  }

  if ((path === '/api/ad/projects/recent' || path === '/app-api/ad/projects/recent') && method === 'GET') {
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
  if ((path === '/api/delivery/avatar/generate' || path === '/app-api/delivery/avatar/generate') && method === 'POST') {
    await new Promise(r => setTimeout(r, 500 + Math.random() * 1000))
    const thumb = placeholderImage(1080, 1920, 'Digital Human')
    sendJson(res, 200, ok({ url: thumb, videoUrl: thumb }))
    return true
  }

  if ((path === '/api/delivery/export-sizes' || path === '/app-api/delivery/export-sizes') && method === 'POST') {
    const body = await parseBody(req)
    const platforms = body.platforms as string[] || []
    const baseImage = body.baseImageUrl as string || body.baseImage as string || ''
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
      console.log('[mock] Mock API server enabled — intercepting /api/* and /app-api/* requests')
      server.middlewares.use(async (req: IncomingMessage, res: ServerResponse, next: () => void) => {
        const urlStr = (req.url || '/')
        if (!urlStr.startsWith('/api/') && !urlStr.startsWith('/app-api/')) {
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
