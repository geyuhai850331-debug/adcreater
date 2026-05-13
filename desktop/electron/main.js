import { app, BrowserWindow, ipcMain } from 'electron'
import path from 'path'
import fs from 'fs'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const isDev = !app.isPackaged

const ASSETS_ROOT = path.join(app.getPath('home'), 'AdCreater', 'assets')
const IMAGES_DIR = path.join(ASSETS_ROOT, 'images')
const VIDEOS_DIR = path.join(ASSETS_ROOT, 'videos')
const EXPORTS_DIR = path.join(ASSETS_ROOT, 'exports')
const TEMPLATES_DIR = path.join(app.getPath('home'), 'AdCreater', 'templates')

function ensureDir(dirPath) {
  if (!fs.existsSync(dirPath)) {
    fs.mkdirSync(dirPath, { recursive: true })
  }
}

function ensureDirectories() {
  ensureDir(ASSETS_ROOT)
  ensureDir(IMAGES_DIR)
  ensureDir(VIDEOS_DIR)
  ensureDir(EXPORTS_DIR)
  ensureDir(TEMPLATES_DIR)
}

function saveBase64File(dataUrl, filePath) {
  const matches = dataUrl.match(/^data:(.+);base64,(.+)$/)
  if (!matches) {
    throw new Error('Invalid data URL format')
  }
  const buffer = Buffer.from(matches[2], 'base64')
  fs.writeFileSync(filePath, buffer)
}

function getMonthDir(subDirPath) {
  const now = new Date()
  const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  const monthDir = path.join(subDirPath, month)
  ensureDir(monthDir)
  return monthDir
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 800,
    minWidth: 1024,
    minHeight: 680,
    title: 'AdCreater',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  })

  if (isDev) {
    win.loadURL('http://localhost:5173')
    win.webContents.openDevTools({ mode: 'detach' })
  } else {
    win.loadFile(path.join(__dirname, '..', 'dist', 'index.html'))
  }

  win.on('closed', () => {
    // cleanup if needed
  })
}

// ── IPC Handlers ───────────────────────────────────────────────

ipcMain.handle('fs:save-file', async (_event, opts) => {
  try {
    const { subDir, filename, dataUrl } = opts
    let targetDir

    switch (subDir) {
      case 'images':
        targetDir = getMonthDir(IMAGES_DIR)
        break
      case 'videos':
        targetDir = getMonthDir(VIDEOS_DIR)
        break
      case 'exports':
        targetDir = getMonthDir(EXPORTS_DIR)
        break
      case 'templates':
        targetDir = TEMPLATES_DIR
        break
      default:
        targetDir = getMonthDir(IMAGES_DIR)
    }

    const filePath = path.join(targetDir, filename)
    saveBase64File(dataUrl, filePath)
    return filePath
  } catch (err) {
    console.error('fs:save-file error:', err)
    throw err
  }
})

ipcMain.handle('fs:list-files', async (_event, opts) => {
  try {
    const { subDir, month } = opts
    let targetDir

    switch (subDir) {
      case 'images':
        targetDir = month ? path.join(IMAGES_DIR, month) : IMAGES_DIR
        break
      case 'videos':
        targetDir = month ? path.join(VIDEOS_DIR, month) : VIDEOS_DIR
        break
      case 'exports':
        targetDir = month ? path.join(EXPORTS_DIR, month) : EXPORTS_DIR
        break
      case 'templates':
        targetDir = TEMPLATES_DIR
        break
      default:
        targetDir = IMAGES_DIR
    }

    if (!fs.existsSync(targetDir)) {
      return []
    }

    const entries = fs.readdirSync(targetDir, { withFileTypes: true })
    const files = entries
      .filter((e) => e.isFile())
      .map((e) => {
        const fullPath = path.join(targetDir, e.name)
        const stat = fs.statSync(fullPath)
        return {
          name: e.name,
          path: fullPath,
          size: stat.size,
          mtime: stat.mtime.toISOString()
        }
      })
      .sort((a, b) => new Date(b.mtime).getTime() - new Date(a.mtime).getTime())

    return files
  } catch (err) {
    console.error('fs:list-files error:', err)
    return []
  }
})

ipcMain.handle('fs:delete-file', async (_event, filePath) => {
  try {
    if (fs.existsSync(filePath)) {
      fs.unlinkSync(filePath)
    }
  } catch (err) {
    console.error('fs:delete-file error:', err)
    throw err
  }
})

ipcMain.handle('fs:get-template-dir', async () => {
  ensureDir(TEMPLATES_DIR)
  return TEMPLATES_DIR
})

ipcMain.handle('fs:get-assets-dir', async () => {
  ensureDir(ASSETS_ROOT)
  return ASSETS_ROOT
})

// ── App Lifecycle ──────────────────────────────────────────────

app.whenReady().then(() => {
  ensureDirectories()
  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})
